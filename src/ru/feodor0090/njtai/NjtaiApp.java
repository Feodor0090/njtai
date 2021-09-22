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
	public static boolean enableCache = false;
	public static boolean useFiles = false;
	public static boolean keepLists = true;
	public static boolean loadCovers = true;
	public static boolean flag6 = true;
	public static boolean flag7 = true;
	public static boolean flag8 = true;

	public static boolean isS60() {
		String model = System.getProperty("microedition.platform");
		return model.indexOf("S60") != -1;

	}

	public static boolean savePrefs() {
		try {
			StringBuffer sb = new StringBuffer();
			sb.append(allowPreload ? "1" : "0");
			sb.append('`');
			sb.append(enableCache ? "1" : "0");
			sb.append('`');
			sb.append(useFiles ? "1" : "0");
			sb.append('`');
			sb.append(keepLists ? "1" : "0");
			sb.append('`');
			sb.append(loadCovers ? "1" : "0");
			sb.append('`');
			sb.append(flag6 ? "1" : "0");
			sb.append('`');
			sb.append(flag7 ? "1" : "0");
			sb.append('`');
			sb.append(flag8 ? "1" : "0");
			sb.append('`');
			sb.append( proxy);
			byte[] dump = sb.toString().getBytes();
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
			keepLists = s[3].equals("1");
			loadCovers = s[4].equals("1");
			flag6 = s[5].equals("1");
			flag7 = s[6].equals("1");
			flag8 = s[7].equals("1");
			proxy = s[8];
		} catch (Exception e) {
			e.printStackTrace();
			allowPreload = false;
			enableCache = false;
			useFiles = false;
			keepLists = true;
			loadCovers = true;
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
