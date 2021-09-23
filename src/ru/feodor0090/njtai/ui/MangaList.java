package ru.feodor0090.njtai.ui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;

import ru.feodor0090.njtai.NJTAI;
import ru.feodor0090.njtai.models.MangaObj;
import ru.feodor0090.njtai.models.MangaObjs;

public class MangaList extends Form implements Runnable, CommandListener {

	public Thread loader;
	protected Displayable prev;
	protected MangaObjs content;

	private Command exitCmd = new Command("Back", Command.BACK, 1);
	private String title;

	protected void sizeChanged(int arg0, int arg1) {
		// ща напишем
	}

	public MangaList(String title, Displayable prev, MangaObjs items) {
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
			MangaObj o = (MangaObj) content.nextElement();
			ImageItem img = new ImageItem(o.title, o.img, 3, null, Item.HYPERLINK);
			OMBHdlr h = new OMBHdlr(o.num, this);
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

	public static class OMBHdlr implements ItemCommandListener {
		private int num;

		public static Command openCmd = new Command("Open", Command.ITEM, 1);

		private Displayable prev;

		public OMBHdlr(int num, Displayable prev) {
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
			NJTAI.setScr(new MangaPage(num, NJTAI.keepLists ? prev : new MMenu()));
		}
	}

}
