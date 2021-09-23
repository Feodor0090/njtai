package njtai.ui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;

import njtai.NJTAI;
import njtai.models.MangaObj;
import njtai.models.MangaObjs;

final class MangaList extends Form implements Runnable, CommandListener {

	private Thread loader;
	private Displayable prev;
	private MangaObjs objs;

	private Command back = new Command("Back", Command.BACK, 1);
	private String title;

	static boolean wasOom = false;

	public MangaList(String title, Displayable prev, MangaObjs items) {
		super("Loading...");
		wasOom = false;
		this.title = title;
		this.prev = prev;
		objs = items;
		this.setCommandListener(this);
		this.addCommand(back);
		loader = new Thread(this);
		loader.start();
	}

	public void run() {
		try {
			while (objs.hasMoreElements()) {
				MangaObj o = (MangaObj) objs.nextElement();
				ImageItem img = new ImageItem(o.title, o.img, 3, null, Item.HYPERLINK);
				OMBHdlr h = new OMBHdlr(o.num, NJTAI.keepLists ? this : prev);
				h.attach(img);
				this.append(img);
				setTitle(title);
			}
			objs = null;
			loader = null;
			setTitle(title);
		} catch (OutOfMemoryError e) {
			wasOom = true;
			objs = null;
			loader = null;
			System.gc();
			NJTAI.keepLists = false;
			NJTAI.savePrefs();
			append(new StringItem("Error", "Not enough memory to show full list"));
		}
	}

	public void commandAction(Command c, Displayable d) {
		if (c == back)
			NJTAI.setScr(prev);
	}

	public static class OMBHdlr implements ItemCommandListener {
		private int n;

		public static Command o = new Command("Open", 8, 1);

		private Displayable p;

		public OMBHdlr(int id, Displayable pr) {
			n = id;
			p = pr;
		}

		public void attach(Item i) {
			i.addCommand(o);
			i.setDefaultCommand(o);
			i.setItemCommandListener(this);
		}

		public void commandAction(Command c, Item i) {
			if (wasOom) {
				if (NJTAI.getScr() instanceof Form) {
					((Form) NJTAI.getScr()).deleteAll();
				}
				NJTAI.setScr(new Form("Loading..."));
				System.gc();
				Thread.yield();
			}
			NJTAI.setScr(new MangaPage(n, wasOom ? null : p));
		}
	}

}
