package njtai.m.ui;

import java.io.*;
import java.util.*;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.*;

import cc.nnproject.utils.JSONUtil;
import njtai.NJTAI;
import njtai.m.MDownloader;
import njtai.m.NJTAIM;
import njtai.models.ExtMangaObj;
import njtai.models.WebAPIA;

/**
 * Class, responsible for loading a list of downloaded titles and opening an
 * offline {@link MangaPage}.
 * 
 * @author Feodor0090
 * @since 1.1.47
 *
 */
public class SavedManager extends Thread implements CommandListener {

	private List list;
	private MMenu mm;
	private String path;

	private final Command delC = new Command(NJTAI.rus ? "Удалить" : "Delete", Command.SCREEN, 3);
	private final Command repairC = new Command(NJTAI.rus ? "Восстановить" : "Repair", Command.SCREEN, 2);
	private final Command switchC = new Command(NJTAI.rus ? "Другая папка" : "Another folder", Command.SCREEN, 4);

	private final Command cancelDelC = new Command(NJTAI.rus ? "Отмена" : "Cancel", Command.CANCEL, 1);
	private final Command confirmDelC = new Command(NJTAI.rus ? "Продолжить" : "Continue", Command.OK, 1);

	/**
	 * Creates a manager.
	 * 
	 * @param l List, where we will work.
	 * @param b Main menu screen.
	 */
	public SavedManager(List l, MMenu b) {
		list = l;
		mm = b;
	}

	public void run() {
		path = MDownloader.getWD();

		refresh();

		list.setTitle(path);
		list.addCommand(MMenu.backCmd);
		list.addCommand(delC);
		list.addCommand(repairC);
		list.addCommand(switchC);
		list.setCommandListener(this);
	}

	/**
	 * Fills the list with folder content.
	 */
	public void refresh() {
		Enumeration e = null;
		FileConnection fc = null;

		// reading folder's list
		try {
			fc = (FileConnection) Connector.open(path, Connector.READ);
			e = fc.list();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (fc != null) fc.close();
			} catch (Exception exx) {
				exx.printStackTrace();
			}
		}

		list.deleteAll();

