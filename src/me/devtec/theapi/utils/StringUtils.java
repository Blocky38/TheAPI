package me.devtec.theapi.utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Location;

import me.devtec.theapi.TheAPI;
import me.devtec.theapi.utils.json.Reader;
import me.devtec.theapi.utils.json.Writer;
import me.devtec.theapi.utils.theapiutils.LoaderClass;

public class StringUtils {
	private static Random random = new Random();

	public static interface ColormaticFactory {
		public String colorize(String text);

		public String getNextColor();
	}

	@SuppressWarnings("unchecked")
	public static String colorizeJson(String json) {
		return Writer.write(colorizeMap((Map<Object, Object>) Reader.read(json)));
	}

	@SuppressWarnings("unchecked")
	public static Map<Object, Object> colorizeMap(Map<Object, Object> json) {
		Map<Object, Object> colorized = new HashMap<>();
		for (Entry<Object, Object> e : json.entrySet()) {
			if (e.getKey() instanceof Collection) {
				if (e.getValue() instanceof Collection) {
					colorized.put(colorizeList((Collection<Object>) e.getKey()),
							colorizeList((Collection<Object>) e.getValue()));
				}
				if (e.getValue() instanceof Map) {
					colorized.put(colorizeList((Collection<Object>) e.getKey()),
							colorizeMap((Map<Object, Object>) e.getValue()));
				}
				if (e.getValue() instanceof Object[]) {
					colorized.put(colorizeList((Collection<Object>) e.getKey()),
							colorizeArray((Object[]) e.getValue()));
				}
				if (e.getValue() instanceof String) {
					colorized.put(colorizeList((Collection<Object>) e.getKey()), colorize((String) e.getValue()));
				} else {
					colorized.put(colorizeList((Collection<Object>) e.getKey()), e.getValue());
				}
			}
			if (e.getKey() instanceof Map) {
				if (e.getValue() instanceof Collection) {
					colorized.put(colorizeMap((Map<Object, Object>) e.getKey()),
							colorizeList((Collection<Object>) e.getValue()));
				}
				if (e.getValue() instanceof Map) {
					colorized.put(colorizeMap((Map<Object, Object>) e.getKey()),
							colorizeMap((Map<Object, Object>) e.getValue()));
				}
				if (e.getValue() instanceof Object[]) {
					colorized.put(colorizeMap((Map<Object, Object>) e.getKey()),
							colorizeArray((Object[]) e.getValue()));
				}
				if (e.getValue() instanceof String) {
					colorized.put(colorizeMap((Map<Object, Object>) e.getKey()), colorize((String) e.getValue()));
				} else {
					colorized.put(colorizeMap((Map<Object, Object>) e.getKey()), e.getValue());
				}
			}
			if (e.getKey() instanceof Object[]) {
				if (e.getValue() instanceof Collection) {
					colorized.put(colorizeArray((Object[]) e.getKey()),
							colorizeList((Collection<Object>) e.getValue()));
				}
				if (e.getValue() instanceof Map) {
					colorized.put(colorizeArray((Object[]) e.getKey()),
							colorizeMap((Map<Object, Object>) e.getValue()));
				}
				if (e.getValue() instanceof Object[]) {
					colorized.put(colorizeArray((Object[]) e.getKey()), colorizeArray((Object[]) e.getValue()));
				}
				if (e.getValue() instanceof String) {
					colorized.put(colorizeArray((Object[]) e.getKey()), colorize((String) e.getValue()));
				} else {
					colorized.put(colorizeArray((Object[]) e.getKey()), e.getValue());
				}
			}
			if (e.getKey() instanceof String) {
				if (e.getValue() instanceof Collection) {
					colorized.put(colorize((String) e.getKey()), colorizeList((Collection<Object>) e.getValue()));
				}
				if (e.getValue() instanceof Map) {
					colorized.put(colorize((String) e.getKey()), colorizeMap((Map<Object, Object>) e.getValue()));
				}
				if (e.getValue() instanceof Object[]) {
					colorized.put(colorize((String) e.getKey()), colorizeArray((Object[]) e.getValue()));
				}
				if (e.getValue() instanceof String) {
					colorized.put(colorize((String) e.getKey()), colorize((String) e.getValue()));
				} else {
					colorized.put(colorize((String) e.getKey()), e.getValue());
				}
			} else {
				if (e.getValue() instanceof Collection) {
					colorized.put(e.getKey(), colorizeList((Collection<Object>) e.getValue()));
				}
				if (e.getValue() instanceof Map) {
					colorized.put(e.getKey(), colorizeMap((Map<Object, Object>) e.getValue()));
				}
				if (e.getValue() instanceof Object[]) {
					colorized.put(e.getKey(), colorizeArray((Object[]) e.getValue()));
				}
				if (e.getValue() instanceof String) {
					colorized.put(e.getKey(), colorize((String) e.getValue()));
				} else {
					colorized.put(e.getKey(), e.getValue());
				}
			}
		}
		return colorized;
	}

