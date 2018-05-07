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
import aletheia.model.statement.Context;
import aletheia.model.statement.Declaration;
import aletheia.model.statement.Statement;
import aletheia.model.term.Term;
import aletheia.persistence.Transaction;

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
			valueProof_ = suitableStatement(ctx, value.getType());
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
			{
				valueProof = findStatementSpec(from.getPersistenceManager(), transaction, from.getActiveContext(), split.get(1));
				if (valueProof == null)
					throw new CommandParseException("Instance proof statement not found: " + split.get(1));

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
