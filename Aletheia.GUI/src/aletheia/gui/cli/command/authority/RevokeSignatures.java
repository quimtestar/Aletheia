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

import java.util.Collection;
import java.util.List;

import aletheia.gui.cli.CliJPanel;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.authority.ContextAuthority;
import aletheia.model.authority.DelegateAuthorizer;
import aletheia.model.authority.DelegateTreeRootNode;
import aletheia.model.authority.PrivatePerson;
import aletheia.model.authority.StatementAuthoritySignature;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "rs", groupPath = "/authority", factory = RevokeSignatures.Factory.class)
public class RevokeSignatures extends TransactionalCommand
{
	private final Collection<StatementAuthoritySignature> signatures;

	public RevokeSignatures(CliJPanel from, Transaction transaction, Collection<StatementAuthoritySignature> signatures)
	{
		super(from, transaction);
		this.signatures = signatures;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		for (StatementAuthoritySignature signature : signatures)
		{
			if (signature.isValid())
			{
				Statement statement = signature.getStatement(getTransaction());
				if (!(statement instanceof RootContext))
				{
					ContextAuthority contextAuthority = statement.getContext(getTransaction()).getAuthority(getTransaction());
					if (contextAuthority != null)
					{
						DelegateAuthorizer da = contextAuthority.delegateAuthorizerByAuthorizerMap(getTransaction(), statement.prefix(getTransaction()))
								.get(signature.getAuthorizer(getTransaction()));
						if (da != null)
						{
							if (da.isSigned() && (da.getDelegate(getTransaction()) instanceof PrivatePerson))
							{
								DelegateTreeRootNode rn = da.getDelegateTreeRootNode(getTransaction());
								if (rn.isSigned())
								{
									da.addRevokedSignatureUuid(signature.getSignatureUuid());
									da.sign(getTransaction());
									da.persistenceUpdate(getTransaction());
								}
								else
									getErr().println("Signature: " + signature.getAuthorizer(getTransaction()) + "'s delegate tree root node is not signed");
							}
							else
								getErr().println("Signature: " + signature.getAuthorizer(getTransaction()) + " non-revokable");
						}
						else
							getErr().println("Signature: " + signature.getAuthorizer(getTransaction()) + " has no delegate authorizer");
					}
					else
						getErr().println("Signature: " + signature.getAuthorizer(getTransaction()) + " has no context authority (!?)");
				}
				else
					getErr().println("Signature: " + signature.getAuthorizer(getTransaction()) + " is for a root context");
			}
			else
				getErr().println("Signature: " + signature.getAuthorizer(getTransaction()) + " not valid");
		}
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<RevokeSignatures>
	{

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		public RevokeSignatures parse(CliJPanel cliJPanel, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			Collection<StatementAuthoritySignature> signatures = specToStatementAuthoritySignatures(cliJPanel.getPersistenceManager(), transaction,
					cliJPanel.getActiveContext(), split);
			return new RevokeSignatures(cliJPanel, transaction, signatures);
		}

		@Override
		protected String paramSpec()
		{
			return "<statement> [<authorizer UUID> | <context> <prefix> (<person UUID> | <nick>)]";
		}

		@Override
		public String shortHelp()
		{
			return "Adds signature(s) to the authorizer's revoke list.";
		}

	}

}