	@SuppressWarnings("unchecked")
	public static Collection<Object> colorizeList(Collection<Object> json) {
		List<Object> colorized = new ArrayList<>();
		for (Object e : json) {
			if (e instanceof Collection) {
				colorized.add(colorizeList((Collection<Object>) e));
			}
			if (e instanceof Map) {
				colorized.add(colorizeMap((Map<Object, Object>) e));
			}
			if (e instanceof Object[]) {
				colorized.add(colorizeArray((Object[]) e));
			}
			if (e instanceof String)
				colorized.add(colorize((String) e));
			else
				colorized.add(e);
		}
		return colorized;
	}

	@SuppressWarnings("unchecked")
	public static Object[] colorizeArray(Object[] json) {
		List<Object> colorized = new ArrayList<>();
		for (Object e : json) {
			if (e instanceof Collection) {
				colorized.add(colorizeList((Collection<Object>) e));
			}
			if (e instanceof Map) {
				colorized.add(colorizeMap((Map<Object, Object>) e));
			}
			if (e instanceof Object[]) {
				colorized.add(colorizeArray((Object[]) e));
			}
			if (e instanceof String)
				colorized.add(colorize((String) e));
			else
				colorized.add(e);
		}
		return colorized.toArray();
	}

	/**
	 * @see see Split text correctly with colors
	 */
	public static List<String> fixedSplit(String text, int lengthOfSplit) {
		List<String> splitted = new ArrayList<>();
		String split = text;
		String prefix = "";
		while (split.length() > lengthOfSplit) {
			int length = lengthOfSplit - 1 - prefix.length();
			String a = prefix + split.substring(0, length);
			if (a.endsWith("§")||a.endsWith("&")) {
				--length;
				a = prefix + split.substring(0, length);
				prefix = getLastColors(a);
			} else
				prefix = getLastColors(a);
			splitted.add(a);
			split = split.substring(length);
		}
		if(!(prefix + split).isEmpty())
		splitted.add(prefix + split);
		return splitted;
	}

	/**
	 * @see see Copy matches of String from Iterable<String> Examples:
	 *      StringUtils.copyPartialMatches("hello", Arrays.asList("helloWorld",
	 *      "hiHouska")) -> helloWorld StringUtils.copyPartialMatches("hello",
	 *      Arrays.asList("helloWorld", "hiHouska", "this_is_list_of_words",
	 *      "helloDevs", "hell")) -> helloWorld, helloDevs
	 * @return List<String>
	 */
	public static List<String> copyPartialMatches(String prefix, Iterable<String> originals) {
		List<String> collection = new ArrayList<>();
		for (String string : originals)
			if (string.length() < prefix.length() ? false : string.regionMatches(true, 0, prefix, 0, prefix.length()))
				collection.add(string);
		return collection;
	}

	/**
	 * @see see Copy matches of String from Iterable<String> Examples:
	 *      StringUtils.copyPartialMatches("hello", Arrays.asList("helloWorld",
	 *      "hiHouska")) -> helloWorld StringUtils.copyPartialMatches("hello",
	 *      Arrays.asList("helloWorld", "hiHouska", "this_is_list_of_words",
	 *      "helloDevs", "hell")) -> helloWorld, helloDevs
	 * @return List<String>
	 */
	public static List<String> copySortedPartialMatches(String prefix, Iterable<String> originals) {
		List<String> collection = new ArrayList<>();
		for (String string : originals)
			if (string.length() < prefix.length() ? false : string.regionMatches(true, 0, prefix, 0, prefix.length()))
				collection.add(string);
		Collections.sort(collection);
		return collection;
	}

