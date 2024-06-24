package njtai;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Image;

import njtai.m.NJTAIM;

/**
 * Main class of the application. Contains basic data and settings.
 * 
 * @author Feodor0090
 *
 */
public class NJTAI {

	/**
	 * Currently used URL prefix. Check {@link #getHP() home page downloading
	 * method} to see how it works.
	 * 
	 * @see {@link #loadPrefs()}, {@link njtai.ui.Prefs#proxy}
	 */
	public static String proxy;
	/**
	 * Base site URL.
	 */
	public static final String baseUrl = "nhentai.net";

	/**
	 * Instance of currently active platform.
	 */
	public static NJTAIM pl;

	/**
	 * Home page content
	 */
	private static String hp = null;

	/**
	 * Is the app already running?
	 */
	public static boolean running = false;

	/**
	 * Should images be kept or preloaded?
	 * <ul>
	 * <li>0 - nothing should be kept
	 * <li>1 - view may keep already viewed images
	 * <li>2 - images can be preloaded
	 * </ul>
	 */
	public static int cachingPolicy = 0;
	/**
	 * Load image on info page?
	 */
	public static boolean loadCoverAtPage = true;
	/**
	 * Keep existing lists when leaving?
	 */
	public static boolean keepLists = true;
	/**
	 * Load imags in lists?
	 */
	public static boolean loadCovers = true;
	/**
	 * Keep decoded images in RAM?
	 */
	public static boolean keepBitmap = true;
	/**
	 * Auto, SWR or HWA.
	 */
	public static int view = 0;
	/**
	 * Use device's memory card?
	 */
	public static boolean files;
	/**
	 * Invert D-PAD directions?
	 */
	public static boolean invertPan;
	/**
	 * Not used yet.
	 */
	public static boolean _f1;
	/**
	 * Not used yet.
	 */
	public static boolean _f2;
	/**
	 * Not used yet.
	 */
	public static boolean _f3;

	/**
	 * Use russian localization?
	 */
	public static boolean rus = false;

	/**
	 * Gets home page.
	 * 
	 * @return Content of the page.
	 * @throws IOException            If nothing was loaded.
	 * @throws IllegalAccessException If empty string was loaded.
	 */
	public synchronized static String getHP() throws IOException, IllegalAccessException {
		String s = hp;
		if (s == null) {
			s = getUtfOrNull(proxy + baseUrl);
			if (s == null)
				throw new IOException();
			if (s.length() < 2)
				throw new IllegalAccessException();

			if (Runtime.getRuntime().totalMemory() != 2048 * 1024) {
				hp = s;
			}
		}
		return s;
	}

	/**
	 * Clears main page's content.
	 */
	public synchronized static void clearHP() {
		hp = null;
	}

	/**
	 * Converts, for example, https://ya.ru to http://proxy.com/proxy.php?ya.ru.
	 * 
	 * @param url Original URL.
	 * @return URL, ready to be loaded.
	 */
	public static String proxyUrl(String url) {
		if (url == null)
			return null;

		if(NJTAI.proxy.length() == 0) {
			return url;
		}
		return NJTAI.proxy + url(url);
	}
	
