package aletheia.gui.contextjtree.sorter;

import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;

public class RootGroupSorter extends RootContextGroupSorter
{

	public RootGroupSorter(PersistenceManager persistenceManager)
	{
		super(null, null, persistenceManager);
	}

	@Override
	public RootContextGroupSorter getGroup()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean degenerate(Transaction transaction)
	{
		return false;
	}

}
