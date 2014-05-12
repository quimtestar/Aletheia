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
import aletheia.model.authority.StatementAuthoritySignature;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "ds", groupPath = "/authority", factory = DeleteSignatures.Factory.class)
public class DeleteSignatures extends TransactionalCommand
{
	private final Collection<StatementAuthoritySignature> signatures;

	public DeleteSignatures(CliJPanel from, Transaction transaction, Collection<StatementAuthoritySignature> signatures)
	{
		super(from, transaction);
		this.signatures = signatures;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		for (StatementAuthoritySignature s : signatures)
			s.delete(getTransaction());
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<DeleteSignatures>
	{

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		public DeleteSignatures parse(CliJPanel cliJPanel, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			Collection<StatementAuthoritySignature> signatures = specToStatementAuthoritySignatures(cliJPanel.getPersistenceManager(), transaction,
					cliJPanel.getActiveContext(), split);
			return new DeleteSignatures(cliJPanel, transaction, signatures);
		}

		@Override
		protected String paramSpec()
		{
			return "<statement> [<authorizer UUID> | <context> <prefix> (<person UUID> | <nick>)]";
		}

		@Override
		public String shortHelp()
		{
			return "Deletes an authorizer signatures from a statement.";
		}

	}

}
