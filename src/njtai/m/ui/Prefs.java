package njtai.m.ui;

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

import njtai.NJTAI;
import njtai.m.MDownloader;

/**
 * Njtai preferences screen.
 * 
 * @author Feodor0090
 *
 */
public final class Prefs extends Form implements ItemCommandListener, CommandListener {

	private final Command bkC = new Command(NJTAI.rus ? "Применить" : "Apply", Command.SCREEN, 2);
	private final Command cnclC = new Command(NJTAI.rus ? "Отмена" : "Revert", Command.BACK, 3);
	private final Command prC = new Command("Proxy setup", 8, 1);
	private final Command changeC = new Command(NJTAI.rus ? "Изменить" : "Change", Command.OK, 1);

	private final String[] yn = new String[] { NJTAI.rus ? "Нет" : "No", NJTAI.rus ? "Да" : "Yes" };
	private final String[] ynr = new String[] { NJTAI.rus ? "Нет (экономит память)" : "No (saves RAM)",
			NJTAI.rus ? "Да" : "Yes" };

	private final StringItem ramWarn = new StringItem(NJTAI.rus ? "Предупреждение" : "Warning",
			NJTAI.rus ? "Включение предзагрузки или кэширования приведёт к ошибкам на устройствах с небольшой памятью."
					: "Enabling preloading/caching will cause crash on low memory devices.");
	private final StringItem s40Warn = new StringItem(NJTAI.rus ? "Работа на S40" : "Working on S40", NJTAI.rus
			? ("Памяти S40 не хватит для просмотра, но хватит для скачивания. Отключите загрузку обложек в списках и на "
					+ "странице, сохранение списков и кэш для работы. Рекомендовано использовать поиск по ID, а не по названию.")
			: ("S40's memory is enough for downloading, but not for viewing. "
					+ "Disable covers and lists keeping to make it work. Using ID input (not search) is recommended."));

	private final ChoiceGroup cache = new ChoiceGroup(NJTAI.rus ? "Поведение кэширования" : "Caching behaviour", 4,
			new String[] { NJTAI.rus ? "Отключено" : "Disabled",
					NJTAI.rus ? "Сохранять уже загруженное" : "Keep already loaded",
					NJTAI.rus ? "Предзагружать" : "Preload" },
			null);
	private final ChoiceGroup covers = new ChoiceGroup(NJTAI.rus ? "Загрузка обложек" : "Covers loading", Choice.MULTIPLE,
			new String[] { NJTAI.rus ? "В списках" : "In lists",NJTAI.rus ? "На странице" : "On page" }, null);
	private final ChoiceGroup invert = new ChoiceGroup(NJTAI.rus ? "Инвертировать прокрутку" : "Invert panning", 4, yn,
			null);
	private final ChoiceGroup lists = new ChoiceGroup(
			NJTAI.rus ? "Запоминать списки при открытии страницы" : "Keep lists when opening pages", 4, ynr, null);
	private final ChoiceGroup bitmaps = new ChoiceGroup(NJTAI.rus ? "Декодировать JPEG единожды (повысит плавность)"
			: "Decode JPEG only once (improves perfomance)", 4, ynr, null);
	protected final TextField proxy = new TextField(NJTAI.rus ? "Префикс прокси" : "Proxy prefix", NJTAI.proxy, 100, 0);
	private final StringItem aboutProxy = new StringItem(null,
			NJTAI.rus ? "Настройка вашего прокси" : "Setting your own proxy", StringItem.BUTTON);

	/**
	 * Working folder switcher button.
	 */
	public final StringItem wd = new StringItem(NJTAI.rus ? "Рабочая папка" : "Working folder",
			MDownloader.currentWD == null ? (NJTAI.rus ? "Автоматически" : "Automatically")
					: MDownloader.currentWD,
			StringItem.HYPERLINK);

	private final ChoiceGroup view = new ChoiceGroup("View type", 4, new String[] { "Auto", "SWR", "HWA" }, null);
	private final ChoiceGroup files = new ChoiceGroup(NJTAI.rus ? "Кэшировать на карту памяти" : "Cache to memory card",
			4, yn, null);

