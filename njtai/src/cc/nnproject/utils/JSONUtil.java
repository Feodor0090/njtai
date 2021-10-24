package cc.nnproject.utils;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Lighter JSON parser by nnproject.cc
 * @author Shinovon
 */
public class JSONUtil {
	/**
	 * @param str JSON string
	 * @return Parsed result. Hashtable, Vector or String
	 */
	public static Object parse(String str) {
		int f = str.charAt(0);
		if(f == '{') {
			return object(str.substring(1, str.length() - 1));
		}
		/*if(f == '[') {
			return array(str.substring(1, str.length() - 1));
		}*/
		if(f == '"') {
			return replace(replace(str.substring(1, str.length() - 1), "\\n", "\n"), "\\\"", "\"");
		}
		return str;
	}

	/*
	public static Vector array(String str) {
		Vector v = new Vector();
		Hashtable ht = new Hashtable();
		int unclosed = 0;
		int index = 0;
		int length = str.length();
		boolean escape = false;
		int splIndex;
		for (; index < length; index = splIndex + 1) {
			splIndex = index;
			boolean quotes;
			for (quotes = false; splIndex < length && (quotes || unclosed > 0 || str.charAt(splIndex) != ','); splIndex++) {
				char c = str.charAt(splIndex);
				if (!escape) {
					if (c == '\\') {
						escape = true;
					} else if (c == '"') {
						quotes = !quotes;
					}
				} else {
					escape = false;
				}
				if (!quotes) {
					if (c == '{') {
						unclosed++;
					} else if (c == '}') {
						unclosed--;
					}
				}
			}
			if (quotes || unclosed > 0) {
				throw new RuntimeException("Corrupted JSON");
			}
			v.addElement(parse(str.substring(index, splIndex)));
		}
		return v;
	}
	*/
	/**
	 * @param str JSON object string
	 * @return Parsed hashtable
	 */
	public static Hashtable object(String str) {
		Hashtable ht = new Hashtable();
		int unclosed = 0;
		int index = 0;
		int length = str.length();
		boolean escape = false;
		int splIndex;
		for (; index < length; index = splIndex + 1) {
			splIndex = index;
			boolean quotes;
			for (quotes = false; splIndex < length && (quotes || unclosed > 0 || str.charAt(splIndex) != ','); splIndex++) {
				char c = str.charAt(splIndex);
				if (!escape) {
					if (c == '\\') {
						escape = true;
					}
					if (c == '"') {
						quotes = !quotes;
					}
				} else {
					escape = false;
				}
				if (!quotes) {
					if (c == '{') {
						unclosed++;
					} else if (c == '}') {
						unclosed--;
					}
				}
			}
			if (quotes || unclosed > 0) {
				throw new RuntimeException("Corrupted JSON");
			}
			String token = str.substring(index, splIndex);
			int splIndex2 = token.indexOf(":");
			ht.put(token.substring(1, splIndex2 - 1).trim(), parse(token.substring(splIndex2 + 1)));
		}
		return ht;
	}

	/**
	 * @param obj Hashtable, Vector or other object to encode in JSON
	 * @return JSON string
	 */
	public static String build(Object obj) {
		if(obj instanceof Hashtable) {
			Hashtable ht = (Hashtable) obj;
			Enumeration en = ht.keys();
			if (!en.hasMoreElements()) {
				return "{}";
			}
			String r = "{";
			while (true) {
				Object e = en.nextElement();
				r = r.concat("\"")
				.concat(e.toString())
				.concat("\":")
				.concat(build(ht.get(e)));
				if (!en.hasMoreElements()) {
					return r.concat("}");
				}
				r = r.concat(",");
			}
		} /*else if (obj instanceof Vector) {
			Vector v = (Vector) obj;
			Enumeration en = v.elements();
			if (!en.hasMoreElements())
				return "[]";
			String r = "[";
			for (;;) {
				r = r.concat(build(en.nextElement()));
				if (!en.hasMoreElements()) {
					return r.concat("]");
				}
				r = r.concat(",");
			}
		}*/ else if(obj instanceof String) {
			String s = (String) obj;
			s = replace(s, "\n", "\\n");
			s = replace(s, "\"", "\\\"");
			return "\"".concat(s).concat("\"");
		} else {
			return String.valueOf(obj);
		}
	}
	

	private static String replace(String str, String from, String to) {
		int j = str.indexOf(from);
		if (j == -1)
			return str;
		final StringBuffer sb = new StringBuffer();
		int k = 0;
		for (int i = from.length(); j != -1; j = str.indexOf(from, k)) {
			sb.append(str.substring(k, j)).append(to);
			k = j + i;
		}
		sb.append(str.substring(k, str.length()));
		return sb.toString();
	}
	
}
