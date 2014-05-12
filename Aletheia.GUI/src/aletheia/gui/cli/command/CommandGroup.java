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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import aletheia.utilities.collections.ReverseList;

public abstract class CommandGroup
{
	private final SortedMap<String, SubCommandGroup> subGroups;
	private final SortedMap<String, AbstractCommandFactory<? extends Command, ?>> factories;

	protected CommandGroup()
	{
		this.subGroups = new TreeMap<String, SubCommandGroup>();
		this.factories = new TreeMap<String, AbstractCommandFactory<? extends Command, ?>>();
	}

	public static class CommandGroupException extends Command.CommandException
	{
		private static final long serialVersionUID = -3610013028558680897L;

		public CommandGroupException()
		{
			super();
		}

		public CommandGroupException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public CommandGroupException(String message)
		{
			super(message);
		}

		public CommandGroupException(Throwable cause)
		{
			super(cause);
		}
	}

	protected synchronized void putSubGroup(SubCommandGroup subGroup) throws CommandGroupException
	{
		if (subGroups.containsKey(subGroup.getName()))
			throw new CommandGroupException("Subgroup name already used on command group");
		subGroups.put(subGroup.getName(), subGroup);
	}

	public synchronized SortedMap<String, SubCommandGroup> getSubGroups()
	{
		return Collections.unmodifiableSortedMap(subGroups);
	}

	public synchronized SubCommandGroup getSubGroup(String name)
	{
		return subGroups.get(name);
	}

	public synchronized SubCommandGroup getOrCreateSubGroup(String name)
	{
		SubCommandGroup subGroup = getSubGroup(name);
		if (subGroup == null)
		{
			try
			{
				subGroup = new SubCommandGroup(this, name);
			}
			catch (CommandGroupException e)
			{
				throw new RuntimeException(e);
			}
		}
		return subGroup;
	}

	public synchronized void removeSubGroup(String tag)
	{
		subGroups.remove(tag);
	}

	public synchronized void putFactory(String tag, AbstractCommandFactory<? extends Command, ?> factory) throws CommandGroupException
	{
		if (factories.containsKey(tag))
			throw new CommandGroupException("Command tag name already used on command group");
		factories.put(tag, factory);
	}

	public synchronized void removeFactory(String tag)
	{
		factories.remove(tag);
	}

	public synchronized SortedMap<String, AbstractCommandFactory<? extends Command, ?>> getFactories()
	{
		return Collections.unmodifiableSortedMap(factories);
	}

	public synchronized boolean isEmpty()
	{
		return subGroups.isEmpty() && factories.isEmpty();
	}

	public CommandGroup resolveOrCreatePath(String path)
	{
		CommandGroup group = this;
		for (String name : path.split("/"))
		{
			name = name.trim();
			if (!name.isEmpty())
				group = group.getOrCreateSubGroup(name);
		}
		return group;
	}

	public CommandGroup resolvePath(String path)
	{
		CommandGroup group = this;
		for (String name : path.split("/"))
		{
			if (group == null)
				break;
			name = name.trim();
			if (!name.isEmpty())
				group = group.getSubGroup(name);
		}
		return group;
	}

	public String path()
	{
		CommandGroup group = this;
		List<String> nameList = new ArrayList<String>();
		while (group instanceof SubCommandGroup)
		{
			SubCommandGroup subGroup = (SubCommandGroup) group;
			nameList.add(subGroup.getName());
			group = subGroup.getParent();
		}
		StringBuffer buf = new StringBuffer("/");
		for (String name : new ReverseList<>(nameList))
			buf.append(name);
		return buf.toString();
	}

}
