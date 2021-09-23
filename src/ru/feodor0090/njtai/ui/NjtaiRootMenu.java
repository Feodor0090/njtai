package ru.feodor0090.njtai.ui;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextBox;

import ru.feodor0090.njtai.Network;
import ru.feodor0090.njtai.NJTAI;
import ru.feodor0090.njtai.StringUtil;
import ru.feodor0090.njtai.models.MangaObjects;

public class NjtaiRootMenu extends List implements CommandListener {

	public NjtaiRootMenu() {
		super("NJTAI", List.IMPLICIT, new String[] { "Enter number", "Settings", "Popular list", "Recently uploaded",
				"Search by title", "About & keys tips" }, null);
		this.addCommand(exitCmd);
		this.setCommandListener(this);
	}

	private Command exitCmd = new Command("Exit", Command.EXIT, 2);
	private Command openCmd = new Command("Go", Command.OK, 1);

	static final String POPULAR_DIV = "<div class=\"container index-container index-popular\">";
	static final String NEW_DIV = "<div class=\"container index-container\">";
	static final String PAGIN_SEC = "<section class=\"pagination\">";

	static final String SEARCH_Q = "/search/?q=";

	public void commandAction(Command c, Displayable d) {
		try {
			if (c == exitCmd) {
				NJTAI.close();
			}
			if (c == List.SELECT_COMMAND) {
				switch (getSelectedIndex()) {
				case 0:
					// number;
					final TextBox tb = new TextBox("Enter ID:", "", 7, 2);
					tb.addCommand(openCmd);
					tb.addCommand(exitCmd);
					final NjtaiRootMenu menu = this;
					tb.setCommandListener(new CommandListener() {

						public void commandAction(Command c, Displayable arg1) {
							if (c == exitCmd) {
								NJTAI.setScreen(menu);
							} else if (c == openCmd) {
								try {
									NJTAI.setScreen(new MangaPage(Integer.parseInt(tb.getString()), menu));
								} catch (Exception e) {
									NJTAI.setScreen(menu);
									NJTAI.pause(100);
									NJTAI.setScreen(new Alert("Failed to go to page",
											"Have you entered correct number?", null, AlertType.ERROR));
								}
							}
						}
					});
					NJTAI.setScreen(tb);
					return;
				case 1:
					// sets
					NJTAI.setScreen(new Prefs(this));
					return;
				case 2:
					// popular
					String section = StringUtil.range(NJTAI.getHomePage(), POPULAR_DIV, NEW_DIV, false);
					NJTAI.setScreen(new MangaList("Popular list", this, new MangaObjects(section)));
					return;
				case 3:
					// new
					String section1 = StringUtil.range(NJTAI.getHomePage(), NEW_DIV, PAGIN_SEC, false);
					NJTAI.setScreen(new MangaList("Recently uploaded", this, new MangaObjects(section1)));
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
					NJTAI.setScreen(a);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void search() {
		final TextBox tb = new TextBox("Enter query:", "", 80, 0);
		tb.addCommand(openCmd);
		tb.addCommand(exitCmd);
		final NjtaiRootMenu menu = this;
		tb.setCommandListener(new CommandListener() {

			public void commandAction(Command c, Displayable arg1) {
				if (c == exitCmd) {
					NJTAI.setScreen(menu);
				} else if (c == openCmd) {
					try {
						String q = NJTAI.proxy + NJTAI.baseUrl + SEARCH_Q + tb.getString();
						String data = Network.httpRequestUTF8(q);
						String section1 = StringUtil.range(data, NEW_DIV, PAGIN_SEC, false);
						NJTAI.setScreen(new MangaList("Search results", menu, new MangaObjects(section1)));
					} catch (Exception e) {
						e.printStackTrace();
						NJTAI.setScreen(menu);
						NJTAI.pause(100);
						NJTAI.setScreen(new Alert("Failed to open", "Have you entered something URL-breaking?", null,
								AlertType.ERROR));
					}
				}
			}
		});
		NJTAI.setScreen(tb);
	}

}
