package njtai.m;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Image;

import njtai.NJTAI;
import njtai.models.ExtMangaObj;

/**
 * Class, responsible for caching/reading images.
 * 
 * @author Feodor0090
 *
 */
public class MDownloader extends Thread implements CommandListener {
	private ExtMangaObj o;
	private Displayable prev;

	/**
	 * Creates a downloader.
	 * 
	 * @param o    Object to work with.
	 * @param prev Screen to work in.
	 */
	public MDownloader(ExtMangaObj o, Displayable prev) {
		this.o = o;
		this.prev = prev;
	}

	Command stopCmd = new Command(NJTAI.L_ACTS[18], Command.STOP, 1);
	boolean stop = false;

	static String dir;

	/**
	 * When true, the program will check files and overwrite them if they are
	 * broken.
	 */
	public boolean repair = false;

	private boolean done = false;

	private static Alert folderMissed() {
		return new Alert(NJTAI.L_ACTS[21], NJTAI.L_ACTS[22], null, AlertType.ERROR);
	}

	/**
	 * Caches an image.
	 * 
	 * @param a Data to write.
	 * @param i Number of the image, [1; pages].
	 */
	public synchronized void cache(ByteArrayOutputStream a, int i) {
		cache(a.toByteArray(), i);
	}

	/**
	 * Caches an image.
	 * 
	 * @param a Data to write.
	 * @param i Number of the image, [1; pages].
	 */
	public synchronized void cache(byte[] a, int i) {
		if (dir == null)
			dir = getWD();
		if (dir == null) {
			NJTAI.setScr(prev);
			NJTAI.pause(100);
			NJTAI.setScr(folderMissed(), prev);
			return;
		}

		FileConnection fc = null;

		String folder = getFolderName();

		DataOutputStream ou = null;

		try {
			String n;
			int j = i + 1;
			if (j < 10) {
				n = "00" + j;
			} else if (j < 100) {
				n = "0" + j;
			} else {
				n = "" + j;
			}
			fc = (FileConnection) Connector.open(folder + o.num + "_" + n + ".jpg");
			if (fc.exists()) {
				fc.close();
				return;
			}
			fc.create();
			ou = fc.openDataOutputStream();

			ou.write(a, 0, a.length);
			ou.flush();
			ou.close();
			fc.close();

		} catch (Exception e) {
			e.printStackTrace();
			try {
				if (ou != null)
					ou.close();
			} catch (IOException e1) {
			}
			try {
				if (fc != null)
					fc.close();
			} catch (IOException e1) {
			}
		}
	}

	/**
	 * Creates working folder.
	 */
	public void createFolder() {
		FileConnection fc = null;
		try {
			fc = (FileConnection) Connector.open(getFolderName(), Connector.WRITE);
			fc.mkdir();
		} catch (Exception e) {
		} finally {
			try {
				fc.close();
			} catch (Exception e) {}
		}
	}

