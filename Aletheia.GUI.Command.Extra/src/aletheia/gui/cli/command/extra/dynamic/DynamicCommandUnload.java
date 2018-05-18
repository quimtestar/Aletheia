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
package aletheia.gui.cli.command.extra.dynamic;

import java.util.List;

import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.Command;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "dcu", groupPath = "/dynamic", factory = DynamicCommandUnload.Factory.class)
public class DynamicCommandUnload extends Command
{
	private final String tag;

	protected DynamicCommandUnload(CommandSource from, String tag)
	{
		super(from);
		this.tag = tag;
	}

	@Override
	public void run() throws Exception
	{
		if (!factory.removeDynamicCommandTag(tag))
			throw new Exception("No dynamic command with this tag");
	}

	public static class Factory extends AbstractVoidCommandFactory<DynamicCommandUnload>
	{

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		public DynamicCommandUnload parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			try
			{
				checkMinParameters(split);
				return new DynamicCommandUnload(from, split.get(0));
			}
			finally
			{

			}
		}

		@Override
		protected String paramSpec()
		{
			return "<tag>";
		}

		@Override
		public String shortHelp()
		{
			return "Dynamically loads a command class.";
		}

	}

}