	/**
	 * @see see Transfer Collection to String
	 * @return String
	 */
	public static String join(Iterable<?> toJoin, String split) {
		if (toJoin == null || split == null)
			return null;
		StringBuilder r = new StringBuilder();
		for (Object s : toJoin)
			if (s == null)
				continue;
			else
				r.append(split).append(s.toString());
		if(r.length()!=0)
			r.delete(0,split.length());
		return r.toString();
	}

	/**
	 * @see see Transfer Object[] to String
	 * @return String
	 */
	public static String join(Object[] toJoin, String split) {
		if (toJoin == null || split == null)
			return null;
		StringBuilder r = new StringBuilder();
		for (Object s : toJoin)
			if (s == null)
				continue;
			else
				r.append(split).append(s.toString());
		if(r.length()!=0)
			r.delete(0,split.length());
		return r.toString();
	}

	/**
	 * @see see Create clickable message
	 * @return HoverMessage
	 */
	public static HoverMessage getHoverMessage(String... message) {
		return new HoverMessage(message);
	}

	private static Pattern getLast = Pattern
			.compile("(#[A-Fa-f0-9]{6}|[&§][Xx]([&§][A-Fa-f0-9]){6}|[&§][A-Fa-f0-9RrK-Ok-oUuXx])");

	/**
	 * @see see Get last colors from String (HEX SUPPORT!)
	 * @return String
	 */
	public static String getLastColors(String s) {
		Matcher m = getLast.matcher(s);
		String colors = "";
		while (m.find()) {
			String last = m.group(1);
			if (last.matches("[§][A-Fa-f0-9]|[§][Xx]([§][A-Fa-f0-9]){6}"))
				colors = last;
			else
				colors += last;
		}
		return colors;
	}

	private static final Pattern hex = Pattern.compile("#[a-fA-F0-9]{6}");
	public static ColormaticFactory color;
	static {
		if (TheAPI.isNewerThan(15)) {
			color = new ColormaticFactory() {
				private String d = "abcdef0123456789";
				private int len = d.length();
				private char[] a = d.toCharArray();
				private Random r = new Random();

				@Override
				public String colorize(String text) {
					return gradient(text, getNextColor(), getNextColor());
				}

				@Override
				public String getNextColor() {
					StringBuilder b = new StringBuilder("#");
					for (int i = 0; i < 6; ++i)
						b.append(a[r.nextInt(len)]);
					return b.toString();
				}
			};
		} else
			color = new ColormaticFactory() {
				private List<String> list = Arrays.asList("&4", "&c", "&6", "&e", "&5", "&d", "&9", "&3", "&b", "&2",
						"&a");
				private int i = 0;

				@Override
				public String colorize(String text) {
					String ff = text;
					int length = ff.length();
					HashMap<Integer, String> l = new HashMap<>();
					ff = ff.replace("", "<!>");
					Matcher ma = old.matcher(ff);
					while (ma.find()) {
						switch (ma.group(3).toLowerCase()) {
						case "o":
						case "l":
						case "m":
						case "n":
						case "k":
							l.put((ff.indexOf(ma.group()) / 4 + 1), ma.group(3).toLowerCase());
							break;
						}
						ff = ff.replaceFirst(ma.group(), "");
					}
					boolean bold = false, strikethrough = false, underlined = false, italic = false, magic = false,
							br = false;
					for (int index = 0; index <= length; index++) {
						String formats = "";
						if (l.containsKey(index)) {
							switch (l.get(index)) {
							case "l":
								bold = true;
								break;
							case "m":
								strikethrough = true;
								break;
							case "n":
								underlined = true;
								break;
							case "o":
								italic = true;
								break;
							case "k":
								magic = true;
								break;
							case "u":
							case "r":
								bold = false;
								strikethrough = false;
								italic = false;
								magic = false;
								underlined = false;
								break;
							default:
								br = true;
								bold = false;
								strikethrough = false;
								italic = false;
								magic = false;
								underlined = false;
								break;
							}
						}
						if (bold)
							formats += "§l";
						if (italic)
							formats += "§o";
						if (underlined)
							formats += "§n";
						if (strikethrough)
							formats += "§m";
						if (magic)
							formats += "§k";
						ff = ff.replaceFirst("<!>", br ? "&" + l.get(index) + formats : color.getNextColor() + formats);
					}
					return ff;
				}

				@Override
				public String getNextColor() {
					if (i >= list.size())
						i = 0;
					return list.get(i++);
				}
			};
	}

