package ru.feodor0090.njtai.models;

import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Image;

import ru.feodor0090.njtai.Images;
import ru.feodor0090.njtai.Network;
import ru.feodor0090.njtai.NjtaiApp;
import ru.feodor0090.njtai.StringUtil;

public class ExtendedMangaObject extends MangaObject {

	public String[] tags;
	public int pages;
	public String[] images;

	public int infoReady = -2;

	public ExtendedMangaObject(int num, String html) {
		this.num = num;
		imgUrl = StringUtil.range(html, "<noscript><img src=\"", "\"", false);
		title = StringUtil.range(StringUtil.range(html, "<h1 class=\"title\">", "</h1>", false),
				"<span class=\"pretty\">", "</span>", false);
		String pagesStr = StringUtil.range(StringUtil.range(html, "Pages:", "</div>", false), "<span class=\"name\">",
				"</span>", false);
		System.out.println(pagesStr);
		pages = Integer.parseInt(pagesStr);
		try {
			String tagsSpan = StringUtil.range(html, "Tags:", "</div>", true);
			tags = StringUtil.splitRanges(tagsSpan, "<span class=\"name\">", "</span>", false);
		} catch (Exception e) {
			tags = null;
		}
	}

	public Image getPage(int i, Canvas caller) {
		if (images == null)
			loadUrls(caller);
		return Images.get(images[i], false);
	}

	private void loadUrls(Canvas cnv) {
		try {
			images = new String[pages];
			for (int i = 1; i <= pages; i++) {
				String html = Network.httpRequestUTF8(NjtaiApp.proxy + NjtaiApp.baseUrl + "/g/" + num + "/" + i);
				String span = StringUtil.range(html, "<section id=\"image-container", "</section", false);
				html = null;
				System.gc();
				String url = StringUtil.range(span, "<img src=\"", "\"", false);
				images[i - 1] = url;
				infoReady = i * 100 / pages;
				if (cnv != null)
					cnv.repaint();
			}
		} catch (Exception e) {
			infoReady = -1;
		}
	}

	public String listTags() {
		if (tags == null)
			return "Error while getting tags.";
		if (tags.length == 0)
			return "No tags.";
		StringBuffer sb = new StringBuffer();
		sb.append(tags[0]);
		for (int i = 1; i < tags.length; i++) {
			sb.append(", ");
			sb.append(tags[i]);
		}
		return sb.toString();
	}
}
