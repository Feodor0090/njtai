package njtai.m;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.rms.RecordStore;

import njtai.IPlatform;
import njtai.NJTAI;
import njtai.StringUtil;
import njtai.m.ui.MMenu;
import njtai.models.WebAPIA;

public class NJTAIM extends MIDlet implements IPlatform {

	public NJTAIM() {
		inst = this;
		initAPIAs();
		new NJTAI();
		NJTAI.pl = this;
	}

	private static Display dsp;
	private static NJTAIM inst;

	public static boolean isS60() {
		return System.getProperty("microedition.platform").indexOf("S60") != -1;
	}

	public static boolean isJ2MEL() {
		String vendor = System.getProperty("java.vendor");
		return (vendor != null && vendor.toLowerCase().indexOf("ndroid") != -1);
	}

	public static String ver() {
		return inst.getAppProperty("MIDlet-Version");
	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
	}

	protected void pauseApp() {
	}

	protected void startApp() throws MIDletStateChangeException {
		String locale = System.getProperty("microedition.locale");
		NJTAI.rus = (locale != null && (locale.equals("ru_RU") || locale.equals("ru-RU")));
		inst = this;
		dsp = Display.getDisplay(inst);
		if (!NJTAI.running) {
			NJTAI.running = true;
			loadPrefs();
			setScr(new MMenu());
		}
	}

	public static int getHeight() {
		return getScr().getHeight();
	}

	public void exit() {
		inst.notifyDestroyed();
	}

	public static Displayable getScr() {
		return dsp.getCurrent();
	}

	public static void setScr(Displayable d) {
		dsp.setCurrent(d);
	}

	public static void setScr(Alert a, Displayable prev) {
		dsp.setCurrent(a, prev);
	}

	public boolean savePrefs() {
		try {
			StringBuffer s = new StringBuffer();
			s.append(NJTAI.files ? "1" : "0");
			s.append('`');
			s.append(String.valueOf(NJTAI.cachingPolicy));
			s.append('`');
			s.append(NJTAI.loadCoverAtPage ? "1" : "0");
			s.append('`');
			s.append(NJTAI.keepLists ? "1" : "0");
			s.append('`');
			s.append(NJTAI.loadCovers ? "1" : "0");
			s.append('`');
			s.append(NJTAI.preloadUrl ? "1" : "0");
			s.append('`');
			s.append(NJTAI.keepBitmap ? "1" : "0");
			s.append('`');
			s.append(String.valueOf(NJTAI.view));
			s.append('`');
			s.append(NJTAI.invertPan ? "1" : "0");
			s.append('`');
			s.append(NJTAI._f1 ? "1" : "0");
			s.append('`');
			s.append(NJTAI._f2 ? "1" : "0");
			s.append('`');
			s.append(NJTAI._f3 ? "1" : "0");
			s.append('`');
			s.append(NJTAI.proxy);
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

	public void loadPrefs() {
		try {
			RecordStore r = RecordStore.openRecordStore("njtai", true);

			if (r.getNumRecords() < 1) {
				r.closeRecordStore();
				throw new RuntimeException();
			}
			byte[] d = r.getRecord(1);
			r.closeRecordStore();
			String[] s = StringUtil.splitFull(new String(d), '`');
			NJTAI.files = s[0].equals("1");
			NJTAI.cachingPolicy = Integer.parseInt(s[1]);
			NJTAI.loadCoverAtPage = s[2].equals("1");
			NJTAI.keepLists = s[3].equals("1");
			NJTAI.loadCovers = s[4].equals("1");
			NJTAI.preloadUrl = s[5].equals("1");
			NJTAI.keepBitmap = s[6].equals("1");
			NJTAI.view = Integer.parseInt(s[7]);
			NJTAI.invertPan = s[8].equals("1");
			NJTAI._f1 = s[9].equals("1");
			NJTAI._f2 = s[10].equals("1");
			NJTAI._f3 = s[11].equals("1");
			NJTAI.proxy = s[12];
		} catch (Exception e) {
			System.out.println("There is no saved settings or they are broken.");
			NJTAI.files = false;
			NJTAI.cachingPolicy = 1;
			NJTAI.loadCoverAtPage = (Runtime.getRuntime().totalMemory() != 2048 * 1024);
			NJTAI.keepLists = true;
			NJTAI.loadCovers = true;
			NJTAI.keepBitmap = true;
			NJTAI.preloadUrl = (Runtime.getRuntime().totalMemory() != 2048 * 1024);
			NJTAI.proxy = "http://nnproject.cc/proxy.php?";
			NJTAI.view = 0;
			NJTAI.invertPan = false;
		}
	}

	public void initAPIAs() {
		WebAPIA.inst = new MIDPWebAPIA();
	}

	public void showNotification(String title, String text, int type, Object prev) {
		AlertType at = null;
		switch (type) {
		case 0:
			at = AlertType.INFO;
			break;
		case 1:
			at = AlertType.CONFIRMATION;
			break;
		case 2:
			at = AlertType.WARNING;
			break;
		case 3:
			at = AlertType.ERROR;
			break;
		default:
			return;
		}

		if (prev != null && prev instanceof Displayable) {
			NJTAIM.setScr((Displayable) prev);
			NJTAI.pause(100);
		}
		NJTAIM.setScr(new Alert(title, text, null, at));
	}

	public void repaint() {
		Displayable s = NJTAIM.getScr();
		if (s instanceof Canvas)
			((Canvas) s).repaint();
	}

	public Object decodeImage(byte[] data) {
		return Image.createImage(data, 0, data.length);
	}

	public Object prescaleCover(Object original) {
		if (!(original instanceof Image))
			return original;
		Image i = (Image) original;
		int h = getHeight() * 2 / 3;
		int w = (int) (((float) h / i.getHeight()) * i.getWidth());
		return NJTAIM.resize(i, w, h);
	}

	public static String[] getStrings(String cat) {
		try {
			String locale = System.getProperty("microedition.locale");
			locale = locale.toLowerCase().substring(0, 2);
			InputStream s = NJTAIM.class.getResourceAsStream("/text/" + cat + "_" + locale + ".txt");
			if (s == null)
				s = NJTAIM.class.getResourceAsStream("/text/" + cat + "_en.txt");

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

					c12 = NJTAI.blend(c2, c1, y_d);
					c34 = NJTAI.blend(c4, c3, y_d);
				}

				// final result
				dst[index1++] = NJTAI.blend(c34, c12, x_d);
			}
		}
	}

	public static boolean isKem() {
		return isClsExists("emulator.custom.CustomMethod");
	}

	public static boolean isClsExists(String clsName) {
		try {
			Class.forName(clsName);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

}
