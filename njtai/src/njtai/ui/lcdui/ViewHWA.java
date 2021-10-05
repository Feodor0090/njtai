package njtai.ui.lcdui;

import java.io.ByteArrayOutputStream;
import java.util.Vector;

import javax.microedition.lcdui.*;
import javax.microedition.m3g.*;

import njtai.NJTAI;
import njtai.models.ExtMangaObj;

/**
 * {@link View} implementation, that uses M3G for realtime scaling.
 * 
 * @author Feodor0090
 * @deprecated It's broken. Will be investigated later.
 */
public class ViewHWA extends View {

	public ViewHWA(ExtMangaObj emo, Displayable prev, int page) {
		super(emo, prev, page);

		// material
		_material = new Material();
		_material.setColor(Material.DIFFUSE, 0xFFFFFFFF); // white
		_material.setColor(Material.SPECULAR, 0xFFFFFFFF); // white
		_material.setShininess(128f);
		_material.setVertexColorTrackingEnable(true);

		// compositing
		_compositing = new CompositingMode();
		_compositing.setAlphaThreshold(0.0f);
		_compositing.setBlending(CompositingMode.ALPHA);

		// pol mode
		_polMode = new PolygonMode();
		_polMode.setWinding(PolygonMode.WINDING_CW);
		_polMode.setCulling(PolygonMode.CULL_NONE);
		_polMode.setShading(PolygonMode.SHADE_SMOOTH);
	}

	protected Material _material;
	protected CompositingMode _compositing;
	protected PolygonMode _polMode;

	PagePart[] p = null;
	int iw, ih;

	protected void reset() {
		p = null;
	}

	protected void prepare(ByteArrayOutputStream data) throws InterruptedException {
		reset();
		byte[] d = data.toByteArray();
		Image i = Image.createImage(d, 0, d.length);
		d = null;
		ih = i.getHeight();
		iw = i.getWidth();
		Vector v = new Vector();
		int s = 512;
		for (int x = 0; x < i.getWidth() + s - 1; x++) {
			for (int y = 0; y < i.getHeight() + s - 1; y++) {
				v.addElement(new PagePart(i, x, y, (short) s));
			}
		}
		PagePart[] tmp = new PagePart[v.size()];
		v.copyInto(tmp);
		v = null;
		p = tmp;
		x = iw / 2;
		y = ih / 2;
	}

	protected void resize(int size) {
		// do nothing for now
	}

	protected void reload() {
		reset();
		System.gc();
		loader = new Thread(this);
		loader.start();
	}

	public boolean canDraw() {
		return p != null;
	}

	protected void paint(Graphics g) {
		try {
			Font f = Font.getFont(0, 0, 8);
			g.setFont(f);

			// bg fill

			if (p == null) {
				g.setGrayScale(0);
				g.fillRect(0, 0, getWidth(), getHeight());
				paintNullImg(g, f);
				g.setColor(0, 0, 255);
				g.fillRect(0, 0, getWidth(), 4);
				g.drawString(iw + "x" + ih, getWidth() / 2, 4, Graphics.TOP | Graphics.HCENTER);
			} else {
				limitOffset();
				Graphics3D g3 = Graphics3D.getInstance();
				g3.bindTarget(g);
				Background b = new Background();
				b.setColorClearEnable(true);
				b.setDepthClearEnable(true);
				g3.clear(b);
				setupM3G(g3);
				for (int i = 0; i < p.length; i++) {
					p[i].paint(g3);
				}
				g3.releaseTarget();
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
		if (x < 0)
			x = 0;
		if (x > iw)
			x = iw;
		if (y < 0)
			y = 0;
		if (y > ih)
			y = ih;
	}

	protected void setupM3G(Graphics3D g3d) {
		Camera cam = new Camera();
		cam.setParallel(ih / zoom, getWidth() / (float) getHeight(), 0.1f, 900f);
		Transform t = new Transform();
		t.postTranslate(x, y, 100);
		Light l = new Light();
		l.setColor(0xffffff); // white light
		l.setIntensity(1f);
		l.setMode(Light.AMBIENT);

		g3d.setCamera(cam, t);
		g3d.resetLights();
		g3d.addLight(l, t);
	}

	class PagePart {
		int size;
		Appearance ap;
		Transform t;
		VertexBuffer vb;
		IndexBuffer ind;

		public PagePart(Image page, int x, int y, short s) {

			// cropping
			Image part = Image.createImage(s, s);
			Graphics pg = part.getGraphics();
			pg.setColor(0);
			pg.fillRect(0, 0, s, s);
			pg.drawImage(page, -x, -y, 0);
			Image spart = Image.createImage(part);
			part = null;

			// appearance
			Image2D image2D = new Image2D(Image2D.RGB, spart);
			Texture2D tex = new Texture2D(image2D);
			tex.setFiltering(Texture2D.FILTER_LINEAR, Texture2D.FILTER_LINEAR);
			tex.setWrapping(Texture2D.WRAP_CLAMP, Texture2D.WRAP_CLAMP);
			tex.setBlending(Texture2D.FUNC_MODULATE);
			ap = new Appearance();
			ap.setTexture(0, tex);
			ap.setMaterial(_material);
			ap.setCompositingMode(_compositing);
			ap.setPolygonMode(_polMode);

			// transform
			t = new Transform();
			t.postTranslate(x, y, 0);

			// quad
			// RT, LT, RB, LB
			short[] vert = { s, 0, 0, 0, 0, 0, s, s, 0, 0, s, 0 };
			short[] uv = { 1, 0, 0, 0, 1, 1, 0, 1 };

			VertexArray vertArray = new VertexArray(vert.length / 3, 3, 2);
			vertArray.set(0, vert.length / 3, vert);

			VertexArray texArray = new VertexArray(uv.length / 2, 2, 2);
			texArray.set(0, uv.length / 2, uv);

			VertexBuffer vb = new VertexBuffer();
			vb.setPositions(vertArray, 1.0f, null);
			vb.setTexCoords(0, texArray, 1.0f, null);
			vb.setDefaultColor(-1);
		}

		public void paint(Graphics3D g) {
			g.render(vb, ind, ap, t);
		}
	}

}
