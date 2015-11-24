package aletheia.gui.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import aletheia.gui.cli.command.Command;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.gui.cli.command.Command.CommandParseException;
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
import aletheia.version.VersionManager;

public class CliConsole implements CommandSource
{
	private final PersistenceManager persistenceManager;

	private Context activeContext;

	public CliConsole(PersistenceManager persistenceManager)
	{
		this.persistenceManager = persistenceManager;
		this.activeContext = null;
	}

	public class UnsupportedOperation extends RuntimeException
	{
		private static final long serialVersionUID = -3793027160064762455L;

		private UnsupportedOperation()
		{
			super("Operation not suported in the console CLI");
		}
	}

	@Override
	public PrintStream getOut()
	{
		return System.out;
	}

	@Override
	public PrintStream getOutB()
	{
		return System.out;
	}

	@Override
	public PrintStream getErr()
	{
		return System.err;
	}

	@Override
	public PrintStream getErrB()
	{
		return System.err;
	}

	@Override
	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	@Override
	public void waitCursor(boolean wait)
	{
	}

	@Override
	public void lock(Collection<Transaction> singleton)
	{
	}

	@Override
	public void commandDone(Command command) throws InterruptedException
	{
		System.out.print("% ");
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
	public char[] passPhrase()
	{
		throw new UnsupportedOperationException();
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
	public void openExtraFrame(String extraTitle)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public ProofFinder getProofFinder()
	{
		throw new UnsupportedOperationException();
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

	protected void command(String s)
	{
		Transaction transaction = getPersistenceManager().beginTransaction();
		try
		{
			Command cmd = Command.parse(this, transaction, s);
			command(cmd);
			if (!(cmd instanceof TransactionalCommand))
				transaction.abort();
		}
		catch (CommandParseException e)
		{
			transaction.abort();
			command(new TraceException(this, e));
		}
	}

	public void run()
	{
		try
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			while (true)
			{
				System.out.print("% ");
				String s = in.readLine();
				if (s == null)
					break;
				command(s);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
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
		CommandLineArguments commandLineArguments = new CommandLineArguments(args);
		Map<String, Switch> globalSwitches = new HashMap<String, Switch>(commandLineArguments.getGlobalSwitches());
		if (globalSwitches.remove("v") != null)
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
			if (!globalSwitches.isEmpty())
				throw new ArgumentsException("Unrecognized switches/options: " + globalSwitches.keySet());
			BerkeleyDBPersistenceManager.Configuration configuration = new BerkeleyDBPersistenceManager.Configuration();
			configuration.setDbFile(dbFile);

			BerkeleyDBPersistenceManager persistenceManager = new BerkeleyDBPersistenceManager(configuration);
			try
			{
				CliConsole cliConsole = new CliConsole(persistenceManager);
				cliConsole.run();
			}
			finally
			{
				persistenceManager.close();
			}
		}
	}

}
