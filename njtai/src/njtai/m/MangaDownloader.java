package njtai.m;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
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
import javax.microedition.lcdui.Image;

import njtai.NJTAI;
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

	static String dir;

	/**
	 * When true, the program will check files and overwrite them if they are
	 * broken.
	 */
	public boolean repair = false;

	private boolean done = false;

	public synchronized void cache(ByteArrayOutputStream a, int i) {
		if (dir == null)
			dir = checkBasePath();
		if (dir == null) {
			NJTAIM.setScr(prev);
			NJTAI.pause(100);
			NJTAIM.setScr(new Alert("Downloader error",
					"There is no folder where we can write data. Try to manually create a folder on C:/Data/Images/ path.",
					null, AlertType.ERROR));
			return;
		}

		FileConnection fc = null;

		String folder = getFolderName();
		// folder
		try {
			fc = (FileConnection) Connector.open(folder, 3);
			if (!fc.exists())
				fc.mkdir();
		} catch (Exception e) {
		} finally {
			try {
				fc.close();
			} catch (IOException e) {
			}
		}

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
			byte[] buf = a.toByteArray();

			ou.write(buf, 0, buf.length);
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

	public synchronized ByteArrayOutputStream read(int i) {
		if (dir == null)
			dir = checkBasePath();
		if (dir == null) {
			NJTAI.pl.showNotification("Downloader error",
					"There is no folder where we can write data. Try to manually create a folder on C:/Data/Images/ path.",
					3, prev);
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
			fc = (FileConnection) Connector.open(folder + o.num + "_" + n + ".jpg");
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
			return b;
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
		Alert a = new Alert(o.title, "Looking for the folder", null, AlertType.INFO);
		a.setTimeout(Alert.FOREVER);
		a.addCommand(stopCmd);
		Gauge g = new Gauge(null, false, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING);
		a.setIndicator(g);
		a.setCommandListener(this);
		NJTAIM.setScr(a);
		NJTAI.pause(1000);
		if (stop)
			return;
		if (dir == null)
			dir = checkBasePath();
		if (dir == null) {
			NJTAIM.setScr(prev);
			NJTAI.pause(NJTAIM.isJ2MEL() ? 200 : 100);
			NJTAIM.setScr(new Alert("Downloader error",
					"There is no folder where we can write data. Try to manually create a folder on C:/Data/Images/ path.",
					null, AlertType.ERROR), prev);
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
					NJTAIM.setScr(prev);
					NJTAI.pause(NJTAIM.isJ2MEL() ? 200 : 100);
					NJTAIM.setScr(new Alert("Downloader error",
							"Cache is not present in current working folder. Download it first.", null,
							AlertType.ERROR), prev);
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
		Exception modelExc = null;

		// writing model
		try {
			String fn = folder + "model.json";
			fc = (FileConnection) Connector.open(fn);
			if (fc.exists())
				fc.truncate(0);
			else
				fc.create();
			DataOutputStream s = fc.openDataOutputStream();
			s.write(o.encode().getBytes("UTF-8"));
			s.close();
		} catch (Exception e) {
			e.printStackTrace();
			modelExc = e;
		} finally {
			try {
				if (fc != null)
					fc.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		for (int i = 0; i < o.pages; i++) {
			int percs = i * 100 / o.pages;
			String url = null;

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
				String fn = folder + o.num + "_" + n + ".jpg";

				System.out.println("Writing a page to " + fn);
				fc = (FileConnection) Connector.open(fn);

				// If the file exists, we may want to check it or just skip
				if (fc.exists()) {
					if (fc.fileSize() == 0) {
						// just overwrite
					} else if (repair) {
						System.out.println("Attempt to check");
						// attempt to decode
						DataInputStream dis = null;
						try {
							dis = fc.openDataInputStream();
							ByteArrayOutputStream b = new ByteArrayOutputStream();
							int l = 0;
							byte[] buf = new byte[1024 * 64];
							while ((l = dis.read(buf)) != -1) {
								b.write(buf, 0, l);
							}
							dis.close();
							try {
								Image.createImage(b.toByteArray(), 0, b.size());
								// Skipping
								fc.close();
								continue;
							} catch (Exception e) {
								// failed
								System.out.println("Attempt to repair");
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
					url = o.loadUrl(i + 1);
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
					NJTAIM.setScr(prev);
					fc.close();
					return;
				}
				if (url == null) {
					fc.close();
					NJTAIM.setScr(prev);
					NJTAI.pause(NJTAIM.isJ2MEL() ? 200 : 100);
					NJTAIM.setScr(new Alert("Downloader error", "Failed to get image's url.", null, AlertType.ERROR),
							prev);
					return;
				}

				a.setString("Downloading " + percs + "%");

				if (url.startsWith("https://"))
					url = url.substring(8);
				if (url.startsWith("http://"))
					url = url.substring(7);
				url = NJTAI.proxy + url;
				System.out.println("Loading from " + url);

				httpCon = (HttpConnection) Connector.open(url);
				httpCon.setRequestMethod("GET");
				int code = httpCon.getResponseCode();
				System.out.println("Code " + code);

				long dataLen = httpCon.getLength();
				if (dataLen > 0) {
					if (freeSpace < (dataLen * 4)) {
						fc.delete();
						fc.close();
						outOfMem = true;
						break;
					} else {
						freeSpace -= dataLen;
					}
				}

				ins = httpCon.openInputStream();
				ou = fc.openDataOutputStream();
				byte[] buf = new byte[1024 * 64];

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

		if (NJTAIM.isJ2MEL()) {
			g.setValue(100);
			done = true;
			if (ioError) {
				a.setString("IO error has occurped. Check, are all the files valid.");
			} else if (modelExc != null) {
				a.setString("Model was not writed due to " + modelExc.toString());
			} else if (outOfMem) {
				a.setString("Downloading was not finished - not enough space on the disk.");
			} else if (filesExisted && !repair) {
				a.setString("Some files existed - they were not overwritten.");
			} else {
				a.setString(repair ? "All pages were checked and repaired." : "All pages were downloaded.");
			}
		} else {
			NJTAIM.setScr(prev);
			NJTAI.pause(100);
			try {
				Alert b;
				if (ioError) {
					b = new Alert("NJTAI", "IO error has occurped. Check, are all the files valid.", null,
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
					b = new Alert("NJTAI",
							repair ? "All pages were checked and repaired." : "All pages were downloaded.", null,
							AlertType.CONFIRMATION);
				}
				NJTAIM.setScr(b, prev);
			} catch (Exception e) {
			}
		}
	}

	public static String checkBasePath() {
		// avoid folders lookups in J2MEL
		if (NJTAIM.isJ2MEL()) {
			return "file:///c:/";
		}
		try {
			FileConnection fc = null;
			try {
				String dir = "file:///E:/Images/";
				fc = (FileConnection) Connector.open(dir, Connector.READ);
				if (!fc.exists())
					throw new RuntimeException();
				fc.close();
				return dir;
			} catch (Throwable t) {
				if (fc != null)
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
				if (fc != null)
					fc.close();
			}
			try {
				String dir = "file:///F:/Images/";
				fc = (FileConnection) Connector.open(dir, Connector.READ);
				if (!fc.exists())
					throw new RuntimeException();
				fc.close();
				return dir;
			} catch (Throwable t) {
				if (fc != null)
					fc.close();
			}
			try {
				String dir = "file:///F:/Data/Images/";
				fc = (FileConnection) Connector.open(dir, Connector.READ);
				if (!fc.exists())
					throw new RuntimeException();
				fc.close();
				return dir;
			} catch (Throwable t) {
				if (fc != null)
					fc.close();
			}
			try {
				String dir = "file:///E:/";
				fc = (FileConnection) Connector.open(dir, Connector.READ);
				if (!fc.exists())
					throw new RuntimeException();
				fc.close();
				return dir;
			} catch (Throwable t) {
				if (fc != null)
					fc.close();
			}
			try {
				String dir = "file:///F:/";
				fc = (FileConnection) Connector.open(dir, Connector.READ);
				if (!fc.exists())
					throw new RuntimeException();
				fc.close();
				return dir;
			} catch (Throwable t) {
				if (fc != null)
					fc.close();
			}
			try {
				String dir = System.getProperty("fileconn.dir.photos");
				fc = (FileConnection) Connector.open(dir, Connector.READ);
				if (!fc.exists())
					throw new RuntimeException();
				fc.close();
				return dir;
			} catch (Throwable t) {
				if (fc != null)
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
				if (fc != null)
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
				if (fc != null)
					fc.close();
			}
			try {
				String dir = "file:///C:/";
				fc = (FileConnection) Connector.open(dir, Connector.READ);
				if (!fc.exists())
					throw new RuntimeException();
				fc.close();
				return dir;
			} catch (Throwable t) {
				if (fc != null)
					fc.close();
			}
			try {
				String dir = "file:///c:/";
				fc = (FileConnection) Connector.open(dir, Connector.READ);
				if (!fc.exists())
					throw new RuntimeException();
				fc.close();
				return dir;
			} catch (Throwable t) {
				if (fc != null)
					fc.close();
			}
			try {
				String dir = "file:///root/";
				fc = (FileConnection) Connector.open(dir, Connector.READ);
				if (!fc.exists())
					throw new RuntimeException();
				fc.close();
				return dir;
			} catch (Throwable t) {
				if (fc != null)
					fc.close();
			}
		} catch (Exception e) {
		}
		return null;
	}

	public void commandAction(Command c, Displayable arg1) {
		if (c == stopCmd) {
			stop = true;

			NJTAIM.setScr(prev);
			if (!done) {
				NJTAI.pause(100);
				try {
					NJTAIM.setScr(new Alert("Downloader error", "Downloading was canceled.", null, AlertType.ERROR));
				} catch (Exception e) {
				}
			}
		}
	}
}
