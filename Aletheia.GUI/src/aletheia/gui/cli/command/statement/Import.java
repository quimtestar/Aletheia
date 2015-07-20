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
package aletheia.gui.cli.command.statement;

import java.io.File;
import java.util.List;

import aletheia.gui.cli.CliJPanel;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.Command;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.persistence.Transaction;
import aletheia.utilities.aborter.ListenableAborter;

@TaggedCommand(tag = "import", groupPath = "/statement", factory = Import.Factory.class)
public class Import extends Command
{
	private final File file;

	private class MyListenableAborter extends ListenableAborter
	{
		boolean cancel = false;
		String cause = null;

		protected synchronized void abort(String cause)
		{
			this.cancel = true;
			this.cause = cause;
			abort();
		}

		@Override
		public synchronized void checkAbort() throws AbortException
		{
			if (cancel)
				throw new AbortException(cause);
		}

	}

	private final MyListenableAborter listenableAborter;

	public Import(CliJPanel from, File file)
	{
		super(from);
		this.file = file;
		this.listenableAborter = new MyListenableAborter();

	}

	@Override
	public void run() throws Exception
	{
		getPersistenceManager().import_(file, listenableAborter);
	}

	@Override
	public void cancel(String cause)
	{
		listenableAborter.abort("Import cancelled " + cause + ".");
	}

	public static class Factory extends AbstractVoidCommandFactory<Import>
	{

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		public Import parse(CliJPanel cliJPanel, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			File file = new File(split.get(0));
			return new Import(cliJPanel, file);
		}

		@Override
		protected String paramSpec()
		{
			return "<file>";
		}

		@Override
		public String shortHelp()
		{
			return "Imports a set of statements from a file.";
		}

	}

}
