package aletheia.gui.cli.command.statement;

import aletheia.gui.cli.CliJPanel;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.CloseableIterable;

public class DeleteStatementsCascade extends DeleteStatements
{

	public DeleteStatementsCascade(CliJPanel from, Transaction transaction, Context context, CloseableIterable<Statement> statements)
	{
		super(from, transaction, context, statements);
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		getContext().deleteStatementsCascade(getTransaction(), getStatements());
		return null;
	}

}
