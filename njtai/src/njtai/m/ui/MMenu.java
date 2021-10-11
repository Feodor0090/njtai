package njtai.m.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextBox;

import njtai.NJTAI;
import njtai.m.NJTAIM;
import njtai.models.MangaObjs;

public final class MMenu extends List implements CommandListener {

	public MMenu() {
		super("NJTAI", List.IMPLICIT, NJTAIM.getStrings("main"), null);
		this.addCommand(exitCmd);
		this.setCommandListener(this);
	}

	private Command exitCmd = new Command(NJTAI.rus ? "Выход" : "Exit", Command.EXIT, 2);
	private Command backCmd = new Command(NJTAI.rus ? "Назад" : "Back", Command.BACK, 2);
	private Command openCmd = new Command(NJTAI.rus ? "Открыть" : "Go", Command.OK, 1);
	private Command searchCmd = new Command(NJTAI.rus ? "Поиск" : "Search", Command.OK, 1);

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
					NJTAIM.setScr(new MangaList("Search results", this, r));
				} catch (NullPointerException e) {
					NJTAIM.setScr(this);
					NJTAI.pause(100);
					NJTAIM.setScr(new Alert("Incorrect query", "Did you entered nothing?", null, AlertType.WARNING));
				} catch (Exception e) {
					e.printStackTrace();
					NJTAIM.setScr(this);
					NJTAI.pause(100);
					NJTAIM.setScr(new Alert("Failed to open",
							"Have you entered something URL-breaking? Is your proxy and network alive?", null,
							AlertType.ERROR));
				}
				return;
			}
			if (c == openCmd) {
				try {
					NJTAIM.setScr(new MangaPage(Integer.parseInt(((TextBox) d).getString()), this));
				} catch (Exception e) {
					NJTAIM.setScr(this);
					NJTAI.pause(100);
					NJTAIM.setScr(new Alert("Failed to go to page", "Have you entered correct number?", null,
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
			if (t instanceof OutOfMemoryError) {
				info = "Not enough memory!";
			} else if (t instanceof IOException) {
				info = "Failed to connect. Check connection and proxy.";
			} else if (t instanceof IllegalAccessException) {
				info = "Proxy returned nothing. Does it work from a country, where the site is banned?";
			} else {
				info = t.toString();
			}
			NJTAIM.setScr(new Alert("App error", info, null, AlertType.ERROR));
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
			final TextBox tb = new TextBox(NJTAI.rus ? "Введите номер:" : "Enter ID:", "", 7, 2);
			tb.addCommand(openCmd);
			tb.addCommand(backCmd);
			tb.setCommandListener(this);
			NJTAIM.setScr(tb);
			return;
		case 4:
			// sets
			NJTAIM.setScr(new Prefs(this));
			return;
		case 1:
			// popular
			NJTAIM.setScr(new MangaList(NJTAI.rus ? "Популярные" : "Popular", this, MangaObjs.getPopularList()));
			return;
		case 2:
			// new
			NJTAIM.setScr(new MangaList(NJTAI.rus ? "Новые" : "Recently added", this, MangaObjs.getNewList()));
			return;
		case 3:
			// search
			search();
			return;
		case 5:
			try {

				NJTAIM.setScr(generateControlsTipsScreen(this));
			} catch (RuntimeException e) {
				NJTAIM.setScr(new Alert("Failed to read texts", "JAR is corrupted. Reinstall the application.", null,
						AlertType.ERROR));
			}
			return;
		case 6:
			Alert a1 = new Alert(NJTAI.rus ? "О программе" : "About",
					"NJTAI v" + NJTAIM.ver() + "\nDevelopers: Feodor0090, Shinovon\nIcon and proxy by Shinovon\nMore info at github.com/Feodor0090/njtai", null,
					AlertType.INFO);
			a1.setTimeout(Alert.FOREVER);
			NJTAIM.setScr(a1);
			return;
		}
	}

	private String processSearchQuery(String data) {
		if (data == null)
			throw new NullPointerException();
		data = data.replace('\n', ' ').replace('\t', ' ').replace('\r', ' ').replace('\0', ' ');
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < data.length(); i++) {
			switch (data.charAt(i)) {
			case '!':
				sb.append("%21");
				break;
			case '#':
				sb.append("%23");
				break;
			case '$':
				sb.append("%24");
				break;
			case '%':
				sb.append("%25");
				break;
			case '&':
				sb.append("%26");
				break;
			case '\'':
				sb.append("%27");
				break;
			case '(':
				sb.append("%28");
				break;
			case ')':
				sb.append("%29");
				break;
			case '*':
				sb.append("%2A");
				break;
			case '+':
				sb.append("%2B");
				break;
			case ',':
				sb.append("%2C");
				break;
			case '/':
				sb.append("%2F");
				break;
			case ':':
				sb.append("%3A");
				break;
			case ';':
				sb.append("%3B");
				break;
			case '=':
				sb.append("%3D");
				break;
			case '?':
				sb.append("%3F");
				break;
			case '@':
				sb.append("%40");
				break;
			case '[':
				sb.append("%5B");
				break;
			case ']':
				sb.append("%5D");
				break;
			case '{':
				sb.append("%7B");
				break;
			case '|':
				sb.append("%7C");
				break;
			case '}':
				sb.append("%7D");
				break;
			case '\\':
				sb.append("%5C");
				break;
			case '~':
				sb.append("%7E");
				break;
			case '-':
				sb.append("%2D");
				break;
			case '_':
				sb.append("%5F");
				break;
			case '"':
				sb.append("%22");
				break;
			case '.':
				sb.append("%2E");
				break;
			case ' ':
				sb.append("%20");
				break;
			default:
				sb.append(data.charAt(i));
				break;
			}
		}
		return sb.toString();
	}

	private void search() {
		final TextBox tb = new TextBox(NJTAI.rus ? "Введите запрос:" : "Enter query:", "", 80, 0);
		tb.addCommand(searchCmd);
		tb.addCommand(backCmd);
		tb.setCommandListener(this);
		NJTAIM.setScr(tb);
	}

	public static Form generateControlsTipsScreen(MMenu m) {
		try {
			Form f = new Form(NJTAI.rus ? "Управление" : "Controls");
			f.setCommandListener(m);
			f.addCommand(m.backCmd);
			String[] items = NJTAIM.getStrings("tips");
			for (int i = 0; i < items.length / 2; i++) {
				StringItem s = new StringItem(null, "["+items[i * 2]+"] "+items[i * 2 + 1]+"\n");
				s.setFont(Font.getFont(0, 0, 8));
				f.append(s);
			}
			return f;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

}
