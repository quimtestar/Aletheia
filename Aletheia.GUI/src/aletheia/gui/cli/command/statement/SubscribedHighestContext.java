/*******************************************************************************
 * Copyright (c) 2014, 2018 Quim Testar.
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
import java.util.Stack;

import org.apache.logging.log4j.Logger;

import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.log4j.LoggerManager;
import aletheia.model.local.ContextLocal;
import aletheia.model.statement.Assumption;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "shc", groupPath = "/statement", factory = SubscribedHighestContext.Factory.class)
public class SubscribedHighestContext extends TransactionalCommand
{
	private static final Logger logger = LoggerManager.instance.logger();

	private final Context context;
	private final boolean unsigned;

	public SubscribedHighestContext(CommandSource from, Transaction transaction, Context context, boolean unsigned)
	{
		super(from, transaction);
		this.context = context;
		this.unsigned = unsigned;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		Stack<Context> stack = new Stack<>();
		stack.push(context);
		while (!stack.isEmpty())
		{
			Context ctx = stack.pop();
			for (Statement statement : ctx.localStatements(getTransaction()).values())
			{
				boolean pushed = false;
				if (statement instanceof Context)
				{
					Context ctx_ = (Context) statement;
					ContextLocal local = ctx_.getLocal(getTransaction());
					if (local != null && local.isSubscribeStatements())
					{
						stack.push(ctx_);
						pushed = true;
					}
				}
				if (!pushed && statement.isProved() && !(statement instanceof Assumption) && (!unsigned || !statement.isValidSignature(getTransaction())))
				{
					logger.trace("Processing: " + statement.getUuid() + ": " + statement.statementPathString(getTransaction()));
					Context ctx_ = statement.highestContext(getTransaction());
					if (!ctx_.equals(ctx))
						getOut().println(statement.statementPathString(getTransaction(), context) + " -> " + ctx_.label());
				}
			}
		}
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<SubscribedHighestContext>
	{

		@Override
		protected int minParameters()
		{
			return 0;
		}

		@Override
		public SubscribedHighestContext parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			boolean unsigned = split.remove("-unsigned");
			checkMinParameters(split);
			Context context;
			if (split.size() > 0)
			{
				try
				{
					context = (Context) findStatementSpec(from.getPersistenceManager(), transaction, from.getActiveContext(), split.get(0));
				}
				catch (ClassCastException e)
				{
					throw new CommandParseException("Not a context.");
				}
			}
			else
			{
				context = from.getActiveContext();
				if (context == null)
					throw new CommandParseException("No active context.");
			}
			return new SubscribedHighestContext(from, transaction, context, unsigned);
		}

		@Override
		protected String paramSpec()
		{
			return "[<context>] [-unsigned]";
		}

		@Override
		public String shortHelp()
		{
			return "Checks if the subscribed descendent statements can be transported to a higher context (keeping their provenness).";
		}

	}

}
