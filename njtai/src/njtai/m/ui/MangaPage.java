package njtai.m.ui;

import javax.microedition.lcdui.*;

import njtai.NJTAI;
import njtai.m.MangaDownloader;
import njtai.m.NJTAIM;
import njtai.models.ExtMangaObj;
import njtai.models.WebAPIA;

final class MangaPage extends Form implements Runnable, CommandListener, ItemCommandListener {

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
	private Command repairLite;
	private Command repairFull;

	boolean stop = false;

	private StringItem prgrs;

	private String[] loc;

	public MangaPage(int num, Displayable prev) {
		super("Manga page");
		id = num;
		p = prev;

		loc = NJTAIM.getStrings("page");

		back = new Command(loc[0], Command.BACK, 1);
		page1 = new StringItem(null, loc[1], StringItem.BUTTON);
		pageN = new StringItem(null, loc[2], StringItem.BUTTON);
		repair = new StringItem(null, loc[3], StringItem.BUTTON);
		save = new StringItem(null, loc[4], StringItem.BUTTON);
		open = new Command(loc[5], Command.ITEM, 1);
		goTo = new Command(loc[6], Command.OK, 1);
		repairLite = new Command(loc[7], Command.SCREEN, 1);
		repairFull = new Command(loc[8], Command.SCREEN, 2);
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
			loadPage();
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

	private void loadPage() {
		status(loc[10]);
		String html = WebAPIA.inst.getUtf(NJTAI.proxy + NJTAI.baseUrl + "/g/" + id + "/");
		if (html == null) {
			status(loc[11]);
			return;
		}

		status(loc[12]);
		if (stop)
			return;
		mo = new ExtMangaObj(id, html);

		status(loc[13]);
		if (stop)
			return;
		if (NJTAI.loadCoverAtPage)
			mo.loadCover();
		if (stop)
			return;

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
		append(new StringItem(loc[19], mo.tags));
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
	}

	private void status(String string) {
		prgrs.setText(string);
	}

	public void commandAction(Command c, Displayable d) {
		if (c == back) {
			stop = true;
			NJTAIM.setScr(p == null ? new MMenu() : p);
		}
	}

	public void commandAction(Command c, Item i) {
		if (c == open) {
			if (i == page1) {
				NJTAIM.setScr(ViewBase.create(mo, this, 0));
			} else if (i == pageN) {
				final TextBox tb = new TextBox(loc[20], "", 7, 2);
				tb.addCommand(goTo);
				tb.addCommand(back);
				final Displayable menu = this;
				tb.setCommandListener(new CommandListener() {

					public void commandAction(Command c, Displayable d) {
						if (c == back) {
							NJTAIM.setScr(menu);
						} else if (c == goTo) {
							try {
								int n = Integer.parseInt(tb.getString());
								if (n < 1)
									n = 1;
								if (n > mo.pages)
									n = mo.pages;
								NJTAIM.setScr(ViewBase.create(mo, menu, n - 1));
							} catch (Exception e) {
								NJTAIM.setScr(menu);
								NJTAI.pause(100);
								NJTAIM.setScr(new Alert(loc[21], loc[22], null, AlertType.ERROR));
							}
						}
					}
				});
				NJTAIM.setScr(tb);
			} else if (i == save) {
				(new MangaDownloader(mo, this)).start();
			} else if (i == repair) {
				Alert a = new Alert(loc[23], loc[24], null, AlertType.WARNING);
				a.setTimeout(Alert.FOREVER);
				a.addCommand(repairFull);
				a.addCommand(repairLite);
				a.addCommand(back);
				final Displayable menu = this;
				a.setCommandListener(new CommandListener() {

					public void commandAction(Command c, Displayable d) {
						NJTAIM.setScr(menu);

						if (c == repairFull) {
							MangaDownloader md = new MangaDownloader(mo, menu);
							md.repair = true;
							md.check = true;
							md.start();
						} else if (c == repairLite) {
							MangaDownloader md = new MangaDownloader(mo, menu);
							md.repair = true;
							md.check = false;
							md.start();
						}
					}
				});
				NJTAIM.setScr(a);
			} else {
				NJTAIM.setScr(ViewBase.create(mo, this, 0));
			}
		}
	}

}
