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
package aletheia.gui.cli.command.local;

import aletheia.gui.cli.CliJPanel;
import aletheia.model.local.ContextLocal;
import aletheia.model.local.StatementLocal;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

public class SubscribeProofCommand extends SubscribeCommand
{

	public SubscribeProofCommand(CliJPanel from, Transaction transaction, Statement statement)
	{
		super(from, transaction, statement);
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		StatementLocal statementLocal = getStatement().getOrCreateLocal(getTransaction());
		statementLocal.setSubscribeProof(getTransaction(), true);
		if (statementLocal instanceof ContextLocal)
		{
			ContextLocal contextLocal = (ContextLocal) statementLocal;
			contextLocal.setSubscribeStatements(getTransaction(), false);
		}
		return null;
	}

}
