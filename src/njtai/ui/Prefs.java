package njtai.ui;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

import njtai.NJTAI;

/**
 * Njtai preferences screen.
 * 
 * @author Feodor0090
 *
 */
final class Prefs extends Form implements ItemCommandListener, CommandListener {

	private MMenu menu;
	private final Command bkC = new Command("Back", 2, 2);
	private final Command prC = new Command("Proxy setup", 8, 1);

	private final String[] yn = new String[] { "No", "Yes" };
	private final String[] ynr = new String[] { "No (saves RAM)", "Yes" };

	private final StringItem ramWarn = new StringItem("Warning",
			"Enabling preloading/caching will cause crash on low memory devices.");

	private final ChoiceGroup cache = new ChoiceGroup("Keep loaded images in RAM", 4, yn, null);
	private final ChoiceGroup preload = new ChoiceGroup("Preload all images", 4, yn, null);
	private final ChoiceGroup files = new ChoiceGroup("Cache everything to file system", 4, yn, null);
	private final ChoiceGroup lists = new ChoiceGroup("Keep lists when opening pages", 4, ynr, null);
	private final ChoiceGroup covers = new ChoiceGroup("Load covers in lists", 4, ynr, null);
	private final ChoiceGroup urls = new ChoiceGroup("Preload image urls", 4, yn, null);
	private final TextField proxy = new TextField("Proxy prefix", NJTAI.proxy, 100, 0);
	private final StringItem aboutProxy = new StringItem(null, "Setting your own proxy", StringItem.BUTTON);

	public Prefs(MMenu menu) {
		super("NJTAI settings");
		this.menu = menu;

		setCommandListener(this);
		addCommand(bkC);

		cache.setSelectedIndex(NJTAI.cache ? 1 : 0, true);
		preload.setSelectedIndex(NJTAI.prldImg ? 1 : 0, true);
		files.setSelectedIndex(NJTAI.useFiles ? 1 : 0, true);
		lists.setSelectedIndex(NJTAI.keepLists ? 1 : 0, true);
		covers.setSelectedIndex(NJTAI.loadCovers ? 1 : 0, true);
		urls.setSelectedIndex(NJTAI.prldUrl ? 1 : 0, true);
		aboutProxy.setDefaultCommand(prC);
		aboutProxy.setItemCommandListener(this);

		this.append(ramWarn);
		this.append(cache);
		this.append(preload);
		// this.append(files);
		this.append(lists);
		this.append(covers);
		this.append(urls);
		this.append(proxy);
		this.append(aboutProxy);
	}

	public final void commandAction(Command c, Displayable arg1) {
		cmd(c);
	}

	public final void commandAction(Command c, Item arg1) {
		cmd(c);
	}

	private final void cmd(Command c) {
		if (c == bkC) {
			NJTAI.cache = cache.getSelectedIndex() == 1;
			NJTAI.prldImg = preload.getSelectedIndex() == 1;
			NJTAI.useFiles = files.getSelectedIndex() == 1;
			NJTAI.keepLists = lists.getSelectedIndex() == 1;
			NJTAI.loadCovers = covers.getSelectedIndex() == 1;
			NJTAI.prldUrl = urls.getSelectedIndex() == 1;
			NJTAI.proxy = proxy.getString();
			NJTAI.setScr(menu);
			if (!NJTAI.savePrefs()) {
				Alert a = new Alert("Settings", "Failed to write settings. They will reset after exit.", null,
						AlertType.ERROR);
				a.setTimeout(Alert.FOREVER);
				NJTAI.setScr(a);
			}
		} else if (c == prC) {
			Alert a = new Alert("Proxy", "Proxy is necessary due to bad TLS support on java and domain blocks. "
					+ "To setup your own server, just create a PHP script that will take URL from query params, "
					+ "request it via CURL and return content.", null, AlertType.INFO);
			a.setTimeout(Alert.FOREVER);
			NJTAI.setScr(a);
		}
	}

}
