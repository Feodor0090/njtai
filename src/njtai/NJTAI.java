package njtai;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import njtai.models.WebAPIA;

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
	public static IPlatform pl;

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
			s = WebAPIA.inst.getUtfOrNull(proxy + baseUrl);
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

		// url proc
		if (url.startsWith("https://"))
			url = url.substring(8);
		if (url.startsWith("http://"))
			url = url.substring(7);
		return NJTAI.proxy + url;
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
			return StringUtil.splitFull(r, '\n');
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
}
