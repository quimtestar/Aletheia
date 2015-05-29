package aletheia.gui.contextjtree.sorter;

import aletheia.model.statement.RootContext;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;

public class RootContextRootGroupSorter extends RootGroupSorter<RootContext>
{

	public RootContextRootGroupSorter(PersistenceManager persistenceManager, Transaction transaction)
	{
		super(persistenceManager.sortedRootContexts(transaction));
	}

}
