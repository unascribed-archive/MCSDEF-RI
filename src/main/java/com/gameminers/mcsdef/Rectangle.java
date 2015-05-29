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

public class Rectangle {
	public int width, height, x, y;

	public Rectangle() {}
	
	public Rectangle(int width, int height, int x, int y) {
		this.width = width;
		this.height = height;
		this.x = x;
		this.y = y;
	}
	
}
