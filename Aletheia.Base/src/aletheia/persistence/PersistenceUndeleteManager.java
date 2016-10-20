/*******************************************************************************
 * Copyright (c) 2016 Quim Testar.
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
package aletheia.persistence;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Stack;

import aletheia.model.statement.Statement;
import aletheia.model.statement.Statement.UndeleteStatementException;
import aletheia.utilities.collections.ReferenceStack;
import aletheia.utilities.collections.SoftReferenceStack;

public class PersistenceUndeleteManager
{
	private final Map<Transaction, Reference<Stack<Statement>>> batchMap;
	private final ReferenceStack<Stack<Statement>> undeleteStack;

	public PersistenceUndeleteManager()
	{
		this.batchMap = new HashMap<>();
		this.undeleteStack = new SoftReferenceStack<>();
	}

	public synchronized void push(Transaction transaction, Statement statement)
	{
		Reference<Stack<Statement>> ref = batchMap.get(transaction);
		Stack<Statement> batch;
		if (ref == null)
		{
			batch = new Stack<>();
			ref = new SoftReference<>(batch);
			batchMap.put(transaction, ref);
			transaction.runWhenClose(new Transaction.Hook()
			{
				@Override
				public void run(Transaction closedTransaction)
				{
					synchronized (PersistenceUndeleteManager.this)
					{
						Reference<Stack<Statement>> ref = batchMap.remove(closedTransaction);
						if (ref != null && closedTransaction.isCommited())
						{
							Stack<Statement> batch = ref.get();
							if (batch != null)
								undeleteStack.push(batch);
							else
								undeleteStack.clear();
						}
					}

				}
			});
		}
		else
			batch = ref.get();
		if (batch != null)
			batch.push(statement);
	}

	public class NoElementsUndeleteStatementException extends UndeleteStatementException
	{
		private static final long serialVersionUID = -8665452260262113011L;

		protected NoElementsUndeleteStatementException()
		{
			super("No more statements to undelete");
		}

	}

	public List<Statement> undelete(Transaction transaction) throws UndeleteStatementException
	{
		try
		{
			Stack<Statement> batch;
			synchronized (this)
			{
				batch = undeleteStack.pop();
			}
			List<Statement> list = new ArrayList<>();
			while (!batch.isEmpty())
				list.add(batch.pop().undelete(transaction));
			return list;
		}
		catch (NoSuchElementException e)
		{
			throw new NoElementsUndeleteStatementException();
		}
	}

}
