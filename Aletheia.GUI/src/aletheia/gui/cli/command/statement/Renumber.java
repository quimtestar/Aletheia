/*******************************************************************************
 * Copyright (c) 2019 Quim Testar.
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
 *******************************************************************************/
package aletheia.gui.cli.command.statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;

import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.log4j.LoggerManager;
import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.NodeNamespace;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.nomenclator.Nomenclator.NomenclatorException;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.ReverseList;

@TaggedCommand(tag = "renumber", groupPath = "/statement", factory = Renumber.Factory.class)
public class Renumber extends TransactionalCommand
{
	private static final Logger logger = LoggerManager.instance.logger();

	private final String expression;
	private final Context context;

	public Renumber(CommandSource from, Transaction transaction, String expression, Context context)
	{
		super(from, transaction);
		this.expression = expression;
		this.context = context;
	}

	private Pattern pattern()
	{
		StringBuilder patternBuilder = new StringBuilder();
		boolean first = true;
		for (String s : expression.split("#", -1))
		{
			if (first)
				first = false;
			else
				patternBuilder.append("[0-9]");
			if (!s.isEmpty())
				patternBuilder.append(Pattern.quote(s));
		}
		return Pattern.compile(patternBuilder.toString());
	}

	public class NumberOverflowException extends Exception
	{
		private static final long serialVersionUID = 4269074352231937255L;

		private NumberOverflowException()
		{
			super("Number overflow error.");
		}
	}

	private Identifier to(int n) throws NumberOverflowException, InvalidNameException
	{
		char[] chars = expression.toCharArray();
		for (int i = chars.length - 1; i >= 0; i--)
		{
			if (chars[i] == '#')
			{
				chars[i] = (char) ('0' + (n % 10));
				n /= 10;
			}
		}
		if (n > 0)
			throw new NumberOverflowException();
		return Identifier.parse(new String(chars));
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws NumberOverflowException, InvalidNameException, NomenclatorException
	{
		Pattern pattern = pattern();
		int splits = expression.length() - expression.replace(".", "").length();
		Stack<Context> stack = new Stack<>();
		stack.push(context);
		Map<Statement, Identifier> renameMap = new HashMap<>();
		int n = 0;
		while (!stack.isEmpty())
		{
			Context ctx = stack.pop();
			Map<NodeNamespace, NodeNamespace> localPrefixMap = new HashMap<>();
			List<Context> addContexts = new ArrayList<>();
			for (Statement st : ctx.localSortedStatements(getTransaction()))
			{
				if (st.getIdentifier() != null && (splits < st.getIdentifier().length()))
				{
					NodeNamespace from = st.getIdentifier().prefixList().get(splits);
					if (pattern.matcher(from.qualifiedName()).matches())
					{
						NodeNamespace to = localPrefixMap.get(from);
						if (to == null)
						{
							to = to(n++);
							localPrefixMap.put(from, to);
						}
						Identifier newId = to.concat(st.getIdentifier().makeSuffix(from)).asIdentifier();
						if (!newId.equals(st.getIdentifier()))
						{
							renameMap.put(st, newId);
							st.unidentify(getTransaction());
							logger.trace("{}: {} -> {}", renameMap.size(), st.getIdentifier(), newId);
						}
					}
				}
				if ((st instanceof Context) && !st.isProved())
					addContexts.add((Context) st);
			}
			for (Context ctx_ : new ReverseList<>(addContexts))
				stack.push(ctx_);
		}
		int i = 0;
		for (Entry<Statement, Identifier> e : renameMap.entrySet())
		{
			e.getKey().identify(getTransaction(), e.getValue());
			logger.trace("<- {}", renameMap.size() - (i++));
		}
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<Renumber>
	{

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		public Renumber parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			Context context;
			if (split.size() > 1)
			{
				Statement statement = findStatementSpec(from.getPersistenceManager(), transaction, from.getActiveContext(), split.get(1));
				if (statement == null)
					throw new CommandParseException("Invalid context");
				if (!(statement instanceof Context))
					throw new CommandParseException("Not a context");
				context = (Context) statement;
			}
			else
				context = from.getActiveContext();
			return new Renumber(from, transaction, split.get(0), context);
		}

		@Override
		protected String paramSpec()
		{
			return "<expression> [<context>]";
		}

		@Override
		public String shortHelp()
		{
			return "Renumbers statements in this context and its non-proved subcontexts that match a particular pattern (replacing with wildcard '#' with new digits).";
		}

	}

}
