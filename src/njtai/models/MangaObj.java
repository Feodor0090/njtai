package njtai.models;

import njtai.NJTAI;

/**
 * Compact object, representing basic data about manga/dojisini.
 * 
 * @author Feodor0090
 *
 */
public class MangaObj {

	/**
	 * ID of this title.
	 */
	public int num;
	/**
	 * Cover URL.
	 */
	public String imgUrl;
	/**
	 * Title.
	 */
	public String title;
	/**
	 * Cover image in platform's format.
	 */
	public Object img;

	/**
	 * Parses this object from html fragment.
	 * 
	 * @param html HTML content of "gallery" block from main/search/similar page.
	 */
	public MangaObj(String html) {
		num = Integer.parseInt(NJTAI.range(html, "<a href=\"/g/", "/\"", false));
		imgUrl = NJTAI.range(NJTAI.range(html, "<noscript>", "</noscript>", false), "<img src=\"", "\"", false);
		title = NJTAI.htmlString(NJTAI.range(html, "<div class=\"caption\">", "</div>", false));
	}

	/**
	 * Creates empty object.
	 */
	public MangaObj() {
	}

	/**
	 * Loads {@link #img} from {@link #imgUrl} using {@link Imgs#getImg(String)}.
	 */
	public void loadCover() {
		try {
			byte[] d = NJTAI.getOrNull(NJTAI.proxyUrl(imgUrl));
			Object i = NJTAI.pl.decodeImage(d);
			d = null;
			System.gc();
			img = NJTAI.pl.prescaleCover(i);
		} catch (Exception e) {
			e.printStackTrace();
			img = null;
		}
	}
}
