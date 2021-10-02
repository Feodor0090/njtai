package njtai.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import njtai.MangaDownloader;
import njtai.NJTAI;
import njtai.models.ExtMangaObj;

/**
 * This class contains methods to work with data to show images.
 * 
 * @author Feodor0090
 *
 */
public abstract class ViewBase extends Canvas implements Runnable {

	protected ExtMangaObj emo;
	protected Displayable prev;
	/**
	 * Number of page from zero.
	 */
	protected int page;

	protected ByteArrayOutputStream[] cache;
	protected MangaDownloader fs;

	protected int zoom = 1;
	protected int x = 0;
	protected int y = 0;

	protected Thread loader;
	protected Thread preloader;
	protected boolean error;

	int nokiaRam;

	public ViewBase(ExtMangaObj emo, Displayable prev, int page) {
		this.emo = emo;
		this.prev = prev;
		this.page = page;
		nokiaRam = (System.getProperty("microedition.platform").indexOf("sw_platform_version=5.") == -1)
				? (15 * 1024 * 1024)
				: (40 * 1024 * 1024);
		NJTAI.clearHP();
		if (NJTAI.files)
			fs = new MangaDownloader(emo, this);
		reload();
		setFullScreenMode(true);
	}

	/**
	 * Loads an image and saves it.
	 * 
	 * @param n Number of image (not page!) [0; emo.pages)
	 * @return Data of loaded image.
	 * @throws InterruptedException
	 */
	protected ByteArrayOutputStream getImage(int n) throws InterruptedException {
		return getImage(n, false);
	}

