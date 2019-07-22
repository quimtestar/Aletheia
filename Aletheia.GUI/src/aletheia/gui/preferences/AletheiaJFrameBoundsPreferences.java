/*******************************************************************************
 * Copyright (c) 2014, 2019 Quim Testar.
 *
 * This file is part of the Aletheia Proof Assistant.
 *
 * The Aletheia Proof Assistant is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * The Aletheia Proof Assistant is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with the Aletheia Proof Assistant. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package aletheia.gui.preferences;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import aletheia.preferences.NodeAletheiaPreferences;

public class AletheiaJFrameBoundsPreferences extends NodeAletheiaPreferences
{
	private final static String NODE_PATH = "aletheiajframe_bounds";

	private final static String PREFERRED_WIDTH = "preferred_width";

	private final static String PREFERRED_HEIGHT = "preferred_height";

	private final static String LOCATION_X = "location_x";

	private final static String LOCATION_Y = "location_y";

	private final static int defaultPreferredWidth = 800;

	private final static int defaultPreferredHeight = 600;

	private final static int defaultLocationX = 0;

	private final static int defaultLocationY = 0;

	protected AletheiaJFrameBoundsPreferences(AppearanceAletheiaPreferences appearanceAletheiaPreferences)
	{
		super(appearanceAletheiaPreferences, NODE_PATH);
	}

	public int getPreferredWidth()
	{
		return getPreferences().getInt(PREFERRED_WIDTH, defaultPreferredWidth);
	}

	public void setPreferredWidth(int preferredWidth)
	{
		getPreferences().putInt(PREFERRED_WIDTH, preferredWidth);
	}

	public int getPreferredHeight()
	{
		return getPreferences().getInt(PREFERRED_HEIGHT, defaultPreferredHeight);
	}

	public void setPreferredHeight(int preferredHeight)
	{
		getPreferences().putInt(PREFERRED_HEIGHT, preferredHeight);
	}

	public Dimension getPreferredSize()
	{
		return new Dimension(getPreferredWidth(), getPreferredHeight());
	}

	public void setPreferredSize(Dimension preferredSize)
	{
		setPreferredWidth((int) preferredSize.getWidth());
		setPreferredHeight((int) preferredSize.getHeight());
	}

	public int getLocationX()
	{
		return getPreferences().getInt(LOCATION_X, defaultLocationX);
	}

	public void setLocationX(int locationX)
	{
		getPreferences().putInt(LOCATION_X, locationX);
	}

	public int getLocationY()
	{
		return getPreferences().getInt(LOCATION_Y, defaultLocationY);
	}

	public void setLocationY(int locationY)
	{
		getPreferences().putInt(LOCATION_Y, locationY);
	}

	public Point getLocation()
	{
		return new Point(getLocationX(), getLocationY());
	}

	public void setLocation(Point location)
	{
		setLocationX(location.x);
		setLocationY(location.y);
	}

	public Rectangle getBounds()
	{
		return new Rectangle(getLocation(), getPreferredSize());
	}

	public void setBounds(Rectangle rectangle)
	{
		setLocation(rectangle.getLocation());
		setPreferredSize(rectangle.getSize());
	}

}
