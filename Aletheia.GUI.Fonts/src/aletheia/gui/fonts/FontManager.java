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
package aletheia.gui.fonts;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JLabel;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;

public class FontManager
{
	private final static Logger logger = LoggerManager.instance.logger();

	private final static String fontPath = "dejavu-fonts-ttf";

	private final static String baseFontFile = fontPath + "/ttf/DejaVuSansMono.ttf";
	private final static String baseBoldFontFile = fontPath + "/ttf/DejaVuSansMono-Bold.ttf";
	private final static String baseItalicFontFile = fontPath + "/ttf/DejaVuSansMono-Oblique.ttf";
	private final static String baseExpandFontFile = fontPath + "/ttf/DejaVuSans.ttf";

	private final Font baseFont;
	private final Font baseBoldFont;
	private final Font baseItalicFont;
	private final Font baseExpandFont;

	private static Font loadFont(String file) throws FontFormatException, IOException
	{
		try (InputStream is = ClassLoader.getSystemResourceAsStream(file))
		{
			return Font.createFont(Font.TRUETYPE_FONT, is);
		}
	}

	private int fontSize;

	public FontManager(int fontSize)
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
		this.baseFont = baseFont_;

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
		this.baseBoldFont = baseBoldFont_;

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
		this.baseItalicFont = baseItalicFont_;

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
		this.baseExpandFont = baseExpandFont_;

		this.fontSize = fontSize;
	}

	public int getFontSize()
	{
		return fontSize;
	}

	public void setFontSize(int fontSize)
	{
		this.fontSize = fontSize;
	}

	public Font defaultFont()
	{
		return baseFont.deriveFont(Font.PLAIN, getFontSize());
	}

	public Font boldFont()
	{
		return baseBoldFont.deriveFont(Font.BOLD, getFontSize());
	}

	public Font italicFont()
	{
		return baseItalicFont.deriveFont(Font.ITALIC, getFontSize());
	}

	public Font expandFont()
	{
		return baseExpandFont.deriveFont(Font.PLAIN, getFontSize());
	}

	public static FontMetrics fontMetrics(Font font)
	{
		return new JLabel().getFontMetrics(font);
	}

	public FontMetrics fontMetrics()
	{
		return fontMetrics(defaultFont());
	}

}
