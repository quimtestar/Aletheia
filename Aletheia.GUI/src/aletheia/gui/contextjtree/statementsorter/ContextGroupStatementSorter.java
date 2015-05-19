package aletheia.gui.contextjtree.statementsorter;

import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

public class ContextGroupStatementSorter extends GroupStatementSorter<Statement>
{

	public ContextGroupStatementSorter(Transaction transaction, Context context)
	{
		super(context, null, context.localSortedStatements(transaction));
	}

}
