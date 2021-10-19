package njtai;

import java.io.IOException;

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
	public static boolean loadCoverAtPage = true;
	public static boolean keepLists = true;
	public static boolean loadCovers = true;
	/**
	 * Enable urls preloading?
	 */
	public static boolean preloadUrl = true;
	public static boolean keepBitmap = true;
	public static int view = 0;
	public static boolean files;
	public static boolean invertPan;
	public static boolean _f1;
	public static boolean _f2;
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

			if ((Runtime.getRuntime().totalMemory() != 2048 * 1024)) {
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
