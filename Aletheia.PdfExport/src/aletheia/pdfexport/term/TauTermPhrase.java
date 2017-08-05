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
package aletheia.pdfexport.term;

import com.itextpdf.text.Font;

import aletheia.model.term.TauTerm;
import aletheia.pdfexport.SimpleChunk;
import aletheia.pdfexport.font.FontManager;

public class TauTermPhrase extends TermPhrase
{
	private static final long serialVersionUID = 6308964401763707063L;

	protected TauTermPhrase(TauTerm type)
	{
		super(type);
		addSimpleChunk(new SimpleChunk("\u03a4", FontManager.instance.getFont(fontSize, Font.BOLD, fontColor)));
	}

	@Override
	public TauTerm getTerm()
	{
		return (TauTerm) super.getTerm();
	}

}
