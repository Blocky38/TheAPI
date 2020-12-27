package me.devtec.theapi.utils.packetlistenerapi;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;

import me.devtec.theapi.TheAPI;
import me.devtec.theapi.scheduler.Tasker;
import me.devtec.theapi.utils.datakeeper.collections.UnsortedList;
import me.devtec.theapi.utils.datakeeper.maps.UnsortedMap;
import me.devtec.theapi.utils.reflections.Ref;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import net.minecraft.util.io.netty.channel.Channel;
import net.minecraft.util.io.netty.channel.ChannelDuplexHandler;
import net.minecraft.util.io.netty.channel.ChannelFuture;
import net.minecraft.util.io.netty.channel.ChannelHandlerContext;
import net.minecraft.util.io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.util.io.netty.channel.ChannelInitializer;
import net.minecraft.util.io.netty.channel.ChannelPromise;

public class PacketHandler_Old implements PacketHandler<Channel> {
	private static Class<?> login = Ref.nms("PacketLoginInStart");
	private Map<String, Channel> channelLookup = new UnsortedMap<>();
	private List<?> networkManagers;
	private List<Channel> serverChannels = new UnsortedList<>();
	private ChannelInboundHandlerAdapter serverChannelHandler;
	private final Object serverConnection = Ref.invoke(Ref.server(),"getServerConnection");
	private ChannelInitializer<Channel> beginInitProtocol, endInitProtocol;
	protected volatile boolean closed;

	public PacketHandler_Old() {
		try {
			registerChannelHandler();
			registerPlayers();
		} catch (Exception ex) {
			new Tasker() {
				public void run() {
					registerChannelHandler();
					registerPlayers();
				}
			}.runTask();
		}
	}

