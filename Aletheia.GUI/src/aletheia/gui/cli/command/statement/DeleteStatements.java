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
package aletheia.gui.cli.command.statement;

import aletheia.gui.cli.CliJPanel;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.CloseableCollection;

public class DeleteStatements extends TransactionalCommand
{
	private final Context context;
	private final CloseableCollection<Statement> statements;

	public DeleteStatements(CliJPanel from, Transaction transaction, Context context, CloseableCollection<Statement> statements)
	{
		super(from, transaction);
		this.context = context;
		this.statements = statements;
	}

	public Context getContext()
	{
		return context;
	}

	public CloseableCollection<Statement> getStatements()
	{
		return statements;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		context.deleteStatements(getTransaction(), statements);
		return null;
	}

}
