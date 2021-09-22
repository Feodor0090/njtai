package ru.feodor0090.njtai;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.rms.RecordStore;

import ru.feodor0090.njtai.ui.NjtaiRootMenu;

public class NjtaiApp extends MIDlet {

	public NjtaiApp() {
		inst = this;
	}

	public static String proxy = "http://nnproject.cc/proxy.php?";
	public static String baseUrl = "nhentai.net";

	private static NjtaiApp inst;
	private static Display disp;

	private static String homePage = null;

	private boolean running = false;

	public static boolean allowPreload = false;
	public static boolean enableCache = true;
	public static boolean useFiles = false;

	public static boolean isS60() {
		String model = System.getProperty("microedition.platform");
		return model.indexOf("S60") != -1;

	}

	public static boolean savePrefs() {
		try {
			String s = (allowPreload ? "1" : "0") + "`" + (enableCache ? "1" : "0") + "`" + (useFiles ? "1" : "0") + "`"
					+ proxy;
			byte[] dump = s.getBytes();
			RecordStore rs = RecordStore.openRecordStore("njtai", true);

			if (rs.getNumRecords() == 0) {
				rs.addRecord(new byte[1], 0, 1);
			}
			rs.setRecord(1, dump, 0, dump.length);
			rs.closeRecordStore();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static void loadPrefs() {
		try {
			RecordStore rs = RecordStore.openRecordStore("njtai", true);

			if (rs.getNumRecords() < 1) {
				rs.closeRecordStore();
				throw new RuntimeException();
			}
			byte[] d = rs.getRecord(1);
			rs.closeRecordStore();
			String[] s = StringUtil.splitFull(new String(d), '`');
			allowPreload = s[0].equals("1");
			enableCache = s[1].equals("1");
			useFiles = s[2].equals("1");
			proxy = s[3];
		} catch (Exception e) {
			e.printStackTrace();
			allowPreload = false;
			enableCache = isS60();
			useFiles = false;
			proxy = "http://nnproject.cc/proxy.php?";
		}
	}

	public static String ver() {
		return inst.getAppProperty("MIDlet-Version");
	}

	public synchronized static String getHomePage() {
		if (homePage == null) {
			homePage = Network.httpRequestUTF8(proxy + baseUrl);
			System.out.println("Home page OK");
		}
		return homePage;
	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
	}

	protected void pauseApp() {
	}

	protected void startApp() throws MIDletStateChangeException {
		inst = this;
		disp = Display.getDisplay(inst);
		if (!running) {
			running = true;
			loadPrefs();
			setScreen(new NjtaiRootMenu());
		}
	}

	public static void close() {
		inst.notifyDestroyed();
	}

	public static Displayable getScreen() {
		return disp.getCurrent();
	}

	public static void setScreen(Displayable d) {
		disp.setCurrent(d);
	}

	public static void pause(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static int getHeight() {
		return getScreen().getHeight();
	}
}