	/**
	 * Creates prefs screen.
	 * @param menu Main menu screen.
	 */
	public Prefs() {
		super("NJTAI settings");

		setCommandListener(this);
		addCommand(bkC);
		addCommand(cnclC);

		String vendor = System.getProperty("java.vendor");
		if (vendor != null && vendor.toLowerCase().indexOf("ndroid") != -1) {
			append(new StringItem("J2MEL-specific bugs",
					"On j2me loader 1.7.4 and lower switches on this screen won't reflect actual settings states due "
							+ "to emulator's bug. Don't forget to set them all as you need before returning!"));
		}

		cache.setSelectedIndex(NJTAI.cachingPolicy, true);
		lists.setSelectedIndex(NJTAI.keepLists ? 1 : 0, true);
		covers.setSelectedIndex(0,NJTAI.loadCovers);
		covers.setSelectedIndex(1,NJTAI.loadCoverAtPage);
		bitmaps.setSelectedIndex(NJTAI.keepBitmap ? 1 : 0, true);
		files.setSelectedIndex(NJTAI.files ? 1 : 0, true);
		view.setSelectedIndex(NJTAI.view, true);
		invert.setSelectedIndex(NJTAI.invertPan ? 1 : 0, true);
		aboutProxy.setDefaultCommand(prC);
		aboutProxy.setItemCommandListener(this);
		wd.setDefaultCommand(changeC);
		wd.setItemCommandListener(this);

		append(ramWarn);
		if (Runtime.getRuntime().totalMemory() == 2048 * 1024)
			append(s40Warn);
		append(cache);
		append(files);
		append(wd);
		append(covers);
		append(lists);
		append(invert);
		//append(bitmaps);
		append(view);
		append(proxy);
		append(aboutProxy);
	}

	public final void commandAction(Command c, Displayable arg1) {
		cmd(c);
	}

	public final void commandAction(Command c, Item arg1) {
		cmd(c);
	}

	Command dfC = new Command("Use E:/NJTAI", 8, 1);
	Command ccC = new Command("Choose", 8, 2);

	private final void cmd(Command c) {
		if (c == dfC) {
			MDownloader.useE_NJTAI(this);
			NJTAI.setScr(this);
		} else if (c == ccC) {
			MDownloader.reselectWD(this);
		} else if (c == changeC) {
			Alert a = new Alert("", "Working folder", null, AlertType.INFO);
			a.addCommand(dfC);
			a.addCommand(ccC);
			a.setCommandListener(this);
			a.setTimeout(Alert.FOREVER);
			NJTAI.setScr(a);
		} else if (c == bkC) {
			NJTAI.cachingPolicy = cache.getSelectedIndex();
			NJTAI.loadCoverAtPage = covers.isSelected(1);
			NJTAI.keepLists = lists.getSelectedIndex() == 1;
			NJTAI.loadCovers = covers.isSelected(0);
			NJTAI.keepBitmap = bitmaps.getSelectedIndex() == 1;
			NJTAI.view = view.getSelectedIndex();
			NJTAI.files = files.getSelectedIndex() == 1;
			NJTAI.proxy = proxy.getString();
			NJTAI.invertPan = invert.getSelectedIndex() == 1;
			if (NJTAI.proxy.length() == 0) {
				NJTAI.proxy = "";
			} else if (NJTAI.proxy.startsWith("http") && NJTAI.proxy.indexOf("://") != 0
					&& NJTAI.proxy.indexOf('.') != 0) {

				NJTAI.setScr(NJTAI.mmenu);
				if (!NJTAI.savePrefs()) {
					Alert a = new Alert("Settings", "Failed to write settings. They will reset after exit.", null,
							AlertType.ERROR);
					a.setTimeout(Alert.FOREVER);
					NJTAI.setScr(a, NJTAI.mmenu);
				}
			} else {
				Alert a = new Alert("Settings",
						"Incorrect proxy URL. Leave the field empty if you don't want to use it.", null,
						AlertType.ERROR);
				a.setTimeout(Alert.FOREVER);
				NJTAI.setScr(a);
			}
		} else if (c == prC) {
			Alert a = new Alert("Proxy", "Proxy is necessary due to bad TLS support on java and domain blocks. "
					+ "To setup your own server, just create a PHP script that will take URL from query params, "
					+ "request it via CURL and return content. Read more info on github. To disable proxy, write \"https://\".",
					null, AlertType.INFO);
			a.setTimeout(Alert.FOREVER);
			NJTAI.setScr(a);
		} else if (c == cnclC) {
			Alert a = new Alert(NJTAI.rus ? "Настройки" : "Settings",
					NJTAI.rus ? "Изменения отменены." : "Made changes were canceled.", null, AlertType.WARNING);
			a.setTimeout(1500);
			NJTAI.setScr(a, NJTAI.mmenu);
		}
	}

}
