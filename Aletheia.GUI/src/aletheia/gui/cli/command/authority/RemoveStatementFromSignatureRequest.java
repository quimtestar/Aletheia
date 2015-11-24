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
import java.util.UUID;

import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.authority.UnpackedSignatureRequest;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "rssr", groupPath = "/authority", factory = RemoveStatementFromSignatureRequest.Factory.class)
public class RemoveStatementFromSignatureRequest extends TransactionalCommand
{
	private final UnpackedSignatureRequest unpackedSignatureRequest;
	private final Statement statement;

	public RemoveStatementFromSignatureRequest(CommandSource from, Transaction transaction, UnpackedSignatureRequest unpackedSignatureRequest,
			Statement statement)
	{
		super(from, transaction);
		this.unpackedSignatureRequest = unpackedSignatureRequest;
		this.statement = statement;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		unpackedSignatureRequest.refresh(getTransaction()).removeStatement(getTransaction(), statement);
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<RemoveStatementFromSignatureRequest>
	{
		@Override
		protected int minParameters()
		{
			return 2;
		}

		@Override
		public RemoveStatementFromSignatureRequest parse(CommandSource from, Transaction transaction, Void extra, List<String> split)
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
			Statement statement = findStatementPath(from.getPersistenceManager(), transaction, from.getActiveContext(), split.get(1));
			if (statement == null)
				throw new CommandParseException("Invalid statement");
			if (!unpackedSignatureRequest.statements(transaction).contains(statement))
				throw new CommandParseException("Statement not in request");
			return new RemoveStatementFromSignatureRequest(from, transaction, unpackedSignatureRequest, statement);
		}

		@Override
		protected String paramSpec()
		{
			return "<UUID> <statement>";
		}

		@Override
		public String shortHelp()
		{
			return "Remove a statement from a signature request.";
		}
	}

}
