package aletheia.gui.cli.command.statement;

import aletheia.gui.cli.CliJPanel;
import aletheia.model.statement.RootContext;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.CloseableIterable;

public class DeleteRootContextsCascade extends DeleteRootContexts
{

	public DeleteRootContextsCascade(CliJPanel from, Transaction transaction, CloseableIterable<RootContext> rootContexts)
	{
		super(from, transaction, rootContexts);
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		RootContext.deleteCascade(getTransaction(), getRootContexts());
		return null;
	}

}
