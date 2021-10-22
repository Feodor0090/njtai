package njtai.m.ui;

import java.io.IOException;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import njtai.NJTAI;
import njtai.m.NJTAIM;
import njtai.models.ExtMangaObj;

/**
 * Class, containing some common drawing methods.
 * 
 * @author Feodor0090
 *
 */
public abstract class View extends ViewBase {

	Image slider;

	/**
	 * Creates the view.
	 * 
	 * @param emo  Object with data.
	 * @param prev Previous screen.
	 * @param page Number of page to start.
	 */
	public View(ExtMangaObj emo, Displayable prev, int page) {
		super(emo, prev, page);
		try {
			slider = Image.createImage("/slider.png");
		} catch (IOException e) {
			e.printStackTrace();
			slider = null;
		}
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
		} else {
			prefetch = (emo.infoReady >= 0 && emo.infoReady < 100) ? ("fetching " + emo.infoReady + "%") : null;
		}
		/*
		 * } else { if (preloadProgress < 100) prefetch = "preloading " +
		 * preloadProgress + "%"; else if (preloadProgress == 104) { prefetch = "OOM"; }
		 * else if (preloadProgress > 101) prefetch = "code " + (preloadProgress % 100);
		 * }
		 */
		/*
		 * String ram; { long used = Runtime.getRuntime().totalMemory() -
		 * Runtime.getRuntime().freeMemory(); used /= 1024; if (used <= 4096) { ram =
		 * used + "K"; } else { ram = (used / 1024) + "M"; } }
		 */
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
		// g.fillRect(getWidth() - f.stringWidth(ram), getHeight() - f.getHeight(),
		// f.stringWidth(ram), f.getHeight());

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
		// g.drawString(ram, getWidth(), getHeight(), Graphics.BOTTOM | Graphics.RIGHT);
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
			info = NJTAI.rus ? "Не удалось загрузить." : "Failed to load image.";
		} else if (emo.infoReady == -1) {
			info = "Failed to fetch pages.";
		} else if (emo.infoReady == -2 && NJTAI.preloadUrl && NJTAI.cachingPolicy != 2 && !emo.isOffline()) {
			info = NJTAI.rus ? "Ожидание" : "Waiting";
		} else {
			info = (NJTAI.rus ? "Подготовка изображения" : "Image preparing");
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
			NJTAIM.setScr(a);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
