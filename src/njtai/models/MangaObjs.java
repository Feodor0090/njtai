package njtai.models;

import java.util.Enumeration;
import java.util.Vector;

import njtai.NJTAI;
import njtai.StringUtil;

public class MangaObjs implements Enumeration {
	/**
	 * Parses this object from html fragment.
	 * 
	 * @param html
	 */
	public MangaObjs(String html) {
		String[] items = StringUtil.split(html, "<div class=\"gallery\"");
		if (items.length == 1 && items[0].length() < 3) {
			list = new MangaObj[0];
			return;
		}
		Vector v = new Vector();
		for (int i = 0; i < items.length; i++) {
			try {
				if (!items[i].startsWith("<h"))
					v.addElement(new MangaObj(items[i]));
			} catch (RuntimeException e) {
				System.out.println("Failed on " + i);
				e.printStackTrace();
			}
		}
		list = new MangaObj[v.size()];
		v.copyInto(list);
		v.removeAllElements();
		v = null;
	}

	MangaObj[] list;

	int next = 0;

	public boolean hasMoreElements() {
		return next < list.length;
	}

	public Object nextElement() {
		if (NJTAI.loadCovers)
			list[next].loadCover();
		next++;
		return list[next - 1];
	}
}
