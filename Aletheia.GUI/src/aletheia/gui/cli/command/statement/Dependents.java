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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.identifier.Identifier;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "dpds", groupPath = "/statement", factory = Dependents.Factory.class)
public class Dependents extends TransactionalCommand
{
	private final Statement statement;

	public Dependents(CommandSource from, Transaction transaction, Statement statement)
	{
		super(from, transaction);
		this.statement = statement;
	}

	protected Statement getStatement()
	{
		return statement;
	}

	@Override
	protected RunTransactionalReturnData runTransactional()
	{
		List<List<? extends Statement>> list = new ArrayList<>();
		for (Statement st : statement.dependents(getTransaction()))
		{
			List<? extends Statement> path = st.statementPath(getTransaction(), getActiveContext());
			list.add(path);
		}

		Comparator<List<? extends Statement>> comparator = new Comparator<>()
		{
			@Override
			public int compare(List<? extends Statement> path1, List<? extends Statement> path2)
			{
				Iterator<? extends Statement> i1 = path1.iterator();
				Iterator<? extends Statement> i2 = path2.iterator();
				while (i1.hasNext() && i2.hasNext())
				{
					Statement st1 = i1.next();
					Statement st2 = i2.next();
					Identifier id1 = st1.identifier(getTransaction());
					Identifier id2 = st2.identifier(getTransaction());
					if ((id1 != null) && (id2 != null))
					{
						int c = id1.compareTo(id2);
						if (c != 0)
							return c;
					}
					else if ((id1 == null) && (id2 != null))
					{
						return -1;
					}
					else if ((id1 != null) && (id2 == null))
					{
						return 1;
					}
					else
					{
						int c = Integer.compare(st1.getVariable().hashCode(), st2.getVariable().hashCode());
						if (c != 0)
							return c;
					}
				}
				if (i1.hasNext())
					return 1;
				else if (i2.hasNext())
					return -1;
				else
					return 0;
			}

		};
		Collections.sort(list, comparator);

		for (List<? extends Statement> path : list)
		{
			StringBuffer sbpath = new StringBuffer();
			boolean first = true;
			for (Statement st2 : path)
			{
				if (!first)
					sbpath.append("/");
				else
					first = false;
				if (!(st2 instanceof RootContext))
				{
					Identifier id = st2.identifier(getTransaction());
					if (id != null)
						sbpath.append(id.toString());
					else
						sbpath.append(st2.getVariable().toString());
				}
			}
			getOut().println(" -> " + sbpath);
		}
		getOut().println("end.");

		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<Dependents>
	{

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		public Dependents parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			Statement statement = findStatementSpec(from.getPersistenceManager(), transaction, from.getActiveContext(), split.get(0));
			if (statement == null)
				throw new CommandParseException("Bad statement:" + split.get(0));
			return new Dependents(from, transaction, statement);
		}

		@Override
		protected String paramSpec()
		{
			return "<statement>";
		}

		@Override
		public String shortHelp()
		{
			return "Lists the statements that depend on the given one.";
		}

	}

}
