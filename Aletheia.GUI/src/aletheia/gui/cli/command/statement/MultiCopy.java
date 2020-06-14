/*******************************************************************************
 * Copyright (c) 2014, 2020 Quim Testar.
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
import java.util.Collection;
import java.util.List;

import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.BufferedList;

@TaggedCommand(tag = "multicopy", groupPath = "/statement", factory = MultiCopy.Factory.class)
public class MultiCopy extends TransactionalCommand
{
	private final List<Statement> statements;

	public MultiCopy(CommandSource from, Transaction transaction, List<Statement> statements)
	{
		super(from, transaction);
		this.statements = new ArrayList<>(statements);
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		Context ctx = getActiveContext();
		if (ctx == null)
			throw new NotActiveContextException();
		List<Statement> list = ctx.copy(getTransaction(), new BufferedList<>(Statement.dependencySortedStatements(getTransaction(), statements)));
		if (list.isEmpty())
			return null;
		Statement last = list.get(list.size() - 1);
		if (last instanceof Context)
			return new RunTransactionalReturnData((Context) last);
		else
			return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<MultiCopy>
	{

		@Override
		protected int minParameters()
		{
			return 0;
		}

		@Override
		public MultiCopy parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			List<Statement> statements = new ArrayList<>();
			for (int i = 0; i < split.size(); i++)
			{
				Collection<Statement> st = findMultiStatementPath(from.getPersistenceManager(), transaction, from.getActiveContext(), split.get(i));
				if (st.isEmpty())
					throw new CommandParseException("Bad statement path: " + split.get(i));
				statements.addAll(st);
			}
			return new MultiCopy(from, transaction, statements);
		}

		@Override
		protected String paramSpec()
		{
			return "<statement>*";
		}

		@Override
		public String shortHelp()
		{
			return "Copies a list of statements to the active context.";
		}

	}

}
