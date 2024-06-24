package njtai.m.ui;

import java.io.IOException;

import javax.microedition.lcdui.*;

import njtai.NJTAI;
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
		backCmd = new Command(NJTAI.L_ACTS[0], Command.BACK, 2);
		openCmd = new Command(NJTAI.L_ACTS[1], Command.OK, 1);
		exitCmd = new Command(NJTAI.L_ACTS[2], Command.EXIT, 2);
		searchCmd = new Command(NJTAI.L_ACTS[3], Command.OK, 1);
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
				NJTAI.setScr(this);
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
					NJTAI.setScr(new MangaList(NJTAI.L_ACTS[4], this, r));
				} catch (IOException e) {
					e.printStackTrace();
					NJTAI.setScr(this);
					NJTAI.pause(100);
					NJTAI.setScr(new Alert(NJTAI.L_ACTS[7], NJTAI.L_ACTS[14], null,
							AlertType.ERROR));
				} catch (NullPointerException e) {
					NJTAI.setScr(this);
					NJTAI.pause(100);
					NJTAI.setScr(new Alert(NJTAI.L_ACTS[5], NJTAI.L_ACTS[6], null, AlertType.WARNING));
				} catch (Exception e) {
					e.printStackTrace();
					NJTAI.setScr(this);
					NJTAI.pause(100);
					NJTAI.setScr(new Alert(NJTAI.L_ACTS[7], NJTAI.L_ACTS[8].concat(" ").concat(e.toString()), null,
							AlertType.ERROR));
				}
				return;
			}
			if (c == openCmd) {
				try {
					NJTAI.setScr(new MangaPage(Integer.parseInt(((TextBox) d).getString()), this, null, null));
				} catch (Exception e) {
					NJTAI.setScr(this);
					NJTAI.pause(100);
					NJTAI.setScr(new Alert(NJTAI.L_ACTS[9], NJTAI.L_ACTS[10], null,
							AlertType.ERROR));
				}
				return;
			}
			if (c == exitCmd) {
				NJTAI.exit();
				return;
			}
			if (c == List.SELECT_COMMAND) {
				mainMenuLinks();
			}
		} catch (Throwable t) {
			System.gc();
			t.printStackTrace();
			NJTAI.setScr(this);
			NJTAI.pause(100);
			String info;
			if (t instanceof OutOfMemoryError) {
				info = NJTAI.L_ACTS[27];
			} else if (t instanceof IOException) {
				info = NJTAI.L_ACTS[25];
			} else if (t instanceof IllegalAccessException) {
				info = "Proxy returned nothing. Does it work from a country, where the site is banned?";
			} else {
				info = t.toString();
			}
			NJTAI.setScr(new Alert(NJTAI.rus ? "Ошибка приложения" : "App error", info, null, AlertType.ERROR));
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
			final TextBox tb = new TextBox(NJTAI.L_ACTS[11], "", 7, 2);
			tb.addCommand(openCmd);
			tb.addCommand(backCmd);
			tb.setCommandListener(this);
			NJTAI.setScr(tb);
			return;
		case 1:
			// popular
			NJTAI.setScr(new MangaList(NJTAI.getStrings("main")[1], this, MangaObjs.getPopularList()));
			return;
		case 2:
			// new
			NJTAI.setScr(new MangaList(NJTAI.getStrings("main")[2], this, MangaObjs.getNewList()));
			return;
		case 3:
			// search
			search();
			return;
		case 4:
			NJTAI.files = true;
			NJTAI.setScr(generateDownloadedScreen());
			return;
		case 5:
			// sets
			NJTAI.setScr(new Prefs(this));
			return;
		case 6:
			try {

				NJTAI.setScr(generateControlsTipsScreen(this));
			} catch (RuntimeException e) {
				NJTAI.setScr(new Alert("Failed to read texts", "JAR is corrupted. Reinstall the application.", null,
						AlertType.ERROR));
			}
			return;
		case 7:
			Form ab = new Form(NJTAI.L_ACTS[12]);
			try {
				ImageItem img = new ImageItem(null, Image.createImage("/njtai.png"), Item.LAYOUT_LEFT, null);
				ab.append(img);
			} catch (Throwable ignored) {}
			StringItem s;
			s = new StringItem(null, "MahoRasp v" + NJTAI.midlet.getAppProperty("MIDlet-Version"));
			s.setFont(Font.getFont(0, 0, Font.SIZE_LARGE));
			s.setLayout(Item.LAYOUT_LEFT | Item.LAYOUT_VCENTER);
			ab.append(s);
			
			s = new StringItem(null, NJTAI.rus ? "Клиент для nhentai.net под J2ME устройства, поддерживающие MIDP 2.0 и CLDC 1.1"
							: "nhentai.net client for J2ME devices with MIDP 2.0 and CLDC 1.1 support.");
			s.setFont(Font.getDefaultFont());
			s.setLayout(Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_LEFT);
			ab.append(s);
			ab.append(new StringItem(NJTAI.rus ? "Основные разработчики" : "Main developers", "Feodor0090, Shinovon\n"));
			ab.append(new StringItem(NJTAI.rus ? "Иконка и прокси" : "Icon and proxy", "Shinovon\n"));
			ab.append(new StringItem(NJTAI.rus ? "Тестирование и ревью" : "Review and testing",
					"stacorp, ales_alte, mineshanya\n"));
			ab.append(new StringItem(NJTAI.rus ? "Локализация" : "Localization", "ales_alte, Jazmin Rocio"));
			ab.append(new StringItem(NJTAI.rus ? "Отдельное спасибо" : "Special thanks to",
					"nnproject, SIStore, Symbian Zone, Jazmin Rocio\n"));
			ab.append(new StringItem(NJTAI.rus ? "Поддержать разработчика" : "Support the developer",
					"donate.stream/f0090\nboosty.to/nnproject/donate\n"));
			ab.append(new StringItem(NJTAI.rus ? "Больше информации:" : "More info:",
					"github.com/Feodor0090/njtai\nhttps://t.me/nnmidletschat"));
			
			s = new StringItem(null, "\n\n\n\n\n\n\n\nИ помните: порода Махо - чёрный пудель!\n292 labs (tm)");
			s.setFont(Font.getFont(0, 0, 8));
			ab.append(s);

			// setting up
			ab.setCommandListener(this);
			ab.addCommand(backCmd);
			NJTAI.setScr(ab);
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
		return NJTAI.url(data.replace('\n', ' ').replace('\t', ' ').replace('\r', ' ').replace('\0', ' '));
	}

	private void search() {
		final TextBox tb = new TextBox(NJTAI.L_ACTS[3], "", 80, 0);
		tb.addCommand(searchCmd);
		tb.addCommand(backCmd);
		tb.setCommandListener(this);
		NJTAI.setScr(tb);
	}

	/**
	 * Creates a screen with control tips.
	 * 
	 * @param m Main menu screen.
	 * @return Form to open.
	 */
	public static Form generateControlsTipsScreen(MMenu m) {
		try {
			Form f = new Form(NJTAI.L_ACTS[13]);
			f.setCommandListener(m);
			f.addCommand(backCmd);
			String[] items = NJTAI.getStrings("tips");
			for (int i = 0; i < items.length / 2; i++) {
				if (NJTAI.isS60v3fp2()) {
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