	private static Pattern reg = Pattern.compile("[&§]([Rrk-oK-O])"),
			colorMatic = Pattern.compile("(<!>)*([&§])<!>([A-Fa-f0-9RrK-Ok-oUu" + (TheAPI.isNewerThan(15) ? "Xx" : "") + "])"),
			old = Pattern.compile("&((<!>)*)([XxA-Za-zUu0-9Rrk-oK-O])");

	public static Pattern gradientFinder;

	public static String gradient(String msg, String fromHex, String toHex) {
		Matcher ma = reg.matcher(msg);
		HashMap<Integer, String> l = new HashMap<>();
		while (ma.find()) {
			l.put(msg.indexOf(ma.group()), ma.group(1).toLowerCase());
			msg = msg.replaceFirst(ma.group(), "");
		}
		int length = msg.length();
		Color fromRGB = Color.decode(fromHex), toRGB = Color.decode(toHex);
		double rStep = Math.abs((double) (fromRGB.getRed() - toRGB.getRed()) / length);
		double gStep = Math.abs((double) (fromRGB.getGreen() - toRGB.getGreen()) / length);
		double bStep = Math.abs((double) (fromRGB.getBlue() - toRGB.getBlue()) / length);
		if (fromRGB.getRed() > toRGB.getRed())
			rStep = -rStep;
		if (fromRGB.getGreen() > toRGB.getGreen())
			gStep = -gStep;
		if (fromRGB.getBlue() > toRGB.getBlue())
			bStep = -bStep;
		Color finalColor = new Color(fromRGB.getRGB());
		msg = msg.replaceAll("#[A-Fa-f0-9]{6}", "");
		msg = msg.replace("", "<!>");
		msg = msg.substring(0, msg.length() - 3);
		msg = msg.replace("<!> "," ");
		Matcher fixColors = colorMatic.matcher(msg);
		String formats = "";
		while (fixColors.find())
			msg = msg.replace(fixColors.group(), fixColors.group(2) + fixColors.group(3));
		for (int index = 0; index <= length; index++) {
			int red = (int) Math.round(finalColor.getRed() + rStep);
			int green = (int) Math.round(finalColor.getGreen() + gStep);
			int blue = (int) Math.round(finalColor.getBlue() + bStep);
			if (red > 255)
				red = 255;
			if (red < 0)
				red = 0;
			if (green > 255)
				green = 255;
			if (green < 0)
				green = 0;
			if (blue > 255)
				blue = 255;
			if (blue < 0)
				blue = 0;
			finalColor = new Color(red, green, blue);
			StringBuilder hex = new StringBuilder("§x");
			char[] c = Integer.toHexString(finalColor.getRGB()).substring(2).toCharArray();
			for (int i = 0; i < c.length; ++i)
				hex.append('§').append(c[i]);
			if (l.containsKey(index))
				switch (l.get(index)) {
				case "l":
					if (!formats.contains("§l"))
						formats += "§l";
					break;
				case "m":
					if (!formats.contains("§m"))
						formats += "§m";
					break;
				case "n":
					if (!formats.contains("§n"))
						formats += "§n";
					break;
				case "o":
					if (!formats.contains("§o"))
						formats += "§o";
					break;
				case "k":
					if (!formats.contains("§k"))
						formats += "§k";
					break;
				default:
					formats = "";
					break;
				}
			msg = msg.replaceFirst("<!>", hex.append(formats).toString());
		}
		return msg;
	}

	public static String gradient(String legacyMsg) {
		for (Entry<String, String> code : LoaderClass.colorMap.entrySet()) {
			String rawCode = LoaderClass.tagG + code.getKey();
			if (!legacyMsg.toLowerCase().contains(rawCode.toLowerCase()))
				continue;
			legacyMsg = legacyMsg.replace(rawCode, code.getValue());
		}
		if (gradientFinder == null)
			return legacyMsg;
		Matcher matcher = gradientFinder.matcher(legacyMsg);
		while (matcher.find()) {
			if (matcher.groupCount() == 0)continue;
			legacyMsg = legacyMsg.replace(matcher.group(),
					gradient(matcher.group(2), matcher.group(1), matcher.group(3)));
		}
		return legacyMsg;
	}

