package njtai;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Gauge;

import njtai.models.ExtMangaObj;

public class MangaDownloader extends Thread implements CommandListener {
	private ExtMangaObj o;
	private Displayable prev;

	public MangaDownloader(ExtMangaObj o, Displayable prev) {
		this.o = o;
		this.prev = prev;
	}

	Command stopCmd = new Command("Cancel", Command.STOP, 1);
	boolean stop = false;

	public void run() {
		Alert a = new Alert(o.title, "Looking for the folder", null, AlertType.INFO);
		a.setTimeout(Alert.FOREVER);
		a.addCommand(stopCmd);
		Gauge g = new Gauge(null, false, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING);
		a.setIndicator(g);
		a.setCommandListener(this);
		NJTAI.setScr(a);
		NJTAI.pause(1000);
		if (stop)
			return;
		String dir = checkBasePath();
		if (dir == null) {
			NJTAI.setScr(prev);
			NJTAI.pause(100);
			NJTAI.setScr(new Alert("Downloader error",
					"There is no folder where we can write data. Try to manually create a folder on C:/Data/Images/ path.",
					null, AlertType.ERROR));
		}
		g = new Gauge(null, false, 100, 0);
		a.setIndicator(g);
		a.setString("Downloading");
		for (int i = 0; i < o.pages; i++) {
			String url = o.loadUrl(i + 1);
			FileConnection fc;
			DataOutputStream ou = null;
			HttpConnection hc = null;
			InputStream in = null;
			try {
				fc = (FileConnection) Connector.open(dir + o.num + "/", 3);
				if (!fc.exists())
					fc.mkdir();
				fc.close();
				fc = (FileConnection) Connector.open(dir + o.num + "/" + o.num + "_" + (i + 1) + ".jpg", 3);
				if (fc.exists()) {

				}
				fc.create();
				hc = (HttpConnection) Connector.open(url);
				hc.setRequestMethod("GET");

				in = hc.openInputStream();
				byte[] b = new byte[16384];
				ou = fc.openDataOutputStream();

				int c;
				while ((c = in.read(b)) != -1) {
					// var10 += (long) var7;
					ou.write(b, 0, c);
					ou.flush();
				}
			} catch (NullPointerException e) {
			} catch (IOException e) {
			} finally {
				try {
					if (in != null)
						in.close();
				} catch (IOException e) {
				}
				try {
					if (hc != null)
						hc.close();
				} catch (IOException e) {
				}
				try {
					if (ou != null)
						ou.close();
				} catch (IOException e) {
				}
			}
			g.setValue(i * 100 / o.pages);
			if (stop)
				return;
		}

		NJTAI.setScr(prev);
		NJTAI.pause(100);
		try {
			NJTAI.setScr(new Alert("NJTAI", "All pages were downloaded.", null, AlertType.CONFIRMATION));
		} catch (Exception e) {
		}
	}

	public static String checkBasePath() {
		try {
			FileConnection fc = null;
			try {
				String dir = System.getProperty("fileconn.dir.photos");
				fc = (FileConnection) Connector.open(dir, Connector.READ);
				if (!fc.exists())
					throw new RuntimeException();
				fc.close();
				return dir;
			} catch (Throwable t) {
				fc.close();
			}
			try {
				String dir = "file:///E:/Images/";
				fc = (FileConnection) Connector.open(dir, Connector.READ);
				if (!fc.exists())
					throw new RuntimeException();
				fc.close();
				return dir;
			} catch (Throwable t) {
				fc.close();
			}
			try {
				String dir = "file:///C:/Images/";
				fc = (FileConnection) Connector.open(dir, Connector.READ);
				if (!fc.exists())
					throw new RuntimeException();
				fc.close();
				return dir;
			} catch (Throwable t) {
				fc.close();
			}
			try {
				String dir = "file:///E:/Data/Images/";
				fc = (FileConnection) Connector.open(dir, Connector.READ);
				if (!fc.exists())
					throw new RuntimeException();
				fc.close();
				return dir;
			} catch (Throwable t) {
				fc.close();
			}
			try {
				String dir = "file:///C:/Data/Images/";
				fc = (FileConnection) Connector.open(dir, Connector.READ);
				if (!fc.exists())
					throw new RuntimeException();
				fc.close();
				return dir;
			} catch (Throwable t) {
				fc.close();
			}
		} catch (IOException e) {
		}
		return null;
	}

	public void commandAction(Command c, Displayable arg1) {
		if (c == stopCmd) {
			stop = true;

			NJTAI.setScr(prev);
			NJTAI.pause(100);
			try {
				NJTAI.setScr(new Alert("Downloader error", "Downloading was canceled.", null, AlertType.ERROR));
			} catch (Exception e) {
			}
		}
	}
}
