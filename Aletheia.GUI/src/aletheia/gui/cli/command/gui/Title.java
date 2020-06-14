/*******************************************************************************
 * Copyright (c) 2014, 2020 Quim Testar.
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

import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.Command;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "title", groupPath = "/gui", factory = Title.Factory.class)
public class Title extends Command
{
	private final String extraTitle;

	public Title(CommandSource from, String extraTitle)
	{
		super(from);
		this.extraTitle = extraTitle;
	}

	public Title(CommandSource from)
	{
		this(from, null);
	}

	@Override
	public void run() throws Exception
	{
		setExtraTitle(extraTitle);
	}

	public static class Factory extends AbstractVoidCommandFactory<Title>
	{

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		public Title parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			return new Title(from, split.get(0));
		}

		@Override
		protected String paramSpec()
		{
			return "<title>";
		}

		@Override
		public String shortHelp()
		{
			return "Change the frame's title (just extra frames, not the main one).";
		}

	}

}