	private static boolean neww = TheAPI.isNewerThan(15);
	private static Pattern fixedSplit = Pattern.compile(
			"(#[A-Fa-f0-9]{6}([&§][K-Ok-oRr])*|[&§][Xx]([&§][A-Fa-f0-9]){6}([&§][K-Ok-oRr])*|[&§][A-Fa-f0-9K-ORrk-oUuXx]([&§][K-Ok-oRr])*)");

	/**
	 * @see see Colorize string with colors (&eHello world -> {YELLOW}Hello world)
	 * @param string
	 * @return String
	 */
	public static String colorize(String msg) {
		if (msg == null||msg.trim().isEmpty())
			return msg;
		if (msg.toLowerCase().contains("&u")) {
			String[] split = fixedSplit.split(msg);
			// atempt to add colors to split
			Matcher m = fixedSplit.matcher(msg);
			int id = 1;
			while (m.find()) {
				try {
					split[id] = m.group(1) + split[id++];
				} catch (Exception err) {
				}
			}
			// colors
			StringBuilder d = new StringBuilder(msg.length());
			for (String ff : split) {
				if (ff.toLowerCase().contains("§u") || ff.toLowerCase().contains("&u"))
					ff = StringUtils.color.colorize(ff.replaceAll("[§&][uU]", ""));
				d.append(ff);
			}
			msg = d.toString();
		}
		if (neww) {
			msg = msg.replace("&x", "§x").replace("&X", "§x");
			msg = gradient(msg);
			if (msg.contains("#")) {
				Matcher match = hex.matcher(msg);
				while (match.find()) {
					String color = match.group();
	                StringBuilder magic = new StringBuilder("§x");
	                char[] c = color.substring(1).toCharArray();
	                for(int i = 0; i < c.length; ++i)
	                    magic.append('§').append(c[i]);
					msg = msg.replace(color, magic.toString());
				}
			}
		}
		return ChatColor.translateAlternateColorCodes('&', msg);
	}

	/**
	 * @see see Build string from String[]
	 * @param args
	 * @return String
	 * 
	 */
	public static String buildString(String[] args) {
		return buildString(0, args);
	}

	/**
	 * @see see Build string from String[]
	 * @param args
	 * @return String
	 * 
	 */
	public static String buildString(int start, String[] args) {
		return buildString(start, args.length, args);
	}

	/**
	 * @see see Build string from String[]
	 * @param args
	 * @return String
	 * 
	 */
	public static String buildString(int start, int end, String[] args) {
		StringBuilder msg = new StringBuilder();
		for (int i = start; i < args.length && i < end; ++i)
			msg.append(' ').append(args[i]);
		if(msg.length()!=0)
			msg.delete(0,1);
		return msg.toString();
	}

	/**
	 * @see see Return random object from list
	 */
	public static <T> T getRandomFromList(List<T> list) {
		if (list.isEmpty() || list == null)
			return null;
		return list.get(random.nextInt(list.size()));
	}

	/**
	 * @see see Return random object from collection
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getRandomFromCollection(Collection<T> list) {
		if (list.isEmpty() || list == null)
			return null;
		if(list instanceof List)return getRandomFromList((List<T>)list);
		return (T) list.toArray()[random.nextInt(list.size())];
	}

	public static Pattern sec, min, hour, day, week,mon, year;

	/**
	 * @see see Get long from string
	 * @param s String
	 * @return long
	 */
	public static long getTimeFromString(String period) {
		return timeFromString(period);
	}

