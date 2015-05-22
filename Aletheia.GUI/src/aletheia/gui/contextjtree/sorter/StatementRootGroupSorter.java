package aletheia.gui.contextjtree.sorter;

import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

public class StatementRootGroupSorter extends RootGroupSorter<Statement>
{

	public StatementRootGroupSorter(Transaction transaction, Context context)
	{
		super(context, context.localSortedStatements(transaction));
	}

}
