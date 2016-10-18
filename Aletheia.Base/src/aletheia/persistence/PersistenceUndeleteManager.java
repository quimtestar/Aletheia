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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;

import aletheia.model.statement.Statement;
import aletheia.model.statement.Statement.UndeleteStatementException;
import aletheia.utilities.collections.ReferenceStack;
import aletheia.utilities.collections.SoftReferenceStack;

public class PersistenceUndeleteManager
{
	private final static int maxBatchSize = 128;

	private final ReferenceStack<Stack<Statement>> undeleteStack;

	public PersistenceUndeleteManager()
	{
		this.undeleteStack = new SoftReferenceStack<>();
	}

	public void openBatch()
	{
		undeleteStack.push(new Stack<Statement>());
	}

	public void push(Statement statement)
	{
		Stack<Statement> batch = undeleteStack.peek();
		if (batch == null || batch.size() >= maxBatchSize)
		{
			batch = new Stack<>();
			undeleteStack.push(batch);
		}
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
			Stack<Statement> batch = undeleteStack.pop();
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
