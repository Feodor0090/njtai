package ru.feodor0090.njtai;

import java.util.Hashtable;

import javax.microedition.lcdui.Image;

public class Images {
	private static Hashtable cache  = new Hashtable();
	
	public static Image get(String url) {
		if(cache.containsKey(url)) return (Image) cache.get(url);
		if(url.startsWith("https://"))url = url.substring(8);
		if(url.startsWith("http://"))url = url.substring(7);
		url = NjtaiApp.proxy+url;
		System.out.println("Loading "+url);
		Image i = Network.loadImage(url);
		cache.put(url, i);
		return i;
	}
}
