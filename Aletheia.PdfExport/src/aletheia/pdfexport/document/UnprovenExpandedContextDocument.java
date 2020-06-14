/*******************************************************************************
 * Copyright (c) 2014, 2020 Quim Testar.
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
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;

import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.pdfexport.statement.ConsequentTable;
import aletheia.pdfexport.statement.StatementTable;
import aletheia.persistence.Transaction;

public class UnprovenExpandedContextDocument extends ContextDocument
{

	public UnprovenExpandedContextDocument(Rectangle pageSize, float marginLeft, float marginRight, float marginTop, float marginBottom,
			Transaction transaction, Context context, OutputStream outputStream)
	{
		super(pageSize, marginLeft, marginRight, marginTop, marginBottom, transaction, context, outputStream);
	}

	public UnprovenExpandedContextDocument(Rectangle pageSize, Transaction transaction, Context context, OutputStream outputStream)
	{
		super(pageSize, transaction, context, outputStream);
	}

	public UnprovenExpandedContextDocument(Transaction transaction, Context context, OutputStream outputStream)
	{
		super(transaction, context, outputStream);
	}

	@Override
	protected void build() throws DocumentException
	{
		class StackEntry
		{
			public final Context context;
			public final Queue<Statement> queue;

			public StackEntry(Context context)
			{
				this.context = context;
				this.queue = new LinkedList<>(context.localDependencySortedStatements(getTransaction()));
			}
		}
		Stack<StackEntry> stack = new Stack<>();
		add(StatementTable.statementTable(this, stack.size(), getTransaction(), getContext()));
		stack.push(new StackEntry(getContext()));
		while (!stack.isEmpty())
		{
			StackEntry e = stack.peek();
			if (!e.queue.isEmpty())
			{
				Statement st = e.queue.poll();
				add(StatementTable.statementTable(this, stack.size(), getTransaction(), st));
				if ((st instanceof Context) && (!st.isProved()))
					stack.push(new StackEntry((Context) st));
			}
			else
			{
				add(new ConsequentTable(this, stack.size(), getTransaction(), e.context));
				stack.pop();
			}
		}
	}

}
