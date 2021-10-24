package njtai.m.ui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;

import njtai.NJTAI;
import njtai.m.NJTAIM;
import njtai.models.MangaObj;
import njtai.models.MangaObjs;

final class MangaList extends Form implements Runnable, CommandListener {

	private Thread loader;
	private Displayable prev;
	private MangaObjs objs;

	private String title;

	static boolean wasOom = false;

	public MangaList(String title, Displayable prev, MangaObjs items) {
		super(NJTAI.rus ? "Загрузка..." : "Loading...");
		wasOom = false;
		this.title = title;
		this.prev = prev;
		objs = items;
		this.setCommandListener(this);
		this.addCommand(MMenu.backCmd);
		loader = new Thread(this);
		loader.start();
	}

	public void run() {
		try {
			try {
				while (objs.hasMoreElements()) {
					MangaObj o = (MangaObj) objs.nextElement();
					ImageItem img = new ImageItem(o.title, (Image) o.img, 3, null, Item.HYPERLINK);
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
				NJTAI.pl.savePrefs();
				append(new StringItem(NJTAI.rus ? "Ошибка" : "Error",
						NJTAI.rus ? "Не хватило памяти для отображения полного списка"
								: "Not enough memory to show full list"));
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void commandAction(Command c, Displayable d) {
		if (c == MMenu.backCmd)
			NJTAIM.setScr(prev);
	}

	public static class OMBHdlr implements ItemCommandListener {
		private int n;

		public static Command o = new Command(NJTAIM.getStrings("acts")[15], 8, 1);

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
				if (NJTAIM.getScr() instanceof Form) {
					((Form) NJTAIM.getScr()).deleteAll();
				}
				NJTAIM.setScr(new Form(NJTAI.rus ? "Загрузка..." : "Loading..."));
				System.gc();
				Thread.yield();
			}
			NJTAIM.setScr(new MangaPage(n, wasOom ? null : p, null, ((ImageItem) i).getImage()));
		}
	}

}
