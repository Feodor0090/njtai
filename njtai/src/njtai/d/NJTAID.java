package njtai.d;

import njtai.IPlatform;

public class NJTAID implements IPlatform {

	public static void main(String[] args) {
		System.out.println();
		System.out.println("There is no desktop frontend. Are you trying to launch mobile-only version?");
	}

	public void loadPrefs() {
	}

	public boolean savePrefs() {
		return false;
	}

	public void initAPIAs() {
	}

	public void exit() {
		Runtime.getRuntime().exit(0);
	}

	public void repaint() {
	}

	public Object decodeImage(byte[] data) {
		return null;
	}

	public Object prescaleCover(Object original) {
		return null;
	}

	public void showNotification(String title, String text, int type, Object prev) {
	}

}