	/**
	 * @see see Get long from string
	 * @param s String
	 * @return long
	 */
	public static long timeFromString(String period) {
		if (period == null || period.trim().isEmpty())
			return 0;
		period = period.trim().toLowerCase(Locale.ENGLISH);
		if (isFloat(period))
			return (long)getFloat(period);
		float time = 0;

		Matcher matcher = sec.matcher(period);
		while (matcher.find()) {
			time+=getFloat(matcher.group());
			period=period.replace(matcher.group(), "");
		}
		matcher = min.matcher(period);
		while (matcher.find()) {
			time+=getFloat(matcher.group())*60;
			period=period.replace(matcher.group(), "");
		}
		matcher = hour.matcher(period);
		while (matcher.find()) {
			time+=getFloat(matcher.group())*3600;
			period=period.replace(matcher.group(), "");
		}
		matcher = day.matcher(period);
		while (matcher.find()) {
			time+=getFloat(matcher.group())*86400;
			period=period.replace(matcher.group(), "");
		}
		matcher = week.matcher(period);
		while (matcher.find()) {
			time+=getFloat(matcher.group())* 86400 * 7;
			period=period.replace(matcher.group(), "");
		}
		matcher = mon.matcher(period);
		while (matcher.find()) {
			time+=getFloat(matcher.group())* 86400 * 31;
			period=period.replace(matcher.group(), "");
		}
		matcher = year.matcher(period);
		while (matcher.find()) {
			time+=getFloat(matcher.group())* 86400 * 31 * 12;
			period=period.replace(matcher.group(), "");
		}
		return (long) time;
	}

	/**
	 * @see see Set long to string
	 * @param period long
	 * @return String
	 */
	public static String setTimeToString(long period) {
		return timeToString(period);
	}
	
	public static String timeFormat = "%time% %format%";

	public static Map<String, List<String>> actions = new HashMap<>();

	static String findCorrectFormat(long i, String string) {
		String result = i+string;
		for(String s : actions.get(string)) {
			if(s.startsWith("=,"))
				if(s.substring(1).split(",")[1].equals(""+i))return s.substring(3+s.substring(1).split(",")[1].length());
			if(s.startsWith("<,"))
				if(Integer.parseInt(s.substring(1).split(",")[1]) > i)return s.substring(3+s.substring(1).split(",")[1].length());
			if(s.startsWith(">,"))
				if(Integer.parseInt(s.substring(1).split(",")[1]) < i)return s.substring(3+s.substring(1).split(",")[1].length());
		}
		return result;
	}
	
	private static String format(long time, String section) {
		return timeFormat.replace("%time%", ""+time).replace("%format%", findCorrectFormat(time,section));
	}

	/**
	 * @see see Set long to string
	 * @param time long
	 * @return String
	 */
	public static String timeToString(long time) {
		if (time == 0)
			return format(0,"Seconds");
		long minutes = (time / 60) % 60;
		long hours = (time / 3600) % 24;
		long days = (time / 86400) % 31;
		long month = 0;
		long year = 0;
		try {
			month = (time / 86400 / 31) % 12;
			year = time / 86400 / 31 / 12;
		} catch (Exception er) {
		}
		StringBuilder date = new StringBuilder(64);
		if (year > 0)
			date.append(format(year, "Years"));
		if (month > 0) {
			if(date.length()!=0)
				date.append(LoaderClass.config.getString("Options.TimeConvertor.Split"));
			date.append(format(month, "Months"));
		}
		if (days > 0) {
			if(date.length()!=0)
				date.append(LoaderClass.config.getString("Options.TimeConvertor.Split"));
			date.append(format(days, "Days"));
		}
		if (hours > 0) {
			if(date.length()!=0)
				date.append(LoaderClass.config.getString("Options.TimeConvertor.Split"));
			date.append(format(hours, "Hours"));
		}
		if (minutes > 0) {
			if(date.length()!=0)
				date.append(LoaderClass.config.getString("Options.TimeConvertor.Split"));
			date.append(format(minutes, "Minutes"));
		}
		if (time % 60 > 0) {
			if(date.length()!=0)
				date.append(LoaderClass.config.getString("Options.TimeConvertor.Split"));
			date.append(format(time % 60, "Seconds"));
		}
		return date.toString();
	}

	/**
	 * @see see Convert Location to String
	 * @return String
	 */
	public static String getLocationAsString(Location loc) {
		return locationAsString(loc);
	}

	/**
	 * @see see Convert Location to String
	 * @return String
	 */
	public static String locationAsString(Location loc) { // New shorter name of method
		return TheCoder.locationToString(loc);
	}

