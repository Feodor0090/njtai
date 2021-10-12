package njtai.m.ui;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;

import cc.nnproject.lwjson.JSON;
import njtai.NJTAI;
import njtai.m.MangaDownloader;
import njtai.m.NJTAIM;
import njtai.models.ExtMangaObj;

public class DownloadedLister extends Thread implements CommandListener {

	private List l;
	private MMenu b;
	private String path;

	public DownloadedLister(List l, MMenu b) {
		this.l = l;
		this.b = b;
	}

	public void run() {
		path = MangaDownloader.checkBasePath();
		Enumeration e = null;
		FileConnection fc = null;
		try {
			fc = (FileConnection) Connector.open(path);
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
		if (e != null)
			while (e.hasMoreElements()) {
				String s = e.nextElement().toString();
				try {
					if (s.charAt(s.length() - 1) != '/')
						continue;
					int i = s.indexOf('-');
					if (i == -1)
						continue;
					String n = s.substring(0, i).trim();
					Integer.parseInt(n);
					l.append(s, null);
				} catch (Exception ex) {
				}
			}
		l.setTitle(path);
		l.addCommand(MMenu.backCmd);
		l.setCommandListener(this);
	}

	public void commandAction(Command c, Displayable arg1) {
		if (c == MMenu.backCmd) {
			NJTAIM.setScr(b);
			return;
		}
		if (c == List.SELECT_COMMAND) {
			MangaPage mp;
			ExtMangaObj o;
			String d = null;
			FileConnection fc = null;
			final String item = l.getString(l.getSelectedIndex());
			try {
				String fn = path + item + "model.json";
				fc = (FileConnection) Connector.open(fn);
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

			try {
				Hashtable h = (Hashtable) JSON.parseJSON(d);
				String n = item.substring(0, item.indexOf('-')).trim();
				o = new ExtMangaObj(Integer.parseInt(n), h);
				h = null;
				Image cover = null;
				if (NJTAI.loadCoverAtPage) {
					int len = 0;
					byte[] buf = new byte[512 * 1024];
					try {
						String fn = path + item + n + "_001.jpg";
						fc = (FileConnection) Connector.open(fn);
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
				mp = new MangaPage(Integer.parseInt(n), l, o, cover);
			} catch (Throwable t) {
				t.printStackTrace();
				NJTAIM.setScr(new Alert(item, "Metadata file is corrupted", null, AlertType.ERROR));
				return;
			}

			NJTAIM.setScr(mp);
		}
	}
}
