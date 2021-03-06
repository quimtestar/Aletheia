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
package aletheia.gui.cli.command.authority;

import java.util.List;
import java.util.Stack;

import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.local.ContextLocal;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "unssub", groupPath = "/authority", factory = UnsignedSubscribed.Factory.class)
public class UnsignedSubscribed extends TransactionalCommand
{
	private final Context context;
	private final boolean proof;

	public UnsignedSubscribed(CommandSource from, Transaction transaction, Context context, boolean proof)
	{
		super(from, transaction);
		this.context = context;
		this.proof = proof;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		Stack<ContextLocal> stack = new Stack<>();
		{
			ContextLocal cl = context.getLocal(getTransaction());
			if ((cl != null) && cl.isSubscribeStatements())
				stack.push(cl);
		}
		while (!stack.isEmpty())
		{
			ContextLocal cl = stack.pop();
			for (Statement st : cl.getStatement(getTransaction()).localSortedStatements(getTransaction()))
			{
				StatementAuthority stAuth = st.getAuthority(getTransaction());
				if (stAuth == null || (!proof && !stAuth.isValidSignature()) || (proof && !stAuth.isSignedProof()))
					getOut().println(" -> " + st.statementPathString(getTransaction(), getActiveContext()) + " " + (st.isProved() ? "\u2713" : ""));
				if (st instanceof Context)
				{
					ContextLocal cl_ = ((Context) st).getLocal(getTransaction());
					if ((cl_ != null) && cl_.isSubscribeStatements())
						stack.push(cl_);
				}
			}
		}
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<UnsignedSubscribed>
	{

		@Override
		protected int minParameters()
		{
			return 0;
		}

		@Override
		public UnsignedSubscribed parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			try
			{
				boolean proof = split.remove("-proof");
				checkMinParameters(split);
				Context context;
				if (split.size() > 0)
				{
					Statement statement = findStatementSpec(from.getPersistenceManager(), transaction, from.getActiveContext(), split.get(0));
					if (statement == null)
						throw new CommandParseException("Invalid statement");
					if (!(statement instanceof Context))
						throw new CommandParseException("Not a context");
					context = (Context) statement;
				}
				else
				{
					context = from.getActiveContext();
					if (context == null)
						throw new NotActiveContextException();
				}
				return new UnsignedSubscribed(from, transaction, context, proof);
			}
			catch (NotActiveContextException e)
			{
				throw new CommandParseException(e);
			}
		}

		@Override
		protected String paramSpec()
		{
			return "[<context>] [-proof]";
		}

		@Override
		public String shortHelp()
		{
			return "Lists all the unsigned statements under a subscribed subcontext of the specified (or the active) one.";
		}

	}

}