	private void createServerChannelHandler() {
		endInitProtocol = new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel channel) throws Exception {
				try {
					synchronized (networkManagers) {
						if (!closed) {
							channel.eventLoop().submit(() -> {
								PacketInterceptor interceptor = new PacketInterceptor(null); //add new hook
								channel.eventLoop().execute(new Runnable() {
									@Override
									public void run() {
										if(channel.pipeline().names().contains("InjectorTheAPI"))
											channel.pipeline().remove("InjectorTheAPI");
										if(channel.pipeline().names().contains("packet_handler"))
											channel.pipeline().addBefore("packet_handler","InjectorTheAPI", interceptor);
										else
											channel.pipeline().addFirst("InjectorTheAPI", interceptor);
										
									}
								});
								return interceptor;
							});
						}
					}
				} catch (Exception e) {
				}
			}

		};
		beginInitProtocol = new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel channel) throws Exception {
				channel.pipeline().addLast(endInitProtocol);
			}

		};

		serverChannelHandler = new ChannelInboundHandlerAdapter() {
			@Override
			public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
				Channel channel = (Channel) msg;
				channel.pipeline().addFirst(beginInitProtocol);
				ctx.fireChannelRead(msg);
			}

		};
	}

	private void registerChannelHandler() {
		networkManagers = (List<?>) Ref.get(serverConnection, TheAPI.isNewerThan(15) ? "listeningChannels" : "g");
		createServerChannelHandler();
		for (Object item : networkManagers) {
			if (!ChannelFuture.class.isInstance(item))break;
			Channel serverChannel = ((ChannelFuture) item).channel();
			serverChannels.add(serverChannel);
			serverChannel.pipeline().addFirst(serverChannelHandler);
		}
	}

	private void unregisterChannelHandler() {
		if (serverChannelHandler == null)return;
		for (Channel serverChannel : serverChannels) 
			serverChannel.eventLoop().execute(new Runnable() {
				@Override
				public void run() {
					try {
					serverChannel.pipeline().remove(serverChannelHandler);
					}catch(Exception err) {}
				}
			});
	}

	private void registerPlayers() {
		for (Player player : TheAPI.getOnlinePlayers())
			add(player);
	}

	public void add(Player player) {
		injectChannelInternal(player, get(player));
	}

	private PacketInterceptor injectChannelInternal(Player a, Channel channel) {
		if (channel == null)return null;
		try {
			if(channel.pipeline().names().contains("InjectorTheAPI"))
				return (PacketInterceptor) channel.pipeline().get("InjectorTheAPI");
			PacketInterceptor interceptor = new PacketInterceptor(a.getName());
			channel.pipeline().addBefore("packet_handler", "InjectorTheAPI", interceptor);
			return interceptor;
		} catch (Exception e) {
			if(channel.pipeline().names().contains("InjectorTheAPI")) {
				PacketInterceptor interceptor = new PacketInterceptor(a.getName()); //add new hook
				channel.eventLoop().execute(new Runnable() {
					@Override
					public void run() {
						channel.pipeline().remove("InjectorTheAPI");
						try {
						channel.pipeline().addBefore("packet_handler", "InjectorTheAPI", interceptor);
						}catch(Exception e) {}
					}
				});
				return interceptor;
			}
			return null;
		}
	}

	public Channel get(Player player) {
		Channel channel = channelLookup.getOrDefault(player.getName(), null);
		if (channel == null) {
			channel = (Channel) Ref.get(Ref.network(Ref.playerCon(player)), "channel");
			channelLookup.put(player.getName(), channel);
		}else {
			Channel pl = (Channel) Ref.get(Ref.network(Ref.playerCon(player)), "channel");
			if(!channel.equals(pl)) {
				channel=pl;
				channelLookup.put(player.getName(), pl);
			}
		}
		return channel;
	}

	public void remove(Channel channel) {
		if (channel == null)return;
		if(channel.eventLoop().inEventLoop()) {
			String owner = null;
			for(Entry<String, Channel> s : channelLookup.entrySet())
				if(s.getValue().equals(channel)) {
					owner=s.getKey();
					break;
				}
			channelLookup.remove(owner);
			if(channel.pipeline().names().contains("InjectorTheAPI"))
			channel.pipeline().remove("InjectorTheAPI");
		}else
		channel.eventLoop().execute(new Runnable() {
			@Override
			public void run() {
				String owner = null;
				for(Entry<String, Channel> s : channelLookup.entrySet())
					if(s.getValue().equals(channel)) {
						owner=s.getKey();
						break;
					}
				channelLookup.remove(owner);
				if(channel.pipeline().names().contains("InjectorTheAPI"))
				channel.pipeline().remove("InjectorTheAPI");
			}
		});
	}

	public boolean has(Channel channel) {
		if (channel == null)return false;
		try {
			return channel.pipeline().get("InjectorTheAPI") != null;
		} catch (Exception e) {
			return false;
		}
	}

	public final void close() {
		if (!closed) {
			closed = true;
			for (Player player : TheAPI.getOnlinePlayers())
				remove(get(player));
			unregisterChannelHandler();
		}
	}

	public final class PacketInterceptor extends ChannelDuplexHandler {
		String player;

		public PacketInterceptor(String player) {
			this.player=player;
		}
		
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			final Channel channel = ctx.channel();
			if (login.isInstance(msg)) {
				GameProfile profile = (GameProfile) Ref.get(msg, "a");
				this.player=profile.getName();
				channelLookup.put(profile.getName(), channel);
			}
			synchronized (msg) {
				try {
					msg = PacketManager.call(player, msg, ctx.channel(), PacketType.PLAY_IN);
				} catch (Exception e) {
					try {
						msg = PacketManager.call(player, msg, ctx.channel(), PacketType.PLAY_IN);
					} catch (Exception er) {
					}
				}
				if (msg != null)
					super.channelRead(ctx, msg);
			}
		}

		@Override
		public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
			synchronized (msg) {
				try {
					msg = PacketManager.call(player, msg, ctx.channel(), PacketType.PLAY_OUT);
				} catch (Exception e) {
					try {
						msg = PacketManager.call(player, msg, ctx.channel(), PacketType.PLAY_OUT);
					} catch (Exception er) {
					}
				}
				if (msg != null)
					super.write(ctx, msg, promise);
			}
		}
	}

	private static Method mm = Ref.method(Ref.nms("NetworkManager"), "sendPacket", Ref.nms("Packet"));
	
	@Override
	public void send(Channel channel, Object packet) {
		Ref.invoke(channel.pipeline().get("packet_handler"),mm,packet);
	}
}