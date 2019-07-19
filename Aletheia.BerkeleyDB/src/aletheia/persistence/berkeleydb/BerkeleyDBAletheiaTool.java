/*******************************************************************************
 * Copyright (c) 2014, 2016 Quim Testar.
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
package aletheia.persistence.berkeleydb;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import aletheia.persistence.berkeleydb.lowlevelbackuprestore.LowLevelBackupRestoreEntityStore;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.CommandLineArguments;
import aletheia.utilities.CommandLineArguments.Option;
import aletheia.utilities.CommandLineArguments.Parameter;
import aletheia.utilities.CommandLineArguments.Switch;
import aletheia.version.VersionManager;

import com.sleepycat.persist.EntityStore;

public class BerkeleyDBAletheiaTool
{

	private class ToolException extends Exception
	{
		private static final long serialVersionUID = -5342220710448665920L;

		public ToolException()
		{
			super();
		}

		public ToolException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public ToolException(String message)
		{
			super(message);
		}

		public ToolException(Throwable cause)
		{
			super(cause);
		}

	}

	private class ArgumentsException extends ToolException
	{
		private static final long serialVersionUID = -5342220710448665920L;

		@SuppressWarnings("unused")
		public ArgumentsException()
		{
			super();
		}

		@SuppressWarnings("unused")
		public ArgumentsException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public ArgumentsException(String message)
		{
			super(message);
		}

		@SuppressWarnings("unused")
		public ArgumentsException(Throwable cause)
		{
			super(cause);
		}

	}

	private class ExecuteException extends ToolException
	{
		private static final long serialVersionUID = -5342220710448665920L;

		@SuppressWarnings("unused")
		public ExecuteException()
		{
			super();
		}

		@SuppressWarnings("unused")
		public ExecuteException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public ExecuteException(String message)
		{
			super(message);
		}

		public ExecuteException(Throwable cause)
		{
			super(cause);
		}

	}

	private final File dbFile;

	private abstract class Command
	{

		protected Command()
		{
		}

		protected abstract void execute() throws ExecuteException;
	}

	private class VersionCommand extends Command
	{

		@Override
		protected void execute() throws ExecuteException
		{
			System.out.println(VersionManager.getVersion());
		}

	}

	private class LStoresCommand extends Command
	{

		protected LStoresCommand(Map<String, Switch> switches, Queue<Parameter> parameters) throws ArgumentsException
		{
			super();
			if (!switches.isEmpty())
				throw new ArgumentsException("Unrecognized switches/options: " + switches.keySet());
		}

		@Override
		protected void execute()
		{
			BerkeleyDBPersistenceManager.Configuration configuration = new BerkeleyDBPersistenceManager.Configuration();
			configuration.setDbFile(dbFile);
			configuration.setAllowCreate(false);
			configuration.setReadOnly(true);
			configuration.setAllowUpgrade(false);
			BerkeleyDBAletheiaEnvironment environment = new BerkeleyDBAletheiaEnvironment(configuration);
			try
			{
				for (String storeName : EntityStore.getStoreNames(environment))
					System.out.println(storeName);
			}
			finally
			{
				environment.close();
			}
		}
	}

	private class DStoreCommand extends Command
	{
		private final String storeName;

		protected DStoreCommand(Map<String, Switch> switches, Queue<Parameter> parameters) throws ArgumentsException
		{
			super();
			if (!switches.isEmpty())
				throw new ArgumentsException("Unrecognized switches/options: " + switches.keySet());
			if (parameters.isEmpty())
				throw new ArgumentsException("Missing parameter");
			Parameter parameter = parameters.poll();
			if (!parameter.getSwitches().isEmpty())
				throw new ArgumentsException("Unrecognized switches/options: " + switches.keySet());
			this.storeName = parameter.getValue();

		}

		@Override
		protected void execute()
		{
			BerkeleyDBPersistenceManager.Configuration configuration = new BerkeleyDBPersistenceManager.Configuration();
			configuration.setDbFile(dbFile);
			configuration.setAllowCreate(false);
			configuration.setReadOnly(false);
			configuration.setAllowUpgrade(false);
			BerkeleyDBAletheiaEnvironment environment = new BerkeleyDBAletheiaEnvironment(configuration);
			try
			{
				environment.removeStore(storeName);
			}
			finally
			{
				environment.close();
			}
		}

	}

	private class MStoreCommand extends Command
	{
		private final String storeNameSrc;
		private final String storeNameDst;

		protected MStoreCommand(Map<String, Switch> switches, Queue<Parameter> parameters) throws ArgumentsException
		{
			super();
			if (!switches.isEmpty())
				throw new ArgumentsException("Unrecognized switches/options: " + switches.keySet());
			{
				if (parameters.isEmpty())
					throw new ArgumentsException("Missing parameter");
				Parameter parameter = parameters.poll();
				if (!parameter.getSwitches().isEmpty())
					throw new ArgumentsException("Unrecognized switches/options: " + switches.keySet());
				this.storeNameSrc = parameter.getValue();
			}
			{
				if (parameters.isEmpty())
					throw new ArgumentsException("Missing parameter");
				Parameter parameter = parameters.poll();
				if (!parameter.getSwitches().isEmpty())
					throw new ArgumentsException("Unrecognized switches/options: " + switches.keySet());
				this.storeNameDst = parameter.getValue();
			}
		}

		@Override
		protected void execute() throws ExecuteException
		{
			BerkeleyDBPersistenceManager.Configuration configuration = new BerkeleyDBPersistenceManager.Configuration();
			configuration.setDbFile(dbFile);
			configuration.setAllowCreate(false);
			configuration.setReadOnly(false);
			configuration.setAllowUpgrade(false);
			BerkeleyDBAletheiaEnvironment environment = new BerkeleyDBAletheiaEnvironment(configuration);
			try
			{
				if (!environment.storeDatabaseNames(storeNameDst).isEmpty())
					throw new ExecuteException("There exist databases that match the destination store name");
				environment.renameStore(storeNameSrc, storeNameDst);
			}
			finally
			{
				environment.close();
			}
		}

	}

	private class BStoreCommand extends Command
	{
		private final String storeName;
		private final File backupFile;

		protected BStoreCommand(Map<String, Switch> switches, Queue<Parameter> parameters) throws ArgumentsException
		{
			super();
			if (!switches.isEmpty())
				throw new ArgumentsException("Unrecognized switches/options: " + switches.keySet());
			{
				if (parameters.isEmpty())
					throw new ArgumentsException("Missing parameter");
				Parameter parameter = parameters.poll();
				if (!parameter.getSwitches().isEmpty())
					throw new ArgumentsException("Unrecognized switches/options: " + switches.keySet());
				this.storeName = parameter.getValue();
			}
			{
				if (parameters.isEmpty())
					throw new ArgumentsException("Missing parameter");
				Parameter parameter = parameters.poll();
				if (!parameter.getSwitches().isEmpty())
					throw new ArgumentsException("Unrecognized switches/options: " + switches.keySet());
				this.backupFile = new File(parameter.getValue());
			}
		}

		@Override
		protected void execute() throws ExecuteException
		{
			BerkeleyDBPersistenceManager.Configuration configuration = new BerkeleyDBPersistenceManager.Configuration();
			configuration.setDbFile(dbFile);
			configuration.setAllowCreate(false);
			configuration.setReadOnly(true);
			configuration.setAllowUpgrade(false);
			BerkeleyDBAletheiaEnvironment environment = new BerkeleyDBAletheiaEnvironment(configuration);
			try
			{
				DataOutputStream out = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(backupFile)));
				try
				{
					new LowLevelBackupRestoreEntityStore(environment).backup(out, storeName);
				}
				finally
				{
					out.close();
				}
			}
			catch (IOException e)
			{
				throw new ExecuteException(e);
			}
			finally
			{
				environment.close();
			}
		}

	}

	private class RStoreCommand extends Command
	{
		private final String storeName;
		private final File backupFile;

		protected RStoreCommand(Map<String, Switch> switches, Queue<Parameter> parameters) throws ArgumentsException
		{
			super();
			if (!switches.isEmpty())
				throw new ArgumentsException("Unrecognized switches/options: " + switches.keySet());
			{
				if (parameters.isEmpty())
					throw new ArgumentsException("Missing parameter");
				Parameter parameter = parameters.poll();
				if (!parameter.getSwitches().isEmpty())
					throw new ArgumentsException("Unrecognized switches/options: " + switches.keySet());
				this.storeName = parameter.getValue();
			}
			{
				if (parameters.isEmpty())
					throw new ArgumentsException("Missing parameter");
				Parameter parameter = parameters.poll();
				if (!parameter.getSwitches().isEmpty())
					throw new ArgumentsException("Unrecognized switches/options: " + switches.keySet());
				this.backupFile = new File(parameter.getValue());
			}
		}

		@Override
		protected void execute() throws ExecuteException
		{
			BerkeleyDBPersistenceManager.Configuration configuration = new BerkeleyDBPersistenceManager.Configuration();
			configuration.setDbFile(dbFile);
			configuration.setAllowCreate(true);
			configuration.setReadOnly(false);
			configuration.setAllowUpgrade(false);
			BerkeleyDBAletheiaEnvironment environment = new BerkeleyDBAletheiaEnvironment(configuration);
			try
			{
				if (!environment.storeDatabaseNames(storeName).isEmpty())
					throw new ExecuteException("There exist databases that match the destination store name");
				DataInputStream in = new DataInputStream(new GZIPInputStream(new FileInputStream(backupFile)));
				try
				{
					new LowLevelBackupRestoreEntityStore(environment).restore(in, storeName);
				}
				finally
				{
					in.close();
				}
			}
			catch (IOException | ProtocolException e)
			{
				throw new ExecuteException(e);
			}
			finally
			{
				environment.close();
			}
		}

	}

	private Command createCommand(Queue<Parameter> parameterQueue) throws ArgumentsException
	{
		Parameter parameter = parameterQueue.poll();
		switch (parameter.getValue())
		{
		case "lStores":
			return new LStoresCommand(parameter.getSwitches(), parameterQueue);
		case "dStore":
			return new DStoreCommand(parameter.getSwitches(), parameterQueue);
		case "mStore":
			return new MStoreCommand(parameter.getSwitches(), parameterQueue);
		case "bStore":
			return new BStoreCommand(parameter.getSwitches(), parameterQueue);
		case "rStore":
			return new RStoreCommand(parameter.getSwitches(), parameterQueue);
		default:
			throw new ArgumentsException("Unrecognized command: " + parameter.getValue());
		}
	}

	private class CommandHelp extends Command
	{
		@Override
		protected void execute() throws ExecuteException
		{
			System.out.println(BerkeleyDBAletheiaTool.class.getName() + " [-v] --dbFile=<environment path> <command>...");
			System.out.println();
			System.out.println("Switches:");
			System.out.println("\t-version: Show version number");
			System.out.println("\t--dbFile: Berkeley DB path location");
			System.out.println("");
			System.out.println("Command list:");
			System.out.println("\tlStores: list of entity stores in the environment");
			System.out.println("\tdStore <store name>: deleting an entity store from the environment");
			System.out.println("\tmStore <old store name> <new store name>: rename an entity store from the environment");
			System.out.println("\tbStore <store name> <filename>: backup an entity store from the environment");
			System.out.println("\trStore <store name> <filename>: restore an entity store into the environment");
		}
	}

	private final List<Command> commandList;

	public BerkeleyDBAletheiaTool(CommandLineArguments commandLineArguments) throws ArgumentsException
	{
		this.commandList = new ArrayList<>();
		Map<String, Switch> globalSwitches = new HashMap<>(commandLineArguments.getGlobalSwitches());
		if (globalSwitches.remove("version") != null)
		{
			dbFile = null;
			commandList.add(new VersionCommand());
		}
		else
		{
			if (commandLineArguments.getParameters().isEmpty())
			{
				dbFile = null;
				commandList.add(new CommandHelp());
			}
			else
			{
				Switch swDbFile = globalSwitches.remove("dbFile");
				if (swDbFile == null || !(swDbFile instanceof Option))
					throw new ArgumentsException("Missing option dbFile");
				String sDbFile = ((Option) swDbFile).getValue();
				if (sDbFile == null)
					throw new ArgumentsException("Missing option value dbFile");
				this.dbFile = new File(sDbFile);
				if (!globalSwitches.isEmpty())
					throw new ArgumentsException("Unrecognized switches/options: " + globalSwitches.keySet());
				Queue<Parameter> parameterQueue = new ArrayDeque<>(commandLineArguments.getParameters());
				while (!parameterQueue.isEmpty())
					commandList.add(createCommand(parameterQueue));
			}
		}
	}

	public void execute() throws ExecuteException
	{
		for (Command command : commandList)
			command.execute();
	}

	public static void main(String[] args)
	{
		try
		{
			BerkeleyDBAletheiaTool tool = new BerkeleyDBAletheiaTool(new CommandLineArguments(args));
			tool.execute();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

}
