/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
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

import aletheia.preferences.NodeAletheiaPreferences;

public class AppearanceAletheiaPreferences extends NodeAletheiaPreferences
{
	private final static String NODE_PATH = "appearance";

	private final static String FONT_SIZE = "font_size";

	private final static int defaultFontSize = 14;

	private final AletheiaJFrameBoundsPreferences aletheiaJFrameBoundsPreferences;

	protected AppearanceAletheiaPreferences(GUIAletheiaPreferences guiAletheiaPreferences)
	{
		super(guiAletheiaPreferences, NODE_PATH);
		this.aletheiaJFrameBoundsPreferences = new AletheiaJFrameBoundsPreferences(this);
	}

	public int getFontSize()
	{
		return getPreferences().getInt(FONT_SIZE, defaultFontSize);
	}

	public void setFontSize(int fontSize)
	{
		getPreferences().putInt(FONT_SIZE, fontSize);
	}

	public AletheiaJFrameBoundsPreferences aletheiaJFrameBounds()
	{
		return aletheiaJFrameBoundsPreferences;
	}

}
