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
package aletheia.gui.cli.command;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.Command.CommandException;
import aletheia.gui.cli.command.Command.CommandParseException;
import aletheia.gui.cli.command.CommandGroup.CommandGroupException;
import aletheia.gui.cli.command.aux.EmptyCommand;
import aletheia.log4j.LoggerManager;
import aletheia.persistence.Transaction;
import aletheia.utilities.MiscUtilities;
import aletheia.utilities.MiscUtilities.NoConstructorException;
import aletheia.utilities.StreamAsStringIterable;
import aletheia.utilities.collections.AdaptedMap;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCloseableCollection;
import aletheia.utilities.collections.BijectionCloseableIterable;
import aletheia.utilities.collections.BijectionMap;
import aletheia.utilities.collections.CloseableIterable;
import aletheia.utilities.collections.CombinedMap;
import aletheia.utilities.collections.EmptyCloseableCollection;
import aletheia.utilities.collections.FilteredCloseableIterable;
import aletheia.utilities.collections.NotNullFilter;
import aletheia.utilities.collections.TrivialCloseableCollection;
import aletheia.utilities.collections.UnionCloseableIterable;

/*
 * Beware: not thread-safe.
 */
public class GlobalCommandFactory extends AbstractVoidCommandFactory<Command>
{
	private static final Logger logger = LoggerManager.instance.logger();

	public static final GlobalCommandFactory instance = new GlobalCommandFactory();

	private static List<String> splitCommand(String command) throws CommandParseException
	{
		List<String> list = new ArrayList<>();
		boolean quoting = false;
		StringBuffer sb = null;
		for (char c : command.toCharArray())
		{
			if (quoting)
			{
				if (c == '\"')
				{
					quoting = false;
					list.add(sb.toString());
					sb = null;
				}
				else
					sb.append(c);
			}
			else
			{
				if (c == '\"')
				{
					if (sb != null)
						list.add(sb.toString());
					sb = new StringBuffer();
					quoting = true;
				}
				else if (Character.isWhitespace(c))
				{
					if (sb != null)
					{
						list.add(sb.toString());
						sb = null;
					}
				}
				else
				{
					if (sb == null)
						sb = new StringBuffer();
					sb.append(c);
				}
			}
		}
		if (quoting)
			throw new CommandParseException("Bad quoting");
		if (sb != null)
			list.add(sb.toString());

		return list;
	}

	private static CloseableIterable<Class<? extends Command>> staticTaggedCommandList(final ClassLoader classLoader, String commandsListResourceName)
	{
		InputStream stream = classLoader.getResourceAsStream(commandsListResourceName);
		if (stream == null)
			return new EmptyCloseableCollection<>();
		return new FilteredCloseableIterable<>(new NotNullFilter<Class<? extends Command>>(),
				new BijectionCloseableIterable<>(new Bijection<String, Class<? extends Command>>()
				{

					@SuppressWarnings("unchecked")
					@Override
					public Class<? extends Command> forward(String input)
					{
						try
						{
							String s = input.replaceAll("#.*", "").trim();
							if (s.isEmpty())
								return null;
							else
								return (Class<? extends Command>) classLoader.loadClass(s);
						}
						catch (ClassNotFoundException e)
						{
							logger.warn("Loading commands", e);
							return null;
						}
					}

					@Override
					public String backward(Class<? extends Command> output)
					{
						throw new UnsupportedOperationException();
					}
				}, new StreamAsStringIterable(stream)));
	}

	private static final String staticTaggedCommandsResourceName = "aletheia/gui/cli/command/staticTaggedCommands.txt";

	private static CloseableIterable<Class<? extends Command>> staticTaggedCommandList()
	{
		class CommandResource
		{
			private final ClassLoader classLoader;
			private final String resourceName;

			public CommandResource(ClassLoader classLoader, String resourceName)
			{
				super();
				this.classLoader = classLoader;
				this.resourceName = resourceName;
			}
		}
		List<CommandResource> commandResourceList = new ArrayList<>();
		commandResourceList.add(new CommandResource(ClassLoader.getSystemClassLoader(), staticTaggedCommandsResourceName));

		String commandsFileName = System.getProperty("aletheia.gui.cli.commands");
		if (commandsFileName != null)
		{
			try
			{
				for (String s : new StreamAsStringIterable(new FileInputStream(commandsFileName)))
				{
					String[] a = s.replaceAll("#.*", "").split(",");
					if (a.length >= 2)
					{
						try
						{
							commandResourceList.add(new CommandResource(new URLClassLoader(new URL[]
							{ new URL(a[0].trim()) }), a[1].trim()));
						}
						catch (Exception e)
						{
						}
					}
				}
			}
			catch (Exception e)
			{
			}
		}
		return new UnionCloseableIterable<>(new BijectionCloseableCollection<>(new Bijection<CommandResource, CloseableIterable<Class<? extends Command>>>()
		{

			@Override
			public CloseableIterable<Class<? extends Command>> forward(CommandResource commandResource)
			{
				return staticTaggedCommandList(commandResource.classLoader, commandResource.resourceName);
			}

			@Override
			public CommandResource backward(CloseableIterable<Class<? extends Command>> output)
			{
				throw new UnsupportedOperationException();
			}
		}, new TrivialCloseableCollection<>(commandResourceList)));
	}

