package njtai.models;

import java.io.IOException;
import java.util.Hashtable;

import njtai.NJTAI;

/**
 * Extension for {@link MangaObj}. Contains data to show pages.
 * 
 * @author Feodor0090
 *
 */
public class ExtMangaObj extends MangaObj {

	/**
	 * List of tags.
	 */
	public String tags;
	/**
	 * Language
	 */
	public String lang;
	/**
	 * Parody section
	 */
	public String parody;
	/**
	 * Count of pages.
	 */
	public int pages;
	/**
	 * Remote folder where to look for images.
	 */
	public String location = null;
	/**
	 * Suffix of image's URL, usually ".jpg".
	 */
	public String imgSuffix = ".jpg";

	/**
	 * Is this object decoded from FS?
	 */
	private boolean offline = false;

	/**
	 * Parses this object from html fragment.
	 * 
	 * @param num  ID of this.
	 * @param html HTML content of the web page.
	 * @throws NumberFormatException Failed to parse pages count. This may indicate
	 *                               broken page.
	 */
	public ExtMangaObj(int num, String html) throws NumberFormatException {
		this.num = num;
		offline = false;

		String meta = NJTAI.range(html, "<section id=\"tags\">", "</sect");

		// pages count
		String pagesStr = NJTAI.range(NJTAI.range(meta, "Pages:", "</div", false), "<span class=\"name\">",
				"</span", false);
		// this fails on 404
		pages = Integer.parseInt(pagesStr);

		// img and title
		imgUrl = NJTAI.range(html, "<noscript><img src=\"", "\"", false);
		title = NJTAI.htmlString(NJTAI.range(NJTAI.range(html, "<h1 class=\"title\">", "</h1", false),
				"<span class=\"pretty\">", "</span", false));

		// metadata
		try {
			tags = listTags(NJTAI.splitRanges(NJTAI.range(meta, "Tags:", "</div", true),
					"<span class=\"name\">", "</span", false));
		} catch (Exception e) {
			tags = "(error)";
		}
		try {
			if (meta.indexOf("Languages:") == -1) {
				lang = null;
			} else {
				String lr = NJTAI.range(meta, "Languages:", "</div", false);
				if (lr.indexOf("class=\"name\">") == -1)
					lang = null;
				else
					lang = listLangs(NJTAI.splitRanges(lr, "<span class=\"name\">", "</span", false));
			}

		} catch (Exception e) {
			e.printStackTrace();
			lang = null;
		}
		if (meta.indexOf("Parodies:") == -1) {
			parody = null;
		} else {
			String pr = NJTAI.range(meta, "Parodies:", "</div", false);
			if (pr.indexOf("class=\"name\">") == -1) {
				parody = null;
			} else {
				parody = NJTAI.range(pr, "<span class=\"name\">", "</span", false);
			}
		}

		System.gc();
	}

	/**
	 * Creates an object from the hashtable.
	 * 
	 * @param num ID.
	 * @param h   Object with data.
	 */
	public ExtMangaObj(int num, Hashtable h) {
		this.num = num;
		offline = true;

		title = h.get("title").toString();
		if (h.containsKey("tags")) {
			tags = h.get("tags").toString();
		}
		if (h.containsKey("parody")) {
			parody = h.get("parody").toString();
		}
		if (h.containsKey("lang")) {
			lang = h.get("lang").toString();
		}
		pages = Integer.parseInt(h.get("pages").toString());
	}

	/**
	 * Gets encoded page's image.
	 * 
	 * @param i Number of image (not page!), [0, pages-1].
	 * @return Loaded page's image.
	 * @throws InterruptedException If web pages fetching was canceled.
	 */
	public byte[] getPage(int i) throws InterruptedException {
		try {
			if (location == null || true) { // TODO
				String u = loadUrl(i+1);
				int sp = u.lastIndexOf('/');
				location = u.substring(0, sp + 1);
				imgSuffix = u.substring(u.lastIndexOf('.'));
			}
			
			String url = location+(i+1)+imgSuffix;
			
			return NJTAI.getOrNull(NJTAI.proxyUrl(url));
		} catch (IOException e) {
			return null;
		}
	}
	public byte[] getPage(int i, String s) throws InterruptedException, IOException {
		return NJTAI.getOrNull(NJTAI.proxyUrl(loadUrl(i+1).concat(s)));
	}

	/**
	 * Gets page's url.
	 * 
	 * @param pageN Number of the page (not index!), [1, {@link #pages}].
	 * @return Page's image's URL.
	 * @throws InterruptedException
	 */
	public String loadUrl(int pageN) throws InterruptedException, IOException {
		return loadUrl(pageN, 0);
	}

	protected synchronized String loadUrl(int pageN, int attempt) throws InterruptedException, IOException {
		// pauses in 429 case
		if (attempt > 5) {
			return null;
		}
		if (attempt > 0) {
			Thread.sleep((attempt + 1) * 500);
		}

		try {
			String html = NJTAI.getUtf(NJTAI.proxyUrl(NJTAI.baseUrl + "/g/" + num + "/" + pageN));
			String body = html.substring(html.indexOf("<bo"));
			html = null;
			if (body.length() < 200 && body.indexOf("429") != -1) {
				return loadUrl(pageN, attempt + 1);
			}
			// looking for URL
			String span = NJTAI.range(body, "<section id=\"image-container", "</sect", false);
			body = null;
			System.gc();
			String url = NJTAI.range(span, "<img src=\"", "\"", false);
			return url;
		} catch (OutOfMemoryError e) {
			System.gc();
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

	/**
	 * Joins langs list with commas, translating known.
	 * 
	 * @param list Array of langs.
	 * @return String like "translated, english".
	 */
	public static String listLangs(String[] list) {
		if (list == null)
			return null;
		if (list.length == 0)
			return null;
		StringBuffer sb = new StringBuffer();
		String[] langs = NJTAI.getStrings("langs");
		String[] langsO = NJTAI.getStrings("langs", "en");
		sb.append(translateLang(list[0], langsO, langs));
		for (int i = 1; i < list.length; i++) {
			sb.append(", ");
			String s = translateLang(list[i], langsO, langs);
			sb.append(s);
		}
		return sb.toString();
	}

	private static String translateLang(String s, String[] o, String[] l) {
		s = s.toLowerCase();
		for (int i = 0; i < l.length; i++) {
			if (s.equals(o[i]))
				return l[i];
		}
		return s;
	}

	/**
	 * Gets JSON encoding of this object.
	 * 
	 * @return JSON string.
	 */
	public String encode() {
		Hashtable h = new Hashtable();
		h.put("title", title);
		if (tags != null)
			h.put("tags", tags);
		if (parody != null)
			h.put("parody", parody);
		if (lang != null)
			h.put("lang", lang);
		h.put("pages", new Integer(pages));

		try {
			return NJTAI.build(h);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Was this object decoded offline?
	 * 
	 * @return Value of {@link #offline}.
	 */
	public boolean isOffline() {
		return offline;
	}
}
