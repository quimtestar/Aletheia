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
import java.util.UUID;

import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.authority.UnpackedSignatureRequest;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "cmsr", groupPath = "/authority", factory = CompleteMissingForSignedProofToSignatureRequest.Factory.class)
public class CompleteMissingForSignedProofToSignatureRequest extends TransactionalCommand
{
	private final UnpackedSignatureRequest unpackedSignatureRequest;

	public CompleteMissingForSignedProofToSignatureRequest(CommandSource from, Transaction transaction, UnpackedSignatureRequest unpackedSignatureRequest)
	{
		super(from, transaction);
		this.unpackedSignatureRequest = unpackedSignatureRequest;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		Collection<Statement> statements = unpackedSignatureRequest.refresh(getTransaction()).completeMissingForSignedProofRequest(getTransaction());
		for (Statement statement : statements)
			getOut().println(statement.statementPathString(getTransaction()));
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<CompleteMissingForSignedProofToSignatureRequest>
	{

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		public CompleteMissingForSignedProofToSignatureRequest parse(CommandSource from, Transaction transaction, Void extra, List<String> split)
				throws CommandParseException
		{
			checkMinParameters(split);
			UUID uuid;
			try
			{
				uuid = UUID.fromString(split.get(0));
			}
			catch (IllegalArgumentException e)
			{
				throw new CommandParseException(e);
			}
			UnpackedSignatureRequest unpackedSignatureRequest = from.getPersistenceManager().getUnpackedSignatureRequest(transaction, uuid);
			if (unpackedSignatureRequest == null)
				throw new CommandParseException("Request not found.");
			return new CompleteMissingForSignedProofToSignatureRequest(from, transaction, unpackedSignatureRequest);
		}

		@Override
		protected String paramSpec()
		{
			return "<UUID>";
		}

		@Override
		public String shortHelp()
		{
			return "Adds missing statements to an existing signature request for obtaining fully signed proofs.";
		}
	}

}
