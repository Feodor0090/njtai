/*
 * This file is a part of the TUBE42 imagelib, released under the LGPL license.
 *
 * Development page: https://github.com/tube42/imagelib
 * License:          http://www.gnu.org/copyleft/lesser.html
 */

package tube42;

import javax.microedition.lcdui.*;

/**
 * This class contains some functions that are common during image manipulation
 * 
 * 
 * In a perfect world, you wouldnt need this class since JSR-234 would do the
 * same work faster and better :(
 * 
 */

public final class ImgUtil {
	public static final int blend(final int c1, final int c2, final int value256) {

		final int v1 = value256 & 0xFF;
		final int c1_RB = c1 & 0x00FF00FF;
		final int c2_RB = c2 & 0x00FF00FF;

		final int c1_AG = (c1 >>> 8) & 0x00FF00FF;

		final int c2_AG_org = c2 & 0xFF00FF00;
		final int c2_AG = (c2_AG_org) >>> 8;

		// the world-famous tube42 blend with one mult per two components:
		final int rb = (c2_RB + (((c1_RB - c2_RB) * v1) >> 8)) & 0x00FF00FF;
		final int ag = (c2_AG_org + ((c1_AG - c2_AG) * v1)) & 0xFF00FF00;
		return ag | rb;

	}

	/**
	 * crop an image:
	 */
	public static Image crop(Image src_i, int x0, int y0, int x1, int y1) {
		if (x0 > x1) {
			int tmp = x0;
			x0 = x1;
			x1 = tmp;
		}
		if (y0 > y1) {
			int tmp = y0;
			y0 = y1;
			y1 = tmp;
		}

		Image ret = Image.createImage(x1 - x0 + 1, y1 - y0 + 1);
		Graphics g = ret.getGraphics();
		g.drawImage(src_i, -x0, -y0, 0);
		return ret;
	}

	/**
	 * resize an image:
	 */
	public static Image resize(Image src_i, int size_w, int size_h, boolean filter) {

		// set source size
		int w = src_i.getWidth();
		int h = src_i.getHeight();

		// no change?
		if (size_w == w && size_h == h)
			return src_i;

		int[] dst = new int[size_w * size_h];

		if (filter)
			resize_rgb_filtered(src_i, dst, w, h, size_w, size_h);
		else
			resize_rgb_unfiltered(src_i, dst, w, h, size_w, size_h);

		// not needed anymore
		src_i = null;

		return Image.createRGBImage(dst, size_w, size_h, true);
	}

	// ------------------------------------------------

	private static final void resize_rgb_unfiltered(Image src_i, int[] dst, int w0, int h0, int w1, int h1) {
		int[] buffer = new int[w0];

		// scale with no filtering
		int index1 = 0;
		int index0_y = 0;

		for (int y = 0; y < h1; y++) {
			int y_ = index0_y / h1;
			int index0_x = 0;
			src_i.getRGB(buffer, 0, w0, 0, y_, w0, 1);

			for (int x = 0; x < w1; x++) {
				int x_ = index0_x / w1;
				dst[index1++] = buffer[x_];

				// for next pixel
				index0_x += w0;
			}
			// For next line
			index0_y += h0;
		}
	}

	private static final void resize_rgb_filtered(Image src_i, int[] dst, int w0, int h0, int w1, int h1) {
		int[] buffer1 = new int[w0];
		int[] buffer2 = new int[w0];

		// UNOPTIMIZED bilinear filtering:
		//
		// The pixel position is defined by y_a and y_b,
		// which are 24.8 fixed point numbers
		//
		// for bilinear interpolation, we use y_a1 <= y_a <= y_b1
		// and x_a1 <= x_a <= x_b1, with y_d and x_d defining how long
		// from x/y_b1 we are.
		//
		// since we are resizing one line at a time, we will at most
		// need two lines from the source image (y_a1 and y_b1).
		// this will save us some memory but will make the algorithm
		// noticeably slower

		for (int index1 = 0, y = 0; y < h1; y++) {

			final int y_a = ((y * h0) << 8) / h1;
			final int y_a1 = y_a >> 8;
			int y_d = y_a & 0xFF;

			int y_b1 = y_a1 + 1;
			if (y_b1 >= h0) {
				y_b1 = h0 - 1;
				y_d = 0;
			}

			// get the two affected lines:
			src_i.getRGB(buffer1, 0, w0, 0, y_a1, w0, 1);
			if (y_d != 0)
				src_i.getRGB(buffer2, 0, w0, 0, y_b1, w0, 1);

			for (int x = 0; x < w1; x++) {
				// get this and the next point
				int x_a = ((x * w0) << 8) / w1;
				int x_a1 = x_a >> 8;
				int x_d = x_a & 0xFF;

				int x_b1 = x_a1 + 1;
				if (x_b1 >= w0) {
					x_b1 = w0 - 1;
					x_d = 0;
				}

				// interpolate in x
				int c12, c34;
				int c1 = buffer1[x_a1];
				int c3 = buffer1[x_b1];

				// interpolate in y:
				if (y_d == 0) {
					c12 = c1;
					c34 = c3;
				} else {
					int c2 = buffer2[x_a1];
					int c4 = buffer2[x_b1];

					c12 = blend(c2, c1, y_d);
					c34 = blend(c4, c3, y_d);
				}

				// final result
				dst[index1++] = blend(c34, c12, x_d);
			}
		}

	}

}
