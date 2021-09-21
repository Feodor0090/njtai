package ru.feodor0090.njtai.ui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;

import ru.feodor0090.njtai.NjtaiApp;

public class NjtaiRootMenu extends List implements CommandListener {

	public NjtaiRootMenu(String arg0, int arg1, String[] arg2, Image[] arg3) {
		super("NJTAI", List.IMPLICIT, new String[] { "Enter number", "Proxy settings", "Popular list", "Search" },
				null);
		this.addCommand(exitCmd);
		this.setCommandListener(this);
	}
	
	private Command exitCmd = new Command("Exit",Command.BACK, 1);

	public void commandAction(Command c, Displayable d) {
		if(c==exitCmd) {
			NjtaiApp.close();
		}
	}

}
