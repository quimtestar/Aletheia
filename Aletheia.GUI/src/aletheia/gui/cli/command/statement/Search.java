/*******************************************************************************
 * Copyright (c) 2016 Quim Testar.
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
import java.util.Map;
import java.util.Map.Entry;

import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.gui.common.NamespacePattern;
import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.CloseableIterator;

@TaggedCommand(tag = "search", groupPath = "/statement", factory = Search.Factory.class)
public class Search extends TransactionalCommand
{
	private final NamespacePattern namespacePattern;

	public Search(CommandSource from, Transaction transaction, String expression)
	{
		super(from, transaction);
		this.namespacePattern = NamespacePattern.instantiate(expression);

	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		final Context ctx = getActiveContext();
		if (ctx == null)
			throw new NotActiveContextException();
		CloseableIterator<Map.Entry<Identifier, Statement>> iterator = ctx.identifierToStatement(getTransaction()).tailMap(namespacePattern.fromKey())
				.entrySet().iterator();
		try
		{
			while (iterator.hasNext())
			{
				Entry<Identifier, Statement> e = iterator.next();
				if (!namespacePattern.isPrefix(e.getKey()))
					break;
				if (namespacePattern.matches(e.getKey()))
					getOut().println(" -> " + e.getValue().statementPathString(getTransaction(), ctx));
			}
		}
		finally
		{
			iterator.close();
		}
		getOut().println("end.");
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<Search>
	{

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		public Search parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			return new Search(from, transaction, split.get(0));
		}

		@Override
		protected String paramSpec()
		{
			return "<search pattern expression>";
		}

		@Override
		public String shortHelp()
		{
			return "Search for a particular statement's identifier pattern in the active context (with wildcards '?' (one char), '#' (one digit) and '*' (any string)).";
		}

	}

}
