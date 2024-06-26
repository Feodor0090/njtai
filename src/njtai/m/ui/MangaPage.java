package njtai.m.ui;

import javax.microedition.lcdui.*;

import njtai.NJTAI;
import njtai.m.MDownloader;
import njtai.models.ExtMangaObj;

public final class MangaPage extends Form implements Runnable, CommandListener, ItemCommandListener {

	private int id;
	private ExtMangaObj mo;
	private Thread l;
	private Displayable p;
	private Command back;
	private Item page1;
	private Item pageN;
	private Item save;
	private Item repair;
	public static Command open;
	private Command goTo;
	private Command repairFull;
	private Image coverImg = null;

	boolean stop = false;

	private StringItem prgrs;

	private String[] loc;

	public MangaPage(int num, Displayable prev, ExtMangaObj obj, Image cover) {
		super(NJTAI.rus ? "Страница манги" : "Manga page");
		id = num;
		p = prev;

		mo = obj;
		coverImg = cover;

		initForm();
	}

	private void initForm() {
		loc = NJTAI.getStrings("page");

		int layout;
		// число выбрано от балды
		if (getWidth() <= 480) {
			layout = Item.LAYOUT_EXPAND | Item.LAYOUT_NEWLINE_AFTER;
		} else {
			layout = Item.LAYOUT_DEFAULT;
		}
		back = new Command(loc[0], Command.BACK, 1);
		page1 = new StringItem(null, loc[1], StringItem.BUTTON);
		page1.setLayout(layout);
		pageN = new StringItem(null, loc[2], StringItem.BUTTON);
		pageN.setLayout(layout);
		repair = new StringItem(null, loc[3], StringItem.BUTTON);
		repair.setLayout(layout);
		save = new StringItem(null, loc[4], StringItem.BUTTON);
		save.setLayout(layout);
		open = new Command(loc[5], Command.ITEM, 1);
		goTo = new Command(loc[6], Command.OK, 1);
		repairFull = new Command(loc[3], Command.SCREEN, 2);
		prgrs = new StringItem(loc[9], "");

		setCommandListener(this);
		addCommand(back);
		append(prgrs);
		setCommandListener(this);

		l = new Thread(this);
		l.setPriority(10);
		l.start();
	}

	public void run() {
		try {
			// loadPage() inlined
			if (mo == null) {
				status(loc[10]);
				String html = NJTAI.getUtfOrNull(NJTAI.proxyUrl(NJTAI.baseUrl + "/g/" + id + "/"));
				if (html == null) {
					status(loc[11]);
					return;
				}

				status(loc[12]);
				if (stop) return;
				mo = new ExtMangaObj(id, html);
			}

			if (coverImg == null) {
				status(loc[13]);
				if (stop) return;
				if (NJTAI.loadCoverAtPage) {
					mo.loadCover();
				}
				if (stop) return;
			} else {
				if (NJTAI.loadCoverAtPage) {
					mo.img = coverImg;
				}
				coverImg = null;
			}

			deleteAll();
			ImageItem cover = new ImageItem(mo.img == null ? loc[14] : null, (Image) mo.img, 0, null);
			cover.setItemCommandListener(this);
			cover.setDefaultCommand(open);
			append(cover);

			setTitle(mo.title);

			append(new StringItem(loc[15], mo.title));
			append(new StringItem("ID", "#" + id));
			append(new StringItem(loc[16], "" + mo.pages));
			if (mo.lang != null)
				append(new StringItem(loc[17], mo.lang));
			if (mo.parody != null)
				append(new StringItem(loc[18], mo.parody));
			Item tg = new StringItem(loc[19], mo.tags);
			tg.setLayout(Item.LAYOUT_NEWLINE_AFTER);
			append(tg);
			page1.setItemCommandListener(this);
			page1.setDefaultCommand(open);
			append(page1);
			pageN.setItemCommandListener(this);
			pageN.setDefaultCommand(open);
			append(pageN);
			save.setItemCommandListener(this);
			save.setDefaultCommand(open);
			append(save);
			repair.setItemCommandListener(this);
			repair.setDefaultCommand(open);
			append(repair);
		} catch (OutOfMemoryError e) {
			System.gc();
			deleteAll();
			append(prgrs);
			status(loc[25]);
		} catch (Throwable t) {
			System.gc();
			deleteAll();
			append(prgrs);
			status(t.toString());
		}
	}

	private void status(String string) {
		prgrs.setText(string);
	}

	public void commandAction(Command c, Displayable d) {
		if (c == back) {
			if (d != this) {
				NJTAI.setScr(this);
				return;
			}
			stop = true;
			NJTAI.setScr(p == null ? NJTAI.mmenu : p);
			return;
		}
		if (c == repairFull) {
			MDownloader md = new MDownloader(mo, this);
			md.repair = true;
			md.start();
			return;
		}
		if (c == goTo) {
			try {
				int n = Integer.parseInt(((TextBox) d).getString());
				if (n < 1)
					n = 1;
				if (n > mo.pages)
					n = mo.pages;
				NJTAI.setScr(ViewBase.create(mo, this, n - 1));
			} catch (Exception e) {
				NJTAI.setScr(this);
				NJTAI.pause(100);
				NJTAI.setScr(new Alert(loc[21], loc[22], null, AlertType.ERROR));
			}
		}
	}

	public void commandAction(Command c, Item i) {
		if (c == open) {
			if (i == page1) {
				NJTAI.setScr(ViewBase.create(mo, this, 0));
			} else if (i == pageN) {
				final TextBox tb = new TextBox(loc[20], "", 7, 2);
				tb.addCommand(goTo);
				tb.addCommand(back);
				tb.setCommandListener(this);
				NJTAI.setScr(tb);
			} else if (i == save) {
				(new MDownloader(mo, this)).start();
			} else if (i == repair) {
				Alert a = new Alert(loc[23], loc[24], null, AlertType.WARNING);
				a.setTimeout(Alert.FOREVER);
				a.addCommand(repairFull);
				a.addCommand(back);
				a.setCommandListener(this);
				NJTAI.setScr(a);
			} else {
				NJTAI.setScr(ViewBase.create(mo, this, 0));
			}
		}
	}

}
