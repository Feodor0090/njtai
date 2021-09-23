package ru.feodor0090.njtai.models;

import javax.microedition.lcdui.Image;

import ru.feodor0090.njtai.Images;
import ru.feodor0090.njtai.StringUtil;

public class MangaObject {

	public int num;
	public String imgUrl;
	public String title;
	public Image img;

	/**
	 * Parses this object from html fragment.
	 * 
	 * @param html
	 */
	public MangaObject(String html) {
		num = Integer.parseInt(StringUtil.range(html, "<a href=\"/g/", "/\"", false));
		imgUrl = StringUtil.range(html, "<noscript><img src=\"", "\"", false);
		title = StringUtil.range(html, "<div class=\"caption\">", "</div>", false);
	}

	public MangaObject() {
	}

	public void loadCover() {
		img = Images.get(imgUrl, true);
	}
}
