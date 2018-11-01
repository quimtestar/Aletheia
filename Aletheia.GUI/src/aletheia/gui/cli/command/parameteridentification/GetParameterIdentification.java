/*******************************************************************************
 * Copyright (c) 2018 Quim Testar
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
package aletheia.gui.cli.command.parameteridentification;

import java.util.List;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.parameteridentification.ParameterIdentification;
import aletheia.model.statement.Context;
import aletheia.model.statement.Declaration;
import aletheia.model.statement.Specialization;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "gpi", groupPath = "/pi", factory = GetParameterIdentification.Factory.class)
public abstract class GetParameterIdentification extends TransactionalCommand
{
	private final Statement statement;

	public GetParameterIdentification(CommandSource from, Transaction transaction, Statement statement)
	{
		super(from, transaction);
		this.statement = statement;
	}

	protected Statement getStatement()
	{
		return statement;
	}

	protected abstract ParameterIdentification getParameterIdentification();

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		ParameterIdentification parameterIdentification = getParameterIdentification();
		if (parameterIdentification != null)
			getOut().println(parameterIdentification);
		return null;
	}

	public static class GetTermParameterIdentification extends GetParameterIdentification
	{

		public GetTermParameterIdentification(CommandSource from, Transaction transaction, Statement statement)
		{
			super(from, transaction, statement);
		}

		@Override
		protected ParameterIdentification getParameterIdentification()
		{
			return getStatement().getTermParameterIdentification();
		}

	}

	public static class GetValueParameterIdentification extends GetParameterIdentification
	{

		public GetValueParameterIdentification(CommandSource from, Transaction transaction, Declaration declaration)
		{
			super(from, transaction, declaration);
		}

		@Override
		protected Declaration getStatement()
		{
			return (Declaration) super.getStatement();
		}

		@Override
		protected ParameterIdentification getParameterIdentification()
		{
			return getStatement().getValueParameterIdentification();
		}

	}

	public static class GetInstanceParameterIdentification extends GetParameterIdentification
	{

		public GetInstanceParameterIdentification(CommandSource from, Transaction transaction, Specialization specialization)
		{
			super(from, transaction, specialization);
		}

		@Override
		protected Specialization getStatement()
		{
			return (Specialization) super.getStatement();
		}

		@Override
		protected ParameterIdentification getParameterIdentification()
		{
			return getStatement().getInstanceParameterIdentification();
		}

	}

	public static class GetConsequentParameterIdentification extends GetParameterIdentification
	{

		public GetConsequentParameterIdentification(CommandSource from, Transaction transaction, Context context)
		{
			super(from, transaction, context);
		}

		@Override
		protected Context getStatement()
		{
			return (Context) super.getStatement();
		}

		@Override
		protected ParameterIdentification getParameterIdentification()
		{
			return getStatement().getConsequentParameterIdentification();
		}

	}

	public static class Factory extends AbstractVoidCommandFactory<GetParameterIdentification>
	{

		@Override
		protected int minParameters()
		{
			return 1;
		}

		private int countBooleans(boolean... vs)
		{
			int c = 0;
			for (boolean v : vs)
				if (v)
					c++;
			return c;
		}

		@Override
		public GetParameterIdentification parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			boolean value = split.remove("-v") || split.remove("-value");
			boolean instance = split.remove("-i") || split.remove("-instance");
			boolean consequent = split.remove("-c") || split.remove("-consequent");
			if (countBooleans(value, instance, consequent) > 1)
				throw new CommandParseException("Can't specify more than one switch in {[v]alue, [i]nstance, [c]onsequent}");
			Statement statement = findStatementSpec(from.getPersistenceManager(), transaction, from.getActiveContext(), split.get(0));
			if (statement == null)
				throw new CommandParseException("Invalid statement");
			if (value)
			{
				if (!(statement instanceof Declaration))
					throw new CommandParseException("Statement not a declaration");
				return new GetValueParameterIdentification(from, transaction, (Declaration) statement);
			}
			else if (instance)
			{
				if (!(statement instanceof Specialization))
					throw new CommandParseException("Statement not a specialization");
				return new GetInstanceParameterIdentification(from, transaction, (Specialization) statement);
			}
			else if (consequent)
			{
				if (!(statement instanceof Context))
					throw new CommandParseException("Statement not a context");
				return new GetConsequentParameterIdentification(from, transaction, (Context) statement);
			}
			else
				return new GetTermParameterIdentification(from, transaction, statement);
		}

		@Override
		protected String paramSpec()
		{
			return "([-v[alue]] | [-i[nstance]] | [-c[onsequent]) <statement>";
		}

		@Override
		public String shortHelp()
		{
			return "Gets the parameter identification of a statement's term (or a declaration's value or a specialization's instance or a context's consequent).";
		}

	}

}
