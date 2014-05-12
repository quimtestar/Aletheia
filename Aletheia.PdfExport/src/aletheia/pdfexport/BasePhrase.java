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
package aletheia.pdfexport;

import aletheia.pdfexport.font.FontManager;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;

public class BasePhrase extends Phrase
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 547941404454426747L;
	protected final static float fontSize = 6;

	public BasePhrase()
	{
		super();
		Font font = FontManager.instance.getFont(fontSize, BaseColor.BLACK);
		this.setFont(font);
		this.setLeading((float) (getFont().getSize() * 1.2));
	}

	protected void addSimpleChunk(SimpleChunk chunk)
	{
		add(chunk);
	}

	protected void addBasePhrase(BasePhrase phrase)
	{
		add(phrase);
	}

	public float getWidthPoint()
	{
		float width = 0;
		for (Chunk chunk : getChunks())
			width += chunk.getWidthPoint();
		return width;
	}

}
