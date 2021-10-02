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
