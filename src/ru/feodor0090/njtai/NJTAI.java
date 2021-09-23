package ru.feodor0090.njtai;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.rms.RecordStore;

import ru.feodor0090.njtai.ui.NjtaiRootMenu;

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
	 * Enable images preloading?
	 */
	public static boolean prldImg = false;
	public static boolean cache = false;
	public static boolean useFiles = false;
	public static boolean keepLists = true;
	public static boolean loadCovers = true;
	/**
	 * Enable urls preloading?
	 */
	public static boolean prldUrl = true;
	public static boolean flag7 = true;
	public static boolean flag8 = true;

	public static boolean isS60() {
		return System.getProperty("microedition.platform").indexOf("S60") != -1;
	}

	public static boolean savePrefs() {
		try {
			StringBuffer s = new StringBuffer();
			s.append(prldImg ? "1" : "0");
			s.append('`');
			s.append(cache ? "1" : "0");
			s.append('`');
			s.append(useFiles ? "1" : "0");
			s.append('`');
			s.append(keepLists ? "1" : "0");
			s.append('`');
			s.append(loadCovers ? "1" : "0");
			s.append('`');
			s.append(prldUrl ? "1" : "0");
			s.append('`');
			s.append(flag7 ? "1" : "0");
			s.append('`');
			s.append(flag8 ? "1" : "0");
			s.append('`');
			s.append( proxy);
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
			prldImg = s[0].equals("1");
			cache = s[1].equals("1");
			useFiles = s[2].equals("1");
			keepLists = s[3].equals("1");
			loadCovers = s[4].equals("1");
			prldUrl = s[5].equals("1");
			flag7 = s[6].equals("1");
			flag8 = s[7].equals("1");
			proxy = s[8];
		} catch (Exception e) {
			e.printStackTrace();
			prldImg = false;
			cache = false;
			useFiles = false;
			keepLists = true;
			loadCovers = true;
			prldUrl = true;
			proxy = "http://nnproject.cc/proxy.php?";
		}
	}

	public static String ver() {
		return inst.getAppProperty("MIDlet-Version");
	}

	public synchronized static String getHomePage() {
		if (hp == null) {
			hp = Network.httpRequestUTF8(proxy + baseUrl);
		}
		return hp;
	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
	}

	protected void pauseApp() {
	}

	protected void startApp() throws MIDletStateChangeException {
		inst = this;
		dsp = Display.getDisplay(inst);
		if (!running) {
			running = true;
			loadPrefs();
			setScr(new NjtaiRootMenu());
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
}
