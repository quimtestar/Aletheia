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
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.authority.DelegateAuthorizer;
import aletheia.model.authority.PrivatePerson;
import aletheia.model.authority.PrivateSignatory;
import aletheia.model.authority.Signatory;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.Namespace;
import aletheia.model.identifier.RootNamespace;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "sign", groupPath = "/authority", factory = Sign.Factory.class)
public class Sign extends TransactionalCommand
{
	private final Statement statement;
	private final StatementAuthority statementAuthority;
	private final PrivatePerson delegate;

	public Sign(CommandSource from, Transaction transaction, Statement statement, StatementAuthority statementAuthority, PrivatePerson delegate)
	{
		super(from, transaction);
		this.statement = statement;
		this.statementAuthority = statementAuthority;
		this.delegate = delegate;
	}

	protected Statement getStatement()
	{
		return statement;
	}

	protected StatementAuthority getStatementAuthority()
	{
		return statementAuthority;
	}

	protected PrivatePerson getDelegate()
	{
		return delegate;
	}

	public class SignCommandException extends CommandException
	{
		private static final long serialVersionUID = -8530724078464055317L;

		public SignCommandException(String message)
		{
			super(message);
		}
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		PrivateSignatory authorizer;
		if (statement instanceof RootContext)
		{
			if (!delegate.equals(statementAuthority.getAuthor(getTransaction())))
				throw new SignCommandException("Not the author");
			authorizer = delegate.getPrivateSignatory(getTransaction());
		}
		else
		{
			StatementAuthority parentAuth = statement.getContext(getTransaction()).getAuthority(getTransaction());
			Identifier identifier = statement.getIdentifier();
			Namespace namespace = identifier == null ? RootNamespace.instance : identifier;
			DelegateAuthorizer da = parentAuth.delegateAuthorizerMap(getTransaction(), namespace).get(delegate);
			if (da == null)
				throw new SignCommandException("Not a delegate");
			Signatory authorizer_ = da.getAuthorizer(getTransaction());
			if (authorizer_ == null)
			{
				authorizer = da.createAuthorizer(getTransaction());
				da.sign(getTransaction());
				da.persistenceUpdate(getTransaction());
			}
			else
			{
				if (!(authorizer_ instanceof PrivateSignatory))
					throw new SignCommandException("Haven't got private data for this authorizer");
				authorizer = (PrivateSignatory) authorizer_;
			}
		}
		statementAuthority.createSignature(getTransaction(), authorizer);
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<Sign>
	{

		@Override
		protected int minParameters()
		{
			return 1;
		}

		protected Sign makeSign(CliJPanel cliJPanel, Transaction transaction, Statement statement, StatementAuthority statementAuthority,
				PrivatePerson delegate) throws CommandParseException
		{
			return new Sign(cliJPanel, transaction, statement, statementAuthority, delegate);
		}

		@Override
		public Sign parse(CliJPanel cliJPanel, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			try
			{
				checkMinParameters(split);
				PrivatePerson delegate = cliJPanel.getPersistenceManager().privatePersonsByNick(transaction).get(split.get(0));
				if (delegate == null)
					throw new CommandParseException("Invalid nick");
				Statement statement;
				if (split.size() > 1)
				{
					statement = findStatementPath(cliJPanel.getPersistenceManager(), transaction, cliJPanel.getActiveContext(), split.get(1));
					if (statement == null)
						throw new CommandParseException("Invalid statement");
				}
				else
				{
					statement = cliJPanel.getActiveContext();
					if (statement == null)
						throw new NotActiveContextException();
				}
				StatementAuthority statementAuthority = statement.getAuthority(transaction);
				if (statementAuthority == null)
					throw new CommandParseException("Statement not authored");
				return makeSign(cliJPanel, transaction, statement, statementAuthority, delegate);
			}
			catch (NotActiveContextException e)
			{
				throw new CommandParseException(e);
			}
		}

		@Override
		protected String paramSpec()
		{
			return "<nick> [<statement>]";
		}

		@Override
		public String shortHelp()
		{
			return "Sign a statement's authority.";
		}

	}

}
