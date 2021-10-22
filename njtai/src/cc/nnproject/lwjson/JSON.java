package cc.nnproject.lwjson;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * 
 * Lighter JSON parser by nnproject.cc<br>
 * 
 * @author Shinovon
 * @version 0.1
 */
public class JSON {

	/**
	 * Creates an object from it's JSON representation.
	 * 
	 * @param str JSON string to parse.
	 * @return Decoded object.
	 * @throws Exception Passed data is corrupted.
	 */
	public static Object parseJSON(String str) throws Exception {
		if (str == null || str.equals(""))
			throw new Exception("Empty string");
		if (str.length() < 2) {
			return str;
		}
		char first = str.charAt(0);
		char last = str.charAt(str.length() - 1);
		if (first == '{' && last != '}' || first == '[' && last != ']' || first == '"' && last != '"') {
			throw new Exception("Unexpected end of text");
		} else if (first == '"') {
			// String
			str = str.substring(1, str.length() - 1);
			return str;
		} else if (first != '{' && first != '[') {
			return str;
		} else {
			// JSON
			str = removeChars(str, '\r');
			str = removeChars(str, '\n');
			str = removeChars(str, '\t');
			// Unclosed delimiters count
			int d = 0;
			// Is Object
			boolean o = first == '{';
			int i = 1;
			int l = str.length() - 1;
			// Next splitter char
			int spl = o ? ':' : ',';
			// Escape
			boolean e = false;
			String n = null;
			Object res = null;
			if (o) res = new Hashtable();
			else res = new Vector();

			for (int j; i < l; i = j + 1) {
				while (i < l - 1 && str.charAt(i) == ' ') {
					i++;
				}
				j = i;

				// Quotes
				boolean s = false;
				while (j < l && (s || d > 0 || str.charAt(j) != spl)) {
					char c = str.charAt(j);
					if (!e) {
						if (c == '\\') e = true;
						if (c == '"') s = !s;
					} else e = false;
					if (!s) {
						if (c != '{' && c != '[') {
							if (c == '}' || c == ']') d--;
						} else d++;
					}
					j++;
				}

				if (s || d > 0) {
					throw new Exception("Corrupted JSON");
				}

				if (o && n == null) {
					n = removeChars(str.substring(i + 1, j - 1), ' ');
					spl = ',';
				} else {
					Object v = str.substring(i, j);
					v = parseJSON((String) v);
					if (o) {
						((Hashtable) res).put(n, v);
						n = null;
						spl = ':';
					} else if (j > i) {
						((Vector) res).addElement(v);
					}
				}
			}
			return res;
		}

	}

	/**
	 * Builds JSON.
	 * 
	 * @param j Object to encode.
	 * @return JSON string.
	 * @throws Exception Object is broken.
	 */
	public static String buildJSON(Object j) throws Exception {
		if (j instanceof Hashtable) {
			Hashtable table = (Hashtable) j;
			if (table.size() == 0)
				return "{}";
			String s = "{";
			Enumeration elements = table.keys();
			int i = 0;
			while (elements.hasMoreElements()) {
				String k = elements.nextElement().toString();
				s += "\"" + k + "\":";
				s += buildJSON(table.get(k));
				i++;
				if (i < table.size())
					s += ",";
			}
			s += "}";
			return s;
		} else if (j instanceof Vector) {
			Vector list = (Vector) j;
			if (list.size() == 0)
				return "[]";
			String s = "[";
			int i = 0;
			while (i < list.size()) {
				s += buildJSON(list.elementAt(i));
				i++;
				if (i < list.size())
					s += ",";
			}
			s += "]";
			return s;
		} else if (j instanceof String) {
			return "\"" + j.toString() + "\"";
		} else if (j instanceof Integer || j instanceof Double || j instanceof Long || j instanceof Byte
				|| j instanceof Float || j instanceof Boolean) {
			return j.toString();
		} else if (j == null) {
			return "null";
		} else {
			return j.toString();
		}
	}

	// Replace util
	private static String removeChars(String str, char from) {
		int j = str.indexOf(from);
		if (j == -1)
			return str;
		int k = 0;
		final StringBuffer sb = new StringBuffer();

		while (j != -1) {
			sb.append(str.substring(k, j));
			k = j + 1;
			j = str.indexOf(from, k);
		}

		sb.append(str.substring(k, str.length()));
		return sb.toString();
	}

}
