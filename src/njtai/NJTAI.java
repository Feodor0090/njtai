package njtai;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.*;
import javax.microedition.rms.RecordStore;

import njtai.m.MDownloader;
import njtai.m.NJTAIM;
import njtai.m.ui.MangaPage;
import njtai.m.ui.Prefs;
import njtai.m.ui.SavedManager;
import njtai.models.MangaObj;
import njtai.models.MangaObjs;

/**
 * Main class of the application. Contains basic data and settings.
 * 
 * @author Feodor0090
 *
 */
public class NJTAI implements CommandListener, ItemCommandListener, Runnable {
	/**
	 * Base site URL.
	 */
	public static final String baseUrl = "https://nhentai.net";
	
	// Threading constants
	public static final int RUN_MANGALIST = 1;
	public static final int RUN_MANGALIST_NEW = 2;

	/**
	 * Currently used URL prefix. Check {@link #getHP() home page downloading
	 * method} to see how it works.
	 * 
	 * @see {@link #loadPrefs()}, {@link njtai.ui.Prefs#proxy}
	 */
	public static String proxy;

	/**
	 * Instance of currently active platform.
	 */
	public static NJTAIM midlet;

	/**
	 * Home page content
	 */
	private static String hp = null;

	/**
	 * Is the app already running?
	 */
	public static boolean started = false;

	/**
	 * Should images be kept or preloaded?
	 * <ul>
	 * <li>0 - nothing should be kept
	 * <li>1 - view may keep already viewed images
	 * <li>2 - images can be preloaded
	 * </ul>
	 */
	public static int cachingPolicy = 0;
	/**
	 * Load image on info page?
	 */
	public static boolean loadCoverAtPage = true;
	/**
	 * Keep existing lists when leaving?
	 */
	public static boolean keepLists = true;
	/**
	 * Load imags in lists?
	 */
	public static boolean loadCovers = true;
	/**
	 * Keep decoded images in RAM?
	 */
	public static boolean keepBitmap = true;
	/**
	 * Auto, SWR or HWA.
	 */
	public static int view = 0;
	/**
	 * Use device's memory card?
	 */
	public static boolean files;
	/**
	 * Invert D-PAD directions?
	 */
	public static boolean invertPan;

	/**
	 * Use russian localization?
	 */
	public static boolean rus = false;
	
	// localizations
	public static String[] L_ACTS;
	public static String[] L_PAGE;
	
	public static NJTAI inst;
	public static Display display;
	
	// UI
	public static List mmenu;
	public static Form mangaList;

	public static Command backCmd;
	public static Command openCmd;
	public static Command exitCmd;
	private static Command searchCmd;
	public static Command mangaItemCmd;
	
	// Threading
	private int run;
	private static boolean running;
	private static String query;

	private static boolean wasOom;
	
