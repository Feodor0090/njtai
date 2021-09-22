package ru.feodor0090.njtai.ui;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import ru.feodor0090.njtai.NjtaiApp;
import ru.feodor0090.njtai.models.ExtendedMangaObject;

public class View extends Canvas implements Runnable {

	Thread loader;
	private ExtendedMangaObject emo;
	private Displayable prev;
	private int page;

	Image toDraw;

	public View(ExtendedMangaObject emo, Displayable prev, int page) {
		this.emo = emo;
		this.prev = prev;
		this.page = page;
		reload();
		setFullScreenMode(true);
	}

	public void run() {
		toDraw = null;
		toDraw = emo.getPage(page, this);
		repaint();
	}

	protected void paint(Graphics g) {
		g.setGrayScale(0);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setGrayScale(255);
		if (toDraw == null) {
			String info;
			if (emo.infoReady == -1) {
				info = "Failed to fetch pages.";
			} else if (emo.infoReady == -2) {
				info = "Waiting...";
			} else if (emo.infoReady == 100) {
				info = "Loading image...";
			} else {
				info = "Fetching pages info " + emo.infoReady + "%";
			}
			g.drawString(info, getWidth() / 2, getHeight() / 2, Graphics.HCENTER | Graphics.TOP);
		} else {
			g.drawImage(toDraw, 0, 0, 0);
		}
	}

	void reload() {
		loader = new Thread(this);
		loader.start();
	}

	protected void keyPressed(int k) {
		if (toDraw == null)
			return;
		if (k == -3) {
			if (page > 1) {
				page--;
				reload();
			}
		} else if (k == -4) {
			if (page < emo.pages) {
				page++;
				reload();
			}
		} else if (k == -7) {
			NjtaiApp.setScreen(prev);
		}
	}

}
