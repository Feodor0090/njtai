package njtai.models;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Image;

import njtai.Imgs;
import njtai.NJTAI;
import njtai.StringUtil;

public class ExtMangaObj extends MangaObj implements Runnable {

	public String tags;
	public int pages;
	public String[] imgs;

	private Thread urlFetcher = null;

	public int infoReady = -2;

	public ExtMangaObj(int num, String html) throws NumberFormatException {
		this.num = num;

		// pages
		String pagesStr = StringUtil.range(StringUtil.range(html, "Pages:", "</div", false), "<span class=\"name\">",
				"</span", false);
		System.out.println(pagesStr);
		// this fails on 404
		pages = Integer.parseInt(pagesStr);
		
		imgs = new String[pages];

		// img and title
		imgUrl = StringUtil.range(html, "<noscript><img src=\"", "\"", false);
		title = StringUtil.range(StringUtil.range(html, "<h1 class=\"title\">", "</h1", false),
				"<span class=\"pretty\">", "</span", false);

		// tags
		try {
			tags = listTags(StringUtil.splitRanges(StringUtil.range(html, "Tags:", "</div", true),
					"<span class=\"name\">", "</span", false));
		} catch (Exception e) {
			tags = "(error)";
		}
		System.gc();
	}

	/**
	 * 
	 * @param i Number of image (not page!), [0, pages-1].
	 * @return Loaded page image.
	 * @throws InterruptedException If web pages fetching was canceled.
	 */
	public synchronized Image getPage(int i) throws InterruptedException {
		if (imgs == null) {
			imgs = new String[pages];
		}
		if (imgs[i] == null) {
			loadUrl(i + 1);
			if (NJTAI.prldUrl) {
				urlFetcher = new Thread(this);
				urlFetcher.setPriority(10);
				urlFetcher.start();
			}
		}
		return Imgs.get(imgs[i], NJTAI.getScr().getHeight() * 3, true);
	}

	private void loadUrls() throws InterruptedException {
		try {
			for (int i = 1; i <= pages; i++) {
				loadUrl(i);
				infoReady = i * 100 / pages;
				if (NJTAI.getScr() instanceof Canvas)
					((Canvas) NJTAI.getScr()).repaint();
				Thread.sleep(10);
			}
		} catch (Exception e) {
			infoReady = -1;
		}
	}

	public void run() {
		try {
			loadUrls();
			urlFetcher = null;
		} catch (InterruptedException e) {
		}
	}

	public void cancelPrefetch() {
		if (urlFetcher != null && urlFetcher.isAlive()) {
			urlFetcher.interrupt();
			urlFetcher = null;
		}
	}

	public synchronized String loadUrl(int pageN) {
		if (imgs[pageN - 1] != null)
			return imgs[pageN - 1];
		try {
			String html = NJTAI.httpUtf(NJTAI.proxy + NJTAI.baseUrl + "/g/" + num + "/" + pageN);
			String span = StringUtil.range(html, "<section id=\"image-container", "</section", false);
			html = null;
			System.gc();
			String url = StringUtil.range(span, "<img src=\"", "\"", false);
			imgs[pageN - 1] = url;
			return url;
		} catch (OutOfMemoryError e) {
			imgs = null;
			System.gc();
			imgs = new String[pages];
			return null;
		}
	}

	public static String listTags(String[] list) {
		if (list == null)
			return "Error while getting tags.";
		if (list.length == 0)
			return "No tags.";
		StringBuffer sb = new StringBuffer();
		sb.append(list[0]);
		for (int i = 1; i < list.length; i++) {
			sb.append(", ");
			sb.append(list[i]);
		}
		return sb.toString();
	}
}
