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
	
	public static String url(String url) {
		StringBuffer sb = new StringBuffer();
		char[] chars = url.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			int c = chars[i];
			if (65 <= c && c <= 90) {
				sb.append((char) c);
			} else if (97 <= c && c <= 122) {
				sb.append((char) c);
			} else if (48 <= c && c <= 57) {
				sb.append((char) c);
			} else if (c == 32) {
				sb.append("%20");
			} else if (c == 45 || c == 95 || c == 46 || c == 33 || c == 126 || c == 42 || c == 39 || c == 40
					|| c == 41) {
				sb.append((char) c);
			} else if (c <= 127) {
				sb.append(hex(c));
			} else if (c <= 2047) {
				sb.append(hex(0xC0 | c >> 6));
				sb.append(hex(0x80 | c & 0x3F));
			} else {
				sb.append(hex(0xE0 | c >> 12));
				sb.append(hex(0x80 | c >> 6 & 0x3F));
				sb.append(hex(0x80 | c & 0x3F));
			}
		}
		return sb.toString();
	}

	private static String hex(int i) {
		String s = Integer.toHexString(i);
		return "%".concat(s.length() < 2 ? "0" : "").concat(s);
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
