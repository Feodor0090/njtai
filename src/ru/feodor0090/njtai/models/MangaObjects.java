package ru.feodor0090.njtai.models;

import java.util.Enumeration;

import ru.feodor0090.njtai.StringUtil;

public class MangaObjects implements Enumeration {
	/**
	 * Parses this object from html fragment.
	 * 
	 * @param html
	 */
	public MangaObjects(String html) {
		String[] items = StringUtil.split(html, "<div class=\"gallery\"");
		list = new MangaObject[items.length];
		for (int i = 0; i < items.length; i++) {
			list[i] = new MangaObject(items[i]);
		}
	}

	MangaObject[] list;

	int next = 0;

	public boolean hasMoreElements() {
		return next < list.length;
	}

	public Object nextElement() {
		list[next].loadCover();
		next++;
		return list[next - 1];
	}
}
