package njtai.ui;

import java.io.ByteArrayOutputStream;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import njtai.NJTAI;
import njtai.models.ExtMangaObj;

public class ViewSWR extends View {

	public ViewSWR(ExtMangaObj emo, Displayable prev, int page) {
		super(emo, prev, page);
	}

	Image toDraw;
	Image orig;

	protected void resize(int size) {
		try {
			toDraw = null;
			System.gc();
			repaint();
			Image origImg;
			if (NJTAI.keepBitmap) {
				origImg = orig;
			} else {
				byte[] b = getImage(page).toByteArray();
				origImg = Image.createImage(b, 0, b.length);
				b = null;
				System.gc();
			}
			int h = getHeight();
			int w = (int) (((float) h / origImg.getHeight()) * origImg.getWidth());

			if (w > getWidth()) {
				w = getWidth();
				h = (int) (((float) w / origImg.getWidth()) * origImg.getHeight());
			}

			h = h * size;
			w = w * size;
			toDraw = NJTAI.resize(origImg, w, h);
		} catch (Throwable e) {
			error = true;
		}
	}

	protected void paint(Graphics g) {
		try {
			Font f = Font.getFont(0, 0, 8);
			g.setFont(f);

			// bg fill
			g.setGrayScale(0);
			g.fillRect(0, 0, getWidth(), getHeight());

			if (toDraw == null) {
				paintNullImg(g, f);
			} else {
				limitOffset();
				if (zoom != 1) {
					g.drawImage(toDraw, x + getWidth() / 2, y + getHeight() / 2, Graphics.HCENTER | Graphics.VCENTER);
				} else {
					g.drawImage(toDraw, (getWidth() - toDraw.getWidth()) / 2, (getHeight() - toDraw.getHeight()) / 2,
							0);
				}

				// touch captions
				if (hasPointerEvents() && touchCtrlShown) {
					drawTouchControls(g, f);
				}
			}
			paintHUD(g, f);
		} catch (Exception e) {
			e.printStackTrace();

			try {
				NJTAI.setScr(new Alert("Repaint error", e.toString(), null, AlertType.ERROR));
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	

	protected void limitOffset() {
		int hw = toDraw.getWidth() / 2;
		int hh = toDraw.getHeight() / 2;
		if (x < -hw)
			x = -hw;
		if (x > hw)
			x = hw;
		if (y < -hh)
			y = -hh;
		if (y > hh)
			y = hh;
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

	protected void reload() {
		toDraw = null;
		System.gc();
		loader = new Thread(this);
		loader.start();
	}

	public boolean canDraw() {
		return toDraw != null;
	}

	protected void reset() {
		toDraw = null;
		orig = null;
	}

	protected void prepare(ByteArrayOutputStream d) throws InterruptedException {
		if (NJTAI.keepBitmap) {
			byte[] b = d.toByteArray();
			orig = Image.createImage(b, 0, b.length);
			b = null;
			System.gc();
		}
	}
}
