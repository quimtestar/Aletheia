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
package aletheia.pdfexport.statement;

import java.util.Map;

import aletheia.model.identifier.Identifier;
import aletheia.model.term.VariableTerm;
import aletheia.pdfexport.BasePhrase;
import aletheia.pdfexport.BaseTable;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.PdfPCell;

public abstract class StatementOrConsequentTable extends BaseTable
{
	protected class MyCell extends PdfPCell
	{
		private final static float borderWidth = 0.2f;

		public final static int BORDER_LEFT = 0x01 << 0;
		public final static int BORDER_RIGHT = 0x01 << 1;
		public final static int BORDER_TOP = 0x01 << 2;
		public final static int BORDER_BOTTOM = 0x01 << 3;
		public final static int BORDER_ALL = BORDER_LEFT | BORDER_RIGHT | BORDER_TOP | BORDER_BOTTOM;

		private void border(int border)
		{
			this.setBorderColor(new BaseColor(256 - 32, 256 - 32, 256 - 32));
			this.setBorderWidth(0);
			if ((border & BORDER_LEFT) != 0)
				this.setBorderWidthLeft(borderWidth);
			if ((border & BORDER_RIGHT) != 0)
				this.setBorderWidthRight(borderWidth);
			if ((border & BORDER_TOP) != 0)
				this.setBorderWidthTop(borderWidth);
			if ((border & BORDER_BOTTOM) != 0)
				this.setBorderWidthBottom(borderWidth);
		}

		public MyCell(BasePhrase phrase, int border)
		{
			super(phrase);
			border(border);
		}

		public MyCell(BasePhrase phrase)
		{
			this(phrase, BORDER_ALL);
		}

	}

	private final Map<? extends VariableTerm, Identifier> variableToIdentifier;

	public StatementOrConsequentTable(int numColumns, Map<? extends VariableTerm, Identifier> variableToIdentifier)
	{
		super(numColumns);
		this.variableToIdentifier = variableToIdentifier;
		this.setSplitRows(false);
	}

	protected Map<? extends VariableTerm, Identifier> getVariableToIdentifier()
	{
		return variableToIdentifier;
	}

}
