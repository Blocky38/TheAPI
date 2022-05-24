package me.devtec.theapi.bukkit.packetlistener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.devtec.shared.Ref;
import me.devtec.shared.scheduler.Tasker;
import me.devtec.theapi.bukkit.BukkitLoader;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import net.minecraft.util.io.netty.channel.Channel;
import net.minecraft.util.io.netty.channel.ChannelDuplexHandler;
import net.minecraft.util.io.netty.channel.ChannelFuture;
import net.minecraft.util.io.netty.channel.ChannelHandler.Sharable;
import net.minecraft.util.io.netty.channel.ChannelHandlerContext;
import net.minecraft.util.io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.util.io.netty.channel.ChannelInitializer;
import net.minecraft.util.io.netty.channel.ChannelPromise;

public class PacketHandlerLegacy implements PacketHandler<Channel> {
	private static final Class<?> login = Ref.nms("PacketLoginInStart");
	private static final Class<?> postlogin = Ref.nms("PacketLoginOutSuccess");
	static final Field f = Ref.field(PacketHandlerLegacy.login, "a");
	static final Field fPost = Ref.field(PacketHandlerLegacy.postlogin, "a");
	private final Map<String, Channel> channelLookup = new ConcurrentHashMap<>();
	private List<?> networkManagers;
	private final List<Channel> serverChannels = new ArrayList<>();
	private ChannelInboundHandlerAdapter serverChannelHandler;
	private Object serverConnection;
	private ChannelInitializer<Channel> beginInitProtocol;
	private ChannelInitializer<Channel> endInitProtocol;
	protected volatile boolean closed;

	public PacketHandlerLegacy(boolean lateBind) {
		serverConnection = Ref.invoke(BukkitLoader.getNmsProvider().getMinecraftServer(), "getServerConnection");
		if (serverConnection == null) // modded server
			for (Field f : Ref.getAllFields(BukkitLoader.getNmsProvider().getMinecraftServer().getClass()))
				if (f.getType() == Ref.nmsOrOld("server.network.ServerConnection", "ServerConnection")) {
					serverConnection = Ref.get(BukkitLoader.getNmsProvider().getMinecraftServer(), f);
					break;
				}
		if (serverConnection == null)
			return;
		if (lateBind)
			while (!(boolean) Ref.get(BukkitLoader.getNmsProvider().getMinecraftServer(), "Q"))
				try {
					Thread.sleep(20);
				} catch (Exception e) {
				}
		new Tasker() {
			@Override
			public void run() {
				PacketHandlerLegacy.this.registerChannelHandler();
				PacketHandlerLegacy.this.registerPlayers();
			}
		}.runLater(1);
	}

