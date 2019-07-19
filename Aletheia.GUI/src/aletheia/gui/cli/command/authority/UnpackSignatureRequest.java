/*******************************************************************************
 * Copyright (c) 2014, 2015 Quim Testar.
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
import aletheia.model.authority.PackedSignatureRequest;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "usr", groupPath = "/authority", factory = UnpackSignatureRequest.Factory.class)
public class UnpackSignatureRequest extends TransactionalCommand
{
	private final PackedSignatureRequest packedSignatureRequest;

	public UnpackSignatureRequest(CommandSource from, Transaction transaction, PackedSignatureRequest packedSignatureRequest)
	{
		super(from, transaction);
		this.packedSignatureRequest = packedSignatureRequest;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		if (!packedSignatureRequest.unpackable(getTransaction()))
			throw new Exception("Not unpackable");
		packedSignatureRequest.unpack(getTransaction());
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<UnpackSignatureRequest>
	{

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		public UnpackSignatureRequest parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
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
			PackedSignatureRequest packedSignatureRequest = from.getPersistenceManager().getPackedSignatureRequest(transaction, uuid);
			if (packedSignatureRequest == null)
				throw new CommandParseException("Request not found.");
			return new UnpackSignatureRequest(from, transaction, packedSignatureRequest);
		}

		@Override
		protected String paramSpec()
		{
			return "<UUID>";
		}

		@Override
		public String shortHelp()
		{
			return "Unpacks a packed signature request (if dependencies are met).";
		}
	}

}
