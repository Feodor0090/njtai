package ru.feodor0090.njtai.ui;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import ru.feodor0090.njtai.Images;
import ru.feodor0090.njtai.NjtaiApp;
import ru.feodor0090.njtai.models.ExtendedMangaObject;
import tube42.lib.imagelib.ImageUtils;

public class View extends Canvas implements Runnable {

	Thread loader;
	private ExtendedMangaObject emo;
	private Displayable prev;
	private int page;

	Image origImg;
	Image toDraw;

	boolean isZoomed = false;
	int x = 0;
	int y = 0;

	Thread preloader;

	public View(ExtendedMangaObject emo, Displayable prev, int page) {
		this.emo = emo;
		this.prev = prev;
		this.page = page;
		reload();
		setFullScreenMode(true);
	}

	public void run() {
		synchronized (this) {
			isZoomed = false;
			x = 0;
			y = 0;
			origImg = null;
			toDraw = null;
			try {
				origImg = emo.getPage(page, this);
				repaint();
				resize(true);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			repaint();
			if (preloader == null && NjtaiApp.allowPreload&& NjtaiApp.enableCache) {
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

	private void resize(boolean mini) {
		int h = getHeight();
		int w = (int) (((float) h / origImg.getHeight()) * origImg.getWidth());
		if (!mini) {
			h = h * 3;
			w = w * 3;
		}
		toDraw = ImageUtils.resize(origImg, w, h, true, false);
	}

	protected void paint(Graphics g) {
		try {
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
			} else {
				if (isZoomed) {
					g.drawImage(toDraw, x, y, Graphics.HCENTER | Graphics.VCENTER);
				} else
					g.drawImage(toDraw, (getWidth() - toDraw.getWidth()) / 2, 0, 0);
			}
			String pageNum = page + "/" + emo.pages;
			Font f = Font.getFont(0, 0, 8);
			g.setFont(f);
			g.setGrayScale(0);
			g.fillRect(0, 0, f.stringWidth(pageNum), f.getHeight());
			g.setGrayScale(255);
			g.drawString(pageNum, 0, 0, 0);
		} catch (Exception e) {
			e.printStackTrace();

			try {
				NjtaiApp.setScreen(new Alert("Repaint error", e.toString(), null, AlertType.ERROR));
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
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
			NjtaiApp.setScreen(prev);
			return;
		}
		if (toDraw == null) {
			repaint();
			return;
		}
		if (isZoomed) {
			if (k == -5) {
				isZoomed = false;
				x = 0;
				y = 0;
				resize(true);
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
				isZoomed = true;
				x = getWidth() / 2;
				y = getHeight() / 2;
				resize(false);
			} else if (k == -3) {
				if (page > 1) {
					page--;
					reload();
				}
			} else if (k == -4) {
				if (page < emo.pages) {
					page++;
					reload();
				}
			}
		}

		repaint();
	}

}
