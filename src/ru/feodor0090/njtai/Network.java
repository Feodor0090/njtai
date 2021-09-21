package ru.feodor0090.njtai;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

public class Network {
	public static byte[] httpRequest(String url) {
		ByteArrayOutputStream o = null;
		HttpConnection hc = null;
		InputStream i = null;
		try {
			o = new ByteArrayOutputStream();
			hc = (HttpConnection) Connector.open(url);
			hc.setRequestMethod("GET");
			//hc.setRequestProperty("User-Agent", userAgent(userAgent));
			
			i = hc.openInputStream();
			byte[] b = new byte[16384];
	
			int c;
			while ((c = i.read(b)) != -1) {
				//var10 += (long) var7;
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
				if(i != null)
					i.close();
			} catch (IOException e) { }
			try {
				if(hc != null)
					hc.close();
			} catch (IOException e) { }
			try {
				if(o != null)
					o.close();
			} catch (IOException e) { }
		}
	}

	public static String httpRequestUTF8(String url) {
		try
		{
			return new String(httpRequest(url), "UTF-8");
		}
		catch(UnsupportedEncodingException e)
		{
			return null;
		}
		catch(NullPointerException e)
		{
			return null;
		}
	}
}
