package aletheia.gui.contextjtree.statementsorter;

import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

public class ContextGroupStatementSorter extends RootGroupStatementSorter<Statement>
{

	public ContextGroupStatementSorter(Transaction transaction, Context context)
	{
		super(context, context.localSortedStatements(transaction));
	}

}
