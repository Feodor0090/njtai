package njtai.mobile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import njtai.models.WebAPIA;

public class MIDPWebAPIA extends WebAPIA {

	public byte[] get(String url) {
		if (url == null)
			return null;
		ByteArrayOutputStream o = null;
		HttpConnection hc = null;
		InputStream i = null;
		try {
			o = new ByteArrayOutputStream();
			hc = (HttpConnection) Connector.open(url);
			hc.setRequestMethod("GET");

			i = hc.openInputStream();
			byte[] b = new byte[16384];

			int c;
			while ((c = i.read(b)) != -1) {
				// var10 += (long) var7;
				o.write(b, 0, c);
				o.flush();
			}

			return o.toByteArray();
		} catch (NullPointerException e) {
			return null;
		} catch (IOException e) {
			return null;
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

	public String getUtf(String url) {
		try {
			return new String(get(url), "UTF-8");
		} catch (Exception e) {
			return null;
		}
	}

}
