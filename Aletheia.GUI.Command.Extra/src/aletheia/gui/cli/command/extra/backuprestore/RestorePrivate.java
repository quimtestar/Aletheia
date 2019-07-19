/*******************************************************************************
 * Copyright (c) 2014, 2018 Quim Testar.
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
package aletheia.gui.cli.command.extra.backuprestore;

import java.io.File;
import java.util.List;

import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "restoreprivate", groupPath = "/backup", factory = RestorePrivate.Factory.class)
public class RestorePrivate extends TransactionalCommand
{

	private final File file;

	public RestorePrivate(CommandSource from, Transaction transaction, File file)
	{
		super(from, transaction);
		this.file = file;
	}

	protected File getFile()
	{
		return file;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		char[] passphrase = passphrase();
		if (passphrase == null)
			throw makeCancelledCommandException();
		getPersistenceManager().restorePrivate(getTransaction(), file, passphrase);
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<RestorePrivate>
	{

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		public RestorePrivate parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			File file = new File(split.get(0));
			return new RestorePrivate(from, transaction, file);
		}

		@Override
		protected String paramSpec()
		{
			return "<file>";
		}

		@Override
		public String shortHelp()
		{
			return "Restore a secure backup of the private keys.";
		}

	}

}