	public static void startApp() {
		if (NJTAI.started) return;
		NJTAI.started = true;
		display = Display.getDisplay(midlet);
		String loc = System.getProperty("microedition.locale");
		if (loc != null) {
			loc = loc.toLowerCase();
			NJTAI.rus = (loc.indexOf("ru") != -1 || loc.indexOf("ua") != -1 || loc.indexOf("kz") != -1
					|| loc.indexOf("by") != -1);
		}
		inst = new NJTAI();
		
		// loadPrefs() inlined
		try {
			RecordStore r = RecordStore.openRecordStore("njtai", true);

			if (r.getNumRecords() < 1) {
				r.closeRecordStore();
				throw new RuntimeException();
			}
			byte[] d = r.getRecord(1);
			r.closeRecordStore();
			String[] s = NJTAI.splitFull(new String(d), '`');
			NJTAI.files = s[0].equals("1");
			NJTAI.cachingPolicy = Integer.parseInt(s[1]);
			NJTAI.loadCoverAtPage = s[2].equals("1");
			NJTAI.keepLists = s[3].equals("1");
			NJTAI.loadCovers = s[4].equals("1");
			//NJTAI._d1 = s[5].equals("1");
			NJTAI.keepBitmap = s[6].equals("1");
			NJTAI.view = Integer.parseInt(s[7]);
			NJTAI.invertPan = s[8].equals("1");
			NJTAI.proxy = s[12];
			MDownloader.currentWD = s[13].equals(" ") ? null : s[13];
		} catch (Exception e) {
			System.out.println("There is no saved settings or they are broken.");
			NJTAI.files = false;
			NJTAI.cachingPolicy = 1;
			NJTAI.loadCoverAtPage = (Runtime.getRuntime().totalMemory() != 2048 * 1024);
			NJTAI.keepLists = true;
			NJTAI.loadCovers = true;
			NJTAI.keepBitmap = true;
			NJTAI.proxy = "http://nnp.nnchan.ru/hproxy.php?";
			NJTAI.view = 0;
			NJTAI.invertPan = false;
			MDownloader.currentWD = null;
		}
		
		// localizations
		
		L_ACTS = getStrings("acts");
		L_PAGE = getStrings("page");

		backCmd = new Command(NJTAI.L_ACTS[0], Command.BACK, 2);
		openCmd = new Command(NJTAI.L_ACTS[1], Command.OK, 1);
		exitCmd = new Command(NJTAI.L_ACTS[2], Command.EXIT, 2);
		searchCmd = new Command(NJTAI.L_ACTS[3], Command.OK, 1);
		
		mangaItemCmd = new Command(NJTAI.L_ACTS[15], 8, 1);
		
		// main menu
		
		mmenu = new List("NJTAI", List.IMPLICIT, NJTAI.getStrings("main"), null);
		mmenu.addCommand(exitCmd);
		mmenu.setCommandListener(inst);
		
		setScr(mmenu);
	}

