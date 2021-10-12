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
					if(s.length()<4) continue;
					// trying to parse folder's name
					if (s.charAt(s.length() - 1) != '/')
						continue;
					int i = s.indexOf('-');
					if (i == -1)
						continue;
					String n = s.substring(0, i).trim();
					Integer.parseInt(n);

					// push
					list.append(s.substring(0, s.length()-1), null);
				} catch (Exception ex) {
					// skipping
				}
			}
		}
		list.setTitle(path);
		list.addCommand(MMenu.backCmd);
		list.setCommandListener(this);
	}

	public void commandAction(Command c, Displayable arg1) {
		if (c == MMenu.backCmd) {
			NJTAIM.setScr(mm);
			return;
		}
		if (c == List.SELECT_COMMAND) {

			// vars
			MangaPage mp;
			ExtMangaObj o;
			String d = null;
			FileConnection fc = null;

			// path of folder where we will work
			final String item = list.getString(list.getSelectedIndex())+"/";

			// reading metadata
			try {
				String fn = path + item + "model.json";
				fc = (FileConnection) Connector.open(fn, Connector.READ);
				if (fc.exists()) {
					DataInputStream s = fc.openDataInputStream();
					byte[] buf = new byte[32 * 1024];
					int len = s.read(buf);
					d = new String(buf, 0, len, "UTF-8");
					s.close();
				}
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
					byte[] buf = new byte[512 * 1024];
					try {
						String fn = path + item + n + "_001.jpg";
						fc = (FileConnection) Connector.open(fn, Connector.READ);
						if (fc.exists()) {
							DataInputStream s = fc.openDataInputStream();
							len = s.read(buf);
							s.close();
						}
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
					cover = Image.createImage(buf, 0, len);
					buf = null;
					System.gc();
					cover = (Image) NJTAI.pl.prescaleCover(cover);
				}

				// creating the screen
				mp = new MangaPage(Integer.parseInt(n), list, o, cover);
				NJTAIM.setScr(mp);
			} catch (Throwable t) {
				t.printStackTrace();
				NJTAIM.setScr(new Alert(item, "Metadata file is corrupted", null, AlertType.ERROR));
				return;
			}
		}
	}
}
