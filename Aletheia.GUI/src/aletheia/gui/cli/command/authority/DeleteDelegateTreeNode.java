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

import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.authority.DelegateTreeNode;
import aletheia.model.authority.DelegateTreeSubNode;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.identifier.Namespace;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.identifier.RootNamespace;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "deldt", groupPath = "/authority", factory = DeleteDelegateTreeNode.Factory.class)
public class DeleteDelegateTreeNode extends TransactionalCommand
{
	private final DelegateTreeNode delegateTreeNode;

	public DeleteDelegateTreeNode(CommandSource from, Transaction transaction, DelegateTreeNode delegateTreeNode)
	{
		super(from, transaction);
		this.delegateTreeNode = delegateTreeNode;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		DelegateTreeNode node = delegateTreeNode;
		DelegateTreeNode node_ = null;
		if (node instanceof DelegateTreeSubNode)
			node_ = ((DelegateTreeSubNode) node).getParent(getTransaction());
		node.delete(getTransaction());
		node = node_;
		while (node != null)
		{
			if (!node.localDelegateAuthorizerMap(getTransaction()).isEmpty())
				break;
			if (!node.localDelegateTreeSubNodeMap(getTransaction()).isEmpty())
				break;
			DelegateTreeNode node__ = null;
			if (node instanceof DelegateTreeSubNode)
				node__ = ((DelegateTreeSubNode) node).getParent(getTransaction());
			node.delete(getTransaction());
			node = node__;
		}
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<DeleteDelegateTreeNode>
	{

		@Override
		protected int minParameters()
		{
			return 0;
		}

		@Override
		public DeleteDelegateTreeNode parse(CommandSource from, final Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			try
			{
				checkMinParameters(split);
				Statement statement;
				Namespace prefix;
				if (split.size() > 0)
				{
					statement = findStatementSpec(from.getPersistenceManager(), transaction, from.getActiveContext(), split.get(0));
					if (statement == null)
						throw new CommandParseException("Invalid statement");
					if (split.size() > 1)
						prefix = Namespace.parse(split.get(1));
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
				DelegateTreeNode node = statementAuthority.getDelegateTreeNode(transaction, prefix);
				if (node == null)
					throw new CommandParseException("Not a delegate on that prefix");
				return new DeleteDelegateTreeNode(from, transaction, node);
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
			return "[<statement> [<prefix>]]";
		}

		@Override
		public String shortHelp()
		{
			return "Deletes a delegate tree node by statement and prefix.";
		}

	}

}
