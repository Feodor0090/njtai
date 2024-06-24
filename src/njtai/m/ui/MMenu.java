package njtai.m.ui;

import java.io.IOException;

import javax.microedition.lcdui.*;

import njtai.NJTAI;
import njtai.m.NJTAIM;
import njtai.models.MangaObjs;

/**
 * Main menu of mobile application.
 * 
 * @author Feodor0090
 *
 */
public final class MMenu extends List implements CommandListener {

	/**
	 * Creates menu screen.
	 */
	public MMenu() {
		super("NJTAI", List.IMPLICIT, NJTAI.getStrings("main"), null);
		String[] l = NJTAI.getStrings("acts");
		backCmd = new Command(l[0], Command.BACK, 2);
		openCmd = new Command(l[1], Command.OK, 1);
		exitCmd = new Command(l[2], Command.EXIT, 2);
		searchCmd = new Command(l[3], Command.OK, 1);
		this.addCommand(exitCmd);
		this.setCommandListener(this);
	}

	private Command exitCmd;
	/**
	 * "Back" command.
	 */
	public static Command backCmd;
	/**
	 * "Go" command.
	 */
	public static Command openCmd;
	private Command searchCmd;

	/**
	 * Main commands processor. For menu actions, see {@link #mainMenuLinks()}.
	 */
	public void commandAction(Command c, Displayable d) {
		try {
			if (c == backCmd) {
				NJTAIM.setScr(this);
				return;
			}
			if (c == searchCmd) {
				try {
					// getting text
					String st = ((TextBox) d).getString();
					// Isn't it empty?
					if (st.length() == 0)
						throw new NullPointerException();

					MangaObjs r = MangaObjs.getSearchList(processSearchQuery(st), this);
					if (r == null) {
						return;
					}
					NJTAIM.setScr(new MangaList(NJTAI.getStrings("acts")[4], this, r));
				} catch (IOException e) {
					e.printStackTrace();
					NJTAIM.setScr(this);
					NJTAI.pause(100);
					String[] l = NJTAI.getStrings("acts");
					NJTAIM.setScr(new Alert(l[7], l[14], null,
							AlertType.ERROR));
				} catch (NullPointerException e) {
					NJTAIM.setScr(this);
					NJTAI.pause(100);
					String[] l = NJTAI.getStrings("acts");
					NJTAIM.setScr(new Alert(l[5], l[6], null, AlertType.WARNING));
				} catch (Exception e) {
					e.printStackTrace();
					NJTAIM.setScr(this);
					NJTAI.pause(100);
					String[] l = NJTAI.getStrings("acts");
					NJTAIM.setScr(new Alert(l[7], l[8].concat(" ").concat(e.toString()), null,
							AlertType.ERROR));
				}
				return;
			}
			if (c == openCmd) {
				try {
					NJTAIM.setScr(new MangaPage(Integer.parseInt(((TextBox) d).getString()), this, null, null));
				} catch (Exception e) {
					NJTAIM.setScr(this);
					NJTAI.pause(100);
					String[] l = NJTAI.getStrings("acts");
					NJTAIM.setScr(new Alert(l[9], l[10], null,
							AlertType.ERROR));
				}
			}
			if (c == exitCmd) {
				NJTAI.pl.exit();
			}
			if (c == List.SELECT_COMMAND) {
				mainMenuLinks();
			}
		} catch (Throwable t) {
			System.gc();
			t.printStackTrace();
			NJTAIM.setScr(this);
			NJTAI.pause(100);
			String info;
			String[] l = null;
			try {
				l = NJTAI.getStrings("acts");
			} catch (Error e) {
			}
			if (t instanceof OutOfMemoryError) {
				if(l == null) {
					info = "Not enough memory!";
				} else info = l[27];
			} else if (t instanceof IOException) {
				info = l[25];
			} else if (t instanceof IllegalAccessException) {
				info = "Proxy returned nothing. Does it work from a country, where the site is banned?";
			} else {
				info = t.toString();
			}
			NJTAIM.setScr(new Alert(NJTAI.rus ? "Ошибка приложения" : "App error", info, null, AlertType.ERROR));
		}
	}

