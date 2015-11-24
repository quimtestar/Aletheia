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
import java.util.Map.Entry;
import java.util.Stack;

import aletheia.gui.cli.CliJPanel;
import aletheia.gui.cli.command.AbstractCommandFactory;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.Command;
import aletheia.gui.cli.command.CommandGroup;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.SubCommandGroup;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.BufferedList;
import aletheia.utilities.collections.ReverseList;

@TaggedCommand(tag = "help", groupPath = "/gui", factory = Help.Factory.class)
public class Help extends Command
{
	private final String tag;
	private final AbstractCommandFactory<?, ?> factory;
	private final CommandGroup commandGroup;

	protected Help(CommandSource from, String tag, AbstractCommandFactory<?, ?> factory, CommandGroup commandGroup)
	{
		super(from);
		this.tag = tag;
		this.factory = factory;
		this.commandGroup = commandGroup;
	}

	@Override
	public void run() throws Exception
	{
		if (tag != null)
		{
			String commandSpec = factory.commandSpec(tag);
			if (commandSpec != null)
				getOutB().println(commandSpec);
		}
		factory.longHelp(getOut());
		getOut().println();
		if (commandGroup != null)
		{
			getOut().println("(Sub) commands:");
			Stack<CommandGroup> stack = new Stack<CommandGroup>();
			stack.push(commandGroup);
			while (!stack.isEmpty())
			{
				CommandGroup group = stack.pop();
				if (group instanceof SubCommandGroup)
					getOutB().println("\t* " + group.path());
				for (Entry<String, AbstractCommandFactory<?, ?>> e : group.getFactories().entrySet())
				{
					if (group instanceof SubCommandGroup)
						getOut().print("\t");
					getOut().print("\t");
					getOutB().print(e.getValue().commandSpec(e.getKey()));
					getOut().print(" : " + e.getValue().shortHelp());
					getOut().println();
				}
				getOut().println();
				stack.addAll(new ReverseList<>(new BufferedList<>(group.getSubGroups().values())));
			}
		}
		getOut().flush();
	}

	public static class Factory extends AbstractVoidCommandFactory<Help>
	{

		@Override
		protected int minParameters()
		{
			return 0;
		}

		@Override
		public Help parse(CliJPanel cliJPanel, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			String tag = null;
			AbstractCommandFactory<?, ?> factory = Command.factory;
			CommandGroup commandGroup = null;
			for (int i = 0; i < split.size(); i++)
			{
				String s = split.get(i);
				if (s.startsWith("/"))
				{
					if (factory.rootCommandGroup() == null)
						throw new CommandParseException("Command has no subgroups");
					commandGroup = factory.rootCommandGroup().resolvePath(s);
					if (commandGroup == null)
						throw new CommandParseException("Subgroup doesn't exist");
					break;
				}
				else
				{
					tag = s;
					factory = factory.getTaggedFactory(tag);
					if (factory == null)
					{
						StringBuffer sb = new StringBuffer();
						for (int j = 0; j < i; j++)
							sb.append(split.get(j) + " ");
						sb.append(split.get(i));
						throw new CommandParseException("Unknown command: " + sb);
					}
				}
			}
			if (commandGroup == null)
				commandGroup = factory.rootCommandGroup();
			return new Help(cliJPanel, tag, factory, commandGroup);
		}

		@Override
		protected String paramSpec()
		{
			return "<command>*";
		}

		@Override
		public String shortHelp()
		{
			return "Gives help on commands (and subcommands).";
		}

	}

}
