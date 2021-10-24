package cc.nnproject.utils;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

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
		if(f == '[') {
			return array(str.substring(1, str.length() - 1));
		}
		if(f == '"') {
			return str.substring(1, str.length() - 1);
		}
		return str;
	}

	/**
	 * @param str JSON array string
	 * @return Parsed vector
	 */
	public static Vector array(String str) {
		Vector v = new Vector();
		int index = str.indexOf(",");
		boolean b = true;
		while (b) {
			if(index == -1) {
				b = false;
				index = str.length();
			}
			v.addElement(parse(str.substring(0, index).trim()));
			if(b) {
				str = str.substring(index + 1);
				index = str.indexOf(",");
			}
		}
		return v;
	}

	/**
	 * @param str JSON object string
	 * @return Parsed hashtable
	 */
	public static Hashtable object(String str) {
		Hashtable ht = new Hashtable();
		int index = str.indexOf(",");
		boolean b = true;
		while (b) {
			if(index == -1) {
				b = false;
				index = str.length();
			}
			String token = str.substring(0, index).trim();
			int index2 = token.indexOf(":");
			ht.put(token.substring(1, index2 - 1).trim(), parse(token.substring(index2 + 1)));
			if(b) {
				str = str.substring(index + 1);
				index = str.indexOf(",");
			}
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
		} else if (obj instanceof Vector) {
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
		} else if(obj instanceof String) {
			String s = (String) obj;
			return "\"".concat(s).concat("\"");
		} else {
			return String.valueOf(obj);
		}
	}
}
