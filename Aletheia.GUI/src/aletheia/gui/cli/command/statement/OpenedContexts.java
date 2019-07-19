/*******************************************************************************
 * Copyright (c) 2014, 2016 Quim Testar.
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
import java.util.List;
import java.util.Stack;

import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "opdctx", groupPath = "/statement", factory = OpenedContexts.Factory.class)
public class OpenedContexts extends TransactionalCommand
{
	public OpenedContexts(CommandSource from, Transaction transaction)
	{
		super(from, transaction);
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws NotActiveContextException
	{
		Context ctx = getActiveContext();
		if (ctx == null)
			throw new NotActiveContextException();
		Stack<Context> stack = new Stack<>();
		stack.push(ctx);
		while (!stack.isEmpty())
		{
			Context ctx_ = stack.pop();
			List<? extends Statement> path = ctx_.statementPath(getTransaction(), ctx);
			StringBuffer sbpath = new StringBuffer();
			for (Statement st2 : path)
			{
				if (!(st2 instanceof RootContext))
				{
					Identifier id = st2.identifier(getTransaction());
					if (id != null)
						sbpath.append(id.toString());
					else
						sbpath.append(st2.getVariable().toString());
				}
				if (!st2.equals(ctx_))
					sbpath.append("/");
			}
			getOut().println(" -> " + sbpath);
			List<Context> add = new ArrayList<>();
			for (Context ctx__ : ctx_.subContexts(getTransaction()))
			{
				if (!ctx__.isProved())
					add.add(ctx__);
			}
			stack.addAll(add);
		}
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<OpenedContexts>
	{

		@Override
		protected int minParameters()
		{
			return 0;
		}

		@Override
		public OpenedContexts parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			return new OpenedContexts(from, transaction);
		}

		@Override
		protected String paramSpec()
		{
			return "";
		}

		@Override
		public String shortHelp()
		{
			return "Lists the paths of all the opened contexts that descend from the active one.";
		}

	}

}
