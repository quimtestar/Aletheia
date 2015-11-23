/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
 *
 * This file is part of the Aletheia Proof Assistant.
 *
 * The Aletheia Proof Assistant is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * The Aletheia Proof Assistant is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with the Aletheia Proof Assistant. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package aletheia.gui.cli.command;

import aletheia.gui.cli.CliJPanel;
import aletheia.model.statement.Context;
import aletheia.persistence.Transaction;

public abstract class TransactionalCommand extends Command
{
	private final Transaction transaction;

	private CancelledCommandException cancelledCommandException;

	public TransactionalCommand(CliJPanel from, Transaction transaction)
	{
		super(from);
		this.transaction = transaction;
	}

	protected Transaction getTransaction()
	{
		return transaction;
	}

	public class RunTransactionalReturnData
	{
		public final Context newActiveContext;

		public RunTransactionalReturnData(Context newActiveContext)
		{
			super();
			this.newActiveContext = newActiveContext;
		}

	}

	@Override
	public void run() throws Exception
	{
		try
		{
			cancelledCommandException = null;
			lock(getTransaction());
			RunTransactionalReturnData data = runTransactional();
			transaction.commit();
			if (data != null && (data.newActiveContext != null))
				setActiveContext(data.newActiveContext);
		}
		catch (Exception e)
		{
			if (cancelledCommandException != null)
				throw cancelledCommandException;
			else
				throw e;
		}
		finally
		{
			transaction.abort();
		}

	}

	protected abstract RunTransactionalReturnData runTransactional() throws Exception;

	@Override
	public void cancel(String cause)
	{
		cancelledCommandException = makeCancelledCommandException(cause);
		transaction.abort();
	}

}