	private final RootCommandGroup rootCommandGroup;

	private final Map<String, AbstractVoidCommandFactory<? extends Command>> staticTaggedFactories;

	private class DynamicTaggedFactoryEntry
	{
		private final URLClassLoader urlClassLoader;
		private final Class<? extends DynamicCommand> commandClass;
		private final CommandGroup commandGroup;
		private final DynamicCommand.Factory<? extends DynamicCommand> factory;

		private DynamicTaggedFactoryEntry(URLClassLoader urlClassLoader, Class<? extends DynamicCommand> commandClass, CommandGroup commandGroup,
				DynamicCommand.Factory<? extends DynamicCommand> factory)
		{
			super();
			this.urlClassLoader = urlClassLoader;
			this.commandClass = commandClass;
			this.commandGroup = commandGroup;
			this.factory = factory;
		}

	}

	private final Map<String, DynamicTaggedFactoryEntry> dynamicTaggedFactoryEntries;
	private final Map<String, DynamicCommand.Factory<? extends DynamicCommand>> dynamicTaggedFactories;
	private final Map<String, AbstractVoidCommandFactory<? extends Command>> taggedFactories;

	private GlobalCommandFactory()
	{
		super();
		this.rootCommandGroup = new RootCommandGroup();
		this.staticTaggedFactories = new HashMap<>();
		try
		{
			for (Class<? extends Command> c : staticTaggedCommandList())
				putStaticCommandClass(c);
		}
		catch (PutCommandException e)
		{
			throw new Error(e);
		}
		finally
		{
		}
		this.dynamicTaggedFactoryEntries = new HashMap<>();
		this.dynamicTaggedFactories = new BijectionMap<>(new Bijection<DynamicTaggedFactoryEntry, DynamicCommand.Factory<? extends DynamicCommand>>()
		{

			@Override
			public DynamicCommand.Factory<? extends DynamicCommand> forward(DynamicTaggedFactoryEntry input)
			{
				return input != null ? input.factory : null;
			}

			@Override
			public DynamicTaggedFactoryEntry backward(DynamicCommand.Factory<? extends DynamicCommand> output)
			{
				throw new UnsupportedOperationException();
			}
		}, dynamicTaggedFactoryEntries);

		this.taggedFactories = new CombinedMap<>(new AdaptedMap<String, AbstractVoidCommandFactory<? extends Command>>(dynamicTaggedFactories),
				staticTaggedFactories);
	}

	@Override
	public RootCommandGroup rootCommandGroup()
	{
		return rootCommandGroup;
	}

	@Override
	public AbstractVoidCommandFactory<? extends Command> getTaggedFactory(String tag)
	{
		return taggedFactories.get(tag);
	}

	public class PutCommandException extends CommandException
	{
		private static final long serialVersionUID = 2521899872397857960L;

		protected PutCommandException()
		{
			super();
		}

		protected PutCommandException(String message, Throwable cause)
		{
			super(message, cause);
		}

		protected PutCommandException(String message)
		{
			super(message);
		}

		protected PutCommandException(Throwable cause)
		{
			super(cause);
		}

	}

	private void putStaticCommandClass(Class<? extends Command> commandClass) throws PutCommandException
	{
		TaggedCommand tc = commandClass.getAnnotation(TaggedCommand.class);
		if (tc == null)
			throw new PutCommandException("Command class not annotated");
		putStaticCommandFactoryClass(tc.tag(), tc.groupPath(), tc.factory());
	}

	private void putStaticCommandFactoryClass(String tag, String groupPath, Class<? extends AbstractCommandFactory<? extends Command, ?>> commandFactoryClass)
			throws PutCommandException
	{
		try
		{
			if (staticTaggedFactories.containsKey(tag))
				throw new PutCommandException("Tag already used");
			Constructor<? extends AbstractCommandFactory<? extends Command, ?>> constructor = commandFactoryClass.getConstructor();
			@SuppressWarnings("unchecked")
			AbstractVoidCommandFactory<? extends Command> factory = (AbstractVoidCommandFactory<? extends Command>) constructor.newInstance();
			staticTaggedFactories.put(tag, factory);
			rootCommandGroup.resolveOrCreatePath(groupPath).putFactory(tag, factory);
		}
		catch (IllegalArgumentException | IllegalAccessException | SecurityException | InstantiationException | ClassCastException | NoSuchMethodException
				| InvocationTargetException | CommandGroupException e)
		{
			throw new PutCommandException(e);
		}
	}

