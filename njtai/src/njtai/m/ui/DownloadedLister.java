package njtai.m.ui;

import java.io.*;
import java.util.*;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.*;

import njtai.NJTAI;
import njtai.m.MangaDownloader;
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
public class DownloadedLister extends Thread implements CommandListener {

	private List list;
	private MMenu mm;
	private String path;

	private final Command delC = new Command(NJTAI.rus ? "Удалить" : "Delete", Command.SCREEN, 3);
	private final Command repairC = new Command(NJTAI.rus ? "Восстановить" : "Repair", Command.SCREEN, 2);
	private final Command switchC = new Command(NJTAI.rus ? "Другая папка" : "Another folder", Command.SCREEN, 4);

	private final Command cancelDelC = new Command(NJTAI.rus ? "Отмена" : "Cancel", Command.CANCEL, 1);
	private final Command confirmDelC = new Command(NJTAI.rus ? "Продолжить" : "Continue", Command.OK, 1);

	public DownloadedLister(List l, MMenu b) {
		list = l;
		mm = b;
	}

	public void run() {
		path = MangaDownloader.getWD();
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
				if (fc != null)
					fc.close();
			} catch (Exception exx) {
				exx.printStackTrace();
			}
		}

		if (e != null) {
			while (e.hasMoreElements()) {
				String s = e.nextElement().toString();
				try {
					if (s.length() < 4)
						continue;
					// trying to parse folder's name
					if (s.charAt(s.length() - 1) != '/')
						continue;
					int i = s.indexOf('-');
					if (i == -1)
						continue;
					String n = s.substring(0, i).trim();
					Integer.parseInt(n);

					// push
					list.append(s.substring(0, s.length() - 1), null);
				} catch (Exception ex) {
					// skipping
				}
			}
		}
		list.setTitle(path);
		list.addCommand(MMenu.backCmd);
		list.addCommand(delC);
		list.addCommand(repairC);
		list.addCommand(switchC);
		list.setCommandListener(this);
	}

	public void commandAction(Command c, Displayable arg1) {
		if (c == MMenu.backCmd) {
			NJTAIM.setScr(mm);
			return;
		}
		if (c == switchC) {
			MangaDownloader.reselectWD(mm);
			return;
		}
		if (c == repairC) {
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
			MangaDownloader md = new MangaDownloader(emo, list);
			md.repair = true;
			md.start();
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
			// path of folder where we will work
			final String item = list.getString(list.getSelectedIndex()) + "/";

			String n = item.substring(0, item.indexOf('-')).trim();

			FileConnection fc = null;

			// model
			try {
				String fn = path + item + "model.json";
				fc = (FileConnection) Connector.open(fn);
				if (fc.exists()) {
					fc.delete();
				}
				fc.close();
				fc = null;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (fc != null)
						fc.close();
				} catch (IOException e1) {
				}
			}

			Enumeration e = null;

			try {
				fc = (FileConnection) Connector.open(path + item, Connector.READ);
				e = fc.list();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				try {
					if (fc != null)
						fc.close();
				} catch (Exception exx) {
					exx.printStackTrace();
				}
			}

			if (e == null)
				return;
			int max = 0;

			while (e.hasMoreElements()) {
				String name = e.nextElement().toString();
				try {
					String l = name.substring(name.indexOf('_'));
					l = l.substring(0, l.indexOf('.'));
					int m = Integer.parseInt(l);
					if (m > max)
						max = m;
				} catch (RuntimeException ex) {
				}
			}

			for (int i = 1; i <= max; i++) {
				try {
					String fn = path + item + n + "_" + i + ".jpg";
					fc = (FileConnection) Connector.open(fn);
					if (fc.exists()) {
						fc.delete();
					}
					fc.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				} finally {
					try {
						if (fc != null)
							fc.close();
					} catch (IOException exx) {
						exx.printStackTrace();
					}
				}
			}
			
			try {
				fc = (FileConnection) Connector.open(path+item);
				fc.delete();
				fc.close();
				Alert a = new Alert("NJTAI", NJTAI.rus ? "Удалено." : "Deleted.", null,
						AlertType.CONFIRMATION);
				a.setTimeout(Alert.FOREVER);
				NJTAIM.setScr(a, list);
			} catch (Exception ex) {
				ex.printStackTrace();
				Alert a = new Alert("NJTAI", NJTAI.rus ? "В папке остались посторонние файлы." : "There are third files in the folder.", null,
						AlertType.WARNING);
				a.setTimeout(Alert.FOREVER);
				NJTAIM.setScr(a, list);
			} finally {
				try {
					if (fc != null)
						fc.close();
				} catch (Exception exx) {
					exx.printStackTrace();
				}
			}
			

			return;
		}
		if (c == List.SELECT_COMMAND) {

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
					d = new String(buf, 0, len, "UTF-8");
					s.close();
				}
				fc.close();
				fc = null;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (fc != null)
						fc.close();
				} catch (IOException e1) {
				}
			}
			if (d == null) {
				NJTAIM.setScr(new Alert(item, "Metadata file is missed!", null, AlertType.ERROR));
				return;
			}

			// restoring ExtMangaObj from loaded data
			try {
				Hashtable h = (Hashtable) cc.nnproject.lwjson.JSON.parseJSON(d);
				String n = item.substring(0, item.indexOf('-')).trim();
				o = new ExtMangaObj(Integer.parseInt(n), h);
				h = null;
				Image cover = null;

				// cover loading
				if (NJTAI.loadCoverAtPage) {
					int len = 0;
					byte[] buf = null;
					try {
						String fn = path + item + n + "_001.jpg";
						fc = (FileConnection) Connector.open(fn, Connector.READ);
						if (fc.exists()) {
							DataInputStream s = fc.openDataInputStream();
							buf = new byte[(int) (fc.fileSize() + 1)];
							len = s.read(buf);
							s.close();
						}
						fc.close();
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						try {
							if (fc != null)
								fc.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					if (len == 0 || buf == null) {
						cover = null;
					} else {
						cover = Image.createImage(buf, 0, len);
						buf = null;
						System.gc();
						cover = (Image) NJTAI.pl.prescaleCover(cover);
					}
				}

				// creating the screen
				mp = new MangaPage(Integer.parseInt(n), list, o, cover);
				if (cover == null) {
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
						(NJTAI.rus ? "Файл метаданных повреждён: " : "Metadata file is corrupted: ") + t.toString(),
						null, AlertType.ERROR));
				return;
			}
		}
	}
}
