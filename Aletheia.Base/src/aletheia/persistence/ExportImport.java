package aletheia.persistence;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.model.authority.DelegateTreeRootNode;
import aletheia.model.authority.Person;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.VersionProtocol;
import aletheia.protocol.authority.DelegateTreeRootNodeWithAuthorizersProtocol;
import aletheia.protocol.authority.PersonProtocol;
import aletheia.protocol.authority.StatementAuthorityProtocol;
import aletheia.protocol.enumerate.ByteExportableEnum;
import aletheia.protocol.enumerate.ByteExportableEnumProtocol;
import aletheia.protocol.enumerate.ExportableEnumInfo;
import aletheia.protocol.primitive.NullableProtocol;
import aletheia.protocol.statement.StatementProtocol;
import aletheia.utilities.aborter.Aborter.AbortException;
import aletheia.utilities.aborter.ListenableAborter;

class ExportImport
{
	private static final Logger logger = LoggerManager.instance.logger();
	private static final int exportVersion = 0;

	private final PersistenceManager persistenceManager;

	private final VersionProtocol versionProtocol = new VersionProtocol();
	private final ByteExportableEnumProtocol<RegisterType> registerTypeProtocol = new ByteExportableEnumProtocol<>(0, RegisterType.class, 0);

	public ExportImport(PersistenceManager persistenceManager)
	{
		this.persistenceManager = persistenceManager;
	}

