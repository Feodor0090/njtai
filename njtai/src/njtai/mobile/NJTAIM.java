package njtai.mobile;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.rms.RecordStore;

import njtai.NJTAI;
import njtai.StringUtil;
import njtai.models.WebAPIA;
import njtai.ui.lcdui.MMenu;

public class NJTAIM extends MIDlet {

	public NJTAIM() {
		new NJTAI();
		NJTAI.midlet = this;
		inst = this;
		WebAPIA.inst = new MIDPWebAPIA();
	}

	private static Display dsp;
	private static NJTAIM inst;

	public static boolean isS60() {
		return System.getProperty("microedition.platform").indexOf("S60") != -1;
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

	public static void close() {
		inst.notifyDestroyed();
	}

	public static Displayable getScr() {
		return dsp.getCurrent();
	}

	public static void setScr(Displayable d) {
		dsp.setCurrent(d);
	}

	public static boolean savePrefs() {
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
			NJTAI.files = s[0].equals("1");
			NJTAI.cachingPolicy = Integer.parseInt(s[1]);
			NJTAI.loadCoverAtPage = s[2].equals("1");
			NJTAI.keepLists = s[3].equals("1");
			NJTAI.loadCovers = s[4].equals("1");
			NJTAI.preloadUrl = s[5].equals("1");
			NJTAI.keepBitmap = s[6].equals("1");
			NJTAI.view = Integer.parseInt(s[7]);
			NJTAI.proxy = s[8];
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
			NJTAI.view = 1;
		}
	}

}
