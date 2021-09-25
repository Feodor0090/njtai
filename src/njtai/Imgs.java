package njtai;

public class Imgs {

	public static byte[] get(String url) {
		if (url == null)
			return null;

		// url proc
		if (url.startsWith("https://"))
			url = url.substring(8);
		if (url.startsWith("http://"))
			url = url.substring(7);
		url = NJTAI.proxy + url;

		return NJTAI.http(url);
	}
}
