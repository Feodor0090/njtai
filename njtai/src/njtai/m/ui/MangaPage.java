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
	private Command back = new Command(NJTAI.rus ? "Назад" : "Back", Command.BACK, 1);
	private Item page1 = new StringItem(null, NJTAI.rus ? "Открыть первую страницу" : "Open first page",
			StringItem.BUTTON);
	private Item pageN = new StringItem(null, NJTAI.rus ? "Ввести номер страницы" : "Enter page number",
			StringItem.BUTTON);
	private Item save = new StringItem(null, NJTAI.rus ? "Скачать" : "Download", StringItem.BUTTON);
	private Item repair = new StringItem(null, NJTAI.rus ? "Восстановить кэш" : "Repair cache", StringItem.BUTTON);
	public static Command open = new Command(NJTAI.rus ? "Выбрать" : "Select", Command.ITEM, 1);
	private Command goTo = new Command("Go", Command.OK, 1);
	private Command repairLite = new Command(NJTAI.rus ? "Докачать" : "Redownload", Command.SCREEN, 1);
	private Command repairFull = new Command(NJTAI.rus ? "Полная проверка" : "Full repair", Command.SCREEN, 2);

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
			deleteAll();
			append(prgrs);
			status("Not enough memory to load!");
		}
	}

	private void loadPage() {
		status(NJTAI.rus ? "Загрузка страницы (1/3)" : "Fetching page (1/3)");
		String html = WebAPIA.inst.getUtf(NJTAI.proxy + NJTAI.baseUrl + "/g/" + id + "/");
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
		ImageItem cover = new ImageItem(
				mo.img == null ? (NJTAI.rus ? "Загрузка обложки была отключена или произошла ошибка." : "Cover loading was disabled or error happened.")
						: null,
				(Image) mo.img, 0, null);
		cover.setItemCommandListener(this);
		cover.setDefaultCommand(open);
		append(cover);
		
		setTitle(mo.title);

		append(new StringItem(NJTAI.rus ? "Название" : "Title", mo.title));
		append(new StringItem("ID", "#" + id));
		append(new StringItem(NJTAI.rus ? "Страницы" : "Pages", "" + mo.pages));
		if (mo.lang != null)
			append(new StringItem(NJTAI.rus ? "Язык" : "Language", mo.lang));
		if (mo.parody != null)
			append(new StringItem(NJTAI.rus ? "Источник" : "Parody", mo.parody));
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
				final TextBox tb = new TextBox(NJTAI.rus ? "Номер страницы:" : "Enter page number:", "", 7, 2);
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
								NJTAIM.setScr(new Alert("Failed to go to page", "Have you entered correct number?",
										null, AlertType.ERROR));
							}
						}
					}
				});
				NJTAIM.setScr(tb);
			} else if (i == save) {
				(new MangaDownloader(mo, this)).start();
			} else if (i == repair) {
				Alert a = new Alert(NJTAI.rus ? "Восстановление кэша" : "Cache repairing", NJTAI.rus
						? "Эта утилита найдёт пустые/утерянные изображения и докачает их (\"Докачать\"). Если какие-то файлы были повреждены, запустите полную проверку для их обнаружения и восстановления. Это не не будет работать на устройствах с <10 мб памяти."
						: "This utility will find empty/missed images in local folder and download them (\"Redownload\"). If some files are broken, run \"Full repair\" to find and redownload them. Warning: this won't work on <10mb-ram devices.",
						null, AlertType.WARNING);
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
