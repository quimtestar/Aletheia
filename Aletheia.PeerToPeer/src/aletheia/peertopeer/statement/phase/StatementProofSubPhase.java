/*******************************************************************************
 * Copyright (c) 2014, 2020 Quim Testar.
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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Stack;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.peertopeer.base.dialog.Dialog.DialogStreamException;
import aletheia.peertopeer.base.phase.SubPhase;
import aletheia.peertopeer.statement.dialog.InitializeStatementProofDialog;
import aletheia.peertopeer.statement.dialog.IterationStatementProofDialog;
import aletheia.peertopeer.statement.dialog.StatementProofDialog;
import aletheia.protocol.ProtocolException;

public class StatementProofSubPhase extends SubPhase
{
	private final static Logger logger = LoggerManager.instance.logger();

	private final Constructor<? extends InitializeStatementProofDialog> initializeStatementProofDialogConstructor;

	public StatementProofSubPhase(StatementSubPhase parentPhase,
			Constructor<? extends InitializeStatementProofDialog> initializeStatementProofDialogConstructor)
	{
		super(parentPhase);
		this.initializeStatementProofDialogConstructor = initializeStatementProofDialogConstructor;
	}

	@Override
	protected StatementSubPhase getParentPhase()
	{
		return (StatementSubPhase) super.getParentPhase();
	}

	@Override
	public void run() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		logger.debug("Entering statement proof phase");
		try
		{
			StatementProofDialog statementProofDialog = initializeStatementProofDialogConstructor.newInstance(this);
			while (true)
			{
				statementProofDialog.run();
				Stack<StatementProofDialog.StatementStackEntry> statementStack = statementProofDialog.getStatementStack();
				logger.debug("statementStack size: " + statementStack.size());
				boolean sending = statementProofDialog.isSending();
				boolean receiving = statementProofDialog.isReceiving();
				if (statementStack.isEmpty() && !sending && !receiving)
					break;
				statementProofDialog = new IterationStatementProofDialog(this, statementStack, sending, receiving);
			}
		}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | SecurityException | InvocationTargetException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			logger.debug("Exiting statement proof phase");
		}
	}

}
