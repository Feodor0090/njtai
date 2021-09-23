package ru.feodor0090.njtai.ui;

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

import ru.feodor0090.njtai.Network;
import ru.feodor0090.njtai.NJTAI;
import ru.feodor0090.njtai.models.ExtendedMangaObject;

public class MangaPage extends Form implements Runnable, CommandListener, ItemCommandListener {

	int num;
	ExtendedMangaObject mo;
	Thread loader;
	private Displayable prev;
	private Command exitCmd = new Command("Back", Command.BACK, 1);
	private Item firstPage = new StringItem(null, "Open first page", StringItem.BUTTON);
	private Item customPage = new StringItem(null, "Enter page number", StringItem.BUTTON);
	public static Command openCmd = new Command("Select", Command.ITEM, 1);
	private Command gotoCmd = new Command("Go", Command.OK, 1);
	
	boolean stop= false;
	
	private StringItem progress = new StringItem("Loading data", "");

	public MangaPage(int num, Displayable prev) {
		super("Manga page");
		this.num = num;
		this.prev = prev;
		this.setCommandListener(this);
		this.addCommand(exitCmd);
		this.append(progress);
		loader = new Thread(this);
		loader.setPriority(Thread.MAX_PRIORITY);
		loader.start();
	}

	public void run() {
		status("Fetching page (1/3)");
		String html = Network.httpRequestUTF8(NJTAI.proxy + NJTAI.baseUrl + "/g/" + num);
		if(html==null) {
			status("Network error! Check connection, return to previous screen and try again.");
			return;
		}
		status("Processing data (2/3)");
		if(stop) return;
		mo = new ExtendedMangaObject(num, html);
		status("Downloading cover (3/3)");
		if(stop) return;
		mo.loadCover();
		if(stop) return;
		this.deleteAll();
		this.append(new ImageItem(null, (Image) mo.img, 0, null));
		setTitle(mo.title);
		this.setCommandListener(this);
		append(new StringItem("Title", mo.title));
		append(new StringItem("ID", "#" + num));
		append(new StringItem("Pages", "" + mo.pages));
		append(new StringItem("Tags", mo.tags));
		firstPage.setItemCommandListener(this);
		firstPage.setDefaultCommand(openCmd);
		append(firstPage);
		customPage.setItemCommandListener(this);
		customPage.setDefaultCommand(openCmd);
		append(customPage);
	}

	private void status(String string) {
		progress.setText(string);
	}

	public void commandAction(Command arg0, Displayable arg1) {
		if (arg0 == exitCmd) {
			stop = true;
			NJTAI.setScreen(prev);
		}
	}

	public void commandAction(Command c, Item i) {
		if (c == openCmd) {
			if (i == firstPage) {
				NJTAI.setScreen(new View(mo, this, 0));
			} else if (i == customPage) {
				final TextBox tb = new TextBox("Enter page number:", "", 7, 2);
				tb.addCommand(gotoCmd);
				tb.addCommand(exitCmd);
				final Displayable menu = this;
				tb.setCommandListener(new CommandListener() {

					public void commandAction(Command c, Displayable arg1) {
						if (c == exitCmd) {
							NJTAI.setScreen(menu);
						} else if (c == gotoCmd) {
							try {
								int n = Integer.parseInt(tb.getString());
								if (n < 0)
									n = 0;
								if (n >= mo.pages)
									n = mo.pages-1;
								NJTAI.setScreen(new View(mo, menu, n));
							} catch (Exception e) {
								NJTAI.setScreen(menu);
								NJTAI.pause(100);
								NJTAI.setScreen(new Alert("Failed to go to page", "Have you entered correct number?", null, AlertType.ERROR));
							}
						}
					}
				});
				NJTAI.setScreen(tb);
			}
		}
	}

}
