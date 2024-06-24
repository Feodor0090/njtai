package njtai.m;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.rms.RecordStore;

import njtai.NJTAI;
import njtai.m.ui.MMenu;

/**
 * Main mobile class.
 * 
 * @author Feodor0090
 *
 */
public class NJTAIM extends MIDlet {

	private static Display dsp;
	private static NJTAIM inst;

	/**
	 * Are we working on 9.3?
	 * 
	 * @return Status of 9.3 detection.
	 */
	public static boolean isS60v3fp2() {
		return System.getProperty("microedition.platform").indexOf("sw_platform_version=3.2") != -1;
	}

	/**
	 * Are we working on J2ME Loader?
	 * 
	 * @return Status of j2meL detection.
	 */
	public static boolean isJ2MEL() {
		String vendor = System.getProperty("java.vendor");
		return (vendor != null && vendor.toLowerCase().indexOf("ndroid") != -1);
	}

	/**
	 * @return Midlet version.
	 */
	public static String ver() {
		return inst.getAppProperty("MIDlet-Version");
	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
	}

	protected void pauseApp() {
	}

	protected void startApp() throws MIDletStateChangeException {
		String loc = System.getProperty("microedition.locale");
		if (loc != null) {
			loc = loc.toLowerCase();
			NJTAI.rus = (loc.indexOf("ru") != -1 || loc.indexOf("ua") != -1 || loc.indexOf("kz") != -1
					|| loc.indexOf("by") != -1);
		}
		inst = this;
		dsp = Display.getDisplay(inst);
		if (!NJTAI.running) {
			NJTAI.running = true;
			new NJTAI();
			NJTAI.pl = this;
			loadPrefs();
			setScr(new MMenu());
		}
	}

	/**
	 * @return Height of display.
	 */
	public static int getHeight() {
		return getScr().getHeight();
	}

	public void exit() {
		inst.notifyDestroyed();
	}

	/**
	 * @return Currently shown screen.
	 */
	public static Displayable getScr() {
		return dsp.getCurrent();
	}

	/**
	 * Sets current screen.
	 * 
	 * @param d Screen to activate.
	 */
	public static void setScr(Displayable d) {
		dsp.setCurrent(d);
	}

	/**
	 * Sets current screen.
	 * 
	 * @param a    Screen to activate.
	 * @param prev Next screen.
	 */
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
			// Keeping the value to avoid data breaking.
			s.append('0');
			//s.append(NJTAI._d1 ? "1" : "0");
			s.append('`');
			s.append(NJTAI.keepBitmap ? "1" : "0");
			s.append('`');
			s.append(String.valueOf(NJTAI.view));
			s.append('`');
			s.append(NJTAI.invertPan ? "1" : "0");
			s.append('`');
//			s.append(NJTAI._f1 ? "1" : "0");
			s.append('`');
//			s.append(NJTAI._f2 ? "1" : "0");
			s.append('`');
//			s.append(NJTAI._f3 ? "1" : "0");
			s.append('`');
			s.append(NJTAI.proxy);
			s.append('`');
			String wd = MDownloader.currentWD;
			s.append(wd == null ? " " : wd);
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
			String[] s = NJTAI.splitFull(new String(d), '`');
			NJTAI.files = s[0].equals("1");
			NJTAI.cachingPolicy = Integer.parseInt(s[1]);
			NJTAI.loadCoverAtPage = s[2].equals("1");
			NJTAI.keepLists = s[3].equals("1");
			NJTAI.loadCovers = s[4].equals("1");
			//NJTAI._d1 = s[5].equals("1");
			NJTAI.keepBitmap = s[6].equals("1");
			NJTAI.view = Integer.parseInt(s[7]);
			NJTAI.invertPan = s[8].equals("1");
			NJTAI.proxy = s[12];
			MDownloader.currentWD = s[13].equals(" ") ? null : s[13];
		} catch (Exception e) {
			System.out.println("There is no saved settings or they are broken.");
			NJTAI.files = false;
			NJTAI.cachingPolicy = 1;
			NJTAI.loadCoverAtPage = (Runtime.getRuntime().totalMemory() != 2048 * 1024);
			NJTAI.keepLists = true;
			NJTAI.loadCovers = true;
			NJTAI.keepBitmap = true;
			NJTAI.proxy = "http://nnp.nnchan.ru/hproxy.php?";
			NJTAI.view = 0;
			NJTAI.invertPan = false;
			MDownloader.currentWD = null;
		}
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
		return NJTAI.resize(i, w, h);
	}

	/**
	 * Are we running on KEmulator?
	 * 
	 * @return KEmulator detection status.
	 */
	public static boolean isKem() {
		return isClsExists("emulator.custom.CustomMethod");
	}

	/**
	 * Checks class' existing.
	 * 
	 * @param clsName Class to check.
	 * @return Can the class be instantiated or not.
	 */
	public static boolean isClsExists(String clsName) {
		try {
			Class.forName(clsName);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

}
