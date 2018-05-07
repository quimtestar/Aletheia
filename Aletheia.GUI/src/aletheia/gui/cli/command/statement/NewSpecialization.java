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
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.model.term.Term;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "spc", factory = NewSpecialization.Factory.class)
public class NewSpecialization extends NewStatement
{
	private final Statement general;

	protected static class ProvedInstance
	{
		final Term instance;
		final Statement instanceProof;

		protected ProvedInstance(Term instance, Statement instanceProof)
		{
			super();
			this.instance = instance;
			this.instanceProof = instanceProof;
		}
	}

	private final List<ProvedInstance> provedInstances;

	public NewSpecialization(CommandSource from, Transaction transaction, Identifier identifier, Statement general, List<ProvedInstance> provedInstances)
	{
		super(from, transaction, identifier);
		this.general = general;
		this.provedInstances = provedInstances;
	}

	protected Statement getGeneral()
	{
		return general;
	}

	protected List<ProvedInstance> getProvedInstances()
	{
		return provedInstances;
	}

	@Override
	protected RunNewStatementReturnData runNewStatement() throws Exception
	{
		Context ctx = getActiveContext();
		if (ctx == null)
			throw new NotActiveContextException();
		Statement statement = general;
		int i = -1;
		for (ProvedInstance provedInstance : provedInstances)
		{
			if (i >= 0)
			{
				if (i >= subStatementOverflow)
					throw new Exception("Substatement identifier numerator overflowed.");
				statement.identify(getTransaction(), new Identifier(getIdentifier(), String.format(subStatementFormat, i)));
			}
			i++;

			Statement instanceProof_ = provedInstance.instanceProof;
			if (instanceProof_ == null)
				instanceProof_ = ctx.statements(getTransaction()).get(provedInstance.instance);
			if (instanceProof_ == null)
				instanceProof_ = suitableStatement(ctx, provedInstance.instance.getType());
			if (instanceProof_ == null)
				throw new Exception("Value proof missing for type: " + provedInstance.instance.getType().toString(getTransaction(), ctx));

			statement = ctx.specialize(getTransaction(), statement, provedInstance.instance, instanceProof_);
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
				Statement general = findStatementSpec(from.getPersistenceManager(), transaction, from.getActiveContext(), split.get(0));
				if (general == null)
					throw new CommandParseException("General statement not found: " + split.get(0));
				List<ProvedInstance> provedInstances = new ArrayList<>();
				for (int i = 1; i < split.size(); i += 2)
				{
					Term instance = parseTerm(from.getActiveContext(), transaction, split.get(i));
					Statement instanceProof = null;
					if (i + 1 < split.size())
					{
						instanceProof = findStatementSpec(from.getPersistenceManager(), transaction, from.getActiveContext(), split.get(i + 1));
						if (instanceProof == null)
							throw new CommandParseException("Instance proof statement not found: " + split.get(i + 1));
					}
					provedInstances.add(new ProvedInstance(instance, instanceProof));
				}
				return new NewSpecialization(from, transaction, identifier, general, provedInstances);
			}
			catch (NotActiveContextException e)
			{
				throw CommandParseEmbeddedException.embed(e);
			}
			finally
			{

			}
		}

		@Override
		protected int minParameters()
		{
			return 2;
		}

		@Override
		protected String paramSpec()
		{
			return "<statement> (<term> <statement>)+";
		}

		@Override
		public String shortHelp()
		{
			return "Creates a series of new specialization statements with the specified general statement and the given succession of instances and their proof statement.";
		}

	}
}
