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

import aletheia.gui.cli.CliJPanel;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.authority.Person;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.identifier.Namespace;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.identifier.RootNamespace;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "addda", groupPath = "/authority", factory = AddDelegateAuthorizer.Factory.class)
public class AddDelegateAuthorizer extends TransactionalCommand
{
	private final Person delegate;
	private final StatementAuthority statementAuthority;
	private final Namespace prefix;

	public AddDelegateAuthorizer(CliJPanel from, Transaction transaction, Person delegate, StatementAuthority statementAuthority, Namespace prefix)
	{
		super(from, transaction);
		this.delegate = delegate;
		this.statementAuthority = statementAuthority;
		this.prefix = prefix;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		statementAuthority.getOrCreateDelegateAuthorizer(getTransaction(), prefix, delegate);
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<AddDelegateAuthorizer>
	{

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		public AddDelegateAuthorizer parse(CliJPanel cliJPanel, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			try
			{
				checkMinParameters(split);
				Person delegate = specToPerson(cliJPanel.getPersistenceManager(), transaction, split.get(0));
				if (delegate == null)
					throw new CommandParseException("Not a person with this UUID or with this nick, or the nick is not unique.");
				Statement statement;
				Namespace prefix;
				if (split.size() > 1)
				{
					statement = findStatementPath(cliJPanel.getPersistenceManager(), transaction, cliJPanel.getActiveContext(), split.get(1));
					if (statement == null)
						throw new CommandParseException("Invalid statement");
					if (split.size() > 2)
						prefix = Namespace.parse(split.get(2));
					else
						prefix = RootNamespace.instance;
				}
				else
				{
					statement = cliJPanel.getActiveContext();
					if (statement == null)
						throw new NotActiveContextException();
					prefix = RootNamespace.instance;
				}
				StatementAuthority statementAuthority = statement.getAuthority(transaction);
				if (statementAuthority == null)
					throw new CommandParseException("Statement not authored");
				return new AddDelegateAuthorizer(cliJPanel, transaction, delegate, statementAuthority, prefix);
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
			return "Creates a new delegate authorizer for the specified or active context.";
		}

	}

}
