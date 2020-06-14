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
package aletheia.gui.cli.command.extra.dynamic;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.Command;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "dcl", groupPath = "/dynamic", factory = DynamicCommandLoad.Factory.class)
public class DynamicCommandLoad extends Command
{
	private final URL url;
	private final String className;

	protected DynamicCommandLoad(CommandSource from, URL url, String className)
	{
		super(from);
		this.url = url;
		this.className = className;
	}

	@Override
	public void run() throws Exception
	{
		factory.putDynamicCommandClass(url, className);
	}

	public static class Factory extends AbstractVoidCommandFactory<DynamicCommandLoad>
	{

		@Override
		protected int minParameters()
		{
			return 2;
		}

		@Override
		public DynamicCommandLoad parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			try
			{
				checkMinParameters(split);
				URL url = new URL(split.get(0));
				String className = split.get(1);
				return new DynamicCommandLoad(from, url, className);
			}
			catch (MalformedURLException e)
			{
				throw new CommandParseException(e);
			}
			finally
			{

			}
		}

		@Override
		protected String paramSpec()
		{
			return "<URL> <factory class name>";
		}

		@Override
		public String shortHelp()
		{
			return "Dynamically loads a command class.";
		}

	}

}
