package ru.feodor0090.njtai.ui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;

import ru.feodor0090.njtai.NJTAI;
import ru.feodor0090.njtai.models.MangaObject;
import ru.feodor0090.njtai.models.MangaObjects;

public class MangaList extends Form implements Runnable, CommandListener {

	public Thread loader;
	protected Displayable prev;
	protected MangaObjects content;

	private Command exitCmd = new Command("Back", Command.BACK, 1);
	private String title;

	protected void sizeChanged(int arg0, int arg1) {
		// ща напишем
	}

	public MangaList(String title, Displayable prev, MangaObjects items) {
		super("Loading...");
		this.title = title;
		this.prev = prev;
		content = items;
		this.setCommandListener(this);
		this.addCommand(exitCmd);
		loader = new Thread(this);
		loader.start();
	}

	public void run() {
		while (content.hasMoreElements()) {
			MangaObject o = (MangaObject) content.nextElement();
			ImageItem img = new ImageItem(o.title, o.img, 3, null, Item.HYPERLINK);
			OpenMangaButtonHandler h = new OpenMangaButtonHandler(o.num, this);
			h.attach(img);
			this.append(img);
			setTitle(title);
		}
		content = null;
		loader = null;
		setTitle(title);
	}

	public void commandAction(Command arg0, Displayable arg1) {
		if (arg0 == exitCmd)
			NJTAI.setScr(prev);
	}

	public static class OpenMangaButtonHandler implements ItemCommandListener {
		private int num;

		public static Command openCmd = new Command("Open", Command.ITEM, 1);

		private Displayable prev;

		public OpenMangaButtonHandler(int num, Displayable prev) {
			this.num = num;
			if (NJTAI.keepLists)
				this.prev = prev;
		}

		public void attach(Item i) {
			i.addCommand(openCmd);
			i.setDefaultCommand(openCmd);
			i.setItemCommandListener(this);
		}

		public void commandAction(Command arg0, Item arg1) {
			NJTAI.setScr(new MangaPage(num, NJTAI.keepLists ? prev : new NjtaiRootMenu()));
		}
	}

}
