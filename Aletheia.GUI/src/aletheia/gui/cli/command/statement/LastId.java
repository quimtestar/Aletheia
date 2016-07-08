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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.gui.common.NamespacePattern;
import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.CloseableIterator;

@TaggedCommand(tag = "lastid", groupPath = "/statement", factory = LastId.Factory.class)
public class LastId extends TransactionalCommand
{
	private final NamespacePattern namespacePattern;
	private final Statement topStatement;

	public LastId(CommandSource from, Transaction transaction, String expression, Statement topStatement)
	{
		super(from, transaction);
		this.namespacePattern = NamespacePattern.instantiate(expression);
		this.topStatement = topStatement;
	}

	private Identifier localLastId(Context ctx)
	{
		Identifier lastId = null;
		CloseableIterator<Map.Entry<Identifier, Statement>> iterator = ctx.localIdentifierToStatement(getTransaction()).tailMap(namespacePattern.fromKey())
				.entrySet().iterator();
		try
		{
			while (iterator.hasNext())
			{
				Identifier id = iterator.next().getKey();
				if (!namespacePattern.isPrefix(id))
					break;
				if (namespacePattern.matches(id))
					if (lastId == null || id.compareTo(lastId) > 0)
						lastId = id;
			}
		}
		finally
		{
			iterator.close();
		}
		return lastId;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		Stack<Context> stack = new Stack<>();
		Identifier lastId = null;

		Context activeContext = getActiveContext();
		if (activeContext == null)
			throw new NotActiveContextException();
		stack.push(activeContext);

		Set<Context> visited = new HashSet<>();

		while (!stack.isEmpty())
		{
			Context ctx = stack.pop();
			if (!visited.contains(ctx))
			{
				visited.add(ctx);
				Identifier id = localLastId(ctx);
				if (id != null && (lastId == null || id.compareTo(lastId) > 0))
					lastId = id;
				for (Context ctx_ : ctx.subContexts(getTransaction()))
					if (!ctx_.isProved())
						stack.push(ctx_);
				if (!(ctx instanceof RootContext) && !ctx.equals(topStatement))
					stack.push(ctx.getContext(getTransaction()));
			}
		}
		if (lastId == null)
			getErr().println("Pattern not found.");
		else
			getOut().println(lastId);
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<LastId>
	{

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		public LastId parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			Statement statement = null;
			if (split.size() > 1)
			{
				statement = findStatementSpec(from.getPersistenceManager(), transaction, from.getActiveContext(), split.get(1));
				if (statement == null)
					throw new CommandParseException("Invalid top statement");
				if (!statement.statementPath(transaction).contains(statement))
					throw new CommandParseException("Top statement not in path");
			}
			return new LastId(from, transaction, split.get(0), statement);
		}

		@Override
		protected String paramSpec()
		{
			return "<expression> [<top statement>]";
		}

		@Override
		public String shortHelp()
		{
			return "Finds the last identifier (in the identifier lexicographical order) used in the active context (and supercontexts and recursivelly-unproved subcontexts) that match a particular pattern (with wildcards '?' (one char), '#' (one digit) and '*' (any string)).";
		}

	}

}
