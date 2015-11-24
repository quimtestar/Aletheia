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

import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.statement.Context;
import aletheia.model.statement.Declaration;
import aletheia.model.statement.Specialization;
import aletheia.model.statement.Statement;
import aletheia.model.statement.UnfoldingContext;
import aletheia.persistence.Transaction;

@Deprecated
public class StatementBuild extends TransactionalCommand
{
	private final Statement statement;

	public StatementBuild(CommandSource from, Transaction transaction, Statement statement)
	{
		super(from, transaction);
		this.statement = statement;
	}

	protected Statement getStatement()
	{
		return statement;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		Context activeContext = getActiveContext();
		if (activeContext == null)
			throw new NotActiveContextException();
		if (statement instanceof Specialization)
		{
			Specialization spec = (Specialization) statement;
			String general = spec.getGeneral(getTransaction()).getVariable().toString(activeContext.variableToIdentifier(getTransaction()));
			String instance = termToString(activeContext, getTransaction(), spec.getInstance());
			getOut().println("spc " + general + " " + "\"" + instance + "\"");
		}
		else if (statement instanceof Context)
		{
			Context ctx = (Context) statement;
			String term = termToString(activeContext, getTransaction(), ctx.getTerm(), ctx.assumptions(getTransaction()));
			if (ctx instanceof UnfoldingContext)
			{
				UnfoldingContext unf = (UnfoldingContext) ctx;
				String declaration = unf.getDeclaration(getTransaction()).getVariable().toString(activeContext.variableToIdentifier(getTransaction()));
				getOut().println("unf " + "\"" + term + "\"" + " " + declaration);
			}
			else
				getOut().println("ctx " + "\"" + term + "\"");
		}
		else if (statement instanceof Declaration)
		{
			Declaration dec = (Declaration) statement;
			String term = termToString(activeContext, getTransaction(), dec.getValue());
			getOut().println("dec " + "\"" + term + "\"");
		}
		else
			throw new Exception("Bad statement type");
		return null;
	}

}
