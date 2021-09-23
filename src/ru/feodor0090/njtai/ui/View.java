package ru.feodor0090.njtai.ui;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import ru.feodor0090.njtai.Images;
import ru.feodor0090.njtai.NJTAI;
import ru.feodor0090.njtai.models.ExtMangaObj;
import tube42.ImgUtil;

public class View extends Canvas implements Runnable {

	Thread loader;
	private ExtMangaObj emo;
	private Displayable prev;
	/**
	 * Number of page from zero.
	 */
	private int page;

	Image origImg;
	Image toDraw;

	int zoom = 1;
	int x = 0;
	int y = 0;

	Thread preloader;

	public View(ExtMangaObj emo, Displayable prev, int page) {
		this.emo = emo;
		this.prev = prev;
		this.page = page;
		reload();
		setFullScreenMode(true);
	}

	public void run() {
		synchronized (this) {
			zoom = 1;
			x = getWidth() / 2;
			y = getHeight() / 2;
			origImg = null;
			toDraw = null;
			try {
				origImg = emo.getPage(page, this);
				repaint();
				if (origImg == null)
					return;
				resize(1);
				zoom = 1;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			repaint();
			if (preloader == null && NJTAI.prldImg && NJTAI.cache) {
				preloader = new Thread() {
					public void run() {
						preload();
					}
				};
				preloader.start();
			}
		}
	}

	void preload() {
		for (int i = 0; i < emo.pages; i++) {
			try {
				emo.getPage(i + 1, this);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
		}
	}

	private void resize(int size) {
		int h = getHeight();
		int w = (int) (((float) h / origImg.getHeight()) * origImg.getWidth());

		h = h * size;
		w = w * size;
		toDraw = ImgUtil.resize(origImg, w, h, true);
	}

	static String[] touchCaps = new String[] { "x1", "x2", "x3", "<-", "->", "close" };

	boolean touchCtrlShown = true;

	protected void paint(Graphics g) {
		try {
			Font f = Font.getFont(0, 0, 8);
			g.setFont(f);

			// bg fill
			g.setGrayScale(0);
			g.fillRect(0, 0, getWidth(), getHeight());

			if (toDraw == null) {
				String info;
				if (emo.infoReady == -1) {
					info = "Failed to fetch pages.";
				} else if (emo.infoReady == -2) {
					info = "Waiting...";
				} else if (emo.infoReady == 100) {
					info = origImg == null ? "Loading image..." : "Resizing...";
				} else {
					info = "Fetching pages info " + emo.infoReady + "%";
				}
				g.setGrayScale(255);
				g.drawString(info, getWidth() / 2, getHeight() / 2, Graphics.HCENTER | Graphics.TOP);
				if (hasPointerEvents()) {
					int fh = f.getHeight();
					// grads
					fillGrad(g, getWidth() * 2 / 3, getHeight() - 50, getWidth() / 3, 51, 0, 0x222222);
					// lines
					g.setGrayScale(255);
					g.drawLine(getWidth() * 2 / 3, getHeight() - 50, getWidth(), getHeight() - 50);
					g.drawLine(getWidth() * 2 / 3, getHeight() - 50, getWidth() * 2 / 3, getHeight());
					// captions
					g.setGrayScale(255);
					g.drawString(touchCaps[5], getWidth() * 5 / 6, getHeight() - 25 - fh / 2,
							Graphics.TOP | Graphics.HCENTER);

				}
			} else {
				if (zoom != 1) {
					g.drawImage(toDraw, x, y, Graphics.HCENTER | Graphics.VCENTER);
				} else {
					g.drawImage(toDraw, (getWidth() - toDraw.getWidth()) / 2, 0, 0);
				}

				// touch captions
				if (hasPointerEvents() && touchCtrlShown) {
					drawTouchControls(g, f);
				}
			}
			String pageNum = (page + 1) + "/" + emo.pages;
			String zoomN = "x" + zoom;
			g.setGrayScale(0);
			g.fillRect(0, 0, f.stringWidth(pageNum), f.getHeight());
			g.fillRect(getWidth() - f.stringWidth(zoomN), 0, f.stringWidth(zoomN), f.getHeight());
			g.setGrayScale(255);
			g.drawString(pageNum, 0, 0, 0);
			g.drawString(zoomN, getWidth() - f.stringWidth(zoomN), 0, 0);
		} catch (Exception e) {
			e.printStackTrace();

			try {
				NJTAI.setScr(new Alert("Repaint error", e.toString(), null, AlertType.ERROR));
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	private void drawTouchControls(Graphics g, Font f) {
		int fh = f.getHeight();
		// grads
		fillGrad(g, 0, 0, getWidth(), 50, 0x222222, 0);
		fillGrad(g, 0, getHeight() - 50, getWidth(), 51, 0, 0x222222);
		// hor lines
		g.setGrayScale(255);
		g.drawLine(0, 50, getWidth(), 50);
		g.drawLine(0, getHeight() - 50, getWidth(), getHeight() - 50);
		// captions
		for (int i = 0; i < 3; i++) {
			g.setGrayScale(255);
			g.drawString(touchCaps[i], getWidth() * (1 + i * 2) / 6, 25 - fh / 2, Graphics.TOP | Graphics.HCENTER);
		}
		for (int i = 3; i < 6; i++) {
			g.setGrayScale(255);
			g.drawString(touchCaps[i], getWidth() * (1 + (i - 3) * 2) / 6, getHeight() - 25 - fh / 2,
					Graphics.TOP | Graphics.HCENTER);
		}
		// vert lines between btns
		g.drawLine(getWidth() / 3, 0, getWidth() / 3, 50);
		g.drawLine(getWidth() * 2 / 3, 0, getWidth() * 2 / 3, 50);
		g.drawLine(getWidth() / 3, getHeight() - 50, getWidth() / 3, getHeight());
		g.drawLine(getWidth() * 2 / 3, getHeight() - 50, getWidth() * 2 / 3, getHeight());
	}

	void reload() {
		toDraw = null;
		origImg = null;
		loader = new Thread(this);
		loader.start();
	}

	protected void keyPressed(int k) {
		if (k == -7) {
			Images.reset();
			if (loader != null && loader.isAlive())
				loader.interrupt();
			if (preloader != null && preloader.isAlive())
				preloader.interrupt();
			NJTAI.setScr(prev);
			return;
		}
		if (toDraw == null) {
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
				x = getWidth() / 2;
				y = getHeight() / 2;
				resize(zoom);
			} else if (k == -3) {
				if (page > 0) {
					page--;
					reload();
				}
			} else if (k == -4) {
				if (page < emo.pages - 1) {
					page++;
					reload();
				}
			}
		}

		repaint();
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
		if (toDraw == null && y > getHeight() - 50 && x > getWidth() * 2 / 3) {
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
				if (page > 0) {
					page--;
					reload();
				}
			} else if (zone == 5) {
				if (page < emo.pages - 1) {
					page++;
					reload();
				}
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
			g.setColor(ImgUtil.blend(c2, c1, i * 255 / h));
			g.drawLine(x, y + i, x + w, y + i);
		}
	}
}
