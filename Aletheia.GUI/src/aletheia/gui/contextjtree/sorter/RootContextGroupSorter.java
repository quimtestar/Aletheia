package aletheia.gui.contextjtree.sorter;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.RootContext;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.collections.statement.GenericRootContextsMap;
import aletheia.persistence.collections.statement.SortedRootContexts;
import aletheia.utilities.MiscUtilities;

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
	public RootContext getStatement(Transaction transaction)
	{
		if (getPrefix() == null)
			return null;
		GenericRootContextsMap rcMap = persistenceManager.identifierToRootContexts(transaction).get(getPrefix());
		if (rcMap == null)
			return null;
		if (rcMap.size() != 1)
			return null;
		return MiscUtilities.firstFromCloseableIterable(rcMap.values());
	}

	@Override
	public SortedRootContexts sortedStatements(Transaction transaction)
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
