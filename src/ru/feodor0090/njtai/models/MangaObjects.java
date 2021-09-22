package ru.feodor0090.njtai.models;

import java.util.Enumeration;
import java.util.Vector;

import ru.feodor0090.njtai.NjtaiApp;
import ru.feodor0090.njtai.StringUtil;

public class MangaObjects implements Enumeration {
	/**
	 * Parses this object from html fragment.
	 * 
	 * @param html
	 */
	public MangaObjects(String html) {
		String[] items = StringUtil.split(html, "<div class=\"gallery\"");
		Vector v = new Vector();
		for (int i = 0; i < items.length; i++) {
			try {
				if (!items[i].startsWith("<h"))
					v.addElement(new MangaObject(items[i]));
			} catch (RuntimeException e) {
				System.out.println("Failed on " + i);
				e.printStackTrace();
			}
		}
		list = new MangaObject[v.size()];
		v.copyInto(list);
		v.removeAllElements();
		v = null;
	}

	MangaObject[] list;

	int next = 0;

	public boolean hasMoreElements() {
		return next < list.length;
	}

	public Object nextElement() {
		if(NjtaiApp.loadCovers) list[next].loadCover();
		next++;
		return list[next - 1];
	}
}
