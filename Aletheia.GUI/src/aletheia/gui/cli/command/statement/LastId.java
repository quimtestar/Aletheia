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
import java.util.Stack;

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
import aletheia.utilities.collections.CloseableSortedMap;

@TaggedCommand(tag = "lastid", groupPath = "/statement", factory = LastId.Factory.class)
public class LastId extends TransactionalCommand
{
	private final NamespacePattern namespacePattern;

	public LastId(CommandSource from, Transaction transaction, String expression)
	{
		super(from, transaction);
		this.namespacePattern = NamespacePattern.instantiate(expression);
	}

	private Identifier localLastId(CloseableSortedMap<Identifier, Statement> identifierToStatement)
	{
		Identifier lastId = null;
		CloseableIterator<Map.Entry<Identifier, Statement>> iterator = identifierToStatement.tailMap(namespacePattern.fromKey()).entrySet().iterator();
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
		Stack<Context> stack = new Stack<Context>();
		Identifier lastId = null;

		Context activeContext = getActiveContext();
		if (activeContext == null)
			throw new NotActiveContextException();
		stack.push(activeContext);

		while (!stack.isEmpty())
		{
			Context ctx = stack.pop();
			Identifier id = localLastId(ctx == activeContext ? ctx.identifierToStatement(getTransaction()) : ctx.localIdentifierToStatement(getTransaction()));
			if (id != null && (lastId == null || id.compareTo(lastId) > 0))
				lastId = id;
			for (Context ctx_ : ctx.subContexts(getTransaction()))
				if (!ctx_.isProved())
					stack.push(ctx_);
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
			return new LastId(from, transaction, split.get(0));
		}

		@Override
		protected String paramSpec()
		{
			return "<expression>";
		}

		@Override
		public String shortHelp()
		{
			return "Finds the last identifier (in the identifier lexicographical order) used in the active context (and supercontexts and recursivelly-unproved subcontexts) that match a particular pattern (with wildcards '?' (one char), '#' (one digit) and '*' (any string)).";
		}

	}

}
