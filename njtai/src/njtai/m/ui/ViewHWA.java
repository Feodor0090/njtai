package njtai.m.ui;

import java.io.ByteArrayOutputStream;

import javax.microedition.lcdui.*;
import javax.microedition.m3g.*;

import njtai.m.NJTAIM;
import njtai.models.ExtMangaObj;

/**
 * {@link View} implementation, that uses M3G for real-time scaling.
 * 
 * @author Feodor0090
 */
public class ViewHWA extends View {

	/**
	 * Creates the view.
	 * 
	 * @param emo  Object with data.
	 * @param prev Previous screen.
	 * @param page Number of page to start.
	 */
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

		// strip
		_ind = new TriangleStripArray(0, new int[] { 4 });
	}

	protected Material _material;
	protected CompositingMode _compositing;
	protected PolygonMode _polMode;
	protected TriangleStripArray _ind;

	World w = null;
	int iw, ih;

	protected void reset() {
		w = null;
	}

	protected void prepare(ByteArrayOutputStream data) throws InterruptedException {
		reset();
		byte[] d = data.toByteArray();
		Image i = Image.createImage(d, 0, d.length);
		d = null;
		ih = i.getHeight();
		iw = i.getWidth();
		int s = 512;
		for (int ix = 0; ix < i.getWidth() + s - 1; ix += s) {
			for (int iy = 0; iy < i.getHeight() + s - 1; iy += s) {
				w.addChild(createTile(i, ix, iy, (short) s));
			}
		}
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
		return w != null;
	}

	protected void paint(Graphics g) {
		try {
			Font f = Font.getFont(0, 0, 8);
			g.setFont(f);

			// bg fill

			if (w == null) {
				g.setGrayScale(0);
				g.fillRect(0, 0, getWidth(), getHeight());
				paintNullImg(g, f);
				g.setColor(0, 0, 255);
				g.fillRect(0, 0, getWidth(), 4);
				g.drawString(iw + "x" + ih, getWidth() / 2, 4, Graphics.TOP | Graphics.HCENTER);
			} else {
				limitOffset();
				Graphics3D g3 = Graphics3D.getInstance();
				g3.bindTarget(g, false, Graphics3D.ANTIALIAS);
				try {
					Background b = new Background();
					Transform id = new Transform();
					id.setIdentity();
					b.setColorClearEnable(true);
					b.setDepthClearEnable(true);
					g3.clear(b);
					setupM3G(g3);
					g3.render(w);
				} catch (Throwable t) {
					t.printStackTrace();
				}
				g3.releaseTarget();
				// touch captions
				if (hasPointerEvents() && touchCtrlShown) {
					drawTouchControls(g, f);
				}
			}
			paintHUD(g, f, false, !touchCtrlShown || !hasPointerEvents());
		} catch (Exception e) {
			e.printStackTrace();

			try {
				NJTAIM.setScr(new Alert("Repaint error", e.toString(), null, AlertType.ERROR));
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
		t.postRotate(180, 0, 0, -1);
		t.postScale(-1, 1, 1);
		Light l = new Light();
		l.setColor(0xffffff); // white light
		l.setIntensity(1f);
		l.setMode(Light.AMBIENT);

		g3d.setCamera(cam, t);
		g3d.resetLights();
		g3d.addLight(l, t);
	}

	protected Node createTile(Image i, int px, int py, short s) {

		Transform t;
		VertexBuffer vb;

		// cropping
		Image part = Image.createImage(s, s);
		Graphics pg = part.getGraphics();
		pg.setColor(0);
		pg.fillRect(0, 0, s, s);
		pg.drawRegion(i, px, py, Math.min(s, i.getWidth() - px), Math.min(s, i.getHeight() - py), 0, 0, 0, 0);
		System.gc();

		// appearance
		Image2D image2D = new Image2D(Image2D.RGB, part);
		Texture2D tex = new Texture2D(image2D);
		tex.setFiltering(Texture2D.FILTER_LINEAR, Texture2D.FILTER_LINEAR);
		tex.setWrapping(Texture2D.WRAP_CLAMP, Texture2D.WRAP_CLAMP);
		tex.setBlending(Texture2D.FUNC_MODULATE);

		Appearance ap = new Appearance();
		ap.setTexture(0, tex);
		ap.setMaterial(_material);
		ap.setCompositingMode(_compositing);
		ap.setPolygonMode(_polMode);

		// transform
		t = new Transform();
		t.postTranslate(px, py, 0);

		// quad
		// RT, LT, RB, LB
		short[] vert = { s, 0, 0, 0, 0, 0, s, s, 0, 0, s, 0 };
		short[] uv = { 1, 0, 0, 0, 1, 1, 0, 1 };

		VertexArray vertArray = new VertexArray(vert.length / 3, 3, 2);
		vertArray.set(0, vert.length / 3, vert);

		VertexArray texArray = new VertexArray(uv.length / 2, 2, 2);
		texArray.set(0, uv.length / 2, uv);

		vb = new VertexBuffer();
		vb.setPositions(vertArray, 1.0f, null);
		vb.setTexCoords(0, texArray, 1.0f, null);
		vb.setDefaultColor(-1);

		Mesh m = new Mesh(vb, _ind, ap);
		m.setTransform(t);
		return m;
	}

	protected int panDeltaMul() {
		return -1 * super.panDeltaMul();
	}

	protected boolean useSmoothZoom() {
		return true;
	}

}
