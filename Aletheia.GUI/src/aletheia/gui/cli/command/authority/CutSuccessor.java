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
package aletheia.gui.cli.command.authority;

import java.util.List;

import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.authority.DelegateTreeRootNode;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "cs", groupPath = "/authority", factory = CutSuccessor.Factory.class)
public class CutSuccessor extends TransactionalCommand
{
	private final StatementAuthority statementAuthority;

	public CutSuccessor(CommandSource from, Transaction transaction, StatementAuthority statementAuthority)
	{
		super(from, transaction);
		this.statementAuthority = statementAuthority;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		DelegateTreeRootNode delegateTreeRootNode = statementAuthority.getDelegateTreeRootNode(getTransaction());
		if (delegateTreeRootNode != null)
			delegateTreeRootNode.cutSuccessorEntries(getTransaction());
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<CutSuccessor>
	{

		@Override
		protected int minParameters()
		{
			return 0;
		}

		@Override
		public CutSuccessor parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			try
			{
				checkMinParameters(split);
				Statement statement;
				if (split.size() > 0)
				{
					statement = findStatementSpec(from.getPersistenceManager(), transaction, from.getActiveContext(), split.get(0));
					if (statement == null)
						throw new CommandParseException("Invalid statement");
				}
				else
				{
					statement = from.getActiveContext();
					if (statement == null)
						throw new NotActiveContextException();
				}
				StatementAuthority statementAuthority = statement.getAuthority(transaction);
				if (statementAuthority == null)
					throw new CommandParseException("Statement not authored");
				return new CutSuccessor(from, transaction, statementAuthority);
			}
			catch (NotActiveContextException e)
			{
				throw new CommandParseException(e);
			}
			finally
			{

			}
		}

		@Override
		protected String paramSpec()
		{
			return "(<UUID> | <nick>) [<statement>]";
		}

		@Override
		public String shortHelp()
		{
			return "Cut successor list to the shortest we have credentials for.";
		}

	}

}
