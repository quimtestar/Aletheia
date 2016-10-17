package aletheia.persistence;

import java.util.NoSuchElementException;
import java.util.Stack;

import aletheia.model.statement.Statement;
import aletheia.utilities.collections.ReferenceStack;
import aletheia.utilities.collections.SoftReferenceStack;

public class PersistenceUndeleteManager
{
	private final PersistenceManager persistenceManager;

	private final ReferenceStack<Stack<Statement>> undeleteStack;

	public PersistenceUndeleteManager(PersistenceManager persistenceManager)
	{
		this.persistenceManager = persistenceManager;
		this.undeleteStack = new SoftReferenceStack<>();
	}

	public void openBatch()
	{
		undeleteStack.push(new Stack<Statement>());
	}

	public void push(Statement statement)
	{
		Stack<Statement> batch = undeleteStack.peek();
		if (batch == null)
		{
			batch = new Stack<>();
			undeleteStack.push(batch);
		}
		batch.push(statement);
	}

	public void undelete(Transaction transaction)
	{
		try
		{
			Stack<Statement> batch = undeleteStack.pop();
			while (!batch.isEmpty())
			{
				Statement statement = batch.pop();
				statement.undelete(transaction);
			}
		}
		catch (NoSuchElementException e)
		{
			//TODO
			throw e;
		}
	}

}