	private synchronized void putDynamicCommandFactoryClass(String tag, String groupPath,
			Class<? extends DynamicCommand.Factory<? extends DynamicCommand>> commandFactoryClass, URLClassLoader urlClassLoader,
			Class<? extends DynamicCommand> commandClass) throws PutCommandException
	{
		try
		{
			if (dynamicTaggedFactoryEntries.containsKey(tag))
				throw new PutCommandException("Tag already used");
			DynamicCommand.Factory<? extends DynamicCommand> factory = MiscUtilities.construct(commandFactoryClass);
			CommandGroup group = rootCommandGroup.resolveOrCreatePath(groupPath);
			dynamicTaggedFactoryEntries.put(tag, new DynamicTaggedFactoryEntry(urlClassLoader, commandClass, group, factory));
			group.putFactory(tag, factory);
		}
		catch (IllegalArgumentException | IllegalAccessException | SecurityException | InstantiationException | ClassCastException | InvocationTargetException
				| NoConstructorException | CommandGroupException e)
		{
			throw new PutCommandException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private synchronized void putDynamicCommandClass(Class<? extends DynamicCommand> commandClass, URLClassLoader urlClassLoader) throws PutCommandException
	{
		TaggedCommand tc = commandClass.getAnnotation(TaggedCommand.class);
		if (tc == null)
			throw new PutCommandException("Command class not annotated");
		try
		{
			putDynamicCommandFactoryClass(tc.tag(), tc.groupPath(), (Class<? extends DynamicCommand.Factory<? extends DynamicCommand>>) tc.factory(),
					urlClassLoader, commandClass);
		}
		catch (ClassCastException e)
		{
			throw new PutCommandException(e);
		}
	}

	public void putDynamicCommandClass(URLClassLoader urlClassLoader, String className) throws PutCommandException
	{
		try
		{
			Class<?> clazz = urlClassLoader.loadClass(className);
			if (!DynamicCommand.class.isAssignableFrom(clazz))
				throw new PutCommandException("Not a DynamicCommand subclass");
			Class<? extends DynamicCommand> commandClass = clazz.asSubclass(DynamicCommand.class);
			putDynamicCommandClass(commandClass, urlClassLoader);
		}
		catch (ClassNotFoundException | NoClassDefFoundError e)
		{
			throw new PutCommandException(e);
		}
	}

	public void putDynamicCommandClass(URL url, String className) throws PutCommandException
	{
		URLClassLoader urlClassLoader = new URLClassLoader(new URL[]
		{ url }, ClassLoader.getSystemClassLoader());
		try
		{
			putDynamicCommandClass(urlClassLoader, className);
		}
		catch (Exception e)
		{
			try
			{
				urlClassLoader.close();
			}
			catch (IOException e1)
			{
			}
			throw e;
		}
	}

	public boolean removeDynamicCommandTag(String tag)
	{
		DynamicTaggedFactoryEntry factoryEntry = dynamicTaggedFactoryEntries.remove(tag);
		if (factoryEntry != null)
			try
			{
				factoryEntry.urlClassLoader.close();
				factoryEntry.commandGroup.removeFactory(tag);
			}
			catch (IOException e)
			{
			}
		return factoryEntry != null;
	}

	public boolean reloadDynamicCommandTag(String tag) throws PutCommandException
	{
		DynamicTaggedFactoryEntry factoryEntry = dynamicTaggedFactoryEntries.remove(tag);
		if (factoryEntry != null)
		{
			factoryEntry.commandGroup.removeFactory(tag);
			try
			{
				factoryEntry.urlClassLoader.close();
			}
			catch (IOException e)
			{
			}
			URLClassLoader urlClassLoader = new URLClassLoader(factoryEntry.urlClassLoader.getURLs(), factoryEntry.urlClassLoader.getParent());
			try
			{
				putDynamicCommandClass(urlClassLoader, factoryEntry.commandClass.getName());
			}
			catch (Exception e)
			{
				try
				{
					factoryEntry.urlClassLoader.close();
				}
				catch (IOException e1)
				{
				}
				throw e;
			}
			return true;
		}
		else
			return false;
	}

	private void close()
	{
		for (DynamicTaggedFactoryEntry factoryEntry : dynamicTaggedFactoryEntries.values())
		{
			try
			{
				factoryEntry.urlClassLoader.close();
			}
			catch (IOException e)
			{
			}
		}
	}

	public final Command parse(CommandSource from, Transaction transaction, String command) throws CommandParseException
	{
		List<String> split = splitCommand(command);
		try
		{
			return parse(from, transaction, split);
		}
		catch (RuntimeException e)
		{
			throw new CommandParseException(e);
		}
	}

	@Override
	public Command parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
	{
		if (split.size() <= 0)
			return new EmptyCommand(from);
		else
		{
			String tag = split.get(0);
			AbstractVoidCommandFactory<? extends Command> factory = taggedFactories.get(tag);
			if (factory == null)
				throw new CommandParseException("Bad command");
			return factory.parse(from, transaction, split.subList(1, split.size()));
		}
	}

	@Override
	protected int minParameters()
	{
		return 0;
	}

	@Override
	protected String paramSpec()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String shortHelp()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void longHelp(PrintStream out)
	{
	}

	@Override
	protected void finalize() throws Throwable
	{
		close();
		super.finalize();
	}

}
