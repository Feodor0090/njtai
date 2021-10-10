package njtai;

import java.io.IOException;

import javax.microedition.lcdui.Image;

import njtai.models.WebAPIA;

/**
 * Main class of the application. Contains basic data and settings.
 * 
 * @author Feodor0090
 *
 */
public class NJTAI {

	public NJTAI() {
		inst = this;
	}

	/**
	 * Currently used URL prefix. Check {@link #getHP() home page downloading
	 * method} to see how it works.
	 * 
	 * @see {@link #loadPrefs()}, {@link njtai.ui.Prefs#proxy}
	 */
	public static String proxy;
	public static String baseUrl = "nhentai.net";

	private static NJTAI inst;
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
			s = WebAPIA.inst.getUtf(proxy + baseUrl);
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

	public synchronized static void clearHP() {
		hp = null;
	}

	/**
	 * Converts, for example, https://ya.ru to http://proxy.com/proxy.php?ya.ru.
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

	public static void pause(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// tube42 lib
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
	 * resize an image:
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

					c12 = blend(c2, c1, y_d);
					c34 = blend(c4, c3, y_d);
				}

				// final result
				dst[index1++] = blend(c34, c12, x_d);
			}
		}

	}
}
