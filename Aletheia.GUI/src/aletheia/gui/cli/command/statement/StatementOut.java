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
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.identifier.Identifier;
import aletheia.model.parameteridentification.ParameterIdentification;
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

	public StatementOut(CommandSource from, Transaction transaction, Statement statement)
	{
		super(from, transaction);
		this.statement = statement;
	}

	private Identifier statementToIdentifier(Context activeContext, Statement st)
	{
		return activeContext.variableToIdentifier(getTransaction()).get(st.getVariable());
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		Context activeContext = getActiveContext();
		if (activeContext == null)
			throw new NotActiveContextException();
		if (statement instanceof Context)
		{
			Context context = (Context) statement;
			Term term = context.getTerm();
			String sterm = termToString(activeContext, getTransaction(), term, context.makeParameterIdentification(getTransaction()));
			if (context instanceof UnfoldingContext)
			{
				UnfoldingContext unfoldingContext = (UnfoldingContext) context;
				Identifier iddec = statementToIdentifier(activeContext, unfoldingContext.getDeclaration(getTransaction()));
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
		else if (statement instanceof Specialization)
		{
			Specialization spec = (Specialization) statement;
			Statement general = spec.getGeneral(getTransaction());
			Term instance = spec.getInstance();
			Statement instanceProof = spec.getInstanceProof(getTransaction());
			Identifier idgeneral = statementToIdentifier(activeContext, general);
			if (idgeneral == null)
				throw new Exception("General is not identified");
			String sgeneral = idgeneral.toString();
			String sinstance = termToString(activeContext, getTransaction(), instance);
			Identifier idInstanceProof = statementToIdentifier(activeContext, instanceProof);
			if (idInstanceProof == null)
				throw new Exception("Instance proof is not identified");
			String sInstanceProof = idInstanceProof.toString();
			getOut().println("spc " + sgeneral + " \"" + sinstance + "\" " + sInstanceProof);
		}
		else if (statement instanceof Declaration)
		{
			Declaration dec = (Declaration) statement;
			Term value = dec.getValue();
			ParameterIdentification valueParameterIdentification = dec.getValueParameterIdentification();
			String svalue = termToString(activeContext, getTransaction(), value, valueParameterIdentification);
			Statement valueProof = dec.getValueProof(getTransaction());
			Identifier idValueProof = statementToIdentifier(activeContext, valueProof);
			if (idValueProof == null)
				throw new Exception("Value proof is not identified");
			String sValueProof = idValueProof.toString();
			getOut().println("dec \"" + svalue + "\" " + sValueProof);
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
		public StatementOut parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			Statement statement = findStatementSpec(from.getPersistenceManager(), transaction, from.getActiveContext(), split.get(0));
			if (statement == null)
				throw new CommandParseException("Invalid statement");
			return new StatementOut(from, transaction, statement);
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