	/**
	 * @see see Create Location from String
	 * @return Location
	 */
	public static Location getLocationFromString(String savedLocation) {
		return locationFromString(savedLocation);
	}

	/**
	 * @see see Create Location from String
	 * @return Location
	 */
	public static Location locationFromString(String savedLocation) { // New shorter name of method
		return TheCoder.locationFromString(savedLocation);
	}

	/**
	 * @see see Get boolean from string
	 * @return boolean
	 */
	public static boolean getBoolean(String fromString) {
		try {
			return fromString.equalsIgnoreCase("true") || fromString.equalsIgnoreCase("yes");
		} catch (Exception er) {
			return false;
		}
	}
	
	/**
	 * @see see Convert String to Math and Calculate exempt
	 * @return double
	 */
	public static double calculate(String val) {
		if(val.contains("(")&&val.contains(")")) {
			val=splitter(val);
		}
		if(val.contains("*")||val.contains("/")) {
			Matcher s = extra.matcher(val);
			while(s.find()) {
				double a = getDouble(s.group(1));
				String b = s.group(3);
				double d = getDouble(s.group(4));
				val=val.replace(s.group(), (a==0||d==0?0:((b.equals("*")?a*d:a/d)))+"");
				s = extra.matcher(val);
			}
		}
		if(val.contains("+")||val.contains("-")) {
			Matcher s = normal.matcher(val);
			while(s.find()) {
				double a = getDouble(s.group(1));
				String b = s.group(3);
				double d = getDouble(s.group(4));
				val=val.replace(s.group(), (b.equals("+")?a+d:a-d)+"");
				s = normal.matcher(val);
			}
		}
		return getDouble(val.replaceAll("[^0-9+.-]", ""));
	}

	private static String splitter(String s) {
		String i = "", fix = "";
		
		int count = 0, waiting = 0;
		for(char c : s.toCharArray()) {
			i+=""+c;
			if(c=='(') {
				fix+=""+c;
				waiting=1;
				++count;
			}else
			if(c==')') {
				fix+=""+c;
				if(--count==0) {
					waiting=0;
					i=i.replace(fix, ""+simpleCalculate(fix.substring(1, fix.length()-1)));
					fix="";
				}
			}else
			if(waiting==1)
				fix+=""+c;
		}
		return i;
	}
	
	private static Pattern extra = Pattern.compile("((^[-])?[ ]*[0-9.]+)[ ]*([*/])[ ]*(-?[ ]*[0-9.]+)"),
			normal = Pattern.compile("((^[-])?[ ]*[0-9.]+)[ ]*([+-])[ ]*(-?[ ]*[0-9.]+)");

	
	private static double simpleCalculate(String val) {
		if(val.contains("(")&&val.contains(")")) {
			val=splitter(val);
		}
		if(val.contains("*")||val.contains("/")) {
			Matcher s = extra.matcher(val);
			while(s.find()) {
				double a = getDouble(s.group(1));
				String b = s.group(3);
				double d = getDouble(s.group(4));
				val=val.replace(s.group(), (a==0||d==0?0:((b.equals("*")?a*d:a/d)))+"");
				s = extra.matcher(val);
			}
		}
		if(val.contains("+")||val.contains("-")) {
			Matcher s = normal.matcher(val);
			while(s.find()) {
				double a = getDouble(s.group(1));
				String b = s.group(3);
				double d = getDouble(s.group(4));
				val=val.replace(s.group(), (b.equals("+")?a+d:a-d)+"");
				s = normal.matcher(val);
			}
		}
		return getDouble(val.replaceAll("[^0-9+.-]", ""));
	}

	static Pattern mat = Pattern.compile("\\.([0-9])([0-9])?");

	public static String fixedFormatDouble(double val) {
		String text = String.format(Locale.ENGLISH,"%.2f", val);
		Matcher m = mat.matcher(text);
		if (m.find()) {
			if (m.groupCount() != 2) {
				if (m.group(1).equals("0")) {
					return m.replaceFirst("");
				}
				return m.replaceFirst(".$1");
			}
			if (m.group(1).equals("0")) {
				if (m.group(2).equals("0")) {
					return m.replaceFirst("");
				}
				return m.replaceFirst(".$1$2");
			}
			if (m.group(2).equals("0")) {
				return m.replaceFirst(".$1");
			}
			return m.replaceFirst(".$1$2");
		}
		return text;
	}

