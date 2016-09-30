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
package aletheia.gui.cli.command.gui;

import java.io.File;
import java.util.List;

import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.Command;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "cf", groupPath = "/gui", factory = ConsoleFile.Factory.class)
public class ConsoleFile extends Command
{
	private final File file;

	public ConsoleFile(CommandSource from, File file)
	{
		super(from);
		this.file = file;
	}

	@Override
	public void run() throws Exception
	{
		consoleFile(file);
	}

	public static class Factory extends AbstractVoidCommandFactory<ConsoleFile>
	{

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		public ConsoleFile parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			switch (split.get(0))
			{
			case "on":
				if (split.size() < 1)
					throw new CommandParseException("Must specify file path");
				return new ConsoleFile(from, new File(split.get(1)));
			case "off":
				return new ConsoleFile(from, null);
			default:
				throw new CommandParseException("Invalid option");
			}
		}

		@Override
		protected String paramSpec()
		{
			return "on [file] | off";
		}

		@Override
		public String shortHelp()
		{
			return "Append console i/o to a file.";
		}

	}

}
