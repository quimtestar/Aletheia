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

import aletheia.gui.cli.CliJPanel;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.statement.Declaration;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Specialization;
import aletheia.model.statement.Statement;
import aletheia.model.statement.UnfoldingContext;
import aletheia.model.term.Term;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "stout", groupPath = "/statement", factory = StatementOut.Factory.class)
public class StatementOut extends TransactionalCommand
{
	private final Statement statement;

	public StatementOut(CliJPanel from, Transaction transaction, Statement statement)
	{
		super(from, transaction);
		this.statement = statement;
	}

	private Identifier statementToIdentifier(Statement st)
	{
		return getFrom().getActiveContext().variableToIdentifier(getTransaction()).get(st.getVariable());
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		if (statement instanceof Context)
		{
			Context context = (Context) statement;
			Term term = context.getTerm();
			String sterm = termToString(getFrom().getActiveContext(), getTransaction(), term, context.assumptions(getTransaction()));
			if (context instanceof UnfoldingContext)
			{
				UnfoldingContext unfoldingContext = (UnfoldingContext) context;
				Identifier iddec = statementToIdentifier(unfoldingContext.getDeclaration(getTransaction()));
				if (iddec == null)
					throw new Exception("Declaration is not identified");
				getOut().println("unf \"" + sterm + "\" " + iddec.toString());
			}
			else if (context instanceof RootContext)
			{
				getOut().println("root \"" + sterm + "\"");
			}
			else
			{
				getOut().println("ctx \"" + sterm + "\"");
			}
		}
		else if (statement instanceof Declaration)
		{
			Declaration dec = (Declaration) statement;
			Term value = dec.getValue();
			String svalue = termToString(getFrom().getActiveContext(), getTransaction(), value);
			getOut().println("dec \"" + svalue + "\"");
		}
		else if (statement instanceof Specialization)
		{
			Specialization spec = (Specialization) statement;
			Statement general = spec.getGeneral(getTransaction());
			Term instance = spec.getInstance();
			Identifier idgeneral = statementToIdentifier(general);
			if (idgeneral == null)
				throw new Exception("General is not identified");
			String sgeneral = idgeneral.toString();
			String sinstance = termToString(getFrom().getActiveContext(), getTransaction(), instance);
			getOut().println("spc " + sgeneral + " \"" + sinstance + "\"");
		}
		else
			throw new Exception("Invalid statement type");
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<StatementOut>
	{

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		public StatementOut parse(CliJPanel cliJPanel, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			Statement statement = findStatementPath(cliJPanel.getPersistenceManager(), transaction, cliJPanel.getActiveContext(), split.get(0));
			if (statement == null)
				throw new CommandParseException("Invalid statement");
			return new StatementOut(cliJPanel, transaction, statement);
		}

		@Override
		protected String paramSpec()
		{
			return "<statement>";
		}

		@Override
		public String shortHelp()
		{
			return "Returns the new statement subcommand that you should use to create an statement identical to the selected one.";
		}

	}

}