	private void createServerChannelHandler() {
		endInitProtocol = new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel channel) {
				try {
					synchronized (networkManagers) {
						if (!closed) {
							PacketInterceptor interceptor = new PacketInterceptor(null);
							channel.eventLoop().submit(() -> {
								if (channel.pipeline().names().contains("InjectorTA"))
									channel.pipeline().remove("InjectorTA");
								channel.pipeline().addBefore("packet_handler", "InjectorTA", interceptor);
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
			protected void initChannel(Channel channel) {
				channel.pipeline().addLast(endInitProtocol);
			}

		};
		serverChannelHandler = new ChannelInHandler();
	}

	@Sharable
	public class ChannelInHandler extends ChannelInboundHandlerAdapter {
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) {
			Channel channel = (Channel) msg;
			channel.pipeline().addFirst(beginInitProtocol);
			ctx.fireChannelRead(channel);
		}
	}

	private void registerChannelHandler() {
		networkManagers = (List<?>) (Ref.get(serverConnection, "e") != null
				? Ref.get(serverConnection, "e")
						: Ref.get(serverConnection, "f"));
		if (networkManagers == null)
			for (Field f : Ref.getAllFields(Ref.nms("ServerConnection")))
				if (List.class == f.getType()) {
					networkManagers = (List<?>) Ref.get(serverConnection, f);
					break;
				}
		if (networkManagers == null)
			return;
		if (networkManagers.isEmpty()) {
			networkManagers = (List<?>) (Ref.get(serverConnection, "f") != null
					? Ref.get(serverConnection, "f")
							: Ref.get(serverConnection, "e"));
			if (networkManagers == null)
				for (Field f : Ref.getAllFields(Ref.nms("ServerConnection")))
					if (List.class == f.getType()) {
						networkManagers = (List<?>) Ref.get(serverConnection, f);
						break;
					}
		}
		if (networkManagers == null)
			return;
		createServerChannelHandler();
		for (Object item : networkManagers) {
			if (!(item instanceof ChannelFuture))
				continue;
			Channel serverChannel = ((ChannelFuture) item).channel();
			serverChannels.add(serverChannel);
			serverChannel.pipeline().addFirst(serverChannelHandler);
		}
	}

	private void unregisterChannelHandler() {
		if (serverChannelHandler == null)
			return;
		for (Channel serverChannel : serverChannels)
			serverChannel.eventLoop().execute(() -> {
				try {
					serverChannel.pipeline().remove(serverChannelHandler);
				} catch (Exception err) {
				}
			});
		serverChannels.clear();
	}

	private void registerPlayers() {
		for (Player player : Bukkit.getOnlinePlayers())
			add(player);
	}

	@Override
	public void add(Player player) {
		injectChannelInternal(player, get(player));
	}

	private PacketInterceptor injectChannelInternal(Player a, Channel channel) {
		if (channel == null)
			return null;
		try {
			PacketInterceptor interceptor = new PacketInterceptor(a.getName());
			channel.eventLoop().submit(() -> {
				if (channel.pipeline().names().contains("InjectorTA"))
					channel.pipeline().remove("InjectorTA");
				channel.pipeline().addBefore("packet_handler", "InjectorTA", interceptor);
				return interceptor;
			});
			return interceptor;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public Channel get(Player player) {
		Channel channel = channelLookup.get(player.getName());
		if (channel == null) {
			Object get = BukkitLoader.getNmsProvider().getNetworkChannel(BukkitLoader.getNmsProvider()
					.getConnectionNetwork(BukkitLoader.getNmsProvider().getPlayerConnection(player)));
			if (get == null)
				return null;
			channelLookup.put(player.getName(), channel = (Channel) get);
		}
		return channel;
	}

	@Override
	public void remove(Channel channel) {
		if (channel == null)
			return;
		channel.eventLoop().execute(() -> {
			String owner = null;
			for (Entry<String, Channel> s : PacketHandlerLegacy.this.channelLookup.entrySet())
				if (s.getValue().equals(channel)) {
					owner = s.getKey();
					break;
				}
			PacketHandlerLegacy.this.channelLookup.remove(owner);
			if (channel.pipeline().names().contains("InjectorTA"))
				channel.pipeline().remove("InjectorTA");
		});
	}

	@Override
	public boolean has(Channel channel) {
		if (channel == null)
			return false;
		try {
			return channel.pipeline().get("InjectorTA") != null;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public final void close() {
		if (!closed) {
			closed = true;
			for (Channel channel : channelLookup.values())
				channel.eventLoop().execute(() -> {
					if (channel.pipeline().names().contains("InjectorTA"))
						channel.pipeline().remove("InjectorTA");
				});
			channelLookup.clear();
			unregisterChannelHandler();
		}
	}

	public final class PacketInterceptor extends ChannelDuplexHandler {
		String player;

		public PacketInterceptor(String player) {
			this.player = player;
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			final Channel channel = ctx.channel();
			Object packet = msg;
			synchronized (packet) {
				if (packet.getClass() == PacketHandlerLegacy.login) {
					player = ((GameProfile) Ref.get(packet, PacketHandlerLegacy.f)).getName();
					channelLookup.put(player, channel);
				}
				try {
					packet = PacketManager.call(player, packet, channel, PacketType.PLAY_IN);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (packet != null)
					super.channelRead(ctx, packet);
			}
		}

		@Override
		public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
			final Channel channel = ctx.channel();
			Object packet = msg;
			synchronized (packet) {
				if (player == null && packet.getClass() == PacketHandlerLegacy.postlogin) { // ProtocolLib cancelled
					// packets
					player = ((GameProfile) Ref.get(packet, PacketHandlerLegacy.fPost)).getName();
					channelLookup.put(player, channel);
				}
				try {
					packet = PacketManager.call(player, packet, channel, PacketType.PLAY_OUT);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (packet != null)
					super.write(ctx, packet, promise);
			}
		}
	}

	@Override
	public void send(Channel channel, Object packet) {
		if (channel == null || packet == null)
			return;
		channel.writeAndFlush(packet);
	}
}