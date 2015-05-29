/*
 * This file is part of the MCSDEF Reference Implementation.
 *
 * The MCSDEF Reference Implementation is free software: you can redistribute
 * it and/or modify it under the terms  of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * The MCSDEF Reference Implementation is distributed in the hope that it will be useful,
 * but WITHOUT ANY  WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A  PARTICULAR PURPOSE.  See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with the MCSDEF Reference Implementation. If not, see <http://www.gnu.org/licenses/>.
 */
package com.gameminers.mcsdef;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class SkinIO {
	/**
	 * If this flag is set, the legacy area will be used. Setting this flag is not
	 * recommended as it severely impacts compatibility with earlier skin data formats.
	 */
	public static final int FLAG_USE_LEGACY_AREA = 1;
	
	private static final List<Rectangle> rects = new ArrayList<Rectangle>();
	private static final List<Rectangle> rectsExt = new ArrayList<Rectangle>();
	static {
		rects.add(new Rectangle( 8,  8,  0,  0));
		rects.add(new Rectangle(16,  8, 24,  0));
		rects.add(new Rectangle( 8,  8, 56,  0));
		rects.add(new Rectangle( 4,  4,  0, 16));
		rects.add(new Rectangle( 8,  4, 12, 16));
		rects.add(new Rectangle( 8,  4, 36, 16));
		rects.add(new Rectangle( 4,  4, 52, 16));
		rects.add(new Rectangle( 8, 16, 56, 16));
		
		rectsExt.addAll(rects);
		rectsExt.add(new Rectangle( 4,  4,  0, 32));
		rectsExt.add(new Rectangle( 8,  4, 12, 32));
		rectsExt.add(new Rectangle( 8,  4, 36, 32));
		rectsExt.add(new Rectangle( 4,  4, 52, 32));
		rectsExt.add(new Rectangle( 8, 16, 56, 32));
		rectsExt.add(new Rectangle( 4,  4,  0, 48));
		rectsExt.add(new Rectangle( 8,  4, 12, 48));
		rectsExt.add(new Rectangle( 8,  4, 28, 48));
		rectsExt.add(new Rectangle( 8,  4, 44, 48));
		rectsExt.add(new Rectangle( 4,  4, 60, 48));
	}
	
	public static void main(String[] args) throws IOException {
		File f = new File("test.png");
		BufferedImage in = ImageIO.read(f);
		BufferedImage test = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = test.createGraphics();
		g2d.drawImage(in, 0, 0, null);
		g2d.dispose();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		dos.writeUTF("Simple test.");
		dos.flush();
		byte[] data = baos.toByteArray();
		System.out.println("In:");
		for (byte b : data) {
			System.out.printf("%02x ", b);
		}
		System.out.println();
		System.out.println();
		System.out.println();
		write(test, data, 0);
		
		ImageIO.write(test, "PNG", f);
		test = ImageIO.read(f);
		
		byte[] bys = read(test, 0);
		System.out.println("Out:");
		for (byte b : bys) {
			System.out.printf("%02x ", b);
		}
		ByteArrayInputStream bais = new ByteArrayInputStream(bys);
		DataInputStream dis = new DataInputStream(bais);
		System.out.println(dis.readUTF());
	}
	
	/**
	 * Writes binary data to a BufferedImage, using the rectangles defined by MCSDEF.
	 * 
	 * @param target A 64x32 or 64x64 Minecraft skin, which will have the data written to it.
	 * @param data The data to write. If the skin is 64x32, this cannot be larger than 1920,
	 *             and for a 64x64 skin, this cannot be larger than 3328. If USE_LEGACY_AREA
	 *             is not set, 256 bytes of possible length are lost.
	 * @param flags A set of bitflags to enable special behavior. See the fields on this class
	 *              to see what possible values for this are. For multiple flags, do
	 *              <code>FLAG_A | FLAG_B</code>. To set no flags, pass <code>0</code>.
	 */
	public static void write(BufferedImage target, byte[] data, int flags) {
		if (target.getWidth() != 64 || (target.getHeight() != 64 && target.getHeight() != 32)) {
			throw new IllegalArgumentException("invalid target; skins must be 64x32 or 64x64, got "+target.getWidth()+"x"+target.getHeight());
		}
		boolean skipFirst = true;
		int len = target.getHeight() < 64 ? 1920 : 3328;
		if ((flags & FLAG_USE_LEGACY_AREA) != 0) {
			skipFirst = false;
		} else {
			len -= 256;
		}
		if (data.length > len) {
			throw new IllegalArgumentException("data too large for this skin; "+len+" bytes available, got "+data.length+" bytes");
		}
		int idx = 0;
		for (Rectangle r : target.getHeight() < 64 ? rects : rectsExt) {
			if (skipFirst) {
				skipFirst = false;
				continue;
			}
			for (int y = r.y; y < r.y+r.height; y++) {
				for (int x = r.x; x < r.x+r.width; x++) {
					int rgb = 0;
					rgb |= (goz(data, idx)   << 16) & 0xFF0000;
					rgb |= (goz(data, idx+1) << 8 ) & 0xFF00;
					rgb |= (goz(data, idx+2)      ) & 0xFF;
					rgb |= (goz(data, idx+3) << 24) & 0xFF000000;
					target.setRGB(x, y, rgb);
					idx += 4;
				}
			}
		}
	}
	
	/**
	 * Reads binary data from a BufferedImage, using the rectangles defined by MCSDEF.
	 * 
	 * @param source A 64x32 or 64x64 Minecraft skin, which will have the data read from it.
	 * @param flags A set of bitflags to enable special behavior. See the fields on this class
	 *              to see what possible values for this are. For multiple flags, do
	 *              <code>FLAG_A | FLAG_B</code>. To set no flags, pass <code>0</code>.
	 * @return A byte array, containing the data found in this skin. Depending on the size of
	 *         the skin, and whether or not USE_LEGACY_AREA was set, this will be 1920,
	 *         1664, 3328, or 3072 bytes long.
	 */
	public static byte[] read(BufferedImage source, int flags) {
		if (source.getWidth() != 64 || (source.getHeight() != 64 && source.getHeight() != 32)) {
			throw new IllegalArgumentException("invalid source; skins must be 64x32 or 64x64, got "+source.getWidth()+"x"+source.getHeight());
		}
		boolean skipFirst = true;
		int len = source.getHeight() < 64 ? 1920 : 3328;
		if ((flags & FLAG_USE_LEGACY_AREA) != 0) {
			skipFirst = false;
		} else {
			len -= 256;
		}
		byte[] out = new byte[len];
		int idx = 0;
		for (Rectangle r : source.getHeight() < 64 ? rects : rectsExt) {
			if (skipFirst) {
				skipFirst = false;
				continue;
			}
			for (int y = r.y; y < r.y+r.height; y++) {
				for (int x = r.x; x < r.x+r.width; x++) {
					int rgb = source.getRGB(x, y);
					out[idx  ] = (byte)((rgb >>> 16) & 0xFF);
					out[idx+1] = (byte)((rgb >>>  8) & 0xFF);
					out[idx+2] = (byte)((rgb       ) & 0xFF);
					out[idx+3] = (byte)((rgb >>> 24) & 0xFF);
					idx+=4;
				}
			}
		}
		return out;
	}
	
	/**
	 * <b>G</b>et <b>O</b>r <b>Z</b>ero
	 */
	private static byte goz(byte[] data, int idx) {
		if (idx < 0 || idx >= data.length) {
			return 0;
		}
		return data[idx];
	}
}
