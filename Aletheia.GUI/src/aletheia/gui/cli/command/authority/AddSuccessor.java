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
import aletheia.model.authority.Person;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "as", groupPath = "/authority", factory = AddSuccessor.Factory.class)
public class AddSuccessor extends TransactionalCommand
{
	private final Person successor;
	private final StatementAuthority statementAuthority;

	public AddSuccessor(CommandSource from, Transaction transaction, Person successor, StatementAuthority statementAuthority)
	{
		super(from, transaction);
		this.successor = successor;
		this.statementAuthority = statementAuthority;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		DelegateTreeRootNode delegateTreeRootNode = statementAuthority.getOrCreateDelegateTreeRootNode(getTransaction());
		delegateTreeRootNode.addSuccessorEntry(getTransaction(), successor);
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<AddSuccessor>
	{

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		public AddSuccessor parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			try
			{
				checkMinParameters(split);
				Person delegate = specToPerson(from.getPersistenceManager(), transaction, split.get(0));
				if (delegate == null)
					throw new CommandParseException("Not a person with this UUID or with this nick, or the nick is not unique.");
				Statement statement;
				if (split.size() > 1)
				{
					statement = findStatementSpec(from.getPersistenceManager(), transaction, from.getActiveContext(), split.get(1));
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
				return new AddSuccessor(from, transaction, delegate, statementAuthority);
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
			return "Adds a successor for the specified or active context.";
		}

	}

}
