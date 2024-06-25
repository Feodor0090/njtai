package njtai.m;

import javax.microedition.midlet.MIDlet;

import njtai.NJTAI;

/**
 * Main mobile class.
 * 
 * @author Feodor0090
 *
 */
public class NJTAIM extends MIDlet {

	protected void destroyApp(boolean u) {
	}

	protected void pauseApp() {
	}

	protected void startApp() {
		NJTAI.midlet = this;
		NJTAI.startApp();
	}

}
