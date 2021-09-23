package njtai.models;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Image;

import njtai.Images;
import njtai.NJTAI;
import njtai.StringUtil;

public class ExtMangaObj extends MangaObj {

	public String tags;
	public int pages;
	public String[] imgs;

	public int infoReady = -2;

	public ExtMangaObj(int num, String html) {
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
			tags = listTags(StringUtil.splitRanges(tagsSpan, "<span class=\"name\">", "</span>", false));
		} catch (Exception e) {
			tags = "(error)";
		}
		System.gc();
	}

	/**
	 * 
	 * @param i Number of image (not page!), [0, pages-1].
	 * @param caller Canvas that want to get image.
	 * @return Loaded page image.
	 * @throws InterruptedException If web pages fetching was canceled.
	 */
	public synchronized Image getPage(int i, Canvas caller) throws InterruptedException {
		if (imgs == null)
			loadUrls(caller);
		return Images.get(imgs[i], caller.getHeight()*3);
	}

	private void loadUrls(Canvas cnv)  throws InterruptedException{
		try {
			imgs = new String[pages];
			for (int i = 1; i <= pages; i++) {
				String html = NJTAI.httpUtf(NJTAI.proxy + NJTAI.baseUrl + "/g/" + num + "/" + i);
				String span = StringUtil.range(html, "<section id=\"image-container", "</section", false);
				html = null;
				System.gc();
				String url = StringUtil.range(span, "<img src=\"", "\"", false);
				imgs[i - 1] = url;
				infoReady = i * 100 / pages;
				if (cnv != null)
					cnv.repaint();
				Thread.sleep(1);
			}
		} catch (Exception e) {
			infoReady = -1;
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
