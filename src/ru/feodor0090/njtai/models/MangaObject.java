package ru.feodor0090.njtai.models;

import java.lang.ref.WeakReference;

import ru.feodor0090.njtai.Images;
import ru.feodor0090.njtai.StringUtil;

public class MangaObject {
	
	public int num;
	public String imgUrl;
	public String title;
	public WeakReference img;
	/**
	 * Parses this object from html fragment.
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
		img = new WeakReference(Images.get(imgUrl, true));
	}
	
	//String ex = "<div class=\"gallery\" data-tags=\"\"><a href=\"/g/373772/\" class=\"cover\" style=\"padding:0 0 141.2% 0\"><img class=\"lazyload\" width=\"250\" height=\"353\" data-src=\"https://t.nhentai.net/galleries/2015122/thumb.jpg\" src=\"https://t.nhentai.net/galleries/2015122/thumb.jpg\"><noscript><img src=\"https://t.nhentai.net/galleries/2015122/thumb.jpg\" width=\"250\" height=\"353\"  /></noscript><div class=\"caption\">[Ame Arare] Natsu Asobi | Summer Play (COMIC ExE 32) [English] [SaLamiLid] [Digital]</div></a></div>";
}
