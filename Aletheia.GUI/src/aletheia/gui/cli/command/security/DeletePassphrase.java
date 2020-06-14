/*******************************************************************************
 * Copyright (c) 2016, 2020 Quim Testar.
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
package aletheia.gui.cli.command.security;

import java.util.List;

import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.Command;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "deletepassphrase", groupPath = "/security", factory = DeletePassphrase.Factory.class)
public class DeletePassphrase extends Command
{

	public DeletePassphrase(CommandSource from)
	{
		super(from);
	}

	@Override
	public void run() throws Exception
	{
		if (confirmDialog("This action might take some minutes, since all the private keys must be decrypted."))
			getPersistenceManager().getSecretKeyManager().deletePassphrase();
	}

	public static class Factory extends AbstractVoidCommandFactory<DeletePassphrase>
	{

		@Override
		protected int minParameters()
		{
			return 0;
		}

		@Override
		public DeletePassphrase parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			return new DeletePassphrase(from);
		}

		@Override
		protected String paramSpec()
		{
			return "";
		}

		@Override
		public String shortHelp()
		{
			return "Deletes the security passphrase. Decrypts every private key.";
		}

	}

}
