package njtai.m;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import njtai.models.WebAPIA;

/**
 * {@link njtai.models.WebAPIA WebAPIA}, that works with MIDP 2.0.
 * 
 * @author Feodor0090
 *
 */
public class MIDPWebAPIA extends WebAPIA {

	public byte[] get(String url) throws IOException {
		if (url == null)
			throw new IllegalArgumentException("URL is null");
		ByteArrayOutputStream o = null;
		HttpConnection hc = null;
		InputStream i = null;
		try {
			o = new ByteArrayOutputStream();
			hc = (HttpConnection) Connector.open(url);
			hc.setRequestMethod("GET");
			int r = hc.getResponseCode();
			if (r == 301) {
				String redir = hc.getHeaderField("Location");
				if (redir.startsWith("/")) {
					String s2 = url.substring(url.indexOf("//") + 2);
					String host = url.substring(0, url.indexOf("//")) + "//" + s2.substring(0, s2.indexOf("/"));
					System.out.println("redir: " + redir + " | " + url + " | " + host + " | " + s2);
					redir = host + redir;
				}
				hc.close();
				hc = (HttpConnection) Connector.open(redir);
				hc.setRequestMethod("GET");
			}
			i = hc.openInputStream();
			byte[] b = new byte[16384];

			int c;
			while ((c = i.read(b)) != -1) {
				o.write(b, 0, c);
				o.flush();
			}

			return o.toByteArray();
		} finally {
			try {
				if (i != null)
					i.close();
			} catch (IOException e) {
			}
			try {
				if (hc != null)
					hc.close();
			} catch (IOException e) {
			}
			try {
				if (o != null)
					o.close();
			} catch (IOException e) {
			}
		}
	}

	public byte[] getOrNull(String url) {
		try {
			return get(url);
		} catch (Exception e) {
			return null;
		}
	}

	public String getUtf(String url) throws IOException {
		return new String(get(url), "UTF-8");
	}

	public String getUtfOrNull(String url) {
		try {
			return getUtf(url);
		} catch (Exception e) {
			return null;
		}
	}

}
