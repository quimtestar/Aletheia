/*******************************************************************************
 * Copyright (c) 2018, 2019 Quim Testar
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
package aletheia.test;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.NoSuchElementException;
import java.util.Optional;

import aletheia.utilities.CommandLineArguments;
import aletheia.utilities.CommandLineArguments.CommandLineArgumentsException;
import aletheia.utilities.CommandLineArguments.Parameter;
import aletheia.utilities.MiscUtilities;

public abstract class Test
{
	static
	{
		MiscUtilities.dummy();
	}

	public void run() throws Exception
	{
	}

	public static void main(String[] args)
	{
		try
		{
			CommandLineArguments cla = new CommandLineArguments(args);
			String className = Optional.ofNullable(MiscUtilities.firstFromIterable(cla.getParameters())).map((Parameter p) -> p.getValue()).get();
			try
			{
				@SuppressWarnings("unchecked")
				Class<? extends Test> testClass = (Class<? extends Test>) ClassLoader.getSystemClassLoader().loadClass(className);
				if (!Test.class.isAssignableFrom(testClass))
				{
					System.err.format("Class '%s' does not derive from '%s'\n", className, Test.class.getName());
				}
				else if (Modifier.isAbstract(testClass.getModifiers()))
				{
					System.err.format("Class '%s' is abstract\n", className);
				}
				else
				{
					if (cla.getGlobalSwitches().containsKey("i"))
					{
						System.out.format("Run test class '%s'?\n", className);
						try
						{
							System.in.read();
						}
						catch (IOException e)
						{
							throw new RuntimeException(e);
						}
					}
					Constructor<? extends Test> constructor = testClass.getConstructor();
					Test test = constructor.newInstance();
					test.run();
					System.exit(0);
				}
			}
			catch (ClassNotFoundException e)
			{
				System.err.format("Class '%s' not found\n", className);
			}
			catch (NoSuchMethodException e)
			{
				System.err.format("Class '%s' have not a public no-argument constructor\n", className);
			}
			catch (SecurityException e)
			{
				System.err.format("Access to class '%s' is denied\n", className);
				e.printStackTrace();
			}
			catch (InstantiationException e)
			{
				System.err.format("Class '%s' could not be instantiated\n", className);
				e.printStackTrace();
			}
			catch (IllegalAccessException e)
			{
				System.err.format("Illegal access instantiating class '%s'\n", className);
				e.printStackTrace();
			}
			catch (IllegalArgumentException e)
			{
				System.err.format("Illegal argument instantiating class '%s'\n", className);
				e.printStackTrace();
			}
			catch (InvocationTargetException e)
			{
				System.err.format("Exception '%s' thrown when instantiating class '%s'\n", e.getCause().getClass().getName(), className);
				e.printStackTrace();
			}
			catch (Exception e)
			{
				System.err.format("Exception '%s' thrown when running test instance of '%s'\n", e.getClass().getName(), className);
				e.printStackTrace();
			}
		}
		catch (CommandLineArgumentsException e)
		{
			System.err.println(e.getMessage());
		}
		catch (NoSuchElementException e)
		{
			System.err.format("Usage: java %s [-i] <test class>", Test.class.getName());
		}
		System.exit(1);
	}

}
