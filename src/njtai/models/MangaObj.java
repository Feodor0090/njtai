package njtai.models;

import javax.microedition.lcdui.Image;

import njtai.Imgs;
import njtai.NJTAI;
import njtai.StringUtil;

public class MangaObj {

	public int num;
	public String imgUrl;
	public String title;
	public Image img;

	/**
	 * Parses this object from html fragment.
	 * 
	 * @param html
	 */
	public MangaObj(String html) {
		num = Integer.parseInt(StringUtil.range(html, "<a href=\"/g/", "/\"", false));
		imgUrl = StringUtil.range(html, "<noscript><img src=\"", "\"", false);
		title = StringUtil.range(html, "<div class=\"caption\">", "</div>", false);
	}

	public MangaObj() {
	}

	public void loadCover() {
		img = Imgs.get(imgUrl, NJTAI.getHeight() * 2 / 3, false);
	}
}
