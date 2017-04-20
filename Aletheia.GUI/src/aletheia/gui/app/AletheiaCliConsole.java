/*******************************************************************************
 * Copyright (c) 2016 Quim Testar.
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
package aletheia.gui.app;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.status.StatusLogger;

import aletheia.gui.cli.command.Command;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.gui.cli.command.Command.CommandParseException;
import aletheia.gui.cli.command.gui.Exit;
import aletheia.gui.cli.command.gui.TraceException;
import aletheia.model.authority.UnpackedSignatureRequest;
import aletheia.model.identifier.Namespace;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.peertopeer.PeerToPeerNode;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.prooffinder.ProofFinder;
import aletheia.utilities.CommandLineArguments;
import aletheia.utilities.CommandLineArguments.CommandLineArgumentsException;
import aletheia.utilities.CommandLineArguments.Option;
import aletheia.utilities.CommandLineArguments.Switch;
import aletheia.utilities.io.TeePrintStream;
import aletheia.utilities.io.WriterOutputStream;
import aletheia.version.VersionManager;

public abstract class AletheiaCliConsole implements CommandSource
{
	private final PersistenceManager persistenceManager;

	private Context activeContext;
	private PrintWriter consolePrintWriter;

	private AletheiaCliConsole(PersistenceManager persistenceManager)
	{
		this.persistenceManager = persistenceManager;
		this.activeContext = null;
		this.consolePrintWriter = null;
	}

	protected PrintWriter getConsolePrintWriter()
	{
		return consolePrintWriter;
	}

	public class UnsupportedOperationException extends java.lang.UnsupportedOperationException
	{
		private static final long serialVersionUID = -3793027160064762455L;

		private UnsupportedOperationException()
		{
			super("Operation not suported in the console CLI");
		}
	}

	@Override
	public abstract PrintStream getOut();

	public abstract String readLine();

	@Override
	public PrintStream getOutB()
	{
		return getOut();
	}

	@Override
	public PrintStream getOutP()
	{
		return getOut();
	}

	@Override
	public abstract PrintStream getErr();

	@Override
	public PrintStream getErrB()
	{
		return getErrB();
	}

	@Override
	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	@Override
	public void lock(Collection<? extends Transaction> owners)
	{
	}

	@Override
	public void setActiveContext(Context activeContext)
	{
		this.activeContext = activeContext;
	}

	@Override
	public Context getActiveContext()
	{
		return activeContext;
	}

	@Override
	public void signatureRequestJTreeSelectStatement(UnpackedSignatureRequest unpackedSignatureRequest, Statement statement)
	{
	}

	@Override
	public void signatureRequestJTreeSelectUnpackedSignatureRequest(UnpackedSignatureRequest unpackedSignatureRequest)
	{
	}

	@Override
	public PeerToPeerNode getPeerToPeerNode()
	{
		return null;
	}

	@Override
	public void pushSelectStatement(Statement statement)
	{
	}

	@Override
	public void pushSelectStatement(Transaction transaction, Statement statement)
	{
	}

	@Override
	public void pushSelectContextConsequent(Context context)
	{
	}

	@Override
	public void pushSelectContextConsequent(Transaction transaction, Context context)
	{
	}

	@Override
	public void expandAllContexts(Context context)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void nodeStructureReset(Context context)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void resetGui()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void collapseAll(Context context)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void expandGroup(Context context, Namespace prefix)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void expandSubscribedContexts(Context context)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void expandUnprovedContexts(Context context)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public AletheiaJFrame openExtraFrame(String extraTitle)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setExtraTitle(String extraTitle)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public ProofFinder getProofFinder()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void consoleFile(File file) throws FileNotFoundException
	{
		if (consolePrintWriter != null)
			consolePrintWriter.close();
		if (file == null)
			consolePrintWriter = null;
		else
			consolePrintWriter = new PrintWriter(new FileOutputStream(file, true), true);
	}

	@Override
	public void command(Command command)
	{
		try
		{
			command.run();
		}
		catch (Exception e)
		{
			command(new TraceException(this, e));
		}
	}

	@Override
	public void exit()
	{
	}

	@Override
	public void restart()
	{
		throw new UnsupportedOperationException();
	}

	protected Command command(String s)
	{
		if (consolePrintWriter != null)
			consolePrintWriter.println(s);
		Transaction transaction = getPersistenceManager().beginTransaction();
		try
		{
			Command cmd = Command.parse(this, transaction, s);
			command(cmd);
			if (!(cmd instanceof TransactionalCommand))
				transaction.abort();
			return cmd;
		}
		catch (CommandParseException e)
		{
			transaction.abort();
			Command cmd = new TraceException(this, e);
			command(cmd);
			return cmd;
		}
	}

	public void run()
	{
		while (true)
		{
			getOut().print("% ");
			String s = readLine();
			if (s == null)
				break;
			Command cmd = command(s);
			if (cmd instanceof Exit)
				break;
		}
	}

	private static class StdIO extends AletheiaCliConsole
	{

		private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		private StdIO(PersistenceManager persistenceManager)
		{
			super(persistenceManager);
		}

		@Override
		public PrintStream getOut()
		{
			return System.out;
		}

		@Override
		public PrintStream getErr()
		{
			return System.err;
		}

		@Override
		public String readLine()
		{
			try
			{
				return reader.readLine();
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		public char[] passphrase(boolean confirm)
		{
			getErr().println("Warning: echo not disabled!");
			getOut().print("Enter passphrase: ");
			return readLine().toCharArray();
		}

		@Override
		public void consoleFile(File file) throws FileNotFoundException
		{
			super.consoleFile(file);
			if (file == null)
			{
				System.setOut(System.out);
				System.setErr(System.err);
			}
			else
			{
				System.setOut(new TeePrintStream(System.out, new PrintStream(new WriterOutputStream(getConsolePrintWriter()), true)));
				System.setErr(new TeePrintStream(System.err, new PrintStream(new WriterOutputStream(getConsolePrintWriter()), true)));
			}
		}

	}

	private static class SystemConsole extends AletheiaCliConsole
	{
		private final Console console;
		private final PrintStream out;

		private PrintStream out_;

		private SystemConsole(PersistenceManager persistenceManager, Console console)
		{
			super(persistenceManager);
			this.console = console;
			this.out = new PrintStream(new WriterOutputStream(console.writer()), true);

			this.out_ = out;
		}

		@Override
		public PrintStream getOut()
		{
			return out_;
		}

		@Override
		public PrintStream getErr()
		{
			return out_;
		}

		@Override
		public String readLine()
		{
			return console.readLine();
		}

		@Override
		public char[] passphrase(boolean confirm)
		{
			char[] passwd = console.readPassword("Enter passphrase: ");
			if (passwd != null && confirm)
			{
				char[] passwdc = console.readPassword("Confirm passphrase: ");
				if (!Arrays.equals(passwd, passwdc))
				{
					getErr().println("Not matched.");
					return null;
				}
			}

			return passwd;
		}

		@Override
		public void consoleFile(File file) throws FileNotFoundException
		{
			super.consoleFile(file);
			if (file == null)
				this.out_ = out;
			else
				this.out_ = new TeePrintStream(out, new PrintStream(new WriterOutputStream(getConsolePrintWriter()), true));
		}

	}

	public static AletheiaCliConsole cliConsole(PersistenceManager persistenceManager)
	{
		Console console = System.console();
		if (console != null)
			return new SystemConsole(persistenceManager, console);
		else
			return new StdIO(persistenceManager);
	}

	private static class ArgumentsException extends Exception
	{
		private static final long serialVersionUID = -5342220710448665920L;

		private ArgumentsException(String message)
		{
			super(message);
		}

	}

	public static void main(String[] args) throws CommandLineArgumentsException, ArgumentsException
	{
		StatusLogger.getLogger().setLevel(Level.OFF);
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
		{
			@Override
			public void uncaughtException(Thread t, Throwable e)
			{
				if (e.getMessage() != null)
					System.err.println(e.getMessage());
				else
					e.printStackTrace(System.err);

			}
		});
		CommandLineArguments commandLineArguments = new CommandLineArguments(args);
		Map<String, Switch> globalSwitches = new HashMap<>(commandLineArguments.getGlobalSwitches());
		if (globalSwitches.remove("version") != null)
			System.out.println(VersionManager.getVersion());
		else
		{
			Switch swDbFile = globalSwitches.remove("dbFile");
			if (swDbFile == null || !(swDbFile instanceof Option))
				throw new ArgumentsException("Missing option dbFile");
			String sDbFile = ((Option) swDbFile).getValue();
			if (sDbFile == null)
				throw new ArgumentsException("Missing option value dbFile");
			File dbFile = new File(sDbFile);
			boolean readOnly = true;
			Switch swReadWrite = globalSwitches.remove("rw");
			if (swReadWrite != null)
				readOnly = false;
			if (!globalSwitches.isEmpty())
				throw new ArgumentsException("Unrecognized switches/options: " + globalSwitches.keySet());
			BerkeleyDBPersistenceManager.Configuration configuration = new BerkeleyDBPersistenceManager.Configuration();
			configuration.setDbFile(dbFile);
			configuration.setReadOnly(readOnly);

			final BerkeleyDBPersistenceManager persistenceManager = new BerkeleyDBPersistenceManager(configuration);
			try
			{
				Runtime.getRuntime().addShutdownHook(new Thread()
				{
					@Override
					public void run()
					{
						if (persistenceManager.isOpen())
							persistenceManager.close();
					}
				});

				AletheiaCliConsole cliConsole = cliConsole(persistenceManager);
				cliConsole.run();
			}
			finally
			{
				persistenceManager.close();
			}
		}
	}

	@Override
	public boolean confirmDialog(String text)
	{
		if (text != null)
			getOut().println(text);
		getOut().println("Are you sure you want to continue? (yes/no)");
		String s = readLine();
		return s != null && s.trim().toLowerCase().equals("yes");
	}

}
