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
import aletheia.model.authority.DelegateAuthorizer;
import aletheia.model.authority.Person;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.identifier.Namespace;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.identifier.RootNamespace;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "rda", groupPath = "/authority", factory = ResetDelegateAuthorizer.Factory.class)
public class ResetDelegateAuthorizer extends TransactionalCommand
{
	private final DelegateAuthorizer delegateAuthorizer;

	public ResetDelegateAuthorizer(CommandSource from, Transaction transaction, DelegateAuthorizer delegateAuthorizer)
	{
		super(from, transaction);
		this.delegateAuthorizer = delegateAuthorizer;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		delegateAuthorizer.clearAuthorizer();
		delegateAuthorizer.persistenceUpdate(getTransaction());
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<ResetDelegateAuthorizer>
	{
		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		public ResetDelegateAuthorizer parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			try
			{
				checkMinParameters(split);
				Person delegate = specToPerson(from.getPersistenceManager(), transaction, split.get(0));
				if (delegate == null)
					throw new CommandParseException("Not a person with this UUID or with this nick, or the nick is not unique.");
				Statement statement;
				Namespace prefix;
				if (split.size() > 1)
				{
					statement = findStatementPath(from.getPersistenceManager(), transaction, from.getActiveContext(), split.get(1));
					if (statement == null)
						throw new CommandParseException("Invalid statement");
					if (split.size() > 2)
						prefix = Namespace.parse(split.get(2));
					else
						prefix = RootNamespace.instance;
				}
				else
				{
					statement = from.getActiveContext();
					if (statement == null)
						throw new NotActiveContextException();
					prefix = RootNamespace.instance;
				}
				StatementAuthority statementAuthority = statement.getAuthority(transaction);
				if (statementAuthority == null)
					throw new CommandParseException("Statement not authored");
				DelegateAuthorizer delegateAuthorizer = statementAuthority.getDelegateAuthorizer(transaction, prefix, delegate);
				if (delegateAuthorizer == null)
					throw new CommandParseException("Not existing delegate authorizer");
				return new ResetDelegateAuthorizer(from, transaction, delegateAuthorizer);
			}
			catch (InvalidNameException | NotActiveContextException e)
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
			return "(<UUID> | <nick>) [<statement> [<prefix>]]";
		}

		@Override
		public String shortHelp()
		{
			return "Resets a delegate authorizer thus invalidating all the signatures it authorizes.";
		}

	}

}
