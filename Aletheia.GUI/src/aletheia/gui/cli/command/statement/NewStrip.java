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

import java.util.List;

import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.nomenclator.Nomenclator.NomenclatorException;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.model.statement.Statement.StatementException;
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.Term;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "strip", factory = NewStrip.Factory.class)
public class NewStrip extends NewStatement
{
	private final Statement statement;

	public NewStrip(CommandSource from, Transaction transaction, Identifier identifier, Statement statement)
	{
		super(from, transaction, identifier);
		this.statement = statement;
	}

	protected Statement getStatement()
	{
		return statement;
	}

	@Override
	protected RunNewStatementReturnData runNewStatement() throws NomenclatorException, InvalidNameException, StatementException, NotActiveContextException
	{
		Context ctx = getActiveContext();
		if (ctx == null)
			throw new NotActiveContextException();
		Statement statement = this.statement;
		Term term = statement.getTerm();
		int i = -1;
		while (term instanceof FunctionTerm)
		{
			FunctionTerm functionTerm = (FunctionTerm) term;
			if (functionTerm.getBody().freeVariables().contains(functionTerm.getParameter()))
				break;
			if (i >= 0)
				statement.identify(getTransaction(), new Identifier(getIdentifier(), String.format("sub_%02d", i)));
			i++;
			Term type = functionTerm.getParameter().getType();

			Statement solver = null;
			for (Statement stsol : ctx.statementsByTerm(getTransaction()).get(type).toArray(new Statement[0]))
			{
				if (stsol.isProved())
				{
					solver = stsol;
					break;
				}
			}
			if (solver == null)
			{
				for (Statement stsol : ctx.localStatementsByTerm(getTransaction()).get(type).toArray(new Statement[0]))
				{
					solver = stsol;
					break;
				}
			}
			if (solver != null)
			{
				statement = ctx.specialize(getTransaction(), statement, solver.getVariable());
			}
			else
			{
				Context subctx = ctx.openSubContext(getTransaction(), type);
				subctx.identify(getTransaction(), new Identifier(getIdentifier(), String.format("sub_%02d", i++)));
				statement = ctx.specialize(getTransaction(), statement, subctx.getVariable());
			}
			term = functionTerm.getBody();
		}
		return new RunNewStatementReturnData(statement);
	}

	public static class Factory extends AbstractNewStatementFactory<NewStrip>
	{

		@Override
		public NewStrip parse(CommandSource from, Transaction transaction, Identifier identifier, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			try
			{
				if (from.getActiveContext() == null)
					throw new NotActiveContextException();
				Statement statement = from.getActiveContext().identifierToStatement(transaction).get(Identifier.parse(split.get(0)));
				if (statement == null)
					throw new CommandParseException("Bad statement: " + split.get(0));
				return new NewStrip(from, transaction, identifier, statement);
			}
			catch (NotActiveContextException | InvalidNameException e)
			{
				throw new CommandParseException(e);
			}
			finally
			{

			}
		}

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		protected String paramSpec()
		{
			return "<statement>";
		}

		@Override
		public String shortHelp()
		{
			return "Strips the given statement.";
		}

	}

}
