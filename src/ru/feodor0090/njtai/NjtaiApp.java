package ru.feodor0090.njtai;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

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
	
	public synchronized static String getHomePage() {
		if(homePage==null) {
			homePage = Network.httpRequestUTF8(proxy+baseUrl);
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
		if(!running) {
			running = true;
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
