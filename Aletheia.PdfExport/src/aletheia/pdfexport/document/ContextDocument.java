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
import aletheia.persistence.Transaction;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;

public abstract class ContextDocument extends Document
{

	private final Transaction transaction;
	private final Context context;
	private final OutputStream outputStream;

	public ContextDocument(Transaction transaction, Context context, OutputStream outputStream)
	{
		super(PageSize.A4.rotate());
		this.transaction = transaction;
		this.context = context;
		this.outputStream = outputStream;
		makeDocument();
	}

	public ContextDocument(Rectangle pageSize, float marginLeft, float marginRight, float marginTop, float marginBottom, Transaction transaction,
			Context context, OutputStream outputStream)
	{
		super(pageSize, marginLeft, marginRight, marginTop, marginBottom);
		this.transaction = transaction;
		this.context = context;
		this.outputStream = outputStream;
		makeDocument();
	}

	public ContextDocument(Rectangle pageSize, Transaction transaction, Context context, OutputStream outputStream)
	{
		super(pageSize);
		this.transaction = transaction;
		this.context = context;
		this.outputStream = outputStream;
		makeDocument();
	}

	private void makeDocument()
	{
		try
		{
			PdfWriter.getInstance(this, outputStream);
			open();
			build();
			close();
		}
		catch (DocumentException e)
		{
			throw new RuntimeException(e);
		}
	}

	protected abstract void build() throws DocumentException;

	public Transaction getTransaction()
	{
		return transaction;
	}

	public Context getContext()
	{
		return context;
	}

	public OutputStream getOutputStream()
	{
		return outputStream;
	}

}
