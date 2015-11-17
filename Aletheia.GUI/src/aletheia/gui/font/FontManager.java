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
package aletheia.gui.font;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JLabel;

import org.apache.logging.log4j.Logger;

import aletheia.gui.preferences.GUIAletheiaPreferences;
import aletheia.log4j.LoggerManager;

public class FontManager
{
	private final static Logger logger = LoggerManager.instance.logger();

	private final static String fontPath = "dejavu-fonts-ttf";

	private final static String baseFontFile = fontPath + "/ttf/DejaVuSansMono.ttf";
	private final static String baseBoldFontFile = fontPath + "/ttf/DejaVuSansMono-Bold.ttf";
	private final static String baseItalicFontFile = fontPath + "/ttf/DejaVuSansMono-Oblique.ttf";
	private final static String baseExpandFontFile = fontPath + "/ttf/DejaVuSans.ttf";

	public final static FontManager instance = new FontManager();

	private final Font baseFont;
	private final Font baseBoldFont;
	private final Font baseItalicFont;
	private final Font baseExpandFont;

	private static Font loadFont(String file) throws FontFormatException, IOException
	{
		InputStream is;
		is = ClassLoader.getSystemResourceAsStream(file);
		try
		{
			return Font.createFont(Font.TRUETYPE_FONT, is);
		}
		finally
		{
			if (is != null)
				try
				{
					is.close();
				}
				catch (IOException e)
				{
				}
		}
	}

	private FontManager()
	{
		Font baseFont_;
		try
		{
			baseFont_ = loadFont(baseFontFile);
		}
		catch (Exception e1)
		{
			logger.warn("Couldn't load plain base font", e1);
			baseFont_ = new Font(Font.MONOSPACED, Font.PLAIN, 1);
		}
		baseFont = baseFont_;

		Font baseBoldFont_;
		try
		{
			baseBoldFont_ = loadFont(baseBoldFontFile);
		}
		catch (Exception e1)
		{
			logger.warn("Couldn't load bold base font", e1);
			baseBoldFont_ = new Font(Font.MONOSPACED, Font.BOLD, 1);
		}
		baseBoldFont = baseBoldFont_;

		Font baseItalicFont_;
		try
		{
			baseItalicFont_ = loadFont(baseItalicFontFile);
		}
		catch (Exception e1)
		{
			logger.warn("Couldn't load base italic base font", e1);
			baseItalicFont_ = new Font(Font.MONOSPACED, Font.ITALIC, 1);
		}
		baseItalicFont = baseItalicFont_;

		Font baseExpandFont_;
		try
		{
			baseExpandFont_ = loadFont(baseExpandFontFile);
		}
		catch (Exception e1)
		{
			logger.warn("Couldn't load expand base font", e1);
			baseExpandFont_ = new Font(Font.DIALOG, Font.PLAIN, 1);
		}
		baseExpandFont = baseExpandFont_;
	}

	private static int fontSize()
	{
		return GUIAletheiaPreferences.instance.appearance().getFontSize();
	}

	public Font defaultFont()
	{
		return baseFont.deriveFont(Font.PLAIN, fontSize());
	}

	public Font boldFont()
	{
		return baseBoldFont.deriveFont(Font.BOLD, fontSize());
	}

	public Font italicFont()
	{
		return baseItalicFont.deriveFont(Font.ITALIC, fontSize());
	}

	public Font expandFont()
	{
		return baseExpandFont.deriveFont(Font.PLAIN, fontSize());
	}

	public FontMetrics fontMetrics(Font font)
	{
		return new JLabel().getFontMetrics(font);
	}
}