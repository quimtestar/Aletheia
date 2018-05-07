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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.statement.Assumption;
import aletheia.model.statement.Context;
import aletheia.model.statement.Declaration;
import aletheia.model.statement.Statement;
import aletheia.model.term.Term;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.BufferedList;

@TaggedCommand(tag = "dec", factory = NewDeclaration.Factory.class)
public class NewDeclaration extends NewStatement
{
	private final Term value;
	private final Statement valueProof;

	public NewDeclaration(CommandSource from, Transaction transaction, Identifier identifier, Term value, Statement valueProof)
	{
		super(from, transaction, identifier);
		this.value = value;
		this.valueProof = valueProof;
	}

	protected Term getValue()
	{
		return value;
	}

	protected Statement getValueProof()
	{
		return valueProof;
	}

	private Statement suitableFromContext(Context context, Term term)
	{
		Statement statement = null;
		for (Context ctx : context.statementPath(getTransaction()))
		{
			List<Statement> candidates = new BufferedList<>(ctx.localStatementsByTerm(getTransaction()).get(term));
			Collections.sort(candidates, new Comparator<Statement>()
			{

				@Override
				public int compare(Statement st1, Statement st2)
				{
					Identifier id1 = st1.getIdentifier();
					Identifier id2 = st2.getIdentifier();
					int c;
					c = -Boolean.compare(st1 instanceof Assumption, st2 instanceof Assumption);
					if (c != 0)
						return c;
					c = Boolean.compare(id1 == null, id2 == null);
					if (c != 0)
						return c;
					if (id1 == null || id2 == null)
						return 0;
					c = Integer.compare(id1.length(), id2.length());
					if (c != 0)
						return c;
					return c;
				}
			});
			for (Statement c : candidates)
			{
				if (c.isProved())
				{
					statement = c;
					break;
				}
			}
			if (statement == null && ctx.equals(context))
				for (Statement c : candidates)
				{
					statement = c;
					break;
				}
			if (statement != null)
				break;
		}
		return statement;

	}

	@Override
	protected RunNewStatementReturnData runNewStatement() throws Exception
	{
		Context ctx = getActiveContext();
		if (ctx == null)
			throw new NotActiveContextException();

		Statement valueProof_ = valueProof;
		if (valueProof_ == null)
			valueProof_ = ctx.statements(getTransaction()).get(value);
		if (valueProof_ == null)
			valueProof_ = suitableFromContext(ctx, value.getType());
		if (valueProof_ == null)
			throw new Exception("Value proof missing for type: " + value.getType().toString(getTransaction(), ctx));

		Declaration declaration = ctx.declare(getTransaction(), value, valueProof_);
		return new RunNewStatementReturnData(declaration);
	}

	public static class Factory extends AbstractNewStatementFactory<NewDeclaration>
	{

		@Override
		public NewDeclaration parse(CommandSource from, Transaction transaction, Identifier identifier, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			Term value = parseTerm(from.getActiveContext(), transaction, split.get(0));
			Statement valueProof = null;
			if (1 < split.size())
				try
				{
					valueProof = from.getActiveContext().identifierToStatement(transaction).get(Identifier.parse(split.get(1)));
				}
				catch (InvalidNameException e)
				{
					throw new CommandParseException(e);
				}
			return new NewDeclaration(from, transaction, identifier, value, valueProof);
		}

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		protected String paramSpec()
		{
			return "<term> [<statement>]";
		}

		@Override
		public String shortHelp()
		{
			return "Creates a new declaration with the specified value and value proof statement.";
		}

	}

}
