package aletheia.gui.contextjtree.sorter;

import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

public class ContextGroupSorter extends StatementGroupSorter
{

	public ContextGroupSorter(Context context)
	{
		super(null, null, context);
	}

	@Override
	public Statement getStatement(Transaction transaction)
	{
		return getContext();
	}

}
