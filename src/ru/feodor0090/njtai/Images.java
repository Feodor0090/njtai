package ru.feodor0090.njtai;

import java.util.Hashtable;

import javax.microedition.lcdui.Image;

public class Images {
	private static Hashtable cache;
	
	public static Image get(String url) {
		if(cache.containsKey(url)) return (Image) cache.get(url);
		Image i = Network.loadImage(url);
		cache.put(url, i);
		return i;
	}
}
