package njtai;

import java.util.Vector;

public class StringUtil {
	
	public static String from(String s, String f) {
		return from(s, f, true);
	}

	public static String from(String s, String f, boolean incl) {
		int si = s.indexOf(f);
		if (si == -1) return "";
		if (!incl) {
			si += f.length();
		}
		return s.substring(si);
	}

	public static String range(String s, String f, String t) {
		return range(s, f, t, false);
	}

	public static String range(String s, String f, String t, boolean incl) {
		if (s.length() == 0) return "";
		int si = s.indexOf(f);
		if (si == -1) {
			si = 0;
		} else if (!incl) {
			si += f.length();
		}
		int ei = s.indexOf(t, si);
		if (ei == -1 || t.length() == 0) {
			return s.substring(si);
		}
		if (incl) {
			ei += t.length();
		}
		return s.substring(si, ei);
	}

	public static String[] splitRanges(String s, String f, String t, boolean incl) {
		Vector v = new Vector();
		int i = 0;
		while (true) {
			int si = s.indexOf(f, i);
			if (si == -1)
				break;
			if (!incl)
				si += f.length();
			int ei = s.indexOf(t, si);
			i = ei + t.length();
			if (incl)
				ei += t.length();
			v.addElement(s.substring(si, ei));
		}
		String[] a = new String[v.size()];
		v.copyInto(a);
		v.removeAllElements();
		v = null;
		return a;
	}

	public static String[] split(String str, String k) {
		Vector v = new Vector(32, 16);
		int lle = 0;
		while (true) {
			int nle = str.indexOf(k, lle);
			if (nle == -1) {
				v.addElement(str.substring(lle, str.length()));
				break;
			}
			v.addElement(str.substring(lle, nle));
			lle = nle + k.length();
		}
		String[] a = new String[v.size()];
		v.copyInto(a);
		v.removeAllElements();
		v.trimToSize();
		v = null;
		return a;
	}

	public static String toSingleLine(String s) {
		return s.replace('\r', ' ').replace('\n', ' ').replace('\t', ' ');
	}

	// SN
	public static String[] splitFull(String str, char c) {
		Vector v = new Vector(32, 16);
		int lle = 0;
		while (true) {
			int nle = str.indexOf(c, lle);
			if (nle == -1) {
				v.addElement(str.substring(lle, str.length()));
				break;
			}
			v.addElement(str.substring(lle, nle));
			lle = nle + 1;
		}
		String[] a = new String[v.size()];
		v.copyInto(a);
		v.removeAllElements();
		v.trimToSize();
		v = null;
		return a;
	}

	/**
	 * @param str
	 * @return String with parsed html escape codes
	 */
	public static String htmlString(String str) {
		str = replace(str, "&#39;", "'");
		str = replace(str, "&#x27;", "'");
		//str = replace(str, "&apos;", "'");
		str = replace(str, "&quot;", "\"");
		//str = replace(str, "&lt;", "<");
		//str = replace(str, "&gt;", ">");
		//str = replace(str, "&nbsp;", " ");
		str = replace(str, "&ndash;", "-");
		str = replace(str, "&amp;", "&");
		return str;
	}
	
	/**
	 * @param str original
	 * @param from string to find
	 * @param to string to replace with
	 * @return replaced string
	 */
	public static String replace(String str, String from, String to) {
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
