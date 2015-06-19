package aletheia.gui.contextjtree.sorter;

import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

public class ContextSorter extends StatementSorter
{

	protected ContextSorter(GroupSorter<? extends Statement> group, Context context)
	{
		super(group, context);
	}

	@Override
	public Context getStatement()
	{
		return (Context) super.getStatement();
	}

	@Override
	public Context getStatement(Transaction transaction)
	{
		return (Context) super.getStatement(transaction);
	}

	public ContextGroupSorter makeContextGroupSorter()
	{
		return new ContextGroupSorter(this);
	}

}
