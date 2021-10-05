package njtai.models;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Displayable;

import njtai.Imgs;
import njtai.NJTAI;
import njtai.StringUtil;

/**
 * Extension for {@link MangaObj}. Contains data to show pages.
 * 
 * @author Feodor0090
 *
 */
public class ExtMangaObj extends MangaObj implements Runnable {

	/**
	 * List of tags.
	 */
	public String tags;
	/**
	 * Count of pages.
	 */
	public int pages;
	/**
	 * Preloaded list of images' urls.
	 */
	public String[] imgs;

	private Thread urlFetcher = null;

	public int infoReady = -2;
	/**
	 * Are URLs already prefetched?
	 */
	private boolean prefetched = false;

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
	 * Gets encoded page's image.
	 * 
	 * @param i Number of image (not page!), [0, pages-1].
	 * @return Loaded page's image.
	 * @throws InterruptedException If web pages fetching was canceled.
	 */
	public byte[] getPage(int i) throws InterruptedException {
		if (imgs == null) {
			imgs = new String[pages];
		}
		String url;
		try {
			url = imgs[i];
		} catch (RuntimeException e) {
			e.printStackTrace();
			url = loadUrl(i + 1);
		}
		if (url == null) {
			url = loadUrl(i + 1);
			if (NJTAI.preloadUrl && NJTAI.cachingPolicy != 2) {
				if (!prefetched) {
					Thread.sleep(100);
					prefetched = true;
					urlFetcher = new Thread(this);
					urlFetcher.setPriority(Thread.MAX_PRIORITY);
					urlFetcher.start();
					Thread.sleep(500);
				}
			} else {
				infoReady = 100;
			}
		}
		if (infoReady == -1) {
			infoReady = 100;
		}
		return Imgs.getImg(url);
	}

	/**
	 * Loads all pages URLs into {@link #imgs}.
	 * 
	 * @throws InterruptedException If the operation was cancelled via
	 *                              {@link #cancelPrefetch()}.
	 * @see {@link #run()}
	 */
	private void loadUrls() throws InterruptedException {
		try {
			for (int i = 1; i <= pages; i++) {
				long t = System.currentTimeMillis();
				loadUrl(i);
				infoReady = i * 100 / pages;
				Displayable s = NJTAI.getScr();
				if (s instanceof Canvas)
					((Canvas) s).repaint();
				t = System.currentTimeMillis() - t;
				Thread.sleep(t > 2000 ? 100 : 500);
			}
		} catch (Exception e) {
			e.printStackTrace();
			infoReady = -1;
		}
	}

	/**
	 * Runs {@link #loadUrls() the prefetcher}.
	 */
	public void run() {
		try {
			loadUrls();
			urlFetcher = null;
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void cancelPrefetch() {
		try {
			if (urlFetcher != null && urlFetcher.isAlive()) {
				urlFetcher.interrupt();
				urlFetcher = null;
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets page's url.
	 * 
	 * @param pageN Number of the page (not index!), [1, {@link #pages}].
	 * @return Page's image's URL.
	 * @throws InterruptedException
	 */
	public String loadUrl(int pageN) throws InterruptedException {
		return loadUrl(pageN, 0);
	}

	protected synchronized String loadUrl(int pageN, int attempt) throws InterruptedException {
		// pauses in 429 case
		if (attempt > 5)
			return null;
		if (attempt > 0)
			Thread.sleep((attempt + 1) * 500);

		if (imgs == null)
			imgs = new String[pages];
		if (imgs[pageN - 1] != null)
			return imgs[pageN - 1];

		try {
			String html = NJTAI.httpUtf(NJTAI.proxy + NJTAI.baseUrl + "/g/" + num + "/" + pageN);
			String body = html.substring(html.indexOf("<bo"));
			html = null;
			if (body.length() < 200 && body.indexOf("429") != -1) {
				return loadUrl(pageN, attempt + 1);
			}
			// looking for URL
			String span = StringUtil.range(body, "<section id=\"image-container", "</sect", false);
			body = null;
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

	/**
	 * Joins tags list with commas.
	 * 
	 * @param list Array of tags.
	 * @return String like "tag1, tag2, tag3".
	 */
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