package aletheia.gui.contextjtree.sorter;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.RootContext;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.collections.statement.SortedRootContexts;

public class RootContextGroupSorter extends GroupSorter<RootContext>
{
	private final PersistenceManager persistenceManager;

	protected RootContextGroupSorter(RootContextGroupSorter group, Identifier prefix, PersistenceManager persistenceManager)
	{
		super(group, prefix);
		this.persistenceManager = persistenceManager;
	}

	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	@Override
	public RootContextGroupSorter getGroup()
	{
		return (RootContextGroupSorter) super.getGroup();
	}

	@Override
	protected SortedRootContexts sortedStatements(Transaction transaction)
	{
		SortedRootContexts sortedStatements = persistenceManager.sortedRootContexts(transaction);
		if (getPrefix() != null)
			return sortedStatements.subSet(getPrefix(), getPrefix().terminator());
		else
			return sortedStatements;
	}

	@Override
	protected RootContextGroupSorter subGroupSorter(Identifier prefix)
	{
		return new RootContextGroupSorter(this, prefix, persistenceManager);
	}

}
