package ru.feodor0090.njtai;

import java.util.Hashtable;

import javax.microedition.lcdui.Image;

import tube42.lib.imagelib.ImageUtils;

public class Images {
	private static Hashtable cache  = new Hashtable();
	
	public static Image get(String url, boolean mini) {
		
		if(cache.containsKey(url)) return (Image) cache.get(url);
		if(url.startsWith("https://"))url = url.substring(8);
		if(url.startsWith("http://"))url = url.substring(7);
		url = NjtaiApp.proxy+url;
		System.out.println("Loading "+url);
		Image i = Network.loadImage(url);
		if(mini) {
			int h = NjtaiApp.getHeight()*2/3;
			int w = (int) (((float)h/i.getHeight())*i.getWidth());
			i = ImageUtils.resize(i, w, h, true, false);
		}
		cache.put(url, i);
		return i;
	}
}
