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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Assumption;
import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "translate", groupPath = "/statement", factory = Translate.Factory.class)
public class Translate extends TransactionalCommand
{
	private final Context context;

	public Translate(CommandSource from, Transaction transaction, Context context)
	{
		super(from, transaction);
		this.context = context;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		Context activeContext = getActiveContext();
		if (activeContext == null)
			throw new NotActiveContextException();
		Context ctx1 = activeContext;

		Map<Statement, Statement> map = new HashMap<Statement, Statement>();

		while (!ctx1.isDescendent(getTransaction(), context))
		{
			for (Statement st : ctx1.localStatements(getTransaction()).values())
			{
				Identifier id = st.identifier(getTransaction());
				if (id != null)
				{
					Statement st_ = context.identifierToStatement(getTransaction()).get(id);
					if ((st_ != null) && (!map.containsKey(st_)))
						map.put(st_, st);
				}
			}
			if (ctx1 instanceof RootContext)
				break;
			ctx1 = ctx1.getContext(getTransaction());
		}

		List<Statement> list = new ArrayList<Statement>();
		for (Statement st : context.localDependencySortedStatements(getTransaction()))
		{
			if (!(st instanceof Assumption) && !map.containsKey(st))
				list.add(st);
		}

		activeContext.copy(getTransaction(), list, map);

		return null;

	}

	public static class Factory extends AbstractVoidCommandFactory<Translate>
	{

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		public Translate parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			try
			{
				Context context = (Context) findStatementPath(from.getPersistenceManager(), transaction, from.getActiveContext(), split.get(0));
				if (context == null)
					throw new CommandParseException("Bad statement path: " + split.get(0));
				return new Translate(from, transaction, context);
			}
			catch (ClassCastException e)
			{
				throw new CommandParseException("Invalid context");
			}
		}

		@Override
		protected String paramSpec()
		{
			return "<context>";
		}

		@Override
		public String shortHelp()
		{
			return "Copies all the statements in the given context on the active one using the identifiers as a correspondence map for dependencies.";
		}

	}

}
