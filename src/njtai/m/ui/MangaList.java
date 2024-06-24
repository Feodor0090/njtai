package njtai.m.ui;

import java.io.IOException;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
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
import njtai.models.MangaObj;
import njtai.models.MangaObjs;

public final class MangaList extends Form implements Runnable, CommandListener {

	private Thread loader;
	private Displayable prev;
	private boolean popular;
	private String query;

	private String title;

	static boolean wasOom = false;

	public MangaList(String title, Displayable prev, String query, boolean popular) {
		super(NJTAI.rus ? "Загрузка..." : "Loading...");
		wasOom = false;
		this.title = title;
		this.prev = prev;
		this.query = query;
		this.popular = popular;
		this.setCommandListener(this);
		this.addCommand(NJTAI.backCmd);
		loader = new Thread(this);
		loader.start();
	}

	public void run() {
		try {
			MangaObjs objs = null;
			try {
				setTitle(title);
				try {
					if (query != null) {
						objs = MangaObjs.getSearchList(query, null);
					} else if (popular) {
						objs = MangaObjs.getPopularList();
					} else {
						objs = MangaObjs.getNewList();
					}
				} catch (Exception e) {
					NJTAI.setScr(new Alert(NJTAI.L_ACTS[7], NJTAI.L_ACTS[14], null, AlertType.ERROR), NJTAI.mmenu);
					e.printStackTrace();
					return;
				}
				while (objs.hasMoreElements()) {
					MangaObj o = (MangaObj) objs.nextElement();
					ImageItem img = new ImageItem(o.title, (Image) o.img, 3, null, Item.HYPERLINK);
					OMBHdlr h = new OMBHdlr(o.num, NJTAI.keepLists ? this : prev);
					h.attach(img);
					this.append(img);
				}
				objs = null;
				loader = null;
			} catch (OutOfMemoryError e) {
				wasOom = true;
				objs = null;
				loader = null;
				System.gc();
				NJTAI.keepLists = false;
				NJTAI.savePrefs();
				append(new StringItem(NJTAI.rus ? "Ошибка" : "Error",
						NJTAI.rus ? "Не хватило памяти для отображения полного списка"
								: "Not enough memory to show full list"));
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void commandAction(Command c, Displayable d) {
		if (c == NJTAI.backCmd)
			NJTAI.setScr(prev == null ? NJTAI.mmenu : prev);
	}

	public static class OMBHdlr implements ItemCommandListener {
		private int n;

		public static Command o = new Command(NJTAI.L_ACTS[15], 8, 1);

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
				NJTAI.setScr(new Form(NJTAI.rus ? "Загрузка..." : "Loading..."));
				System.gc();
				Thread.yield();
			}
			NJTAI.setScr(new MangaPage(n, wasOom ? null : p, null, ((ImageItem) i).getImage()));
		}
	}

}
