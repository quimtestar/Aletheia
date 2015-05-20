package aletheia.gui.contextjtree.statementsorter;

import aletheia.model.statement.RootContext;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;

public class RootContextGroupStatementSorter extends RootGroupStatementSorter<RootContext>
{

	public RootContextGroupStatementSorter(PersistenceManager persistenceManager, Transaction transaction)
	{
		super(null, persistenceManager.sortedRootContexts(transaction));
	}

}
