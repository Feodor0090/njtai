package ru.feodor0090.njtai.ui;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

import ru.feodor0090.njtai.NjtaiApp;

public class Prefs extends Form implements ItemCommandListener, CommandListener {

	private NjtaiRootMenu menu;
	private Command exitCmd = new Command("Exit", Command.BACK, 2);
	private Command proxyCmd = new Command("Proxy setup", Command.ITEM, 1);
	
	public ChoiceGroup cache = new ChoiceGroup("Keep images in RAM (may cause crash on devices with low memory)", Choice.EXCLUSIVE, new String[] { "No","Yes"}, null);
	public ChoiceGroup preload = new ChoiceGroup("Preload all pages (may cause crash on devices with low memory)", Choice.EXCLUSIVE, new String[] { "No","Yes"}, null);
	public ChoiceGroup files = new ChoiceGroup("Cache everything to file system", Choice.EXCLUSIVE, new String[] { "No","Yes"}, null);
	public TextField proxy = new TextField("Proxy prefix", NjtaiApp.proxy, 100, 0);
	public StringItem aboutProxy = new StringItem(null, "Proxy setup", StringItem.BUTTON);
	
	public Prefs(NjtaiRootMenu menu) {
		super("NJTAI settings");
		this.menu = menu;
		
		setCommandListener(this);
		
		cache.setSelectedIndex(NjtaiApp.enableCache?1:0, true);
		preload.setSelectedIndex(NjtaiApp.allowPreload?1:0, true);
		files.setSelectedIndex(NjtaiApp.useFiles?1:0, true);
		aboutProxy.setDefaultCommand(proxyCmd);
		aboutProxy.setItemCommandListener(this);
		
		this.append(cache);
		this.append(preload);
		//this.append(files);
		this.append(proxy);
		this.append(aboutProxy);
	}

	public void commandAction(Command c, Displayable arg1) {
		cmd(c);
	}

	public void commandAction(Command c, Item arg1) {
		cmd(c);
	}

	private void cmd(Command c) {
		if(c==exitCmd) {
			NjtaiApp.enableCache = cache.getSelectedIndex()==1;
			NjtaiApp.allowPreload = preload.getSelectedIndex()==1;
			NjtaiApp.useFiles = files.getSelectedIndex()==1;
			NjtaiApp.proxy = proxy.getString();
			NjtaiApp.setScreen(menu);
			if(!NjtaiApp.savePrefs()) {
				Alert a = new Alert("Settngs", "Failed to write settings. They will reset after exit.", null,
						AlertType.ERROR);
				a.setTimeout(Alert.FOREVER);
				NjtaiApp.setScreen(a);
			}
		} else if(c==proxyCmd) {
			Alert a = new Alert("Proxy", "Proxy is necessary due to bad TLS support on java and blocks. "
					+ "To setup your own server, just create a PHP script that will take URL from query params, "
					+ "request it via CURL and return content.", null,
					AlertType.INFO);
			a.setTimeout(Alert.FOREVER);
			NjtaiApp.setScreen(a);
		}
	}
 
}
