package njtai.m.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.TextBox;

import njtai.NJTAI;
import njtai.m.MDownloader;
import njtai.models.ExtMangaObj;

/**
 * This class contains methods to work with data to show images.
 * 
 * @author Feodor0090
 *
 */
public abstract class ViewBase extends Canvas implements Runnable, CommandListener {

	protected ExtMangaObj emo;
	protected Displayable prev;
	/**
	 * Number of page from zero.
	 */
	protected int page;

	protected ByteArrayOutputStream[] cache;
	protected MDownloader fs;

	protected float zoom = 1;
	protected float x = 0;
	protected float y = 0;

	protected Thread loader;
	protected Thread preloader;
	protected boolean error;

	int nokiaRam;

	private boolean cacheOk = false;

	Image slider;

	/**
	 * Creates the view.
	 * 
	 * @param emo  Object with data.
	 * @param prev Previous screen.
	 * @param page Number of page to start.
	 */
	public ViewBase(ExtMangaObj emo, Displayable prev, int page) {
		this.emo = emo;
		this.prev = prev;
		this.page = page;
		nokiaRam = (System.getProperty("microedition.platform").indexOf("sw_platform_version=5.") == -1)
				? (15 * 1024 * 1024)
				: (40 * 1024 * 1024);
		NJTAI.clearHP();
		if (NJTAI.files)
			fs = new MDownloader(emo, this);
		reload();
		setFullScreenMode(true);
		try {
			slider = Image.createImage("/slider.png");
		} catch (IOException e) {
			e.printStackTrace();
			slider = null;
		}
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
		if (forceCacheIgnore)
			Thread.sleep(500);
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

				if (!cacheOk) {
					cacheOk = true;
					fs.createFolder();
					fs.writeModel(fs.getFolderName());
				}

				a = new ByteArrayOutputStream(b.length);

				fs.cache(b, n);

				a.write(b);
				return a;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		try {
			if (cache == null) {
				cache = new ByteArrayOutputStream[emo.pages];
			}
			if (forceCacheIgnore) {
				cache[n] = null;
			} else if (cache[n] != null) {
				return cache[n];
			}
			
			synchronized (cache) {
				long ct = System.currentTimeMillis();
				if (ct - lastTime < 350)
					Thread.sleep(ct - lastTime);
				lastTime = ct;
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

	/**
	 * Releases some images to prevent OOM errors.
	 */
	protected synchronized void emergencyCacheClear() {
		try {
			if (NJTAI.files) {
				cache = null;
				return;
			}
			if (cache == null) {
				return;
			}
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
		if (p < 0) p = 0;
		return p;
	}

	protected synchronized void checkCacheAfterPageSwitch() {
		if (NJTAI.files) {
			cache = null;
			return;
		}
		if (cache == null) {
			return;
		}
		if (NJTAI.cachingPolicy == 0) {
			for (int i = 0; i < cache.length; i++) {
				if (i != page) {
					cache[i] = null;
				}
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
					} else {
						break;
					}
				}
			} else if (NJTAI.cachingPolicy == 2) {
				runPreloader();
			}

		}

	}

	long lastTime = System.currentTimeMillis();

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
					if (preloadProgress != 100) {
						preloadProgress = i * 100 / emo.pages;
					}
					repaint();
					Thread.sleep(300);
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
			if (cache == null) {
				return;
			}
			try {
				if (cache[i] != null) {
					continue;
				}
				if (canStorePages() < 1) {
					preloadProgress = 102;
					preloader = null;
					return;
				}
				getImage(i);
				Thread.sleep(300);
				if (preloadProgress != 100) {
					preloadProgress = i * 100 / emo.pages;
				}
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

	String[] touchCaps = new String[] { "x1", "x2", "x3", "<-", "goto", "->", NJTAI.L_ACTS[16] };

	boolean touchCtrlShown = true;

	protected abstract void reload();

	/**
	 * Is there something to draw?
	 * 
	 * @return False if view is blocked.
	 */
	public abstract boolean canDraw();

	protected void keyPressed(int k) {
		k = qwertyToNum(k);
		if (k == -7 || k == KEY_NUM9) {
			try {
				if (loader != null && loader.isAlive()) {
					loader.interrupt();
				}
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
			try {
				if (preloader != null && preloader.isAlive()) {
					preloader.interrupt();
				}
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
			NJTAI.setScr(prev == null ? NJTAI.mmenu : prev);

			cache = null;
			return;
		}
		if (!canDraw()) {
			repaint();
			return;
		}

		if (k == KEY_NUM7 || k == -10 || k == 8) {
			TextBox tb = new TextBox(NJTAI.L_ACTS[17], "", 7, 2);
			tb.addCommand(NJTAI.openCmd);
			tb.addCommand(NJTAI.backCmd);
			tb.setCommandListener(this);
			NJTAI.setScr(tb);
		}

		if (k == KEY_NUM1) {
			changePage(-1);
		} else if (k == KEY_NUM3 || k == 32) {
			changePage(1);
		}

		// zooming via *0#
		if (k == KEY_STAR) {
			zoom = 1;
			resize((int) zoom);
		}
		if (k == KEY_NUM0) {
			zoom = 2;
			resize((int) zoom);
		}
		if (k == KEY_POUND) {
			zoom = 3;
			resize((int) zoom);
		}

		// zoom is active
		if (zoom != 1) {
			if (k == -5) {
				zoom++;
				if (zoom > 3)
					zoom = 1;

				resize((int) zoom);
			} else if (k == -1 || k == KEY_NUM2 || k == 'w') {
				// up
				y += getHeight() / 4;
			} else if (k == -2 || k == KEY_NUM8 || k == 's') {
				y -= getHeight() / 4;
			} else if (k == -3 || k == KEY_NUM4 || k == 'a') {
				x += getWidth() / 4;
			} else if (k == -4 || k == KEY_NUM6 || k == 'd') {
				x -= getWidth() / 4;
			}
		} else {
			// zoom inactive
			if (k == -5) {
				zoom = 2;
				x = 0;
				y = 0;
				resize((int) zoom);
			} else if (k == -3) {
				changePage(-1);
			} else if (k == -4) {
				changePage(1);
			}
		}

		repaint();
	}

	protected void keyRepeated(int k) {
		k = qwertyToNum(k);
		if (!canDraw()) {
			repaint();
			return;
		}
		// zoom is active
		if (zoom != 1) {
			if (k == -1 || k == KEY_NUM2 || k == 'w') {
				// up
				y += getHeight() * panDeltaMul() / 4;
			} else if (k == -2 || k == KEY_NUM8 || k == 's') {
				y -= getHeight() * panDeltaMul() / 4;
			} else if (k == -3 || k == KEY_NUM4 || k == 'a') {
				x += getWidth() * panDeltaMul() / 4;
			} else if (k == -4 || k == KEY_NUM6 || k == 'd') {
				x -= getWidth() * panDeltaMul() / 4;
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
	 * <li>5 - goto
	 * <li>6 - next
	 * <li>7 - return
	 * <li>8 - zoom slider
	 * </ul>
	 */
	int touchHoldPos = 0;
	int lx, ly;
	int sx, sy;

	protected void pointerPressed(int tx, int ty) {
		if (!canDraw() && ty > getHeight() - 50 && tx > getWidth() * 2 / 3) {
			keyPressed(-7);
			return;
		}
		touchHoldPos = 0;
		lx = (sx = tx);
		ly = (sy = ty);
		if (!touchCtrlShown)
			return;
		if (ty < 50 && useSmoothZoom()) {
			setSmoothZoom(tx, getWidth());
			touchHoldPos = 8;
		} else if (ty < 50) {
			int b;
			if (tx < getWidth() / 3) {
				b = 1;
			} else if (tx < getWidth() * 2 / 3) {
				b = 2;
			} else {
				b = 3;
			}
			touchHoldPos = b;
		} else if (ty > getHeight() - 50) {
			int b;
			if (tx < getWidth() / 4) {
				b = 4;
			} else if (tx < getWidth() * 2 / 4) {
				b = 5;
			} else if (tx < getWidth() * 3 / 4) {
				b = 6;
			} else {
				b = 7;
			}
			touchHoldPos = b;
		}
		repaint();
	}

	protected void setSmoothZoom(int dx, int w) {
		dx -= 25;
		w -= 50;
		zoom = 1 + 4f * ((float) dx / w);
		if (zoom < 1.01f)
			zoom = 1;
		if (zoom > 4.99f)
			zoom = 5;
	}

	protected abstract boolean useSmoothZoom();

	/**
	 * @return -1 if drag must be inverted, 1 overwise.
	 */
	protected float panDeltaMul() {
		return NJTAI.invertPan ? -1 : 1;
	}

	protected void pointerDragged(int tx, int ty) {
		if (touchHoldPos == 8) {
			setSmoothZoom(tx, getWidth());
			repaint();
			return;
		}
		if (touchHoldPos != 0)
			return;
		x += (tx - lx) * panDeltaMul() / (useSmoothZoom() ? zoom : 1f);
		y += (ty - ly) * panDeltaMul() / (useSmoothZoom() ? zoom : 1f);
		lx = tx;
		ly = ty;
		repaint();
	}

	protected void pointerReleased(int tx, int ty) {
		if (!touchCtrlShown || touchHoldPos == 0) {
			if (Math.abs(sx - tx) < 10 && Math.abs(sy - ty) < 10) {
				touchCtrlShown = !touchCtrlShown;
			}
		}
		if (touchHoldPos == 8) {
			touchHoldPos = 0;
			repaint();
			return;
		}
		int zone = 0;
		if (ty < 50) {
			int b;
			if (tx < getWidth() / 3) {
				b = 1;
			} else if (tx < getWidth() * 2 / 3) {
				b = 2;
			} else {
				b = 3;
			}
			zone = b;
		} else if (ty > getHeight() - 50) {
			int b;
			if (tx < getWidth() / 4) {
				b = 4;
			} else if (tx < getWidth() * 2 / 4) {
				b = 5;
			} else if (tx < getWidth() * 3 / 4) {
				b = 6;
			} else {
				b = 7;
			}
			zone = b;
		}
		if (zone == touchHoldPos) {
			if (zone >= 1 && zone <= 3) {
				zoom = zone;
				resize(zone);
			} else if (zone == 4) {
				changePage(-1);
			} else if (zone == 5) {
				keyPressed(KEY_NUM7);
			} else if (zone == 6) {
				changePage(1);
			} else if (zone == 7) {
				keyPressed(-7);
			}
		}
		touchHoldPos = 0;
		repaint();
	}

	/**
	 * Listener for textbox.
	 * 
	 * @param c
	 * @param d
	 */
	public void commandAction(Command c, Displayable d) {
		TextBox tb = (TextBox) d;
		NJTAI.setScr(this);
		if (c == NJTAI.openCmd) {
			try {
				int n = Integer.parseInt(tb.getString());
				if (n < 1) {
					n = 1;
				} else if (n > emo.pages) {
					n = emo.pages;
				}
				page = n - 1;
				checkCacheAfterPageSwitch();
				reload();
			} catch (Exception e) {
				NJTAI.pause(100);
				NJTAI.setScr(
						new Alert(NJTAI.L_ACTS[9], NJTAI.L_ACTS[10], null, AlertType.ERROR));
			}
		}
		repaint();
	}
	protected void paintHUD(Graphics g, Font f, boolean drawZoom, boolean drawPages) {
		String pageNum = (page + 1) + "/" + emo.pages;
		String zoomN = useSmoothZoom() ? String.valueOf(zoom) : String.valueOf((int) zoom);
		if (zoomN.length() > 3)
			zoomN = zoomN.substring(0, 3);
		zoomN = "x" + zoomN;
		String prefetch = null;
		// if (preloadProgress == 101) {
		if (NJTAI.cachingPolicy == 2) {
			prefetch = (preloadProgress > 0 && preloadProgress < 100)
					? ((NJTAI.files ? "downloading " : "caching ") + preloadProgress + "%")
					: null;
		}

		// BGs
		g.setGrayScale(0);
		if (drawPages) {
			g.fillRect(0, 0, f.stringWidth(pageNum), f.getHeight());
		}
		if (drawZoom) {
			g.fillRect(getWidth() - f.stringWidth(zoomN), 0, f.stringWidth(zoomN), f.getHeight());
		}
		if (prefetch != null) {
			g.fillRect(0, getHeight() - f.getHeight(), f.stringWidth(prefetch), f.getHeight());
		}

		// texts
		g.setGrayScale(255);
		if (drawPages) {
			g.drawString(pageNum, 0, 0, 0);
		}
		if (drawZoom) {
			g.drawString(zoomN, getWidth() - f.stringWidth(zoomN), 0, 0);
		}
		if (prefetch != null) {
			g.drawString(prefetch, 0, getHeight() - f.getHeight(), 0);
		}

	}

	protected void drawTouchControls(Graphics g, Font f) {
		int fh = f.getHeight();

		// captions
		for (int i = 3; i < 7; i++) {
			fillGrad(g, getWidth() * (i - 3) / 4, getHeight() - 50, getWidth() / 4, 51, 0,
					touchHoldPos == (i + 1) ? 0x357EDE : 0x222222);
			g.setGrayScale(255);
			g.drawString(i == 4 ? ((page + 1) + "/" + emo.pages) : touchCaps[i], getWidth() * (1 + (i - 3) * 2) / 8,
					getHeight() - 25 - fh / 2, Graphics.TOP | Graphics.HCENTER);
		}
		// hor lines
		g.setGrayScale(255);
		g.drawLine(0, getHeight() - 50, getWidth(), getHeight() - 50);
		// vert lines between btns
		g.drawLine(getWidth() / 4, getHeight() - 50, getWidth() / 4, getHeight());
		g.drawLine(getWidth() * 2 / 4, getHeight() - 50, getWidth() * 2 / 4, getHeight());
		g.drawLine(getWidth() * 3 / 4, getHeight() - 50, getWidth() * 3 / 4, getHeight());

		if (useSmoothZoom()) {
			drawZoomSlider(g, f);
			return;
		}
		for (int i = 0; i < 3; i++) {
			fillGrad(g, getWidth() * i / 3, 0, getWidth() / 3 + 1, 50, touchHoldPos == (i + 1) ? 0x357EDE : 0x222222,
					0);
			g.setGrayScale(255);
			g.drawString(touchCaps[i], getWidth() * (1 + i * 2) / 6, 25 - fh / 2, Graphics.TOP | Graphics.HCENTER);
		}
		// bottom hor line
		g.setGrayScale(255);
		g.drawLine(0, 50, getWidth(), 50);
		// vert lines between btns
		g.drawLine(getWidth() / 3, 0, getWidth() / 3, 50);
		g.drawLine(getWidth() * 2 / 3, 0, getWidth() * 2 / 3, 50);
	}

	private void drawZoomSlider(Graphics g, Font f) {
		int px = (int) (25 + ((getWidth() - 50) * (zoom - 1) / 4));

		// slider's body
		if (slider == null) {
			for (int i = 0; i < 10; i++) {
				g.setColor(NJTAI.blend(touchHoldPos == 8 ? 0x357EDE : 0x444444, 0xffffff, i * 255 / 9));
				g.drawRoundRect(25 - i, 25 - i, getWidth() - 50 + (i * 2), i * 2, i, i);
			}
		} else {
			int spy = touchHoldPos == 8 ? 20 : 0;
			g.drawRegion(slider, 0, spy, 35, 20, 0, 0, 15, 0);
			g.drawRegion(slider, 35, spy, 35, 20, 0, getWidth() - 35, 15, 0);
			g.setClip(35, 0, getWidth() - 70, 50);
			for (int i = 35; i < getWidth() - 34; i += 20) {
				g.drawRegion(slider, 25, spy, 20, 20, 0, i, 15, 0);
			}
			g.setClip(0, 0, getWidth(), getHeight());
		}

		// slider's pin
		for (int i = 0; i < 15; i++) {
			g.setColor(NJTAI.blend(touchHoldPos == 8 ? 0x357EDE : 0x444444, 0, i * 255 / 14));
			g.fillArc(px - 15 + i, 10 + i, 30 - i * 2, 30 - i * 2, 0, 360);
		}
		g.setColor(touchHoldPos == 8 ? 0x357EDE : -1);

		g.drawArc(px - 16, 9, 30, 30, 0, 360);

		String ft = String.valueOf(zoom);
		if (ft.length() > 3) {
			ft = ft.substring(0, 3);
		}
		g.setColor(-1);
		g.drawString(ft, px, 25 - f.getHeight() / 2, Graphics.TOP | Graphics.HCENTER);
	}

	protected void paintNullImg(Graphics g, Font f) {
		String info;
		if (error) {
			g.setGrayScale(0);
			g.fillRect(0, 0, getWidth(), getHeight());
			info = NJTAI.rus ? "Не удалось загрузить." : "Failed to load image.";
		} else {
			info = (NJTAI.rus ? "Подготовка" : "Preparing");
		}
		g.setGrayScale(0);
		int w = g.getFont().stringWidth(info);
		int h = g.getFont().getHeight();
		g.fillRect(getWidth() / 2 - w / 2, getHeight() / 2, w, h);
		g.setGrayScale(255);
		g.drawString(info, getWidth() / 2, getHeight() / 2, Graphics.HCENTER | Graphics.TOP);
		if (hasPointerEvents()) {
			int fh = f.getHeight();
			// grads
			fillGrad(g, getWidth() * 3 / 4, getHeight() - 50, getWidth() / 4, 51, 0, 0x222222);
			// lines
			g.setGrayScale(255);
			g.drawLine(getWidth() * 3 / 4, getHeight() - 50, getWidth(), getHeight() - 50);
			g.drawLine(getWidth() * 3 / 4, getHeight() - 50, getWidth() * 3 / 4, getHeight());
			// captions
			g.setGrayScale(255);
			g.drawString(touchCaps[6], getWidth() * 7 / 8, getHeight() - 25 - fh / 2, Graphics.TOP | Graphics.HCENTER);
		}
	}

	protected void showBrokenNotify() {
		Alert a = new Alert("Image file is corrupted",
				"It is recommended to run cache repairer from manga's page. The image will be downloaded again for now.",
				null, AlertType.ERROR);
		a.setTimeout(Alert.FOREVER);
		try {
			NJTAI.setScr(a);
		} catch (Exception e) {
			e.printStackTrace();
		}
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

	/**
	 * Converts qwerty key code to corresponding 12k key code.
	 * 
	 * @param k Original key code.
	 * @return Converted key code.
	 */
	public static int qwertyToNum(int k) {
		char c = (char) k;
		switch (c) {
		case 'r':
		case 'R':
		case 'к':
			return Canvas.KEY_NUM1;

		case 't':
		case 'T':
		case 'е':
			return Canvas.KEY_NUM2;

		case 'y':
		case 'Y':
		case 'н':
			return Canvas.KEY_NUM3;

		case 'f':
		case 'F':
		case 'а':
			return Canvas.KEY_NUM4;

		case 'g':
		case 'G':
		case 'п':
			return Canvas.KEY_NUM5;

		case 'h':
		case 'H':
		case 'р':
			return Canvas.KEY_NUM6;

		case 'v':
		case 'V':
		case 'м':
			return Canvas.KEY_NUM7;

		case 'b':
		case 'B':
		case 'и':
			return Canvas.KEY_NUM8;

		case 'n':
		case 'N':
		case 'т':
			return Canvas.KEY_NUM9;

		case 'm':
		case 'M':
		case 'ь':
			return Canvas.KEY_NUM0;

		default:
			return k;
		}
	}

	/**
	 * Creates a view.
	 * 
	 * @param mo Object to work with.
	 * @param d  Previous screen.
	 * @param i  Page number.
	 * @return Created view.
	 */
	public static ViewBase create(ExtMangaObj mo, Displayable d, int i) {
		if (NJTAI.view == 1) {
			return new ViewSWR(mo, d, i);
		}
		if (NJTAI.view == 2) {
			return new ViewHWA(mo, d, i);
		}
		String vram = System.getProperty("com.nokia.gpu.memory.total");
		if (vram != null && !vram.equals("0")) {
			return new ViewHWA(mo, d, i);
		}
		return new ViewSWR(mo, d, i);
	}
}
