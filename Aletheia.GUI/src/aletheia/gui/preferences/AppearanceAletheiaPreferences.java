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

import aletheia.gui.lookandfeel.AletheiaLookAndFeel;
import aletheia.preferences.NodeAletheiaPreferences;

public class AppearanceAletheiaPreferences extends NodeAletheiaPreferences
{
	private final static String NODE_PATH = "appearance";

	private final static String THEME = "theme";

	private final static String FONT_SIZE = "font_size";

	private final static String COMPACTATION_THRESHOLD = "compactation_threshold";

	private final static AletheiaLookAndFeel.Theme defaultTheme = AletheiaLookAndFeel.Theme.Light;

	private final static int defaultFontSize = 14;

	private final static int defaultCompactationThreshold = 128;

	private final AletheiaJFrameBoundsPreferences aletheiaJFrameBoundsPreferences;

	protected AppearanceAletheiaPreferences(GUIAletheiaPreferences guiAletheiaPreferences)
	{
		super(guiAletheiaPreferences, NODE_PATH);
		this.aletheiaJFrameBoundsPreferences = new AletheiaJFrameBoundsPreferences(this);
	}

	public AletheiaLookAndFeel.Theme getTheme()
	{
		try
		{
			return AletheiaLookAndFeel.Theme.valueOf(getPreferences().get(THEME, defaultTheme.name()));
		}
		catch (IllegalArgumentException e)
		{
			return defaultTheme;
		}
	}

	public void setTheme(AletheiaLookAndFeel.Theme theme)
	{
		getPreferences().put(THEME, theme.name());
	}

	public int getFontSize()
	{
		return getPreferences().getInt(FONT_SIZE, defaultFontSize);
	}

	public void setFontSize(int fontSize)
	{
		getPreferences().putInt(FONT_SIZE, fontSize);
	}

	public int getCompactationThreshold()
	{
		return getPreferences().getInt(COMPACTATION_THRESHOLD, defaultCompactationThreshold);
	}

	public void setCompactationThreshold(int compactationThreshold)
	{
		getPreferences().putInt(COMPACTATION_THRESHOLD, compactationThreshold);
	}

	public AletheiaJFrameBoundsPreferences aletheiaJFrameBounds()
	{
		return aletheiaJFrameBoundsPreferences;
	}

}