		if (e != null) {
			while (e.hasMoreElements()) {
				String s = e.nextElement().toString();
				try {
					if (s.length() < 4) {
						continue;
					}
					// trying to parse folder's name
					if (s.charAt(s.length() - 1) != '/') {
						continue;
					}
					int i = s.indexOf('-');
					if (i == -1) {
						continue;
					}
					String n = s.substring(0, i).trim();
					Integer.parseInt(n);

					// push
					list.append(s.substring(0, s.length() - 1), null);
				} catch (Exception ex) {
					// skipping
				}
			}
		}
	}

	public void commandAction(Command c, Displayable arg1) {
		if (c == MMenu.backCmd) {
			NJTAIM.setScr(mm);
			return;
		}
		if (c == switchC) {
			MDownloader.reselectWD(mm);
			return;
		}
		if (c == repairC) {
			onRepair();
			return;
		}
		if (c == delC) {
			// is the list empty?
			if (list.size() == 0) {
				Alert a = new Alert("NJTAI", NJTAI.rus ? "Папка не выбрана." : "No folder is selected.", null,
						AlertType.WARNING);
				a.setTimeout(Alert.FOREVER);
				NJTAIM.setScr(a, list);
				return;
			}

			String item = list.getString(list.getSelectedIndex());
			Alert a = new Alert(item, NJTAI.rus
					? "Папка и её содержимое будет удалено. Если вы храните в ней свои данные, они могут быть повреждены."
					: "The folder and it's content will be deleted. If you keep you data there, it may be damaged.",
					null, AlertType.WARNING);
			a.setTimeout(Alert.FOREVER);
			a.setCommandListener(this);
			a.addCommand(cancelDelC);
			a.addCommand(confirmDelC);
			NJTAIM.setScr(a);
			return;
		}
		if (c == cancelDelC) {
			NJTAIM.setScr(list);
			return;
		}
		if (c == confirmDelC) {
			onDelete();
			return;
		}

		if (c == List.SELECT_COMMAND) {
			onSelect();
			return;
		}
	}

	private void onDelete() {
		final CommandListener listener = this;
		(new Thread() {
			public void run() {

				String item = list.getString(list.getSelectedIndex());

				// alert
				{
					NJTAIM.setScr(list);
					NJTAI.pause(100);
					Alert a = new Alert(item, NJTAI.rus ? "Удаление" : "Deleting", null, AlertType.INFO);
					a.setTimeout(Alert.FOREVER);
					Gauge g = new Gauge(null, false, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING);
					a.setIndicator(g);
					a.setCommandListener(listener);
					NJTAIM.setScr(a);
				}

				// path of folder where we will work
				item = item + "/";

				String id = item.substring(0, item.indexOf('-')).trim();

				FileConnection fc = null;

				// model
				try {
					String fn = path + item + "model.json";
					fc = (FileConnection) Connector.open(fn, Connector.WRITE);
					fc.delete();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					cfc(fc);
				}

				Enumeration e = null;

				try {
					fc = (FileConnection) Connector.open(path + item, Connector.READ);
					e = fc.list();
				} catch (Exception ex) {
					ex.printStackTrace();
				} finally {
					cfc(fc);
				}

				if (e == null)
					return;

				while (e.hasMoreElements()) {

					String name = e.nextElement().toString();

					if (!name.endsWith(".jpg"))
						continue;
					if (!name.startsWith(id))
						continue;
					if (name.indexOf('_') == -1)
						continue;
					try {
						String fn = path + item + name;
						fc = (FileConnection) Connector.open(fn, Connector.WRITE);
						fc.delete();
					} catch (Exception ex) {
					} finally {
						cfc(fc);
					}
				}

				try {
					fc = (FileConnection) Connector.open(path + item, Connector.WRITE);
					fc.delete();
					fc.close();
					NJTAIM.setScr(list);
					NJTAI.pause(100);
					Alert a = new Alert("NJTAI", NJTAI.rus ? "Удалено." : "Deleted.", null, AlertType.CONFIRMATION);
					a.setTimeout(Alert.FOREVER);
					NJTAIM.setScr(a, list);
				} catch (Exception ex) {
					ex.printStackTrace();
					NJTAIM.setScr(list);
					NJTAI.pause(100);
					Alert a = new Alert("NJTAI",
							NJTAI.rus ? "В папке остались посторонние файлы." : "There are third files in the folder.",
							null, AlertType.WARNING);
					a.setTimeout(Alert.FOREVER);
					NJTAIM.setScr(a, list);
				} finally {
					cfc(fc);
				}

				refresh();
			}
		}).start();

		return;
	}

	private void onRepair() {
		// is the list empty?
		if (list.size() == 0) {
			Alert a = new Alert("NJTAI", NJTAI.rus ? "Папка не выбрана." : "No folder is selected.", null,
					AlertType.WARNING);
			a.setTimeout(Alert.FOREVER);
			NJTAIM.setScr(a, list);
			return;
		}
		// loading EMO from the site
		String item = list.getString(list.getSelectedIndex());
		String n = item.substring(0, item.indexOf('-')).trim();
		String html = WebAPIA.inst.getUtfOrNull(NJTAI.proxy + NJTAI.baseUrl + "/g/" + n + "/");
		if (html == null) {
			Alert a = new Alert(item, NJTAI.rus ? "Сетевая ошибка." : "Network error.", null, AlertType.ERROR);
			a.setTimeout(Alert.FOREVER);
			NJTAIM.setScr(a, list);
			return;
		}
		ExtMangaObj emo = new ExtMangaObj(Integer.parseInt(n), html);
		// running downloader
		MDownloader md = new MDownloader(emo, list);
		md.repair = true;
		md.start();
	}

	private void onSelect() {
		// vars
		MangaPage mp;
		ExtMangaObj o;
		String d = null;
		FileConnection fc = null;

		// path of folder where we will work
		final String item = list.getString(list.getSelectedIndex()) + "/";

		// reading metadata
		try {
			String fn = path + item + "model.json";
			fc = (FileConnection) Connector.open(fn, Connector.READ);
			if (fc.exists()) {
				DataInputStream s = fc.openDataInputStream();
				byte[] buf = new byte[(int) (fc.fileSize() + 1)];
				int len = s.read(buf);
				s.close();
				d = new String(buf, 0, len, "UTF-8");
			}
			fc.close();
			fc = null;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cfc(fc);
		}
		if (d == null) {
			NJTAIM.setScr(new Alert(item, "Metadata file is missed!", null, AlertType.ERROR));
			return;
		}

		// restoring ExtMangaObj from loaded data
		try {
			Hashtable h = JSONUtil.object(d.substring(1, d.length() - 1));
			String n = item.substring(0, item.indexOf('-')).trim();
			o = new ExtMangaObj(Integer.parseInt(n), h);
			d = null;
			h = null;
			Image cover = null;

			// cover loading
			if (NJTAI.loadCoverAtPage) {
				try {
					String fn = path + item + n + "_cover.jpg";
					fc = (FileConnection) Connector.open(fn, Connector.READ);
					InputStream s = null;
					if (fc.exists()) {
						s = fc.openInputStream();
						try {
							cover = Image.createImage(s);
						} catch (Throwable t) {
							t.printStackTrace();
							cover = null;
						}
						s.close();
					} else {
						fc.close();
						fc = (FileConnection) Connector.open(path + item + n + "_001.jpg", Connector.READ);
						if (fc.exists()) {
							s = fc.openInputStream();
							try {
								cover = Image.createImage(s);
							} catch (Throwable t) {
								t.printStackTrace();
								cover = null;
							}
							s.close();
						}
					}
					fc.close();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					cfc(fc);
				}

				if (cover != null) {
					cover = (Image) NJTAI.pl.prescaleCover(cover);
				}
			}

			// creating the screen
			mp = new MangaPage(Integer.parseInt(n), list, o, cover);
			if (cover == null && NJTAI.loadCoverAtPage) {
				Alert a1 = new Alert(item,
						NJTAI.rus ? "Ни одного изображения не скачано." : "No images are downloaded.", null,
						AlertType.WARNING);
				a1.setTimeout(Alert.FOREVER);
				NJTAIM.setScr(a1, mp);
			} else {
				NJTAIM.setScr(mp);
			}
		} catch (Throwable t) {
			t.printStackTrace();
			NJTAIM.setScr(new Alert(item,
					(NJTAI.rus ? "Файл метаданных повреждён: " : "Metadata file is corrupted: ") + t.toString(), null,
					AlertType.ERROR));
			return;
		}
	}

	private void cfc(FileConnection c) {
		try {
			if (c != null) c.close();
		} catch (Exception e) {
		}
	}
}
