/*******************************************************************************
 * Copyright (c) 2015 Quim Testar.
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
	private static final int exportVersion = 2;
	private static final long transactionTimeout = 10000;

	private final PersistenceManager persistenceManager;

	private final VersionProtocol versionProtocol = new VersionProtocol();
	private final ByteExportableEnumProtocol<RegisterType> registerTypeProtocol = new ByteExportableEnumProtocol<>(0, RegisterType.class, 0);

	public ExportImport(PersistenceManager persistenceManager)
	{
		this.persistenceManager = persistenceManager;
	}

	private Transaction beginTransaction()
	{
		return persistenceManager.beginTransaction(transactionTimeout);
	}

	public void export(File file, Transaction transaction, Collection<Statement> statements, boolean signed, boolean skipSignedProof) throws IOException
	{
		DataOutputStream out = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(file)));
		try
		{
			export(out, transaction, statements, signed, skipSignedProof);
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
			NullableProtocol<DelegateTreeRootNode> ndtrnProtocol = new NullableProtocol<>(0, dtrnProtocol);
			ndtrnProtocol.send(out, statementAuthority.getDelegateTreeRootNode(getTransaction()));
		}

		@Override
		public StatementAuthority recv(DataInput in) throws IOException, ProtocolException
		{
			StatementAuthority statementAuthority = super.recv(in);
			DelegateTreeRootNodeWithAuthorizersProtocol dtrnProtocol = new DelegateTreeRootNodeWithAuthorizersProtocol(0, getPersistenceManager(),
					getTransaction(), statementAuthority);
			NullableProtocol<DelegateTreeRootNode> ndtrnProtocol = new NullableProtocol<>(0, dtrnProtocol);
			ndtrnProtocol.recv(in);
			return statementAuthority;
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			super.skip(in);
			DelegateTreeRootNodeWithAuthorizersProtocol dtrnProtocol = new DelegateTreeRootNodeWithAuthorizersProtocol(0, getPersistenceManager(),
					getTransaction(), null);
			NullableProtocol<DelegateTreeRootNode> ndtrnProtocol = new NullableProtocol<>(0, dtrnProtocol);
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

	public void export(DataOutput out, Transaction transaction, Collection<Statement> statements, boolean signed, boolean skipSignedProof) throws IOException
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

		Deque<DequeEntry> deque = new LinkedList<>();
		{
			Set<Statement> queued = new HashSet<>();
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

		StatementProtocol statementProtocol = new StatementProtocol(2, persistenceManager, transaction);
		StatementAuthorityDelegateTreeProtocol statementAuthorityProtocol = new StatementAuthorityDelegateTreeProtocol(0, persistenceManager, transaction);
		PersonProtocol personProtocol = new PersonProtocol(0, persistenceManager, transaction);

		Set<Statement> pushedDependencies = new HashSet<>();
		Set<Person> exportedPersons = new HashSet<>();
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
			if (e.descendents && e.statement instanceof Context && (!skipSignedProof || !e.statement.isSignedProof(transaction)))
			{
				Context ctx = (Context) e.statement;
				for (Statement d : ctx.localDependencySortedStatements(transaction))
					deque.offer(new DequeEntry(d, true, false));
			}
		}
		registerTypeProtocol.send(out, RegisterType.End);
	}

	private void import_(DataInput in, ListenableAborter aborter, int statementProtocolVersion, int statementAuthorityDelegateTreeProtocolVersion,
			int personProtocolVersion) throws IOException, ProtocolException, AbortException
	{
		Transaction transaction = beginTransaction();
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
		StatementProtocol statementProtocol = new StatementProtocol(statementProtocolVersion, persistenceManager, transaction);
		StatementAuthorityDelegateTreeProtocol statementAuthorityProtocol = new StatementAuthorityDelegateTreeProtocol(
				statementAuthorityDelegateTreeProtocolVersion, persistenceManager, transaction);
		PersonProtocol personProtocol = new PersonProtocol(personProtocolVersion, persistenceManager, transaction);

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
					transaction.commit();
					break loop;
				}

				recvd++;
				if (recvd % 1000 == 0)
				{
					logger.info("--> restore:" + recvd);
					transaction.commit();
					transaction = beginTransaction();
					aborterListener.setTransaction(transaction);
					statementProtocol = new StatementProtocol(statementProtocolVersion, persistenceManager, transaction);
					statementAuthorityProtocol = new StatementAuthorityDelegateTreeProtocol(statementAuthorityDelegateTreeProtocolVersion, persistenceManager,
							transaction);
					personProtocol = new PersonProtocol(personProtocolVersion, persistenceManager, transaction);
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

	public void import_(DataInput in, ListenableAborter aborter) throws IOException, ProtocolException, AbortException
	{
		int version = versionProtocol.recv(in);
		if (version != exportVersion)
			throw new VersionException(version);
		import_(in, aborter, 2, 0, 0);
	}

}
