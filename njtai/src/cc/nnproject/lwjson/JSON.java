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

	// configuration constants

	public final static boolean parse_other_values = false;
	public final static boolean parse_hexunicode = false;
	public final static boolean build_reconstruct_strings = false;

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
			if (parse_hexunicode) {
				if (str.indexOf("\\u") > -1) {
					try {
						int l = str.length();
						StringBuffer sb = new StringBuffer();
						int i = 0;
						while (i < l) {
							char c = str.charAt(i);
							switch (c) {
							case '\\':
								if (str.charAt(i + 1) == 'u') {
									i++;
									String h = "" + str.charAt(i++) + str.charAt(i++) + str.charAt(i++)
											+ str.charAt(i++);
									if (h.charAt(2) < '0' || h.charAt(2) > 'F' || h.charAt(3) < '0'
											|| h.charAt(3) > 'F')
										throw new Exception("Wrong hex");
									i += 5;
									sb.append((char) Integer.parseInt(h, 16));
								}
								break;
							default:
								sb.append(c);
								i++;
							}
						}
						str = sb.toString();
					} catch (Exception e) {
					}
				}
			}
			/*
			 * str = replace(str, "<br>", "\n"); str = replace(str, "&amp;", "&"); str =
			 * replace(str, "&lt;", "<"); str = replace(str, "&gt;", ">"); str =
			 * replace(str, "&quot;", "\""); str = replace(str, "\\n", "\n"); str =
			 * replace(str, "\\\"", "\""); str = replace(str, "\\\'", "\'"); str =
			 * replace(str, "\\/", "/"); str = replace(str, "\\\\", "\\");
			 */
			return str;
		} else if (first != '{' && first != '[') {
			if (parse_other_values) {
				// Null
				// if (str.equals("null"))
				// return null;
				// Boolean
				if (str.equals("true"))
					return Boolean.TRUE;
				if (str.equals("false"))
					return Boolean.FALSE;
				// Hexadecimal
				if (str.charAt(0) == '0' && str.charAt(1) == 'x') {
					try {
						return new Integer(Integer.parseInt(str.substring(2), 16));
					} catch (Exception e) {
						try {
							return new Long(Long.parseLong(str.substring(2), 16));
						} catch (Exception e2) {
							// Skip
						}
					}
				}
				// Numbers
				try {
					return Integer.valueOf(str);
				} catch (Exception e) {
					try {
						return new Long(Long.parseLong(str));
					} catch (Exception e2) {
						try {
							return Double.valueOf(str);
						} catch (Exception e3) {
						}
					}
				}
			}
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
			if (o)
				res = new Hashtable();
			else
				res = new Vector();

			int j;
			for (; i < l; i = j + 1) {
				while (i < l - 1 && str.charAt(i) == ' ') {
					i++;
				}

				j = i;

				// Quotes
				boolean s;
				for (s = false; j < l && (s || d > 0 || str.charAt(j) != spl); j++) {
					char c = str.charAt(j);
					if (!e) {
						if (c == '\\')
							e = true;

						if (c == '"')
							s = !s;
					} else
						e = false;

					if (!s) {
						if (c != '{' && c != '[') {
							if (c == '}' || c == ']')
								d--;
						} else
							d++;
					}
				}

				if (s || d > 0) {
					throw new Exception("Corrupted JSON");
				}

				if (o && n == null) {
					n = removeChars(str.substring(i + 1, j - 1), ' ');
					spl = ',';
				} else {
					Object v = str.substring(i, j);
					v = parseJSON(v.toString());
					if (o) {
						((Hashtable) res).put(n, v);
						n = null;
						spl = ':';
					} else if (j > i)
						((Vector) res).addElement(v);
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
		} else if (build_reconstruct_strings) {
			if (j instanceof String) {
				return reconstructJSONString((String) j);
			}
			return j.toString();
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

	/**
	 * @deprecated
	 */
	private static String reconstructJSONString(String s) {
		if (s.equals("true") || s.equals("false") || s.equals("null"))
			return s;
		try {
			return "" + Integer.parseInt(s);
		} catch (Exception e) {
			try {
				return "" + Long.parseLong(s);
			} catch (Exception e2) {
				try {
					return "" + Double.parseDouble(s);
				} catch (Exception e3) {
				}
			}
		}
		return "\"" + s + "\"";
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
