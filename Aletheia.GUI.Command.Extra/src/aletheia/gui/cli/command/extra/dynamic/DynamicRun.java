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
import java.net.URLClassLoader;
import java.util.List;

import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.Command;
import aletheia.gui.cli.command.DynamicCommand;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.persistence.Transaction;
import aletheia.utilities.MiscUtilities;

@TaggedCommand(tag = "dr", groupPath = "/dynamic", factory = DynamicRun.Factory.class)
public class DynamicRun extends Command
{
	private final URL url;
	private final String className;
	private final List<String> params;

	private DynamicCommand command;

	protected DynamicRun(CommandSource from, URL url, String className, List<String> params)
	{
		super(from);
		this.url = url;
		this.className = className;
		this.params = params;

		this.command = null;
	}

	@Override
	public void run() throws Exception
	{
		URLClassLoader urlClassLoader = new URLClassLoader(new URL[]
		{ url }, ClassLoader.getSystemClassLoader());
		try
		{
			Class<?> clazz = urlClassLoader.loadClass(className);
			if (!DynamicCommand.class.isAssignableFrom(clazz))
				throw new Exception("Not a DynamicCommand subclass");
			Class<? extends DynamicCommand> commandClass = clazz.asSubclass(DynamicCommand.class);
			TaggedCommand tc = commandClass.getAnnotation(TaggedCommand.class);
			if (tc == null)
				throw new Exception("Command class not annotated");
			DynamicCommand.Factory<? extends DynamicCommand> factory = MiscUtilities.construct(dynamicFactory(tc));
			Transaction transaction = getPersistenceManager().beginTransaction();
			try
			{
				command = factory.dynamicParse(getFrom(), transaction, params);
				transaction.commit();
			}
			finally
			{
				transaction.abort();
			}
			command.run();

		}
		finally
		{
			urlClassLoader.close();
		}

	}

	@SuppressWarnings("unchecked")
	private Class<? extends DynamicCommand.Factory<? extends DynamicCommand>> dynamicFactory(TaggedCommand tc) throws Exception
	{
		try
		{
			return (Class<? extends DynamicCommand.Factory<? extends DynamicCommand>>) tc.factory();
		}
		catch (ClassCastException e)
		{
			throw new Exception("Factory class is not subclassing DynamicCommand.SubClass");
		}
	}

	@Override
	public void cancel(String cause)
	{
		super.cancel(cause);
		if (command != null)
			command.cancel(cause);
	}

	public static class Factory extends AbstractVoidCommandFactory<DynamicRun>
	{

		@Override
		protected int minParameters()
		{
			return 2;
		}

		@Override
		public DynamicRun parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			try
			{
				checkMinParameters(split);
				URL url = new URL(split.get(0));
				String className = split.get(1);
				return new DynamicRun(from, url, className, split.subList(2, split.size()));
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
			return "<URL> <dynamic runable class name>";
		}

		@Override
		public String shortHelp()
		{
			return "Dynamically runs some code.";
		}

	}

}