	public static boolean savePrefs() {
		try {
			StringBuffer s = new StringBuffer();
			s.append(NJTAI.files ? "1" : "0");
			s.append('`');
			s.append(String.valueOf(NJTAI.cachingPolicy));
			s.append('`');
			s.append(NJTAI.loadCoverAtPage ? "1" : "0");
			s.append('`');
			s.append(NJTAI.keepLists ? "1" : "0");
			s.append('`');
			s.append(NJTAI.loadCovers ? "1" : "0");
			s.append('`');
			// Keeping the value to avoid data breaking.
			s.append('0');
			//s.append(NJTAI._d1 ? "1" : "0");
			s.append('`');
			s.append(NJTAI.keepBitmap ? "1" : "0");
			s.append('`');
			s.append(String.valueOf(NJTAI.view));
			s.append('`');
			s.append(NJTAI.invertPan ? "1" : "0");
			s.append('`');
//			s.append(NJTAI._f1 ? "1" : "0");
			s.append('`');
//			s.append(NJTAI._f2 ? "1" : "0");
			s.append('`');
//			s.append(NJTAI._f3 ? "1" : "0");
			s.append('`');
			s.append(NJTAI.proxy);
			s.append('`');
			String wd = MDownloader.currentWD;
			s.append(wd == null ? " " : wd);
			byte[] d = s.toString().getBytes();
			RecordStore r = RecordStore.openRecordStore("njtai", true);

			if (r.getNumRecords() == 0) {
				r.addRecord(new byte[1], 0, 1);
			}
			r.setRecord(1, d, 0, d.length);
			r.closeRecordStore();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public void commandAction(Command c, Item item) {
		// TODO global command handler
		if (c == mangaItemCmd) {
			try {
				int n = Integer.parseInt(((ImageItem) item).getAltText());
				if (wasOom) {
					if (NJTAI.getScr() instanceof Form) {
						((Form) NJTAI.getScr()).deleteAll();
					}
					NJTAI.setScr(loadingForm());
					System.gc();
					Thread.yield();
				}
				NJTAI.setScr(new MangaPage(n, wasOom ? null : mangaList, null, ((ImageItem) item).getImage()));
				mangaList = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void commandAction(Command c, Displayable d) {
		// TODO global command handler
		
		if (d == mmenu) {
			if (c == backCmd) {
				setScr(mmenu);
				return;
			}
			if (c == openCmd) {
				try {
					setScr(new MangaPage(Integer.parseInt(((TextBox) d).getString()), mmenu, null, null));
				} catch (Exception e) {
					setScr(errorAlert(9, 10), mmenu);
				}
				return;
			}
			if (c == exitCmd) {
				exit();
				return;
			}
			if (c == List.SELECT_COMMAND) {
				switch (mmenu.getSelectedIndex()) {
				case -1: 
					return;
				case 0: {
					// number;
					final TextBox tb = new TextBox(NJTAI.L_ACTS[11], "", 7, 2);
					tb.addCommand(openCmd);
					tb.addCommand(backCmd);
					tb.setCommandListener(this);
					NJTAI.setScr(tb);
					return;
				}
				case 1: {
					// popular
					if (running) return;
					mangaList = new Form(NJTAI.getStrings("main")[1]);
					mangaList.setCommandListener(this);
					mangaList.addCommand(NJTAI.backCmd);
					NJTAI.setScr(mangaList);
					start(RUN_MANGALIST);
					return;
				}
				case 2: {
					// new
					if (running) return;
					mangaList = new Form(NJTAI.getStrings("main")[2]);
					mangaList.setCommandListener(this);
					mangaList.addCommand(NJTAI.backCmd);
					start(RUN_MANGALIST_NEW);
					return;
				}
				case 3: {
					// search
					final TextBox tb = new TextBox(NJTAI.L_ACTS[3], "", 80, 0);
					tb.addCommand(searchCmd);
					tb.addCommand(backCmd);
					tb.setCommandListener(this);
					NJTAI.setScr(tb);
					return;
				}
				case 4:
					NJTAI.files = true;
					List l = new List("Loading...", List.IMPLICIT);
					(new SavedManager(l)).start();
					NJTAI.setScr(l);
					return;
				case 5:
					// sets
					NJTAI.setScr(new Prefs());
					return;
				case 6:
					try {
						Form f = new Form(NJTAI.L_ACTS[13]);
						f.setCommandListener(this);
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
			return;
		}
		if (d instanceof TextBox) {
			if (c == searchCmd) {
				// getting text
				String st = ((TextBox) d).getString().trim();
				// Isn't it empty?
				if (st.length() == 0) {
					setScr(new Alert(L_ACTS[5], L_ACTS[6], null, AlertType.WARNING));
					return;
				}
	
				query = NJTAI.url(st.replace('\n', ' ').replace('\t', ' ').replace('\r', ' ').replace('\0', ' '));
				mangaList = new Form(L_ACTS[4]);
				mangaList.setCommandListener(this);
				mangaList.addCommand(NJTAI.backCmd);
				start(RUN_MANGALIST);
				return;
			}
		}
		if (c == backCmd) {
			setScr(mmenu);
			return;
		}
	}
	
	public void run() {
		int run;
		synchronized(this) {
			run = this.run;
			notify();
		}
		running = true;
		switch (run) {
		case RUN_MANGALIST: 
		case RUN_MANGALIST_NEW: {
			wasOom = false;
			if (mangaList == null) break;
			try {
				MangaObjs objs = null;
				try {
					NJTAI.setScr(NJTAI.loadingAlert(), mangaList);
					try {
						if (query != null) {
							objs = MangaObjs.getSearchList(query, null);
							query = null;
						} else if (run == RUN_MANGALIST_NEW) {
							objs = MangaObjs.getNewList();
						} else {
							objs = MangaObjs.getPopularList();
						}
					} catch (Exception e) {
						e.printStackTrace();
						NJTAI.setScr(new Alert(NJTAI.L_ACTS[7], NJTAI.L_ACTS[14].concat(" ").concat(e.toString()), null, AlertType.ERROR), NJTAI.mmenu);
						return;
					}
					boolean show = true;
					while (objs.hasMoreElements()) {
						MangaObj o = (MangaObj) objs.nextElement();
						ImageItem img = new ImageItem(o.title, (Image) o.img, 3, Integer.toString(o.num), Item.HYPERLINK);
						img.addCommand(mangaItemCmd);
						img.setDefaultCommand(mangaItemCmd);
						img.setItemCommandListener(this);
						mangaList.append(img);
						if (show) {
							NJTAI.setScr(mangaList);
							show = false;
						}
					}
					objs = null;
				} catch (OutOfMemoryError e) {
					wasOom = true;
					objs = null;
					System.gc();
					NJTAI.keepLists = false;
					NJTAI.savePrefs();
					mangaList.append(new StringItem(NJTAI.rus ? "Ошибка" : "Error",
							NJTAI.rus ? "Не хватило памяти для отображения полного списка"
									: "Not enough memory to show full list"));
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
			break;
		}
		}
		running = false;
	}

	private void start(int i) {
		try {
			synchronized(this) {
				run = i;
				new Thread(this).start();
				wait();
				run = 0;
			}
		} catch (Exception e) {}
	}
	

	/**
	 * @return Currently shown screen.
	 */
	public static Displayable getScr() {
		return display.getCurrent();
	}

	/**
	 * Sets current screen.
	 * 
	 * @param d Screen to activate.
	 */
	public static void setScr(Displayable d) {
		display.setCurrent(d);
	}

	/**
	 * Sets current screen.
	 * 
	 * @param a    Screen to activate.
	 * @param prev Next screen.
	 */
	public static void setScr(Alert a, Displayable prev) {
		display.setCurrent(a, prev);
	}
	
	private static Alert errorAlert(int title, int text) {
		Alert a = new Alert(L_ACTS[title], L_ACTS[text], null, AlertType.ERROR);
		a.setTimeout(3000);
		return a;
	}
	
	public static Alert loadingAlert() {
		Alert a = new Alert(" ", NJTAI.rus ? "Загрузка..." : "Loading...", null, null);
		a.setIndicator(new Gauge(null, false, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING));
		a.setTimeout(30000);
		return a;
	}
	
	public static Form loadingForm() {
		return new Form(NJTAI.rus ? "Загрузка..." : "Loading...");
	}

	public static void showNotification(String title, String text, int type, Object prev) {
		AlertType at = null;
		switch (type) {
		case 0:
			at = AlertType.INFO;
			break;
		case 1:
			at = AlertType.CONFIRMATION;
			break;
		case 2:
			at = AlertType.WARNING;
			break;
		case 3:
			at = AlertType.ERROR;
			break;
		default:
			return;
		}

		if (prev != null && prev instanceof Displayable) {
			NJTAI.setScr((Displayable) prev);
			NJTAI.pause(100);
		}
		NJTAI.setScr(new Alert(title, text, null, at));
	}

	public static void repaint() {
		Displayable s = getScr();
		if (s instanceof Canvas)
			((Canvas) s).repaint();
	}

	public static Image decodeImage(byte[] data) {
		return Image.createImage(data, 0, data.length);
	}

	public static Image prescaleCover(Image original) {
		Image i = (Image) original;
		int h = getHeight() * 2 / 3;
		int w = (int) (((float) h / i.getHeight()) * i.getWidth());
		return NJTAI.resize(i, w, h);
	}

	/**
	 * @return Height of display.
	 */
	public static int getHeight() {
		return getScr().getHeight();
	}

	public static void exit() {
		midlet.notifyDestroyed();
	}
	/**
	 * @return Midlet version.
	 */
	public static String ver() {
		return midlet.getAppProperty("MIDlet-Version");
	}

	/**
	 * Gets home page.
	 * 
	 * @return Content of the page.
	 * @throws IOException            If nothing was loaded.
	 * @throws IllegalAccessException If empty string was loaded.
	 */
	public synchronized static String getHP() throws IOException, IllegalAccessException {
		String s = hp;
		if (s == null) {
			String url = baseUrl;
			if (proxy.length() > 0 && !"https://".equals(proxy))
				url = proxy.concat(url(url));
			s = getUtfOrNull(url);
			if (s == null)
				throw new IOException();
			if (s.length() < 2)
				throw new IllegalAccessException();

			if (Runtime.getRuntime().totalMemory() != 2048 * 1024) {
				hp = s;
			}
		}
		return s;
	}

	public static Image getImage(String imgUrl) {
		// TODO
		byte[] d = NJTAI.getOrNull(NJTAI.proxyUrl(imgUrl));
		return Image.createImage(d, 0, d.length);
	}

	/**
	 * Clears main page's content.
	 */
	public synchronized static void clearHP() {
		hp = null;
	}

	/**
	 * Converts, for example, https://ya.ru to http://proxy.com/proxy.php?ya.ru.
	 * 
	 * @param url Original URL.
	 * @return URL, ready to be loaded.
	 */
	public static String proxyUrl(String url) {
		if (url == null)
			return null;

		if(proxy.length() == 0 || "https://".equals(proxy)) {
			return url;
		}
		return proxy + url(url);
	}
	
	public static String url(String url) {
		StringBuffer sb = new StringBuffer();
		char[] chars = url.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			int c = chars[i];
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

	private static String hex(int i) {
		String s = Integer.toHexString(i);
		return "%".concat(s.length() < 2 ? "0" : "").concat(s);
	}

	/**
	 * Stops a thread, ignoring interruptions.
	 * 
	 * @param ms Ms to wait.
	 */
	public static void pause(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads localization file.
	 * 
	 * @param cat    Category of strings.
	 * @param locale Language code to use.
	 * @return List of strings to use.
	 */
	public static String[] getStrings(String cat, String locale) {
		try {
			if (locale == null) {
				if ((locale = System.getProperty("microedition.locale")) != null)
					locale = locale.toLowerCase().substring(0, 2);
			}
			InputStream in = NJTAI.class.getResourceAsStream("/text/" + cat + "_" + locale + ".txt");
			if (in == null)
				in = NJTAI.class.getResourceAsStream("/text/" + cat + "_en.txt");
			String[] l = new String["main".equals(cat) ? 8 : 50];
			InputStreamReader r = new InputStreamReader(in, "UTF-8");
			StringBuffer s = new StringBuffer();
			int c;
			int i = 0;
			while((c = r.read()) > 0) {
				if(c == '\r') continue;
				if(c == '\\') {
					s.append((c = r.read()) == 'n' ? '\n' : (char) c);
					continue;
				}
				if(c == '\n') {
					l[i++] = s.toString();
					s.setLength(0);
					continue;
				}
				s.append((char) c);
			}
			if(s.length() > 0) {
				l[i++] = s.toString();
			}
			r.close();
			return l;
		} catch (Exception e) {
			e.printStackTrace();
			// null is returned to avoid massive try-catch constructions near every call.
			// Normally, it always return english file.
			return null;
		}
	}

	/**
	 * Loads localization file.
	 * 
	 * @param cat Category of strings.
	 * @return List of strings to use.
	 */
	public static String[] getStrings(String cat) {
		return getStrings(cat, null);
	}
	
	// StringUtils

	public static String from(String s, String f) {
		return from(s, f, true);
	}

	public static String from(String s, String f, boolean incl) {
		int si = s.indexOf(f);
		if (si == -1)
			return "";
		if (!incl) {
			si += f.length();
		}
		return s.substring(si);
	}

	public static String range(String s, String f, String t) {
		return range(s, f, t, false);
	}

	public static String range(String s, String f, String t, boolean incl) {
		if (s.length() == 0)
			return "";
		int si = s.indexOf(f);
		if (si == -1) {
			si = 0;
		} else if (!incl) {
			si += f.length();
		}
		int ei = s.indexOf(t, si);
		if (ei == -1 || t.length() == 0) {
			return s.substring(si);
		}
		if (incl) {
			ei += t.length();
		}
		return s.substring(si, ei);
	}

	public static String[] splitRanges(String s, String f, String t, boolean incl) {
		Vector v = new Vector();
		int i = 0;
		while (true) {
			int si = s.indexOf(f, i);
			if (si == -1)
				break;
			if (!incl)
				si += f.length();
			int ei = s.indexOf(t, si);
			i = ei + t.length();
			if (incl)
				ei += t.length();
			v.addElement(s.substring(si, ei));
		}
		String[] a = new String[v.size()];
		v.copyInto(a);
		v.removeAllElements();
		v = null;
		return a;
	}

	public static String[] split(String str, String k) {
		Vector v = new Vector(32, 16);
		int lle = 0;
		while (true) {
			int nle = str.indexOf(k, lle);
			if (nle == -1) {
				v.addElement(str.substring(lle, str.length()));
				break;
			}
			v.addElement(str.substring(lle, nle));
			lle = nle + k.length();
		}
		String[] a = new String[v.size()];
		v.copyInto(a);
		v.removeAllElements();
		v.trimToSize();
		v = null;
		return a;
	}

	public static String toSingleLine(String s) {
		return s.replace('\r', ' ').replace('\n', ' ').replace('\t', ' ');
	}
	
	// SN
	public static String[] splitFull(String str, char c) {
		Vector v = new Vector(32, 16);
		int lle = 0;
		while (true) {
			int nle = str.indexOf(c, lle);
			if (nle == -1) {
				v.addElement(str.substring(lle, str.length()));
				break;
			}
			v.addElement(str.substring(lle, nle));
			lle = nle + 1;
		}
		String[] a = new String[v.size()];
		v.copyInto(a);
		return a;
	}

	/**
	 * Replaces some common html entities.
	 * 
	 * @param str String to process.
	 * @return String with parsed html escape codes
	 * @author Shinovon
	 */
	public static String htmlString(String str) {
		str = replace(str, "&#39;", "'");
		str = replace(str, "&#x27;", "'");
		// str = replace(str, "&apos;", "'");
		str = replace(str, "&quot;", "\"");
		str = replace(str, "&lt;", "<");
		str = replace(str, "&gt;", ">");
		// str = replace(str, "&nbsp;", " ");
		str = replace(str, "&ndash;", "-");
		str = replace(str, "&amp;", "&");
		return str;
	}

	/**
	 * @param str  original
	 * @param from string to find
	 * @param to   string to replace with
	 * @return replaced string
	 * @author Shinovon
	 */
	public static String replace(String str, String from, String to) {
		int j = str.indexOf(from);
		if (j == -1)
			return str;
		final StringBuffer sb = new StringBuffer();
		int k = 0;
		for (int i = from.length(); j != -1; j = str.indexOf(from, k)) {
			sb.append(str.substring(k, j)).append(to);
			k = j + i;
		}
		sb.append(str.substring(k, str.length()));
		return sb.toString();
	}
	
	// Web
	
	public static byte[] get(String url) throws IOException {
		HttpConnection hc = null;
		InputStream i = null;
		try {
			hc = (HttpConnection) Connector.open(url);
			hc.setRequestMethod("GET");
			int r = hc.getResponseCode();
			if (r == 301 || r == 302) {
				String redir = hc.getHeaderField("Location");
				if (redir.startsWith("/")) {
					String tmp = url.substring(url.indexOf("//") + 2);
					String host = url.substring(0, url.indexOf("//")) + "//" + tmp.substring(0, tmp.indexOf("/"));
					redir = host + redir;
				}
				hc.close();
				hc = (HttpConnection) Connector.open(redir);
				hc.setRequestMethod("GET");
			}
			// TODO
//			if (r >= 400) throw new IOException("HTTP ".concat(Integer.toString(r)));
			i = hc.openInputStream();
			return readBytes(i, (int) hc.getLength(), 16384, 16384);
		} finally {
			try {
				if (i != null) i.close();
			} catch (IOException e) {
			}
			try {
				if (hc != null) hc.close();
			} catch (IOException e) {
			}
		}
	}
	
	private static byte[] readBytes(InputStream inputStream, int initialSize, int bufferSize, int expandSize) throws IOException {
		if (initialSize <= 0) initialSize = bufferSize;
		byte[] buf = new byte[initialSize];
		int count = 0;
		byte[] readBuf = new byte[bufferSize];
		int readLen;
		while ((readLen = inputStream.read(readBuf)) != -1) {
			if(count + readLen > buf.length) {
				byte[] newbuf = new byte[count + expandSize];
				System.arraycopy(buf, 0, newbuf, 0, count);
				buf = newbuf;
			}
			System.arraycopy(readBuf, 0, buf, count, readLen);
			count += readLen;
		}
		if(buf.length == count) {
			return buf;
		}
		byte[] res = new byte[count];
		System.arraycopy(buf, 0, res, 0, count);
		return res;
	}

	public static byte[] getOrNull(String url) {
		try {
			return get(url);
		} catch (Exception e) {
			return null;
		}
	}

	public static String getUtf(String url) throws IOException {
		return new String(get(url), "UTF-8");
	}

	public static String getUtfOrNull(String url) {
		try {
			return getUtf(url);
		} catch (Exception e) {
			return null;
		}
	}
	
	// JSONUtil

	/**
	 * @param str JSON string
	 * @return Parsed result. Hashtable, Vector or String
	 */
	public static Object parse(String str) {
		int f = str.charAt(0);
		if(f == '{') {
			return object(str.substring(1, str.length() - 1));
		}
		/*if(f == '[') {
			return array(str.substring(1, str.length() - 1));
		}*/
		if(f == '"') {
			return NJTAI.replace(NJTAI.replace(str.substring(1, str.length() - 1), "\\n", "\n"), "\\\"", "\"");
		}
		return str;
	}
	
	/**
	 * @param str JSON object string
	 * @return Parsed hashtable
	 */
	public static Hashtable object(String str) {
		Hashtable ht = new Hashtable();
		int unclosed = 0;
		int index = 0;
		int length = str.length();
		boolean escape = false;
		int splIndex;
		for (; index < length; index = splIndex + 1) {
			splIndex = index;
			boolean quotes;
			for (quotes = false; splIndex < length && (quotes || unclosed > 0 || str.charAt(splIndex) != ','); splIndex++) {
				char c = str.charAt(splIndex);
				if (!escape) {
					if (c == '\\') {
						escape = true;
					}
					if (c == '"') {
						quotes = !quotes;
					}
				} else {
					escape = false;
				}
				if (!quotes) {
					if (c == '{') {
						unclosed++;
					} else if (c == '}') {
						unclosed--;
					}
				}
			}
			if (quotes || unclosed > 0) {
				throw new RuntimeException("Corrupted JSON");
			}
			String token = str.substring(index, splIndex);
			int splIndex2 = token.indexOf(":");
			ht.put(token.substring(1, splIndex2 - 1).trim(), parse(token.substring(splIndex2 + 1)));
		}
		return ht;
	}

	/**
	 * @param obj Hashtable, Vector or other object to encode in JSON
	 * @return JSON string
	 */
	public static String build(Object obj) {
		if(obj instanceof Hashtable) {
			Hashtable ht = (Hashtable) obj;
			Enumeration en = ht.keys();
			if (!en.hasMoreElements()) {
				return "{}";
			}
			String r = "{";
			while (true) {
				Object e = en.nextElement();
				r = r.concat("\"")
				.concat(e.toString())
				.concat("\":")
				.concat(build(ht.get(e)));
				if (!en.hasMoreElements()) {
					return r.concat("}");
				}
				r = r.concat(",");
			}
		} else if(obj instanceof String) {
			String s = (String) obj;
			s = NJTAI.replace(s, "\n", "\\n");
			s = NJTAI.replace(s, "\"", "\\\"");
			return "\"".concat(s).concat("\"");
		} else {
			return String.valueOf(obj);
		}
	}
	
	// ImageUtils

	/**
	 * Resizes the image.
	 * 
	 * @param src_i  Original image.
	 * @param size_w
	 * @param size_h
	 * @return Resized image.
	 */
	public static Image resize(Image src_i, int size_w, int size_h) {

		// set source size
		int w = src_i.getWidth();
		int h = src_i.getHeight();

		// no change?
		if (size_w == w && size_h == h)
			return src_i;

		int[] dst = new int[size_w * size_h];

		resize_rgb_filtered(src_i, dst, w, h, size_w, size_h);

		// not needed anymore
		src_i = null;

		return Image.createRGBImage(dst, size_w, size_h, true);
	}

	private static final void resize_rgb_filtered(Image src_i, int[] dst, int w0, int h0, int w1, int h1) {
		int[] buffer1 = new int[w0];
		int[] buffer2 = new int[w0];

		// UNOPTIMIZED bilinear filtering:
		//
		// The pixel position is defined by y_a and y_b,
		// which are 24.8 fixed point numbers
		//
		// for bilinear interpolation, we use y_a1 <= y_a <= y_b1
		// and x_a1 <= x_a <= x_b1, with y_d and x_d defining how long
		// from x/y_b1 we are.
		//
		// since we are resizing one line at a time, we will at most
		// need two lines from the source image (y_a1 and y_b1).
		// this will save us some memory but will make the algorithm
		// noticeably slower

		for (int index1 = 0, y = 0; y < h1; y++) {

			final int y_a = ((y * h0) << 8) / h1;
			final int y_a1 = y_a >> 8;
			int y_d = y_a & 0xFF;

			int y_b1 = y_a1 + 1;
			if (y_b1 >= h0) {
				y_b1 = h0 - 1;
				y_d = 0;
			}

			// get the two affected lines:
			src_i.getRGB(buffer1, 0, w0, 0, y_a1, w0, 1);
			if (y_d != 0)
				src_i.getRGB(buffer2, 0, w0, 0, y_b1, w0, 1);

			for (int x = 0; x < w1; x++) {
				// get this and the next point
				int x_a = ((x * w0) << 8) / w1;
				int x_a1 = x_a >> 8;
				int x_d = x_a & 0xFF;

				int x_b1 = x_a1 + 1;
				if (x_b1 >= w0) {
					x_b1 = w0 - 1;
					x_d = 0;
				}

				// interpolate in x
				int c12, c34;
				int c1 = buffer1[x_a1];
				int c3 = buffer1[x_b1];

				// interpolate in y:
				if (y_d == 0) {
					c12 = c1;
					c34 = c3;
				} else {
					int c2 = buffer2[x_a1];
					int c4 = buffer2[x_b1];

					c12 = NJTAI.blend(c2, c1, y_d);
					c34 = NJTAI.blend(c4, c3, y_d);
				}

				// final result
				dst[index1++] = NJTAI.blend(c34, c12, x_d);
			}
		}
	}

	/**
	 * Part of tube42 imagelib. Blends 2 colors.
	 * 
	 * @param c1
	 * @param c2
	 * @param value256
	 * @return Blended value.
	 */
	public static final int blend(final int c1, final int c2, final int value256) {

		final int v1 = value256 & 0xFF;
		final int c1_RB = c1 & 0x00FF00FF;
		final int c2_RB = c2 & 0x00FF00FF;

		final int c1_AG = (c1 >>> 8) & 0x00FF00FF;

		final int c2_AG_org = c2 & 0xFF00FF00;
		final int c2_AG = (c2_AG_org) >>> 8;

		// the world-famous tube42 blend with one mult per two components:
		final int rb = (c2_RB + (((c1_RB - c2_RB) * v1) >> 8)) & 0x00FF00FF;
		final int ag = (c2_AG_org + ((c1_AG - c2_AG) * v1)) & 0xFF00FF00;
		return ag | rb;

	}
	
	// platform utils

	/**
	 * Are we working on 9.3?
	 * 
	 * @return Status of 9.3 detection.
	 */
	public static boolean isS60v3fp2() {
		return System.getProperty("microedition.platform").indexOf("sw_platform_version=3.2") != -1;
	}

	/**
	 * Are we working on J2ME Loader?
	 * 
	 * @return Status of j2meL detection.
	 */
	public static boolean isJ2MEL() {
		String vendor = System.getProperty("java.vendor");
		return (vendor != null && vendor.toLowerCase().indexOf("ndroid") != -1);
	}

	/**
	 * Are we running on KEmulator?
	 * 
	 * @return KEmulator detection status.
	 */
	public static boolean isKem() {
		return isClsExists("emulator.custom.CustomMethod");
	}

	/**
	 * Checks class' existing.
	 * 
	 * @param clsName Class to check.
	 * @return Can the class be instantiated or not.
	 */
	public static boolean isClsExists(String clsName) {
		try {
			Class.forName(clsName);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

}
