package aletheia.gui.contextjtree.sorter.old;

import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

public class StatementRootGroupSorter extends RootGroupSorter<Statement>
{
	private final Context context;

	public StatementRootGroupSorter(Transaction transaction, Context context)
	{
		super(context.localSortedStatements(transaction));
		this.context=context;
	}

	public Context getContext()
	{
		return context;
	}
	
	
	

}