	public void export(File file, Transaction transaction, Collection<Statement> statements, boolean signed) throws IOException
	{
		DataOutputStream out = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(file)));
		try
		{
			export(out, transaction, statements, signed);
		}
		finally
		{
			out.close();
		}
	}

	public void import_(DataInput in) throws IOException, ProtocolException
	{
		try
		{
			import_(in, ListenableAborter.nullListenableAborter);
		}
		catch (AbortException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void import_(File file, ListenableAborter aborter) throws IOException, ProtocolException, AbortException
	{
		DataInputStream in = new DataInputStream(new GZIPInputStream(new FileInputStream(file)));
		try
		{
			import_(in, aborter);
		}
		finally
		{
			in.close();
		}
	}

	public void import_(File file) throws IOException, ProtocolException
	{
		try
		{
			import_(file, ListenableAborter.nullListenableAborter);
		}
		catch (AbortException e)
		{
			throw new RuntimeException(e);
		}
	}

	public class VersionException extends ProtocolException
	{
		private static final long serialVersionUID = -4815096882622954150L;

		protected VersionException(int version)
		{
			super("Export version " + version + " not supported :(");
		}

	}

	@ProtocolInfo(availableVersions = 0)
	private class StatementAuthorityDelegateTreeProtocol extends StatementAuthorityProtocol
	{

		public StatementAuthorityDelegateTreeProtocol(int requiredVersion, PersistenceManager persistenceManager, Transaction transaction)
		{
			super(0, persistenceManager, transaction);
			checkVersionAvailability(StatementAuthorityDelegateTreeProtocol.class, requiredVersion);
		}

		@Override
		public void send(DataOutput out, StatementAuthority statementAuthority) throws IOException
		{
			super.send(out, statementAuthority);
			DelegateTreeRootNodeWithAuthorizersProtocol dtrnProtocol = new DelegateTreeRootNodeWithAuthorizersProtocol(0, getPersistenceManager(),
					getTransaction(), statementAuthority);
			NullableProtocol<DelegateTreeRootNode> ndtrnProtocol = new NullableProtocol<DelegateTreeRootNode>(0, dtrnProtocol);
			ndtrnProtocol.send(out, statementAuthority.getDelegateTreeRootNode(getTransaction()));
		}

		@Override
		public StatementAuthority recv(DataInput in) throws IOException, ProtocolException
		{
			StatementAuthority statementAuthority = super.recv(in);
			DelegateTreeRootNodeWithAuthorizersProtocol dtrnProtocol = new DelegateTreeRootNodeWithAuthorizersProtocol(0, getPersistenceManager(),
					getTransaction(), statementAuthority);
			NullableProtocol<DelegateTreeRootNode> ndtrnProtocol = new NullableProtocol<DelegateTreeRootNode>(0, dtrnProtocol);
			ndtrnProtocol.recv(in);
			return statementAuthority;
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			super.skip(in);
			DelegateTreeRootNodeWithAuthorizersProtocol dtrnProtocol = new DelegateTreeRootNodeWithAuthorizersProtocol(0, getPersistenceManager(),
					getTransaction(), null);
			NullableProtocol<DelegateTreeRootNode> ndtrnProtocol = new NullableProtocol<DelegateTreeRootNode>(0, dtrnProtocol);
			ndtrnProtocol.skip(in);
		}
	}

	@ExportableEnumInfo(availableVersions = 0)
	private enum RegisterType implements ByteExportableEnum<RegisterType>
	{
		Statement((byte) 0), Authority((byte) 1), Person((byte) 2), End((byte) 0xff);

		private final byte code;

		private RegisterType(byte code)
		{
			this.code = code;
		}

		@Override
		public Byte getCode(int version)
		{
			return code;
		}
	};

	private boolean isDescendent(Transaction transaction, Collection<Statement> statements, Statement d)
	{
		for (Statement s : statements)
			if (s instanceof Context)
				if (((Context) s).isDescendent(transaction, d))
					return true;
		return false;
	}

	public void export(DataOutput out, Transaction transaction, Collection<Statement> statements, boolean signed) throws IOException
	{
		versionProtocol.send(out, exportVersion);
		class DequeEntry
		{
			final Statement statement;
			final boolean descendents;
			@SuppressWarnings("unused")
			final boolean proof;

			private DequeEntry(Statement statement, boolean descendents, boolean proof)
			{
				super();
				this.statement = statement;
				this.descendents = descendents;
				this.proof = proof;
			}

		}

		Deque<DequeEntry> deque = new LinkedList<DequeEntry>();
		{
			Set<Statement> queued = new HashSet<Statement>();
			for (Statement statement : statements)
			{
				boolean push = false;
				for (Statement asc : statement.statementPath(transaction))
				{
					if (asc.equals(statement) || (!asc.isSignedDependencies(transaction) && !queued.contains(asc)))
						push = true;
					if (push)
					{
						deque.offer(new DequeEntry(asc, asc.equals(statement), false));
						queued.add(asc);
					}
				}
			}
		}

		StatementProtocol statementProtocol = new StatementProtocol(0, persistenceManager, transaction);
		StatementAuthorityDelegateTreeProtocol statementAuthorityProtocol = new StatementAuthorityDelegateTreeProtocol(0, persistenceManager, transaction);
		PersonProtocol personProtocol = new PersonProtocol(0, persistenceManager, transaction);

		Set<Statement> pushedDependencies = new HashSet<Statement>();
		Set<Person> exportedPersons = new HashSet<Person>();
		while (!deque.isEmpty())
		{
			DequeEntry e = deque.element();
			{
				if (signed && !e.statement.isSignedDependencies(transaction))
				{
					deque.remove();
					continue;
				}
			}
			{
				boolean pushed = false;
				for (Statement dep : e.statement.dependencies(transaction))
				{
					if (!pushedDependencies.contains(dep) && !isDescendent(transaction, statements, dep) && !dep.isSignedDependencies(transaction))
					{
						deque.push(new DequeEntry(dep, false, false));
						pushedDependencies.add(dep);
						pushed = true;
					}
				}
				if (pushed)
					continue;
				deque.remove();
			}
			registerTypeProtocol.send(out, RegisterType.Statement);
			statementProtocol.send(out, e.statement);
			StatementAuthority stAuth = e.statement.getAuthority(transaction);
			if (stAuth != null)
			{
				for (Person person : stAuth.personDependencies(transaction))
					if (!exportedPersons.contains(person))
					{
						registerTypeProtocol.send(out, RegisterType.Person);
						personProtocol.send(out, person);
						exportedPersons.add(person);
					}
				registerTypeProtocol.send(out, RegisterType.Authority);
				statementAuthorityProtocol.send(out, stAuth);
			}
			if (e.descendents && e.statement instanceof Context)
			{
				Context ctx = (Context) e.statement;
				for (Statement d : ctx.localDependencySortedStatements(transaction))
					deque.offer(new DequeEntry(d, true, false));
			}
		}
		registerTypeProtocol.send(out, RegisterType.End);
	}

	public void import_(DataInput in, ListenableAborter aborter) throws IOException, ProtocolException, AbortException
	{
		int version = versionProtocol.recv(in);
		if (version != 0)
			throw new VersionException(version);
		Transaction transaction = persistenceManager.beginTransaction();
		class AborterListener implements ListenableAborter.Listener
		{
			private Transaction transaction;

			public void setTransaction(Transaction transaction)
			{
				this.transaction = transaction;
			}

			@Override
			public void abort()
			{
				transaction.abort();
			}
		}
		AborterListener aborterListener = new AborterListener();
		aborter.addListener(aborterListener);
		aborterListener.setTransaction(transaction);
		StatementProtocol statementProtocol = new StatementProtocol(0, persistenceManager, transaction);
		StatementAuthorityDelegateTreeProtocol statementAuthorityProtocol = new StatementAuthorityDelegateTreeProtocol(0, persistenceManager, transaction);
		PersonProtocol personProtocol = new PersonProtocol(0, persistenceManager, transaction);

		int recvd = 0;
		try
		{
			loop: while (true)
			{
				RegisterType type = registerTypeProtocol.recv(in);
				switch (type)
				{
				case Statement:
					statementProtocol.recv(in);
					break;
				case Authority:
					statementAuthorityProtocol.recv(in);
					break;
				case Person:
					personProtocol.recv(in);
					break;
				case End:
					break loop;
				}

				recvd++;
				if (recvd % 1000 == 0)
				{
					logger.info("--> restore:" + recvd);
					transaction.commit();
					transaction = persistenceManager.beginTransaction();
					aborterListener.setTransaction(transaction);
					statementProtocol = new StatementProtocol(0, persistenceManager, transaction);
					statementAuthorityProtocol = new StatementAuthorityDelegateTreeProtocol(0, persistenceManager, transaction);
					personProtocol = new PersonProtocol(0, persistenceManager, transaction);
				}

			}
		}
		catch (Exception e)
		{
			aborter.checkAbort();
			throw e;
		}
		finally
		{
			aborter.removeListener(aborterListener);
			transaction.abort();
		}
		logger.info("--> restore:" + recvd);
	}

}
