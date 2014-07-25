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
package aletheia.gui.cli;

import org.apache.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.model.statement.Context;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.prooffinder.Proof;
import aletheia.prooffinder.Proof.ExecutionException;
import aletheia.prooffinder.ProofFinder.Listener;

public class ProofFinderExecutor implements Listener
{
	private final static Logger logger = LoggerManager.logger();

	private final PersistenceManager persistenceManager;
	private final CliJPanel cliJPanel;

	public ProofFinderExecutor(PersistenceManager persistenceManager, CliJPanel cliJPanel)
	{
		this.persistenceManager = persistenceManager;
		this.cliJPanel = cliJPanel;
	}

	@Override
	public void contextProved(Context context, Proof proof)
	{
		Transaction transaction = persistenceManager.beginTransaction();
		try
		{
			try
			{
				proof.execute(transaction);
				cliJPanel.message("Context:" + context.identifier(transaction) + " proved!");
				transaction.commit();
			}
			catch (ExecutionException e)
			{
				logger.error("Error", e);
				cliJPanel.exception(e);
			}
		}
		catch (InterruptedException e)
		{
			logger.error("Error", e);
		}
		finally
		{
			transaction.abort();
		}
	}

	@Override
	public void contextDiscarded(Context context)
	{
		Transaction transaction = persistenceManager.beginDirtyTransaction();
		try
		{
			cliJPanel.message("Context:" + context.identifier(transaction) + " couldn't be proved!");
		}
		catch (InterruptedException e)
		{
			logger.error("Error", e);
		}
		finally
		{
			transaction.abort();
		}
	}

}
