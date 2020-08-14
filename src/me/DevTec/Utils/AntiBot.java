package me.DevTec.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;

import me.DevTec.Bans.PlayerBanList;
import me.DevTec.Bans.PunishmentAPI;
import me.DevTec.Other.LoaderClass;

public class AntiBot {
	private static long last;
	private static List<String> c = new ArrayList<String>();

	public static boolean hasAccess(UUID e) {
		boolean s = canJoin();
		if (!s)
			c.add(e.toString());
		last = System.currentTimeMillis() / 100;
		return s;
	}

	public static boolean isDisallowed(String uuid) {
		String p = Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName();
		PlayerBanList a = PunishmentAPI.getBanList(p);
		return c.contains(uuid) || a.isBanned() || a.isTempBanned() || a.isIPBanned() || a.isTempIPBanned();
	}

	@SuppressWarnings("deprecation")
	public static boolean containsDisallowed(String string) {
		boolean f = false;
		for (String s : c) {
			if (string.contains(s)) {
				f = true;
				break;
			}
		}
		if (!f)
			for (String s : PunishmentAPI.getBanList().getBanned()) {
				if (string.contains(Bukkit.getOfflinePlayer(s).getUniqueId().toString())) {
					f = true;
					break;
				}
			}
		return f;
	}

	public static boolean canJoin() {
		if (!LoaderClass.config.getBoolean("Options.AntiBot.Use"))
			return true;
		return last - System.currentTimeMillis() / 100
				+ LoaderClass.config.getLong("Options.AntiBot.TimeBetweenPlayer") < 0;
	}
}
