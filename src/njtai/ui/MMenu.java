package njtai.ui;

import java.io.IOException;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextBox;

import njtai.NJTAI;
import njtai.StringUtil;
import njtai.models.MangaObjs;

public final class MMenu extends List implements CommandListener {

	public MMenu() {
		super("NJTAI", List.IMPLICIT, new String[] { "Enter number", "Settings", "Popular list", "Recently uploaded",
				"Search by title", "About & keys tips" }, null);
		this.addCommand(exitCmd);
		this.setCommandListener(this);
	}

	private Command exitCmd = new Command("Exit", Command.EXIT, 2);
	private Command backCmd = new Command("Back", Command.BACK, 2);
	private Command openCmd = new Command("Go", Command.OK, 1);
	private Command searchCmd = new Command("Search", Command.OK, 1);

	static final String POPULAR_DIV = "<div class=\"container index-container index-popular\">";
	static final String NEW_DIV = "<div class=\"container index-container\">";
	static final String PAGIN_SEC = "<section class=\"pagination\">";

	static final String SEARCH_Q = "/search/?q=";

	public void commandAction(Command c, Displayable d) {
		try {
			if (c == backCmd) {
				NJTAI.setScr(this);
				return;
			}
			if (c == searchCmd) {
				try {
					String q = NJTAI.proxy + NJTAI.baseUrl + SEARCH_Q + ((TextBox) d).getString();
					String data = NJTAI.httpUtf(q);
					String section1 = StringUtil.range(data, NEW_DIV, PAGIN_SEC, false);
					NJTAI.setScr(new MangaList("Search results", this, new MangaObjs(section1)));
				} catch (Exception e) {
					e.printStackTrace();
					NJTAI.setScr(this);
					NJTAI.pause(100);
					NJTAI.setScr(new Alert("Failed to open", "Have you entered something URL-breaking?", null,
							AlertType.ERROR));
				}
				return;
			}
			if (c == openCmd) {
				try {
					NJTAI.setScr(new MangaPage(Integer.parseInt(((TextBox) d).getString()), this));
				} catch (Exception e) {
					NJTAI.setScr(this);
					NJTAI.pause(100);
					NJTAI.setScr(new Alert("Failed to go to page", "Have you entered correct number?", null,
							AlertType.ERROR));
				}
			}
			if (c == exitCmd) {
				NJTAI.close();
			}
			if (c == List.SELECT_COMMAND) {
				switch (getSelectedIndex()) {
				case 0:
					// number;
					final TextBox tb = new TextBox("Enter ID:", "", 7, 2);
					tb.addCommand(openCmd);
					tb.addCommand(backCmd);
					tb.setCommandListener(this);
					NJTAI.setScr(tb);
					return;
				case 1:
					// sets
					NJTAI.setScr(new Prefs(this));
					return;
				case 2:
					// popular
					String section = StringUtil.range(NJTAI.getHP(), POPULAR_DIV, NEW_DIV, false);
					NJTAI.setScr(new MangaList("Popular list", this, new MangaObjs(section)));
					return;
				case 3:
					// new
					String section1 = StringUtil.range(NJTAI.getHP(), NEW_DIV, PAGIN_SEC, false);
					NJTAI.setScr(new MangaList("Recently uploaded", this, new MangaObjs(section1)));
					return;
				case 4:
					// search
					search();
					return;
				case 5:
					Alert a = new Alert("About & keys tips", "NJTAI v" + NJTAI.ver() + "\n Developer: Feodor0090"
							+ "\n\nControls: OK to zoom in/out, D-PAD to move page when zoomed and switch them when not, RSK to return.",
							null, AlertType.INFO);
					a.setTimeout(Alert.FOREVER);
					NJTAI.setScr(a);
				}
			}
		} catch (Throwable t) {
			System.gc();
			t.printStackTrace();
			NJTAI.setScr(this);
			NJTAI.pause(100);
			String info;
			if (t instanceof OutOfMemoryError) {
				info = "Not enough memory!";
			} else if (t instanceof IOException) {
				info = "Failed to connect.";
			} else {
				info = t.toString();
			}
			NJTAI.setScr(new Alert("Error", info, null, AlertType.ERROR));
		}
	}

	private void search() {
		final TextBox tb = new TextBox("Enter query:", "", 80, 0);
		tb.addCommand(searchCmd);
		tb.addCommand(backCmd);
		tb.setCommandListener(this);
		NJTAI.setScr(tb);
	}

}
