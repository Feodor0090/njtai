package ru.feodor0090.njtai.ui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;

import ru.feodor0090.njtai.NjtaiApp;
import ru.feodor0090.njtai.StringUtil;
import ru.feodor0090.njtai.models.MangaObjects;

public class NjtaiRootMenu extends List implements CommandListener {

	public NjtaiRootMenu() {
		super("NJTAI", List.IMPLICIT,
				new String[] { "Enter number", "Proxy settings", "Popular list", "New", "Search" }, null);
		this.addCommand(exitCmd);
		this.setCommandListener(this);
	}

	private Command exitCmd = new Command("Exit", Command.BACK, 1);

	static final String POPULAR_DIV = "<div class=\"container index-container index-popular\">";
	static final String NEW_DIV = "<div class=\"container index-container\">";
	static final String PAGIN_SEC = "<section class=\"pagination\">";

	public void commandAction(Command c, Displayable d) {
		if (c == exitCmd) {
			NjtaiApp.close();
		}
		if (c == List.SELECT_COMMAND) {
			switch (getSelectedIndex()) {
			case 0:
				// number;
				return;
			case 1:
				// proxy
				return;
			case 2:
				// popular
				String section = StringUtil.range(NjtaiApp.getHomePage(), POPULAR_DIV, NEW_DIV, false);
				NjtaiApp.setScreen(new MangaList("Popular list", this, new MangaObjects(section)));
				return;
			case 3:
				// new
				String section1 = StringUtil.range(NjtaiApp.getHomePage(), NEW_DIV, PAGIN_SEC, false);
				NjtaiApp.setScreen(new MangaList("Popular list", this, new MangaObjects(section1)));
				return;
			case 4:
				// search
				return;
			}
		}
	}

}
