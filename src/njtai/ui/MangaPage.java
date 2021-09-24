package njtai.ui;

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
import javax.microedition.lcdui.TextBox;

import njtai.Imgs;
import njtai.MangaDownloader;
import njtai.NJTAI;
import njtai.models.ExtMangaObj;

final class MangaPage extends Form implements Runnable, CommandListener, ItemCommandListener {

	private int id;
	private ExtMangaObj mo;
	private Thread l;
	private Displayable p;
	private Command back = new Command(NJTAI.rus ? "Назад" : "Back", Command.BACK, 1);
	private Item page1 = new StringItem(null, NJTAI.rus ? "Открыть первую страницу" : "Open first page",
			StringItem.BUTTON);
	private Item pageN = new StringItem(null, NJTAI.rus ? "Ввести номер страницы" : "Enter page number",
			StringItem.BUTTON);
	private Item save = new StringItem(null, NJTAI.rus ? "Скачать" : "Download", StringItem.BUTTON);
	public static Command open = new Command(NJTAI.rus ? "Выбрать" : "Select", Command.ITEM, 1);
	private Command goTo = new Command("Go", Command.OK, 1);

	boolean stop = false;

	private StringItem prgrs = new StringItem(NJTAI.rus ? "Загрузка данных" : "Loading data", "");

	public MangaPage(int num, Displayable prev) {
		super("Manga page");
		id = num;
		p = prev;

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
			Imgs.reset();
			deleteAll();
			append(prgrs);
			status("Not enough memory to load!");
		}
	}

	private void loadPage() {
		status(NJTAI.rus ? "Загрузка страницы (1/3)" : "Fetching page (1/3)");
		String html = NJTAI.httpUtf(NJTAI.proxy + NJTAI.baseUrl + "/g/" + id);
		if (html == null) {
			status("Network error! Check connection, return to previous screen and try again.");
			return;
		}

		status(NJTAI.rus ? "Обработка данныых (2/3)" : "Processing data (2/3)");
		if (stop)
			return;
		mo = new ExtMangaObj(id, html);

		status(NJTAI.rus ? "Скачивание обложки (3/3)" : "Downloading cover (3/3)");
		if (stop)
			return;
		if (NJTAI.loadCoverAtPage)
			mo.loadCover();
		if (stop)
			return;

		deleteAll();
		append(new ImageItem(mo.img==null?(NJTAI.rus?"Загрузка обложки была отключена.":"Cover loading was disabled."):null, mo.img, 0, null));
		setTitle(mo.title);

		append(new StringItem(NJTAI.rus ? "Название" : "Title", mo.title));
		append(new StringItem("ID", "#" + id));
		append(new StringItem(NJTAI.rus ? "Страницы" : "Pages", "" + mo.pages));
		append(new StringItem(NJTAI.rus ? "Тэги" : "Tags", mo.tags));
		page1.setItemCommandListener(this);
		page1.setDefaultCommand(open);
		append(page1);
		pageN.setItemCommandListener(this);
		pageN.setDefaultCommand(open);
		append(pageN);
		save.setItemCommandListener(this);
		save.setDefaultCommand(open);
		append(save);
	}

	private void status(String string) {
		prgrs.setText(string);
	}

	public void commandAction(Command c, Displayable d) {
		if (c == back) {
			stop = true;
			NJTAI.setScr(p == null ? new MMenu() : p);
		}
	}

	public void commandAction(Command c, Item i) {
		if (c == open) {
			if (i == page1) {
				NJTAI.setScr(new View(mo, this, 0));
			} else if (i == pageN) {
				final TextBox tb = new TextBox(NJTAI.rus ? "Номер страницы:" : "Enter page number:", "", 7, 2);
				tb.addCommand(goTo);
				tb.addCommand(back);
				final Displayable menu = this;
				tb.setCommandListener(new CommandListener() {

					public void commandAction(Command c, Displayable d) {
						if (c == back) {
							NJTAI.setScr(menu);
						} else if (c == goTo) {
							try {
								int n = Integer.parseInt(tb.getString());
								if (n < 1)
									n = 1;
								if (n > mo.pages)
									n = mo.pages;
								NJTAI.setScr(new View(mo, menu, n - 1));
							} catch (Exception e) {
								NJTAI.setScr(menu);
								NJTAI.pause(100);
								NJTAI.setScr(new Alert("Failed to go to page", "Have you entered correct number?", null,
										AlertType.ERROR));
							}
						}
					}
				});
				NJTAI.setScr(tb);
			} else if (i == save) {
				(new MangaDownloader(mo, this)).start();
			}
		}
	}

}
