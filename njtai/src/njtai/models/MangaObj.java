package njtai.models;

import javax.microedition.lcdui.Image;

import njtai.Imgs;
import njtai.NJTAI;
import njtai.StringUtil;

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
	 * Cover image.
	 */
	public Image img;

	/**
	 * Parses this object from html fragment.
	 * 
	 * @param html HTML content of "gallery" block from main/search/similar page.
	 */
	public MangaObj(String html) {
		num = Integer.parseInt(StringUtil.range(html, "<a href=\"/g/", "/\"", false));
		imgUrl = StringUtil.range(html, "<noscript><img src=\"", "\"", false);
		title = StringUtil.range(html, "<div class=\"caption\">", "</div>", false);
	}

	public MangaObj() {
	}

	/**
	 * Loads {@link #img} from {@link #imgUrl} using {@link Imgs#getImg(String)}.
	 */
	public void loadCover() {
		try {
			byte[] d = Imgs.getImg(imgUrl);
			Image i = Image.createImage(d, 0, d.length);
			d = null;
			System.gc();
			int h = NJTAI.getHeight() * 2 / 3;
			int w = (int) (((float) h / i.getHeight()) * i.getWidth());
			img = NJTAI.resize(i, w, h);
		} catch (Exception e) {
			e.printStackTrace();
			img = Image.createImage(1, 1);

		}
	}
}