	/**
	 * @see see Get double from string
	 * @return double
	 */
	public static double getDouble(String fromString) {
		if (fromString == null)
			return 0.0D;
		String a = fromString.replaceAll("[^+0-9E.,-]+", "").replace(",", ".");
		if (isDouble(a)) {
			return Double.parseDouble(a);
		} else {
			return 0.0;
		}
	}

	/**
	 * @see see Is string, double ?
	 * @return boolean
	 */
	public static boolean isDouble(String fromString) {
		try {
			Double.parseDouble(fromString);
			return true;
		} catch (Exception err) {
			return false;
		}
	}

	/**
	 * @see see Get long from string
	 * @return long
	 */
	public static long getLong(String fromString) {
		return (long) getFloat(fromString);
	}

	/**
	 * @see see Is string, long ?
	 * @return
	 */
	public static boolean isLong(String fromString) {
		try {
			Long.parseLong(fromString);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * @see see Get int from string
	 * @return int
	 */
	public static int getInt(String fromString) {
		return (int) (getDouble(fromString) == 0 ? getLong(fromString) : getDouble(fromString));
	}

	/**
	 * @see see Is string, int ?
	 * @return boolean
	 */
	public static boolean isInt(String fromString) {
		try {
			Integer.parseInt(fromString);
			return true;
		} catch (Exception err) {
			return false;
		}
	}

	/**
	 * @see see Is string, float ?
	 * @return boolean
	 */
	public static boolean isFloat(String fromString) {
		try {
			Float.parseFloat(fromString);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * @see see Get float from string
	 * @return float
	 */
	public static float getFloat(String fromString) {
		if (fromString == null)
			return 0F;
		String a = fromString.replaceAll("[^+0-9E.,-]+", "").replace(",", ".");
		if (isFloat(a)) {
			return Float.parseFloat(a);
		} else {
			return 0;
		}
	}

	/**
	 * @see see Is string, float ?
	 * @return boolean
	 */
	public static boolean isByte(String fromString) {
		try {
			Byte.parseByte(fromString);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * @see see Get float from string
	 * @return float
	 */
	public static byte getByte(String fromString) {
		if (fromString == null)
			return (byte) 0;
		String a = fromString.replaceAll("[^+0-9E-]+", "");
		if (isByte(a)) {
			return Byte.parseByte(a);
		} else {
			return (byte) 0;
		}
	}

	/**
	 * @see see Is string, float ?
	 * @return boolean
	 */
	public static boolean isShort(String fromString) {
		try {
			Short.parseShort(fromString);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * @see see Get float from string
	 * @return float
	 */
	public static short getShort(String fromString) {
		if (fromString == null)
			return (short) 0;
		String a = fromString.replaceAll("[^+0-9E-]+", "");
		if (isShort(a)) {
			return Short.parseShort(a);
		} else {
			return (short) 0;
		}
	}

	/**
	 * @see see Is string, number ?
	 * @return boolean
	 */
	public static boolean isNumber(String fromString) {
		return isInt(fromString) || isDouble(fromString) || isLong(fromString) || isByte(fromString)
				|| isShort(fromString) || isFloat(fromString);
	}

	/**
	 * @see see Is string, boolean ?
	 * @return boolean
	 */
	public static boolean isBoolean(String fromString) {
		if (fromString == null)
			return false;
		return fromString.equalsIgnoreCase("true") || fromString.equalsIgnoreCase("false")
				|| fromString.equalsIgnoreCase("yes") || fromString.equalsIgnoreCase("no");
	}

	private static Pattern special = Pattern.compile("[^A-Z-a-z0-9_]+");

	public static boolean containsSpecial(String value) {
		return special.matcher(value).find();
	}

	public static Number getNumber(String o) {
		if (isDouble(o))
			return getDouble(o);
		if (isInt(o))
			return getInt(o);
		if (isLong(o))
			return getLong(o);
		if (isByte(o))
			return getByte(o);
		if (isShort(o))
			return getShort(o);
		if (isFloat(o))
			return getFloat(o);
		return null;
	}
}
