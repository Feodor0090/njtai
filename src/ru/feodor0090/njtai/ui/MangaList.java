package ru.feodor0090.njtai.ui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.StringItem;

import ru.feodor0090.njtai.models.MangaObject;
import ru.feodor0090.njtai.models.MangaObjects;

public class MangaList extends Form implements Runnable {
	
	public Thread loader;
	protected Displayable prev;
	protected MangaObjects content;

	public MangaList(String title, Displayable prev, MangaObjects items) {
		super(title);
		this.prev = prev;
		content = items;
		loader = new Thread(this);
		loader.start();
	}
	
	public void run() {
		while(content.hasMoreElements()) {
			MangaObject o = (MangaObject) content.nextElement();
			ImageItem img = new ImageItem(o.title, (Image) o.img.get(), 3, null, Item.HYPERLINK);
			OpenMangaButtonHandler h = new OpenMangaButtonHandler(o.num);
			h.attach(img);
			this.append(img);
		}
	}

	public static class OpenMangaButtonHandler implements ItemCommandListener {
		private int num;

		public static Command openCmd = new Command("Open", Command.ITEM, 1);

		public OpenMangaButtonHandler(int num) {
			this.num = num;
		}
		
		public void attach(Item i) {
			i.addCommand(openCmd);
			i.setDefaultCommand(openCmd);
			i.setItemCommandListener(this);
		}

		public void commandAction(Command arg0, Item arg1) {
			// TODO Auto-generated method stub
			System.out.println("Opening " + num);
		}
	}
}