	public static String url(String url) {
		StringBuffer sb = new StringBuffer();
		char[] chars = url.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			int c = chars[i];
			if (65 <= c && c <= 90) {
				sb.append((char) c);
			} else if (97 <= c && c <= 122) {
				sb.append((char) c);
			} else if (48 <= c && c <= 57) {
				sb.append((char) c);
			} else if (c == 32) {
				sb.append("%20");
			} else if (c == 45 || c == 95 || c == 46 || c == 33 || c == 126 || c == 42 || c == 39 || c == 40
					|| c == 41) {
				sb.append((char) c);
			} else if (c <= 127) {
				sb.append(hex(c));
			} else if (c <= 2047) {
				sb.append(hex(0xC0 | c >> 6));
				sb.append(hex(0x80 | c & 0x3F));
			} else {
				sb.append(hex(0xE0 | c >> 12));
				sb.append(hex(0x80 | c >> 6 & 0x3F));
				sb.append(hex(0x80 | c & 0x3F));
			}
		}
		return sb.toString();
	}

	private static String hex(int i) {
		String s = Integer.toHexString(i);
		return "%".concat(s.length() < 2 ? "0" : "").concat(s);
	}

	/**
	 * Stops a thread, ignoring interruptions.
	 * 
	 * @param ms Ms to wait.
	 */
	public static void pause(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads localization file.
	 * 
	 * @param cat    Category of strings.
	 * @param locale Language code to use.
	 * @return List of strings to use.
	 */
	public static String[] getStrings(String cat, String locale) {
		try {
			if (locale == null) {
				locale = System.getProperty("microedition.locale");
				locale = locale.toLowerCase().substring(0, 2);
			}
			InputStream s = NJTAI.class.getResourceAsStream("/text/" + cat + "_" + locale + ".txt");
			if (s == null)
				s = NJTAI.class.getResourceAsStream("/text/" + cat + "_en.txt");

			char[] buf = new char[32 * 1024];
			InputStreamReader isr = new InputStreamReader(s, "UTF-8");
			int l = isr.read(buf);
			isr.close();
			String r = new String(buf, 0, l).replace('\r', ' ');
			return splitFull(r, '\n');
		} catch (Exception e) {
			e.printStackTrace();
			// null is returned to avoid massive try-catch constructions near every call.
			// Normally, it always return english file.
			return null;
		}
	}

	/**
	 * Loads localization file.
	 * 
	 * @param cat Category of strings.
	 * @return List of strings to use.
	 */
	public static String[] getStrings(String cat) {
		return getStrings(cat, null);
	}
	
	// StringUtils

	public static String from(String s, String f) {
		return from(s, f, true);
	}

	public static String from(String s, String f, boolean incl) {
		int si = s.indexOf(f);
		if (si == -1)
			return "";
		if (!incl) {
			si += f.length();
		}
		return s.substring(si);
	}

	public static String range(String s, String f, String t) {
		return range(s, f, t, false);
	}

	public static String range(String s, String f, String t, boolean incl) {
		if (s.length() == 0)
			return "";
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
	 * Replaces some common html entities.
	 * 
	 * @param str String to process.
	 * @return String with parsed html escape codes
	 * @author Shinovon
	 */
	public static String htmlString(String str) {
		str = replace(str, "&#39;", "'");
		str = replace(str, "&#x27;", "'");
		// str = replace(str, "&apos;", "'");
		str = replace(str, "&quot;", "\"");
		str = replace(str, "&lt;", "<");
		str = replace(str, "&gt;", ">");
		// str = replace(str, "&nbsp;", " ");
		str = replace(str, "&ndash;", "-");
		str = replace(str, "&amp;", "&");
		return str;
	}

	/**
	 * @param str  original
	 * @param from string to find
	 * @param to   string to replace with
	 * @return replaced string
	 * @author Shinovon
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
	
	// Web
	
	public static byte[] get(String url) throws IOException {
		if (url == null)
			throw new IllegalArgumentException("URL is null");
		ByteArrayOutputStream o = null;
		HttpConnection hc = null;
		InputStream i = null;
		try {
			o = new ByteArrayOutputStream();
			hc = (HttpConnection) Connector.open(url);
			hc.setRequestMethod("GET");
			int r = hc.getResponseCode();
			if (r == 301 || r == 302) {
				String redir = hc.getHeaderField("Location");
				if (redir.startsWith("/")) {
					String tmp = url.substring(url.indexOf("//") + 2);
					String host = url.substring(0, url.indexOf("//")) + "//" + tmp.substring(0, tmp.indexOf("/"));
					redir = host + redir;
				}
				hc.close();
				hc = (HttpConnection) Connector.open(redir);
				hc.setRequestMethod("GET");
			}
			i = hc.openInputStream();
			byte[] b = new byte[16384];

			int c;
			while ((c = i.read(b)) != -1) {
				o.write(b, 0, c);
				o.flush();
			}

			return o.toByteArray();
		} finally {
			try {
				if (i != null) i.close();
			} catch (IOException e) {
			}
			try {
				if (hc != null) hc.close();
			} catch (IOException e) {
			}
			try {
				if (o != null) o.close();
			} catch (IOException e) {
			}
		}
	}

	public static byte[] getOrNull(String url) {
		try {
			return get(url);
		} catch (Exception e) {
			return null;
		}
	}

	public static String getUtf(String url) throws IOException {
		return new String(get(url), "UTF-8");
	}

	public static String getUtfOrNull(String url) {
		try {
			return getUtf(url);
		} catch (Exception e) {
			return null;
		}
	}
	
	// ImageUtils

	/**
	 * Resizes the image.
	 * 
	 * @param src_i  Original image.
	 * @param size_w
	 * @param size_h
	 * @return Resized image.
	 */
	public static Image resize(Image src_i, int size_w, int size_h) {

		// set source size
		int w = src_i.getWidth();
		int h = src_i.getHeight();

		// no change?
		if (size_w == w && size_h == h)
			return src_i;

		int[] dst = new int[size_w * size_h];

		resize_rgb_filtered(src_i, dst, w, h, size_w, size_h);

		// not needed anymore
		src_i = null;

		return Image.createRGBImage(dst, size_w, size_h, true);
	}

	private static final void resize_rgb_filtered(Image src_i, int[] dst, int w0, int h0, int w1, int h1) {
		int[] buffer1 = new int[w0];
		int[] buffer2 = new int[w0];

		// UNOPTIMIZED bilinear filtering:
		//
		// The pixel position is defined by y_a and y_b,
		// which are 24.8 fixed point numbers
		//
		// for bilinear interpolation, we use y_a1 <= y_a <= y_b1
		// and x_a1 <= x_a <= x_b1, with y_d and x_d defining how long
		// from x/y_b1 we are.
		//
		// since we are resizing one line at a time, we will at most
		// need two lines from the source image (y_a1 and y_b1).
		// this will save us some memory but will make the algorithm
		// noticeably slower

		for (int index1 = 0, y = 0; y < h1; y++) {

			final int y_a = ((y * h0) << 8) / h1;
			final int y_a1 = y_a >> 8;
			int y_d = y_a & 0xFF;

			int y_b1 = y_a1 + 1;
			if (y_b1 >= h0) {
				y_b1 = h0 - 1;
				y_d = 0;
			}

			// get the two affected lines:
			src_i.getRGB(buffer1, 0, w0, 0, y_a1, w0, 1);
			if (y_d != 0)
				src_i.getRGB(buffer2, 0, w0, 0, y_b1, w0, 1);

			for (int x = 0; x < w1; x++) {
				// get this and the next point
				int x_a = ((x * w0) << 8) / w1;
				int x_a1 = x_a >> 8;
				int x_d = x_a & 0xFF;

				int x_b1 = x_a1 + 1;
				if (x_b1 >= w0) {
					x_b1 = w0 - 1;
					x_d = 0;
				}

				// interpolate in x
				int c12, c34;
				int c1 = buffer1[x_a1];
				int c3 = buffer1[x_b1];

				// interpolate in y:
				if (y_d == 0) {
					c12 = c1;
					c34 = c3;
				} else {
					int c2 = buffer2[x_a1];
					int c4 = buffer2[x_b1];

					c12 = NJTAI.blend(c2, c1, y_d);
					c34 = NJTAI.blend(c4, c3, y_d);
				}

				// final result
				dst[index1++] = NJTAI.blend(c34, c12, x_d);
			}
		}
	}

	/**
	 * Part of tube42 imagelib. Blends 2 colors.
	 * 
	 * @param c1
	 * @param c2
	 * @param value256
	 * @return Blended value.
	 */
	public static final int blend(final int c1, final int c2, final int value256) {

		final int v1 = value256 & 0xFF;
		final int c1_RB = c1 & 0x00FF00FF;
		final int c2_RB = c2 & 0x00FF00FF;

		final int c1_AG = (c1 >>> 8) & 0x00FF00FF;

		final int c2_AG_org = c2 & 0xFF00FF00;
		final int c2_AG = (c2_AG_org) >>> 8;

		// the world-famous tube42 blend with one mult per two components:
		final int rb = (c2_RB + (((c1_RB - c2_RB) * v1) >> 8)) & 0x00FF00FF;
		final int ag = (c2_AG_org + ((c1_AG - c2_AG) * v1)) & 0xFF00FF00;
		return ag | rb;

	}
}
