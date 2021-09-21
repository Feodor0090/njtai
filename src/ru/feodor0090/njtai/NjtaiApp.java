package ru.feodor0090.njtai;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

public class NjtaiApp extends MIDlet {

	public NjtaiApp() {
		inst = this;
	}
	
	public static String proxy = "http://nnproject.cc/proxy.php?";
	public static String baseUrl = "nhentai.net";
	
	private static NjtaiApp inst;
	private static Display disp;
	
	private static String homePage = null;
	
	public synchronized static String getHomePage() {
		if(homePage==null) {
			homePage = Network.httpRequestUTF8(proxy+baseUrl);
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
}