	/**
	 * Method that handle list selection.
	 * 
	 * @throws IOException           Failed to connect.
	 * @throws IllegalStateException Proxy returned empty string.
	 */
	private void mainMenuLinks() throws IOException, IllegalAccessException {
		switch (getSelectedIndex()) {
		case 0:
			// number;
			final TextBox tb = new TextBox(NJTAI.getStrings("acts")[11], "", 7, 2);
			tb.addCommand(openCmd);
			tb.addCommand(backCmd);
			tb.setCommandListener(this);
			NJTAIM.setScr(tb);
			return;
		case 1:
			// popular
			NJTAIM.setScr(new MangaList(NJTAI.getStrings("main")[1], this, MangaObjs.getPopularList()));
			return;
		case 2:
			// new
			NJTAIM.setScr(new MangaList(NJTAI.getStrings("main")[2], this, MangaObjs.getNewList()));
			return;
		case 3:
			// search
			search();
			return;
		case 4:
			NJTAI.files = true;
			NJTAIM.setScr(generateDownloadedScreen());
			return;
		case 5:
			// sets
			NJTAIM.setScr(new Prefs(this));
			return;
		case 6:
			try {

				NJTAIM.setScr(generateControlsTipsScreen(this));
			} catch (RuntimeException e) {
				NJTAIM.setScr(new Alert("Failed to read texts", "JAR is corrupted. Reinstall the application.", null,
						AlertType.ERROR));
			}
			return;
		case 7:
			Form ab = new Form(NJTAI.getStrings("acts")[12]);
			ab.append(new StringItem("NJTAI v" + NJTAIM.ver() + " (r3)",
					NJTAI.rus ? "Клиент для nhentai.net под J2ME устройства, поддерживающие MIDP 2.0 и CLDC 1.1"
							: "nhentai.net client for J2ME devices with MIDP 2.0 and CLDC 1.1 support."));
			try {
				ab.append(Image.createImage("/njtai.png"));
			} catch (Throwable t) {
				ab.append(new StringItem("Тут должна быть иконка", "но её сожрали неко"));
			}
			ab.append(new StringItem(NJTAI.rus ? "Основные разработчики" : "Main developers", "Feodor0090, Shinovon"));
			ab.append(new StringItem(NJTAI.rus ? "Иконка и прокси" : "Icon and proxy", "Shinovon"));
			ab.append(new StringItem(NJTAI.rus ? "Тестирование и ревью" : "Review and testing",
					"stacorp, ales_alte, mineshanya"));
			ab.append(new StringItem(NJTAI.rus ? "Локализация" : "Localization", "ales_alte, Jazmin Rocio"));
			ab.append(new StringItem(NJTAI.rus ? "Отдельное спасибо" : "Special thanks to",
					"nnproject, SIStore, Symbian Zone, Jazmin Rocio"));
			ab.append(new StringItem(NJTAI.rus ? "Поддержать разработчика" : "Support the developer",
					"2200 2404 4035 6554\ndonate.stream/f0090"));
			ab.append(new StringItem(NJTAI.rus ? "Больше информации:" : "More info:",
					"github.com/Feodor0090/njtai\nhttps://t.me/symnovel"));
			StringItem bottomJoke = new StringItem(null, "\n\n\n\n\n\n\n\nИ помните: порода Махо - чёрный пудель!");
			bottomJoke.setFont(Font.getFont(0, 0, 8));
			ab.append(bottomJoke);

			// setting up
			ab.setCommandListener(this);
			ab.addCommand(backCmd);
			NJTAIM.setScr(ab);
			return;
		default:
			return;
		}
	}

	private Displayable generateDownloadedScreen() {
		List l = new List("Loading...", List.IMPLICIT);
		(new SavedManager(l, this)).start();
		return l;
	}

	private String processSearchQuery(String data) {
		if (data == null)
			throw new NullPointerException();
		data = data.replace('\n', ' ').replace('\t', ' ').replace('\r', ' ').replace('\0', ' ');
		// URL encoding
		StringBuffer sb = new StringBuffer();
		int len = data.length();
		for (int i = 0; i < len; i++) {
			int c = data.charAt(i);
			if (65 <= c && c <= 90) {
				sb.append((char) c);
			} else if (97 <= c && c <= 122) {
				sb.append((char) c);
			} else if (48 <= c && c <= 57) {
				sb.append((char) c);
			} else if (c == 32) {
				sb.append("%20");
			} else if (c == 45 || c == 95 || c == 46 || c == 33 || c == 126 || c == 42 || c == 39 || c == 40
					|| c == 41) {
				sb.append((char) c);
			} else if (c <= 127) {
				sb.append(hex(c));
			} else if (c <= 2047) {
				sb.append(hex(0xC0 | c >> 6));
				sb.append(hex(0x80 | c & 0x3F));
			} else {
				sb.append(hex(0xE0 | c >> 12));
				sb.append(hex(0x80 | c >> 6 & 0x3F));
				sb.append(hex(0x80 | c & 0x3F));
			}
		}
		return sb.toString();
	}

	private String hex(int i) {
		String s = Integer.toHexString(i);
		return "%" + (s.length() < 2 ? "0" : "") + s;
	}

	private void search() {
		final TextBox tb = new TextBox(NJTAI.getStrings("acts")[3], "", 80, 0);
		tb.addCommand(searchCmd);
		tb.addCommand(backCmd);
		tb.setCommandListener(this);
		NJTAIM.setScr(tb);
	}

	/**
	 * Creates a screen with control tips.
	 * 
	 * @param m Main menu screen.
	 * @return Form to open.
	 */
	public static Form generateControlsTipsScreen(MMenu m) {
		try {
			Form f = new Form(NJTAI.getStrings("acts")[13]);
			f.setCommandListener(m);
			f.addCommand(backCmd);
			String[] items = NJTAI.getStrings("tips");
			for (int i = 0; i < items.length / 2; i++) {
				if (NJTAIM.isS60v3fp2()) {
					f.append(new StringItem(null, items[i * 2 + 1]));
					f.append(new StringItem(items[i * 2], null));
				} else {
					StringItem s = new StringItem(null, "[" + items[i * 2] + "] " + items[i * 2 + 1] + "\n");
					s.setFont(Font.getFont(0, 0, 8));
					f.append(s);
				}
			}
			return f;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

}
