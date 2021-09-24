package njtai;

import java.util.Hashtable;

import javax.microedition.lcdui.Image;

public class Imgs {
	private static Hashtable c = new Hashtable();
	private static Object lock = new Object();

	public static void reset() {
		c = new Hashtable();
		System.gc();
	}

	public static Image get(String url, int targetH, boolean keep) {
		if (url == null)
			return null;
		// cache lookup
		if (c.containsKey(url))
			return (Image) c.get(url);
		
		synchronized (lock) {
			// check again
			if (c.containsKey(url))
				return (Image) c.get(url);
			String url1 = url;
			// url proc
			if (url.startsWith("https://"))
				url = url.substring(8);
			if (url.startsWith("http://"))
				url = url.substring(7);
			url = NJTAI.proxy + url;

			Image i = NJTAI.httpImg(url);
			int h = targetH;
			int w = (int) (((float) h / i.getHeight()) * i.getWidth());
			i = NJTAI.resize(i, w, h);
			if (NJTAI.cache && keep)
				c.put(url1, i);
			return i;
		}
	}
}
