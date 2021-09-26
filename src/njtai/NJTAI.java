package njtai;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.rms.RecordStore;

import njtai.ui.MMenu;

public class NJTAI extends MIDlet {

	public NJTAI() {
		inst = this;
	}

	public static String proxy;
	public static String baseUrl = "nhentai.net";

	private static NJTAI inst;
	private static Display dsp;

	/**
	 * Home page content
	 */
	private static String hp = null;

	private boolean running = false;

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
	
	public static boolean isS60() {
		return System.getProperty("microedition.platform").indexOf("S60") != -1;
	}

	public static boolean rus = false;

	public static boolean savePrefs() {
		try {
			StringBuffer s = new StringBuffer();
			s.append(files?"1":"0");
			s.append('`');
			s.append(String.valueOf(cachingPolicy));
			s.append('`');
			s.append(loadCoverAtPage ? "1" : "0");
			s.append('`');
			s.append(keepLists ? "1" : "0");
			s.append('`');
			s.append(loadCovers ? "1" : "0");
			s.append('`');
			s.append(preloadUrl ? "1" : "0");
			s.append('`');
			s.append(keepBitmap ? "1" : "0");
			s.append('`');
			s.append(String.valueOf(view));
			s.append('`');
			s.append(proxy);
			byte[] d = s.toString().getBytes();
			RecordStore r = RecordStore.openRecordStore("njtai", true);

			if (r.getNumRecords() == 0) {
				r.addRecord(new byte[1], 0, 1);
			}
			r.setRecord(1, d, 0, d.length);
			r.closeRecordStore();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static void loadPrefs() {
		try {
			RecordStore r = RecordStore.openRecordStore("njtai", true);

			if (r.getNumRecords() < 1) {
				r.closeRecordStore();
				throw new RuntimeException();
			}
			byte[] d = r.getRecord(1);
			r.closeRecordStore();
			String[] s = StringUtil.splitFull(new String(d), '`');
			files = s[0].equals("1");
			cachingPolicy = Integer.parseInt(s[1]);
			loadCoverAtPage = s[2].equals("1");
			keepLists = s[3].equals("1");
			loadCovers = s[4].equals("1");
			preloadUrl = s[5].equals("1");
			keepBitmap = s[6].equals("1");
			view = Integer.parseInt(s[7]);
			proxy = s[8];
		} catch (Exception e) {
			e.printStackTrace();
			files = false;
			cachingPolicy = 1;
			loadCoverAtPage = (Runtime.getRuntime().totalMemory() != 2048 * 1024);
			keepLists = true;
			loadCovers = true;
			keepBitmap = true;
			preloadUrl = (Runtime.getRuntime().totalMemory() != 2048 * 1024);
			proxy = "http://nnproject.cc/proxy.php?";
			view = 1;
		}
	}

	public static String ver() {
		return inst.getAppProperty("MIDlet-Version");
	}

	/**
	 * Gets home page.
	 * 
	 * @return Content of the page.
	 * @throws IOException
	 */
	public synchronized static String getHP() throws IOException {
		String s = hp;
		if (s == null) {
			s = httpUtf(proxy + baseUrl);
			if ((Runtime.getRuntime().totalMemory() != 2048 * 1024)) {
				hp = s;
			}
		}
		if (s == null)
			throw new IOException();
		return s;
	}

	public synchronized static void clearHP() {
		hp = null;
	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
	}

	protected void pauseApp() {
	}

	protected void startApp() throws MIDletStateChangeException {
		String locale = System.getProperty("microedition.locale");
		rus = (locale != null && (locale.equals("ru_RU") || locale.equals("ru-RU")));
		inst = this;
		dsp = Display.getDisplay(inst);
		if (!running) {
			running = true;
			loadPrefs();
			setScr(new MMenu());
		}
	}

	public static void close() {
		inst.notifyDestroyed();
	}

	public static Displayable getScr() {
		return dsp.getCurrent();
	}

	public static void setScr(Displayable d) {
		dsp.setCurrent(d);
	}

	public static void pause(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static int getHeight() {
		return getScr().getHeight();
	}

	// Net util

	public static byte[] http(String url) {
		ByteArrayOutputStream o = null;
		HttpConnection hc = null;
		InputStream i = null;
		try {
			o = new ByteArrayOutputStream();
			hc = (HttpConnection) Connector.open(url);
			hc.setRequestMethod("GET");

			i = hc.openInputStream();
			byte[] b = new byte[16384];

			int c;
			while ((c = i.read(b)) != -1) {
				// var10 += (long) var7;
				o.write(b, 0, c);
				o.flush();
			}

			return o.toByteArray();
		} catch (NullPointerException e) {
			return null;
		} catch (IOException e) {
			return null;
		} finally {
			try {
				if (i != null)
					i.close();
			} catch (IOException e) {
			}
			try {
				if (hc != null)
					hc.close();
			} catch (IOException e) {
			}
			try {
				if (o != null)
					o.close();
			} catch (IOException e) {
			}
		}
	}

	public static Image httpImg(String url) {
		byte[] b = http(url);
		return Image.createImage(b, 0, b.length);
	}

	public static String httpUtf(String url) {
		try {
			return new String(http(url), "UTF-8");
		} catch (Exception e) {
			return null;
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
