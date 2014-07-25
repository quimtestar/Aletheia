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

import aletheia.model.statement.Context;
import aletheia.pdfexport.BasePhrase;
import aletheia.pdfexport.SimpleChunk;
import aletheia.persistence.Transaction;

import com.itextpdf.text.Document;

public class ContextTable extends StatementTable
{

	protected class ContextPhrase extends BasePhrase
	{
		private static final long serialVersionUID = 1733632129361357209L;

		public ContextPhrase()
		{
			super();
			addSimpleChunk(new SimpleChunk("Context"));
		}
	}

	public ContextTable(Document doc, int depth, Transaction transaction, Context statement)
	{
		super(doc, depth, transaction, statement);
		addContextCell();
	}

	protected void addContextCell()
	{
		addCell(new MyCell(new ContextPhrase()));
	}

	@Override
	public Context getStatement()
	{
		return (Context) super.getStatement();
	}

}
