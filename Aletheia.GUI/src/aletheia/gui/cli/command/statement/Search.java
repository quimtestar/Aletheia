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
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.regex.Pattern;

import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.Namespace;
import aletheia.model.identifier.NodeNamespace;
import aletheia.model.identifier.RootNamespace;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "search", groupPath = "/statement", factory = Search.Factory.class)
public class Search extends TransactionalCommand
{
	private final String name;

	public Search(CommandSource from, Transaction transaction, String name)
	{
		super(from, transaction);
		this.name = name;
	}

	private Pattern toPattern(String pattern)
	{
		StringBuilder regex = new StringBuilder();
		int i = 0;
		for (String s : pattern.split("[\\*\\?]"))
		{
			regex.append(Pattern.quote(s));
			i += s.length();
			loop: while (i < pattern.length())
			{
				switch (pattern.charAt(i))
				{
				case '?':
					regex.append(".");
					break;
				case '*':
					regex.append(".*");
					break;
				default:
					break loop;
				}
				i++;
			}
		}
		return Pattern.compile(regex.toString());
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		final Context ctx = getActiveContext();
		if (ctx == null)
			throw new NotActiveContextException();
		SortedMap<Identifier, Statement> map = ctx.identifierToStatement(getTransaction());
		int iq = name.indexOf('?');
		int ia = name.indexOf('*');
		int p = Math.min(iq >= 0 ? iq : name.length(), ia >= 0 ? ia : name.length());
		Namespace prefix = Namespace.parse(name.substring(0, p));
		if (prefix instanceof NodeNamespace)
			map = map.subMap(((NodeNamespace) prefix).asIdentifier(), prefix.terminator());
		else if (!(prefix instanceof RootNamespace))
			throw new Error();
		Pattern pattern = toPattern(name);
		for (Entry<Identifier, Statement> e : map.entrySet())
			if (pattern.matcher(e.getKey().qualifiedName()).matches())
				getOut().println(" -> " + e.getValue().statementPathString(getTransaction(), ctx));
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
			return "<pattern>";
		}

		@Override
		public String shortHelp()
		{
			return "Search for a particular statement's identifier pattern in the active context (with wildcards '?' and '*').";
		}

	}

}
