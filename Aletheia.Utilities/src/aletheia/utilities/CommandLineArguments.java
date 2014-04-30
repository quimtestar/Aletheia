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
package aletheia.utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses the value of an args[] parameter of a main method to a list of
 * parameters and a set of switches.
 * 
 * @author Quim Testar
 */
public class CommandLineArguments
{
	public abstract class CommandLineArgumentsException extends Exception
	{
		private static final long serialVersionUID = 3218544496784246840L;

		public CommandLineArgumentsException()
		{
			super();
		}

		public CommandLineArgumentsException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public CommandLineArgumentsException(String message)
		{
			super(message);
		}

		public CommandLineArgumentsException(Throwable cause)
		{
			super(cause);
		}
	}

	public abstract class Argument
	{
		private final int position;

		private Argument(int position)
		{
			this.position = position;
		}

		public int getPosition()
		{
			return position;
		}
	}

	public class Switch extends Argument
	{
		private final String key;

		private Switch(int position, String key)
		{
			super(position);
			this.key = key;
		}

		public String getKey()
		{
			return key;
		}
	}

	public class Option extends Switch
	{
		private final String value;

		private Option(int position, String key, String value)
		{
			super(position, key);
			this.value = value;
		}

		private Option(int position, String key)
		{
			super(position, key);
			this.value = null;
		}

		public String getValue()
		{
			return value;
		}
	}

	public class Parameter extends Argument
	{
		private final String value;
		private final Map<String, Switch> switches;

		private Parameter(int position, String value)
		{
			super(position);
			this.value = value;
			this.switches = new HashMap<String, Switch>();
		}

		public String getValue()
		{
			return value;
		}

		public Map<String, Switch> getSwitches()
		{
			return Collections.unmodifiableMap(switches);
		}
	}

	private final Map<String, Switch> globalSwitches;
	private final List<Parameter> parameters;

	public CommandLineArguments(String[] args) throws CommandLineArgumentsException
	{
		this.globalSwitches = new HashMap<String, Switch>();
		this.parameters = new ArrayList<Parameter>();
		parseArgs(args);
	}

	public class DuplicateSwitchException extends CommandLineArgumentsException
	{
		private static final long serialVersionUID = 4553733638594698752L;

		public DuplicateSwitchException(String key)
		{
			super("Command line argument error: duplicate switch key: " + key);
		}

	}

	private int parseSwitches(String[] args, int pos, Map<String, Switch> switches) throws DuplicateSwitchException
	{
		while (pos < args.length)
		{
			if (args[pos].startsWith("-"))
			{
				if (args[pos].startsWith("--"))
				{
					int ie = args[pos].indexOf('=');
					if (ie >= 0)
					{
						String key = args[pos].substring(2, ie);
						String value = args[pos].substring(ie + 1);
						if (switches.put(key, new Option(pos, key, value)) != null)
							throw new DuplicateSwitchException(key);
					}
					else
					{
						String key = args[pos].substring(2);
						if (switches.put(key, new Option(pos, key)) != null)
							throw new DuplicateSwitchException(key);
					}
				}
				else
				{
					String key = args[pos].substring(1);
					if (switches.put(key, new Switch(pos, key)) != null)
						throw new DuplicateSwitchException(key);
				}
			}
			else
				break;
			pos++;
		}
		return pos;
	}

	private int parseParameter(String[] args, int pos, List<Parameter> parameters) throws DuplicateSwitchException
	{
		String value = args[pos];
		Parameter param = new Parameter(pos, value);
		pos++;
		pos = parseSwitches(args, pos, param.switches);
		parameters.add(param);
		return pos;
	}

	private void parseArgs(String[] args) throws DuplicateSwitchException
	{
		int pos = 0;
		pos = parseSwitches(args, pos, globalSwitches);
		while (pos < args.length)
			pos = parseParameter(args, pos, parameters);
	}

	public Map<String, Switch> getGlobalSwitches()
	{
		return Collections.unmodifiableMap(globalSwitches);
	}

	public List<Parameter> getParameters()
	{
		return Collections.unmodifiableList(parameters);
	}

}
