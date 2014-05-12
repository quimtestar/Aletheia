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
package aletheia.pdfexport.document;

import java.io.OutputStream;

import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.pdfexport.statement.ConsequentTable;
import aletheia.pdfexport.statement.StatementTable;
import aletheia.persistence.Transaction;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;

public class UnexpandedContextDocument extends ContextDocument
{

	public UnexpandedContextDocument(Rectangle pageSize, float marginLeft, float marginRight, float marginTop, float marginBottom, Transaction transaction,
			Context context, OutputStream outputStream)
	{
		super(pageSize, marginLeft, marginRight, marginTop, marginBottom, transaction, context, outputStream);
	}

	public UnexpandedContextDocument(Rectangle pageSize, Transaction transaction, Context context, OutputStream outputStream)
	{
		super(pageSize, transaction, context, outputStream);
	}

	public UnexpandedContextDocument(Transaction transaction, Context context, OutputStream outputStream)
	{
		super(transaction, context, outputStream);
	}

	@Override
	protected void build() throws DocumentException
	{
		add(StatementTable.statementTable(this, 0, getTransaction(), getContext()));
		for (Statement st : getContext().localDependencySortedStatements(getTransaction()))
		{
			add(StatementTable.statementTable(this, 1, getTransaction(), st));
		}
		add(new ConsequentTable(this, 0, getTransaction(), getContext()));
	}

}
