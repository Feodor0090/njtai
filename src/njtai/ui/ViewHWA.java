package njtai.ui;

import java.io.ByteArrayOutputStream;

import javax.microedition.lcdui.*;
import javax.microedition.m3g.*;

import njtai.models.ExtMangaObj;

public class ViewHWA extends View {

	public ViewHWA(ExtMangaObj emo, Displayable prev, int page) {
		super(emo, prev, page);
	}

	protected void reset() {
		// TODO Auto-generated method stub

	}

	protected void prepare(ByteArrayOutputStream data) throws InterruptedException {
		// TODO Auto-generated method stub

	}

	protected void resize(int size) {
		// TODO Auto-generated method stub

	}

	protected void reload() {
		// TODO Auto-generated method stub

	}

	public boolean canDraw() {
		// TODO Auto-generated method stub
		return false;
	}

	protected void paint(Graphics g) {
		// TODO Auto-generated method stub

	}

	protected void setupM3G(Graphics3D g3d) {
		Camera cam = new Camera();
		cam.setParallel(360, 16f / 9f, 0.1f, 900f);
		Transform t = new Transform();
		t.postTranslate(0, 360 / 2, 100);
		Light l = new Light();
		l.setColor(0xffffff); // white light
		l.setIntensity(1f);
		l.setMode(Light.AMBIENT);

		g3d.setCamera(cam, t);
		g3d.resetLights();
		g3d.addLight(l, t);
	}

	static class PagePart {
		int size;
		Appearance ap;
		Transform t;
		VertexBuffer vb;
		IndexBuffer ind;

		protected Material _material = null;
		protected CompositingMode _compositing = null;
		protected PolygonMode _polMode = null;

		public PagePart(Image page, int x, int y, short s) {
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

			// cropping
			Image part = Image.createImage(s, s);
			Graphics pg = part.getGraphics();
			pg.setColor(0);
			pg.fillRect(0, 0, s, s);
			pg.drawImage(page, -x, -y, 0);
			Image spart = Image.createImage(part);
			part = null;

			// appearance
			Image2D image2D = new Image2D(Image2D.RGBA, spart);
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
