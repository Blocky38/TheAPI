package me.devtec.theapi.velocity.commands.selectors;

import java.util.ArrayList;
import java.util.List;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import me.devtec.shared.utility.StringUtils;
import me.devtec.theapi.velocity.VelocityLoader;

public class Utils {

	public static boolean check(Iterable<String> iterable, String string) {
		for(String text : iterable)
			if(text.equalsIgnoreCase(string))return true;
		return false;
	}

	public static boolean check(SelectorType[] selectorTypes, String string) {
		for(SelectorType selectorType : selectorTypes)
			switch(selectorType) {
			case PLAYER:
				return !VelocityLoader.server.matchPlayer(string).isEmpty();
			case SERVER:
				return VelocityLoader.server.getServer(string).isPresent();
			case NUMBER:
				return StringUtils.isNumber(string);
				default:
					break;
			}
		return false;
	}
	
	public static List<String> buildSelectorKeys(SelectorType[] selectorTypes) {
		List<String> text = new ArrayList<>();
		for(SelectorType selectorType : selectorTypes)
			switch(selectorType) {
			case PLAYER:
				for(Player s : VelocityLoader.server.getAllPlayers())
					text.add(s.getUsername());
				break;
			case SERVER:
				for(RegisteredServer s : VelocityLoader.server.getAllServers())
					text.add(s.getServerInfo().getName());
				break;
			case NUMBER:
				text.add("{number}");
				break;
				default:
					break;
			}
		return text;
	}
}
