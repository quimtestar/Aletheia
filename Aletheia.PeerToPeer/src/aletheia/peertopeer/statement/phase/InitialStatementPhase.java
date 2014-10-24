/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
 *
 * This file is part of the Aletheia Proof Assistant.
 *
 * The Aletheia Proof Assistant is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The Aletheia Proof Assistant is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with the Aletheia Proof Assistant.
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package aletheia.peertopeer.statement.phase;

import java.io.IOException;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.peertopeer.base.dialog.Dialog.DialogStreamException;
import aletheia.peertopeer.statement.dialog.StatementProofInitialDialog;
import aletheia.peertopeer.statement.dialog.StatementSubscriptionInitialDialog;
import aletheia.protocol.ProtocolException;

public class InitialStatementPhase extends StatementSubPhase
{
	private final static Logger logger = LoggerManager.instance.logger();

	public InitialStatementPhase(StatementPhase statementPhase)
	{
		super(statementPhase);
	}

	@Override
	protected StatementPhase getParentPhase()
	{
		return super.getParentPhase();
	}

	private void statementSubscriptionInitialDialog() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		dialog(StatementSubscriptionInitialDialog.class, this);
	}

	private void statementProofSubPhase() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		statementProofSubPhase(StatementProofInitialDialog.class, this);
	}

	@Override
	public void run() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		logger.debug("Entering initial phase");
		try
		{
			statementSubscriptionInitialDialog();
			statementProofSubPhase();
		}
		finally
		{
			logger.debug("Exiting initial phase");
		}
	}

}
