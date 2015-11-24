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

import java.util.ArrayList;
import java.util.List;

import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.nomenclator.Nomenclator.NomenclatorException;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.model.statement.Statement.StatementException;
import aletheia.model.term.Term;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "spc", factory = NewSpecialization.Factory.class)
public class NewSpecialization extends NewStatement
{
	protected final static String tag = "spc";

	private final Statement general;
	private final List<Term> instances;

	public NewSpecialization(CommandSource from, Transaction transaction, Identifier identifier, Statement general, List<Term> instances)
	{
		super(from, transaction, identifier);
		this.general = general;
		this.instances = new ArrayList<Term>(instances);
	}

	protected Statement getGeneral()
	{
		return general;
	}

	protected List<Term> getInstances()
	{
		return instances;
	}

	@Override
	protected RunNewStatementReturnData runNewStatement() throws NomenclatorException, InvalidNameException, StatementException, NotActiveContextException
	{
		Context ctx = getActiveContext();
		if (ctx == null)
			throw new NotActiveContextException();
		Statement statement = general;
		int i = -1;
		for (Term instance : instances)
		{
			if (i >= 0)
				statement.identify(getTransaction(), new Identifier(getIdentifier(), String.format("sub_%02d", i)));
			i++;
			statement = ctx.specialize(getTransaction(), statement, instance);
		}
		return new RunNewStatementReturnData(statement);
	}

	public static class Factory extends AbstractNewStatementFactory<NewSpecialization>
	{

		@Override
		public NewSpecialization parse(CommandSource from, Transaction transaction, Identifier identifier, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			try
			{
				if (from.getActiveContext() == null)
					throw new NotActiveContextException();
				Statement general = from.getActiveContext().identifierToStatement(transaction).get(Identifier.parse(split.get(0)));
				if (general == null)
					throw new CommandParseException("Statement not found: " + split.get(0));
				List<Term> instances = new ArrayList<Term>();
				for (int i = 1; i < split.size(); i++)
				{
					Term instance = parseTerm(from.getActiveContext(), transaction, split.get(i));
					instances.add(instance);
				}
				return new NewSpecialization(from, transaction, identifier, general, instances);
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
			return "<statement> <term>*";
		}

		@Override
		public String shortHelp()
		{
			return "Creates a series of new specialization statements with the specified general statement and the given succession of instances.";
		}

	}
}
