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
import aletheia.model.authority.DelegateTreeNode;
import aletheia.model.authority.DelegateTreeRootNode;
import aletheia.model.authority.DelegateTreeSubNode;
import aletheia.model.authority.Person;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.identifier.Namespace;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.identifier.RootNamespace;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCloseableCollection;
import aletheia.utilities.collections.CloseableIterator;
import aletheia.utilities.collections.FilteredCloseableCollection;
import aletheia.utilities.collections.NotNullFilter;

@TaggedCommand(tag = "delda", groupPath = "/authority", factory = DeleteDelegateAuthorizer.Factory.class)
public class DeleteDelegateAuthorizer extends TransactionalCommand
{
	private final DelegateAuthorizer delegateAuthorizer;

	public DeleteDelegateAuthorizer(CommandSource from, Transaction transaction, DelegateAuthorizer delegateAuthorizer)
	{
		super(from, transaction);
		this.delegateAuthorizer = delegateAuthorizer;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		DelegateTreeNode node = delegateAuthorizer.getDelegateTreeNode(getTransaction());
		delegateAuthorizer.delete(getTransaction());
		while (node != null && !(node instanceof DelegateTreeRootNode))
		{
			if (!node.localDelegateAuthorizerMap(getTransaction()).isEmpty())
				break;
			if (!node.localDelegateTreeSubNodeMap(getTransaction()).isEmpty())
				break;
			DelegateTreeNode node_ = null;
			if (node instanceof DelegateTreeSubNode)
				node_ = ((DelegateTreeSubNode) node).getParent(getTransaction());
			node.delete(getTransaction());
			node = node_;
		}
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<DeleteDelegateAuthorizer>
	{

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		public DeleteDelegateAuthorizer parse(CliJPanel cliJPanel, final Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			try
			{
				checkMinParameters(split);
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
				final DelegateTreeNode node = statementAuthority.getDelegateTreeNode(transaction, prefix);
				if (node == null)
					throw new CommandParseException("Not a delegate on that prefix");
				CloseableIterator<DelegateAuthorizer> iterator = new FilteredCloseableCollection<DelegateAuthorizer>(new NotNullFilter<DelegateAuthorizer>(),
						new BijectionCloseableCollection<Person, DelegateAuthorizer>(new Bijection<Person, DelegateAuthorizer>()
						{

							@Override
							public DelegateAuthorizer forward(Person delegate)
							{
								return node.localDelegateAuthorizerMap(transaction).get(delegate);
							}

							@Override
							public Person backward(DelegateAuthorizer output)
							{
								throw new UnsupportedOperationException();
							}
						}, specToPersons(cliJPanel.getPersistenceManager(), transaction, split.get(0)))).iterator();
				try
				{
					if (!iterator.hasNext())
						throw new CommandParseException("Not a delegate on that prefix with that nick/UUID");
					DelegateAuthorizer da = iterator.next();
					if (iterator.hasNext())
						throw new CommandParseException("Duplicate delegate on that prefix with that nick");
					return new DeleteDelegateAuthorizer(cliJPanel, transaction, da);
				}
				finally
				{
					iterator.close();
				}
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
			return "(<UUID> | <nick>) [<statement [<prefix>]]";
		}

		@Override
		public String shortHelp()
		{
			return "Deletes a delegate authorizer for the active context";
		}

	}

}