	/**
	 * Reads cached image.
	 * 
	 * @param i Number of the image, [1; pages].
	 * @return Content of the file.
	 */
	public synchronized byte[] read(int i) {
		if (dir == null)
			dir = getWD();
		if (dir == null) {
			NJTAI.setScr(folderMissed(), prev);
			return null;
		}

		FileConnection fc = null;

		String folder = getFolderName();

		DataInputStream di = null;
		ByteArrayOutputStream b = new ByteArrayOutputStream();

		try {
			String n;
			int j = i + 1;
			if (j < 10) {
				n = "00" + j;
			} else if (j < 100) {
				n = "0" + j;
			} else {
				n = "" + j;
			}
			fc = (FileConnection) Connector.open(folder + o.num + "_" + n + ".jpg", Connector.READ);
			if (!fc.exists()) {
				return null;
			}
			di = fc.openDataInputStream();

			byte[] buf = new byte[1024 * 64];

			int len = 1;
			while ((len = di.read(buf)) != -1) {
				b.write(buf, 0, len);
			}
			di.close();
			fc.close();
			return b.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				if (di != null)
					di.close();
			} catch (IOException e1) {
			}
			try {
				if (fc != null)
					fc.close();
			} catch (IOException e1) {
			}
		}
		return null;
	}

	/**
	 * Gets the name of the folder, where current manga is stored.
	 * 
	 * @return Path to cache.
	 */
	public String getFolderName() {
		StringBuffer t = new StringBuffer();
		for (int i = 0; (i < o.title.length() && i < 24); i++) {
			char c = o.title.charAt(i);
			if (c == ' ' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '-'
					|| c == '_' || c == '!') {
				t.append(c);
			}
		}

		return dir + o.num + " - " + t.toString().trim() + "/";
	}

	public void run() {
		done = false;
		NJTAI.pause(500);
		Alert a = new Alert(o.title, NJTAI.L_ACTS[19], null, AlertType.INFO);
		a.setTimeout(Alert.FOREVER);
		a.addCommand(stopCmd);
		Gauge g = new Gauge(null, false, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING);
		a.setIndicator(g);
		a.setCommandListener(this);
		NJTAI.setScr(a, prev);
		NJTAI.pause(1000);
		if (stop)
			return;
		if (dir == null)
			dir = getWD();
		if (dir == null) {
			NJTAI.setScr(prev);
			NJTAI.pause(NJTAI.isJ2MEL() ? 200 : 100);
			Alert a1 = folderMissed();
			a1.setTimeout(-2);
			NJTAI.setScr(a1, prev);
			return;
		}
		g = new Gauge(null, false, 100, 0);
		a.setIndicator(g);
		a.setString("Downloading");

		FileConnection fc = null;

		long freeSpace = Long.MAX_VALUE;
		String folder = getFolderName();
		// folder
		try {
			fc = (FileConnection) Connector.open(folder, 3);
			if (!fc.exists()) {
				if (repair) {
					fc.close();
					NJTAI.setScr(prev);
					NJTAI.pause(NJTAI.isJ2MEL() ? 200 : 100);
					Alert a1 = new Alert(NJTAI.L_ACTS[21],
							NJTAI.rus ? "Кэш отсутствует в данной рабочей папке. Сначала скачайте."
									: "Cache is not present in current working folder. Download it first.",
							null, AlertType.ERROR);
					a1.setTimeout(-2);
					NJTAI.setScr(a1, prev);
					return;
				}
				fc.mkdir();
			}
			freeSpace = fc.availableSize();
		} catch (Exception e) {
		} finally {
			try {
				fc.close();
			} catch (IOException e) {
			}
		}

		if (freeSpace == -1)
			freeSpace = Long.MAX_VALUE;

		boolean filesExisted = false;
		boolean ioError = false;
		boolean outOfMem = false;

		// writing model
		Exception modelExc = writeModel(folder);

		byte[] buf = new byte[1024 * 64];
		String url = null;
		for (int i = (o.imgUrl == null ? 0 : -1); i < o.pages; i++) {
			int percs = Math.max(0, i * 100 / o.pages);
			url = null;

			DataOutputStream ou = null;
			HttpConnection httpCon = null;
			InputStream ins = null;

			try {
				String n;
				int j = i + 1;
				if (j < 10) {
					n = "00" + j;
				} else if (j < 100) {
					n = "0" + j;
				} else {
					n = "" + j;
				}

				a.setString("Checking " + percs + "%");
				String fn = folder + o.num + "_" + (i == -1 ? "cover" : n) + ".jpg";

				System.out.println("Writing a page to " + fn);
				fc = (FileConnection) Connector.open(fn);

				// If the file exists, we may want to check it or just skip
				if (fc.exists()) {
					if (fc.fileSize() == 0) {
						// just overwrite
					} else if (repair) {
						System.out.println("Attempt to check");
						// attempt to decode
						InputStream dis = null;
						try {
							dis = fc.openInputStream();
							try {
								Image.createImage(dis);
								dis.close();
								dis = null;
								// Skipping
								fc.close();
								g.setValue(percs);
								continue;
							} catch (Exception e) {
								// failed
								System.out.println("Attempt to repair");
								if (dis != null)
									dis.close();
								fc.truncate(0);
							}
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							if (dis != null)
								dis.close();
						}
					} else {
						// skipping
						System.out.println("File exists, skipping...");
						fc.close();
						filesExisted = true;
						g.setValue(percs);
						continue;
					}
				} else {
					// There is no file? Creating.
					fc.create();
				}

				a.setString("Fetching " + percs + "%");
				try {
					url = i < 0 ? o.imgUrl : o.loadUrl(i + 1);
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
					NJTAI.setScr(prev);
					fc.close();
					return;
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (url == null) {
					fc.close();
					NJTAI.setScr(prev);
					NJTAI.pause(NJTAI.isJ2MEL() ? 200 : 100);
					NJTAI.setScr(new Alert(NJTAI.L_ACTS[21], "Failed to get image's url.", null,
							AlertType.ERROR), prev);
					return;
				}

				a.setString("Downloading " + percs + "%");
				url = NJTAI.proxyUrl(url);
				System.out.println("Loading from " + url);

				httpCon = (HttpConnection) Connector.open(url);
				httpCon.setRequestMethod("GET");
				int code = httpCon.getResponseCode();
				System.out.println("Code " + code);

				long dataLen = httpCon.getLength();
				if (dataLen > 0) {
					freeSpace = fc.availableSize();
					if (freeSpace < (dataLen * 2) || freeSpace < 1024 * 1024) {
						fc.delete();
						fc.close();
						outOfMem = true;
						break;
					}

					freeSpace -= dataLen;
				}

				ins = httpCon.openInputStream();
				ou = fc.openDataOutputStream();
				

				int len = 1;
				if (ins == null) {
					throw new RuntimeException("Input stream is lost");
				}
				while ((len = ins.read(buf)) != -1) {
					ou.write(buf, 0, len);
					ou.flush();
				}
				ou.close();
				httpCon.close();
				fc.close();

			} catch (Exception e) {
				e.printStackTrace();
				ioError = true;
				try {
					if (ou != null)
						ou.close();
				} catch (IOException e1) {
				}
				try {
					if (fc != null)
						fc.close();
				} catch (IOException e1) {
				}
				try {
					if (httpCon != null)
						httpCon.close();
				} catch (IOException e1) {
				}
			}
			g.setValue(percs);
			if (stop)
				return;
		}

		if (NJTAI.isJ2MEL()) {
			g.setValue(100);
			done = true;
			if (ioError) {
				a.setString("IO error has occurred. Check, are all the files valid.");
			} else if (modelExc != null) {
				a.setString("Model was not writed due to " + modelExc.toString());
			} else if (outOfMem) {
				a.setString("Downloading was not finished - not enough space on the disk.");
			} else if (filesExisted && !repair) {
				a.setString("Some files existed - they were not overwritten.");
			} else {
				a.setString(NJTAI.L_ACTS[repair ? 23 : 24]);
			}
		} else {
			NJTAI.setScr(prev);
			NJTAI.pause(100);
			try {
				Alert b;
				if (ioError) {
					b = new Alert("NJTAI", "IO error has occurred. Check, are all the files valid.", null,
							AlertType.ERROR);
				} else if (modelExc != null) {
					b = new Alert("NJTAI", "Model was not writed due to " + modelExc.toString(), null,
							AlertType.WARNING);
				} else if (outOfMem) {
					b = new Alert("NJTAI", "Downloading was not finished - not enough space on the disk.", null,
							AlertType.WARNING);
				} else if (filesExisted && !repair) {
					b = new Alert("NJTAI", "Some files existed - they were not overwritten.", null, AlertType.WARNING);
				} else {
					b = new Alert("NJTAI", NJTAI.L_ACTS[repair ? 23 : 24], null, AlertType.CONFIRMATION);
				}
				NJTAI.setScr(b, prev);
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Writes model's dump to FS.
	 * @param folder Folder to place.
	 * @return Error, if occurred.
	 */
	public Exception writeModel(String folder) {
		FileConnection fc = null;
		Exception ex = null;
		try {
			String fn = folder + "model.json";
			fc = (FileConnection) Connector.open(fn, Connector.WRITE);
			try {
				fc.create();
			} catch (IOException ioe) {
				fc.truncate(0);
			}
			DataOutputStream s = fc.openDataOutputStream();
			s.write(o.encode().getBytes("UTF-8"));
			s.close();
		} catch (Exception e) {
			e.printStackTrace();
			ex = e;
		} finally {
			try {
				if (fc != null)
					fc.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return ex;
	}

	private static boolean pathExists(String p) {
		FileConnection fc = null;
		try {
			fc = (FileConnection) Connector.open(p, Connector.READ);
			if (!fc.exists())
				throw new RuntimeException();
			fc.close();
			return true;
		} catch (Throwable t) {
			try {
				if (fc != null)
					fc.close();
			} catch (IOException e) {
			}
		}
		return false;
	}

	/**
	 * Finds available folders to work in.
	 * 
	 * @param quick False to list all the locations, true to find the first.
	 * @return List of available WDs.
	 */
	public static String[] getWDs(boolean quick) {

		if (WDs != null)
			return WDs;

		String[] all;
		if (NJTAI.isJ2MEL()) {
			all = new String[] { "file:///c:/NJTAI/", "file:///c:/" };
		} else {
			all = new String[] { "file:///E:/NJTAI/", "file:///E:/Images/", "file:///E:/Data/Images/",
					"file:///F:/Images/", "file:///F:/Data/Images/", "file:///E:/", "file:///F:/", "file:///C:/Images/",
					System.getProperty("fileconn.dir.photos"), "file:///e:/", "file:///c:/", "file:///root/" };
		}

		Vector v = new Vector();

		for (int i = 0; i < all.length; i++) {
			if (pathExists(all[i])) {
				if (quick) {
					currentWD = all[i];
					return new String[] { all[i] };
				}
				v.addElement(all[i]);
			}
		}
		WDs = new String[v.size()];
		v.copyInto(WDs);
		v = null;
		currentWD = WDs.length == 0 ? null : WDs[0];
		return WDs;
	}

	private static String[] WDs = null;

	/**
	 * Folder, currently used for cache.
	 */
	public static String currentWD = null;

	/**
	 * Gets WD path.
	 * 
	 * @return WD path.
	 */
	public static String getWD() {
		if (currentWD != null)
			return currentWD;
		getWDs(true);
		return currentWD;
	}

	/**
	 * Sets working directory to file:///E:/NJTAI/ and creates it.
	 * 
	 * @param scr Screen from what this call was made.
	 */
	public static void useE_NJTAI() {
		FileConnection fc = null;
		try {
			String d = "file:///E:/NJTAI/";
			fc = (FileConnection) Connector.open(d);
			if (!fc.exists())
				fc.mkdir();
			fc.close();
			currentWD = d;
			if (NJTAI.wdBtn != null)
				NJTAI.wdBtn.setText(d);
		} catch (Throwable t) {
			if (NJTAI.wdBtn != null)
				NJTAI.wdBtn.setText("Error. Try to reselect.");
			try {
				if (fc != null)
					fc.close();
			} catch (IOException e) {
			}
		}

	}

	public void commandAction(Command c, Displayable arg1) {
		if (c == stopCmd) {
			stop = true;

			NJTAI.setScr(prev);
			if (!done) {
				NJTAI.pause(100);
				try {
					NJTAI.setScr(new Alert(NJTAI.L_ACTS[21], "Downloading was canceled.", null,
							AlertType.ERROR), prev);
				} catch (Exception e) {
				}
			}
		}
	}
}