	/**
	 * Loads an image, optionally ignoring the cache.
	 * 
	 * @param n Number of image (not page!) [0; emo.pages)
	 * @return Data of loaded image.
	 * @throws InterruptedException
	 */
	protected ByteArrayOutputStream getImage(int n, boolean forceCacheIgnore) throws InterruptedException {
		if (NJTAI.files && !forceCacheIgnore) {
			ByteArrayOutputStream a = fs.read(n);
			if (a != null)
				return a;
			byte[] b = emo.getPage(n);
			try {
				if (b == null) {
					error = true;
					repaint();
					return null;
				}
				a = new ByteArrayOutputStream(b.length);

				a.write(b);
				fs.cache(a, n);
				return a;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			try {
				if (cache == null)
					cache = new ByteArrayOutputStream[emo.pages];

				if (forceCacheIgnore)
					cache[n] = null;
				else if (cache[n] != null)
					return cache[n];

				synchronized (cache) {
					byte[] b = emo.getPage(n);
					try {
						if (b == null) {
							error = true;
							repaint();
							return null;
						}
						ByteArrayOutputStream s = new ByteArrayOutputStream(b.length);

						s.write(b);

						cache[n] = s;
						return s;
					} catch (IOException e) {
						e.printStackTrace();
						return null;
					}
				}
			} catch (RuntimeException e) {
				return null;
			}
		}
	}

	/**
	 * Releases some images to prevent OOM errors.
	 */
	protected synchronized void emergencyCacheClear() {
		try {
			if (NJTAI.files) {
				cache = null;
				return;
			}
			if (cache == null)
				return;
			for (int i = 0; i < page - 1; i++) {
				cache[i] = null;
			}
			for (int i = emo.pages - 1; i > page; i--) {
				if (cache[i] != null) {
					cache[i] = null;
					break;
				}
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Tries to guess, how many pages can be preloaded before problems with free
	 * memory.
	 * 
	 * @return Aproximatry count of pages that is safe to load.
	 */
	protected int canStorePages() {
		if (NJTAI.files) {
			return 999;
		}
		int f = (int) Runtime.getRuntime().freeMemory();
		int free = 0;
		try {
			String nokiaMem = System.getProperty("com.nokia.memoryramfree");
			free = (int) Math.min(Integer.parseInt(nokiaMem), nokiaRam - (Runtime.getRuntime().totalMemory() - f));
		} catch (Throwable t) {
			free = f;
		}
		free = free - (10 * 1024 * 1024);
		int p = free / (300 * 1024);
		if (p < 0)
			p = 0;
		return p;
	}

	protected synchronized void checkCacheAfterPageSwitch() {
		if (NJTAI.files) {
			cache = null;
			return;
		}
		if (cache == null)
			return;
		if (NJTAI.cachingPolicy == 0) {
			for (int i = 0; i < cache.length; i++) {
				if (i != page)
					cache[i] = null;
			}
		} else {
			if (canStorePages() <= 2) {
				for (int i = 0; i < page - 1; i++) {
					if (canStorePages() <= 2) {
						cache[i] = null;
					}
				}
				for (int i = emo.pages - 1; i > page; i--) {
					if (canStorePages() == 0) {
						cache[i] = null;
					} else
						break;
				}
			} else if (NJTAI.cachingPolicy == 2) {
				runPreloader();
			}

		}

	}

	public void run() {
		try {
			synchronized (this) {
				error = false;
				zoom = 1;
				x = 0;
				y = 0;
				reset();
				try {
					prepare(getImage(page));
					repaint();
					resize(1);
					zoom = 1;
				} catch (Exception e) {
					error = true;
					e.printStackTrace();
				}
				repaint();
				runPreloader();
			}
		} catch (OutOfMemoryError e) {
			cache = null;
			NJTAI.setScr(prev);
			NJTAI.pause(100);
			NJTAI.setScr(new Alert("Error", "Not enough memory to continue viewing. Try to disable caching.", null,
					AlertType.ERROR));
			return;
		}
	}

	private void runPreloader() {
		if (preloader == null && NJTAI.cachingPolicy == 2) {
			preloader = new Thread() {
				public void run() {
					try {
						preload();
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			};
			preloader.start();
		}
	}

	int preloadProgress = 101;

	void preload() throws InterruptedException {
		Thread.sleep(1000);
		if (NJTAI.files) {
			for (int i = 0; i < emo.pages; i++) {
				try {
					getImage(i);
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
					preloadProgress = 103;
					repaint();
					return;
				} catch (OutOfMemoryError e) {
					preloadProgress = 104;
					repaint();
					return;
				} catch (Throwable e) {
					error = true;
					repaint();
				}
			}
			preloadProgress = 100;
			return;
		}
		for (int i = page; i < emo.pages; i++) {
			if (cache == null)
				return;
			try {
				if (cache[i] != null)
					continue;
				if (canStorePages() < 1) {
					preloadProgress = 102;
					preloader = null;
					return;
				}
				getImage(i);
				if (preloadProgress != 100)
					preloadProgress = i * 100 / emo.pages;
				repaint();
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
				preloadProgress = 103;
				repaint();
				preloader = null;
				return;
			} catch (OutOfMemoryError e) {
				emergencyCacheClear();
				preloadProgress = 104;
				preloader = null;
				repaint();
				return;
			} catch (NullPointerException e) {
				preloadProgress = 100;
				preloader = null;
				repaint();
			}
		}
		preloadProgress = 100;
		preloader = null;
		repaint();
	}

	protected abstract void limitOffset();

	/**
	 * Clears any data, used for rendering.
	 */
	protected abstract void reset();

	/**
	 * Implementation must prepare {@link #page} for drawing. No resizing is needed.
	 */
	protected abstract void prepare(ByteArrayOutputStream data) throws InterruptedException;

	/**
	 * Called when image must change it's zoom.
	 * 
	 * @param size New zoom to apply.
	 */
	protected abstract void resize(int size);

	String[] touchCaps = new String[] { "x1", "x2", "x3", "<-", "->", NJTAI.rus ? "закрыть" : "close" };

	boolean touchCtrlShown = true;

	protected abstract void reload();

	public abstract boolean canDraw();

	protected void keyPressed(int k) {
		if (k == -7) {
			emo.cancelPrefetch();
			try {
				if (loader != null && loader.isAlive())
					loader.interrupt();
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
			try {
				if (preloader != null && preloader.isAlive())
					preloader.interrupt();
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
			NJTAI.setScr(prev == null ? new MMenu() : prev);

			cache = null;
			return;
		}
		if (!canDraw()) {
			repaint();
			return;
		}
		// zoom is active
		if (zoom != 1) {
			if (k == -5) {
				zoom++;
				if (zoom > 3)
					zoom = 1;

				resize(zoom);
			} else if (k == -1) {
				// up
				y += getHeight() / 4;
			} else if (k == -2) {
				y -= getHeight() / 4;
			} else if (k == -3) {
				x += getWidth() / 4;
			} else if (k == -4) {
				x -= getWidth() / 4;
			}
		} else {
			if (k == -5) {
				zoom = 2;
				x = 0;
				y = 0;
				resize(zoom);
			} else if (k == -3) {
				changePage(-1);
			} else if (k == -4) {
				changePage(1);
			}
		}

		repaint();
	}

	protected void keyRepeated(int k) {
		if (!canDraw()) {
			repaint();
			return;
		}
		// zoom is active
		if (zoom != 1) {
			if (k == -1) {
				// up
				y += getHeight() / 4;
			} else if (k == -2) {
				y -= getHeight() / 4;
			} else if (k == -3) {
				x += getWidth() / 4;
			} else if (k == -4) {
				x -= getWidth() / 4;
			}
		}

		repaint();
	}

	protected void changePage(int delta) {
		if (delta < 0) {
			if (page > 0) {
				page--;
				checkCacheAfterPageSwitch();
				reload();
			}
		} else if (delta > 0) {
			if (page < emo.pages - 1) {
				page++;
				checkCacheAfterPageSwitch();
				reload();
			}
		}
	}

	/**
	 * <ul>
	 * <li>0 - nothing
	 * <li>1 - zoom x1
	 * <li>2 - zoom x2
	 * <li>3 - zoom x3
	 * <li>4 - prev
	 * <li>5 - next
	 * <li>6 - return
	 * </ul>
	 */
	int touchHoldPos = 0;
	int lx, ly;
	int sx, sy;

	protected void pointerPressed(int x, int y) {
		if (!canDraw() && y > getHeight() - 50 && x > getWidth() * 2 / 3) {
			keyPressed(-7);
			return;
		}
		touchHoldPos = 0;
		lx = (sx = x);
		ly = (sy = y);
		if (!touchCtrlShown)
			return;
		if (y < 50 || y > getHeight() - 50) {
			int add = y < 50 ? 1 : 4;
			int b;
			if (x < getWidth() / 3) {
				b = 0;
			} else if (x < getWidth() * 2 / 3) {
				b = 1;
			} else {
				b = 2;
			}
			touchHoldPos = b + add;
		}
		repaint();
	}

	protected void pointerDragged(int tx, int ty) {
		if (touchHoldPos != 0)
			return;
		x += (tx - lx);
		y += (ty - ly);
		lx = tx;
		ly = ty;
		repaint();
	}

	protected void pointerReleased(int x, int y) {
		if (!touchCtrlShown || touchHoldPos == 0) {
			if (Math.abs(sx - x) < 10 && Math.abs(sy - y) < 10) {
				touchCtrlShown = !touchCtrlShown;
			}
		}
		int zone = 0;
		if (y < 50 || y > getHeight() - 50) {
			int add = y < 50 ? 1 : 4;
			int b;
			if (x < getWidth() / 3) {
				b = 0;
			} else if (x < getWidth() * 2 / 3) {
				b = 1;
			} else {
				b = 2;
			}
			zone = b + add;
		}
		if (zone == touchHoldPos) {
			if (zone >= 1 && zone <= 3) {
				zoom = zone;
				resize(zoom);
			} else if (zone == 4) {
				changePage(-1);
			} else if (zone == 5) {
				changePage(1);
			} else if (zone == 6) {
				keyPressed(-7);
			}
		}
		repaint();
	}

	/**
	 * Fills an opaque gradient on the canvas.
	 * 
	 * @param g  Graphics object to draw in.
	 * @param x  X.
	 * @param y  Y.
	 * @param w  Width.
	 * @param h  Height.
	 * @param c1 Top color.
	 * @param c2 Bottom color.
	 */
	public static void fillGrad(Graphics g, int x, int y, int w, int h, int c1, int c2) {
		for (int i = 0; i < h; i++) {
			g.setColor(NJTAI.blend(c2, c1, i * 255 / h));
			g.drawLine(x, y + i, x + w, y + i);
		}
	}

	public static ViewBase create(ExtMangaObj mo, Displayable d, int i) {
		if (NJTAI.view == 1)
			return new ViewSWR(mo, d, i);
		if (NJTAI.view == 2)
			return new ViewHWA(mo, d, i);
		if (System.getProperty("microedition.platform").indexOf("sw_platform_version=5.") != -1)
			return new ViewHWA(mo, d, i);

		return new ViewSWR(mo, d, i);
	}
}
