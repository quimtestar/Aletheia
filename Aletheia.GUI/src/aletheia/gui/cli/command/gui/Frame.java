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

import java.util.List;

import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.Command;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "frame", groupPath = "/gui", factory = Frame.Factory.class)
public class Frame extends Command
{
	private final String extraTitle;

	public Frame(CommandSource from, String extraTitle)
	{
		super(from);
		this.extraTitle = extraTitle;
	}

	public Frame(CommandSource from)
	{
		this(from, null);
	}

	@Override
	public void run() throws Exception
	{
		openExtraFrame(extraTitle);
	}

	public static class Factory extends AbstractVoidCommandFactory<Frame>
	{

		@Override
		protected int minParameters()
		{
			return 0;
		}

		@Override
		public Frame parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			if (split.size() > 0)
				return new Frame(from, split.get(0));
			else
				return new Frame(from);
		}

		@Override
		protected String paramSpec()
		{
			return "[<title>]";
		}

		@Override
		public String shortHelp()
		{
			return "Opens an extra application frame.";
		}

	}

}
