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
package aletheia.pdfexport.font;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;

public class FontManager
{
	public static final FontManager instance = new FontManager();

	private final BaseFont baseFont;

	public FontManager()
	{
		try
		{
			this.baseFont = BaseFont.createFont("aletheia/pdfexport/font/DejaVuSansMono.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
		}
		catch (Exception e)
		{
			throw new Error(e);
		}
	}

	public BaseFont getBaseFont()
	{
		return baseFont;
	}

	public Font getFont(float size, BaseColor color)
	{
		return new Font(baseFont, size, Font.NORMAL, color);
	}

}
