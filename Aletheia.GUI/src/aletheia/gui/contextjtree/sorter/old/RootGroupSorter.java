package aletheia.gui.contextjtree.sorter.old;

import aletheia.model.statement.Statement;
import aletheia.persistence.collections.statement.SortedStatements;

public abstract class RootGroupSorter<S extends Statement> extends GroupSorter<S>
{
	protected RootGroupSorter(SortedStatements<S> sortedStatements)
	{
		super(null, null, sortedStatements);
	}

}
