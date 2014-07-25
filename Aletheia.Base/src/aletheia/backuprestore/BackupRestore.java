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
package aletheia.backuprestore;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.model.authority.DelegateTreeRootNode;
import aletheia.model.authority.Person;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.local.StatementLocal;
import aletheia.model.statement.Context;
import aletheia.model.statement.Context.StatementNotInContextException;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.VersionProtocol;
import aletheia.protocol.authority.DelegateTreeRootNodeWithAuthorizersProtocol;
import aletheia.protocol.authority.PersonProtocol;
import aletheia.protocol.authority.StatementAuthorityProtocol;
import aletheia.protocol.collection.NonReturningCollectionProtocol;
import aletheia.protocol.local.StatementLocalProtocol;
import aletheia.protocol.primitive.IntegerProtocol;
import aletheia.protocol.primitive.NullableProtocol;
import aletheia.protocol.primitive.UUIDProtocol;
import aletheia.protocol.statement.StatementProtocol;
import aletheia.utilities.aborter.ListenableAborter;
import aletheia.utilities.collections.BufferedList;

/**
 * A class for backing up and restoring the full persistence environment using
 * the protocol tools.
 *
 * The data is arranged in the following way, starting from the root context:
 * <ul>
 * <li>The statement is placed via the {@link StatementProtocol}.</li>
 * <li>If the statement is a {@link Context}:</li>
 * <blockquote>
 * <li>The number of statements of the context is placed via the
 * {@link IntegerProtocol}</li>
 * <li>The statements in the context are placed (in the same way described here,
 * recursively) as returned by the method
 * {@link Context#localDependencySortedStatements(Transaction)}. </blockquote>
 * </ul>
 *
 * @see IntegerProtocol
 * @see UUIDProtocol
 * @see StatementProtocol
 */
public class BackupRestore
{
	private static final Logger logger = LoggerManager.logger();
	private static final int backupVersion = 0;

	private final PersistenceManager persistenceManager;

	public BackupRestore(PersistenceManager persistenceManager)
	{
		this.persistenceManager = persistenceManager;
	}

	private void backupVersion(DataOutput out, int version) throws IOException
	{
		VersionProtocol versionProtocol = new VersionProtocol();
		versionProtocol.send(out, version);
	}

	private int restoreVersion(DataInput in) throws IOException, ProtocolException
	{
		VersionProtocol versionProtocol = new VersionProtocol();
		int version = versionProtocol.recv(in);
		return version;
	}

	public void backup(DataOutput out, Transaction transaction) throws IOException
	{
		backupVersion(out, backupVersion);
		backupPersons(out, transaction, backupVersion);
		backupStatements(out, transaction, backupVersion);
	}

	public void backup(File file, Transaction transaction) throws IOException
	{
		DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
		try
		{
			backup(out, transaction);
		}
		finally
		{
			out.close();
		}
	}

	public void restore(DataInput in, ListenableAborter aborter) throws IOException, ProtocolException
	{
		int version = restoreVersion(in);
		final Transaction transaction = persistenceManager.beginTransaction();
		ListenableAborter.Listener aborterListener = new ListenableAborter.Listener()
		{
			@Override
			public void abort()
			{
				transaction.abort();
			}
		};
		aborter.addListener(aborterListener);
		try
		{
			restorePersons(in, transaction, version);
			transaction.commit();
		}
		finally
		{
			aborter.removeListener(aborterListener);
			transaction.abort();
		}
		restoreStatements(in, version, aborter);
	}

	public void restore(DataInput in) throws IOException, ProtocolException
	{
		restore(in, ListenableAborter.nullListenableAborter);
	}

	public void restore(File file, ListenableAborter aborter) throws IOException, ProtocolException
	{
		DataInputStream in = new DataInputStream(new FileInputStream(file));
		try
		{
			restore(in, aborter);
		}
		finally
		{
			in.close();
		}
	}

	public void restore(File file) throws IOException, ProtocolException
	{
		restore(file, ListenableAborter.nullListenableAborter);
	}

	public void restoreClean(DataInput in, Transaction transaction) throws IOException, ProtocolException
	{
		int version = restoreVersion(in);
		restorePersons(in, transaction, version);
		restoreStatementsClean(in, transaction, version);
	}

	public void restoreClean(File file, Transaction transaction) throws IOException, ProtocolException
	{
		DataInputStream in = new DataInputStream(new FileInputStream(file));
		try
		{
			restoreClean(in, transaction);
		}
		finally
		{
			in.close();
		}
	}

	public void backupPersons(DataOutput out, Transaction transaction, int version) throws IOException
	{
		PersonProtocol personProtocol = new PersonProtocol(0, persistenceManager, transaction);
		NonReturningCollectionProtocol<Person> personCollectionProtocol = new NonReturningCollectionProtocol<>(0, personProtocol);
		personCollectionProtocol.send(out, persistenceManager.persons(transaction).values());
	}

	public class VersionException extends ProtocolException
	{
		private static final long serialVersionUID = -4815096882622954150L;

		protected VersionException(int version)
		{
			super("Backup version " + version + " not supported :(");
		}

	}

	public void restorePersons(DataInput in, Transaction transaction, int version) throws IOException, ProtocolException
	{
		if (version != 0)
			throw new VersionException(version);
		PersonProtocol personProtocol = new PersonProtocol(0, persistenceManager, transaction);
		NonReturningCollectionProtocol<Person> personCollectionProtocol = new NonReturningCollectionProtocol<>(0, personProtocol);
		personCollectionProtocol.recv(in);
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

	/**
	 * Sends the full data set of a persistence environment through a
	 * {@link DataOutput}. Consistent with
	 * {@link #restoreStatements(DataInput, PersistenceManager, Transaction)}
	 * and
	 * {@link #restoreStatementsClean(DataInput, PersistenceManager, Transaction)}
	 * .
	 *
	 * @param out
	 *            The data output to send the data to.
	 * @param persistenceManager
	 *            The persistence manager.
	 * @param transaction
	 *            The transaction.
	 * @throws IOException
	 */
	public void backupStatements(DataOutput out, Transaction transaction, int version) throws IOException
	{
		IntegerProtocol integerProtocol = new IntegerProtocol(0);
		UUIDProtocol uuidProtocol = new UUIDProtocol(0);
		StatementProtocol statementProtocol = new StatementProtocol(0, persistenceManager, transaction);
		StatementAuthorityDelegateTreeProtocol statementAuthorityProtocol = new StatementAuthorityDelegateTreeProtocol(0, persistenceManager, transaction);
		NullableProtocol<StatementAuthority> nullableStatementAuthorityProtocol = new NullableProtocol<>(0, statementAuthorityProtocol);
		StatementLocalProtocol statementLocalProtocol = new StatementLocalProtocol(0, persistenceManager, transaction);
		NullableProtocol<StatementLocal> nullableStatementLocalProtocol = new NullableProtocol<>(0, statementLocalProtocol);
		int sendd = 0;
		Collection<RootContext> rootContexts = new BufferedList<>(persistenceManager.rootContexts(transaction).values());
		integerProtocol.send(out, rootContexts.size());
		Queue<Context> queue = new ArrayDeque<Context>();
		for (RootContext rootContext : rootContexts)
		{
			queue.add(rootContext);
			statementProtocol.send(out, rootContext);
			nullableStatementAuthorityProtocol.send(out, rootContext.getAuthority(transaction));
			nullableStatementLocalProtocol.send(out, rootContext.getLocal(transaction));
			sendd++;
			if (sendd % 1000 == 0)
				logger.info("--> backup:" + sendd);
		}
		while (!queue.isEmpty())
		{
			Context context = queue.poll();
			uuidProtocol.send(out, context.getUuid());
			Collection<Statement> localStatements = new BufferedList<>(context.localDependencySortedStatements(transaction));
			integerProtocol.send(out, localStatements.size());
			for (Statement statement : localStatements)
			{
				statementProtocol.send(out, statement);
				nullableStatementAuthorityProtocol.send(out, statement.getAuthority(transaction));
				nullableStatementLocalProtocol.send(out, statement.getLocal(transaction));
				sendd++;
				if (sendd % 1000 == 0)
					logger.info("--> backup:" + sendd);
				if (statement instanceof Context)
					queue.add((Context) statement);

			}
		}
		logger.info("--> backup:" + sendd);
	}

	/**
	 * Receives a full data set of a persistence environment through a
	 * {@link DataInput}. Cleans up any information previously present on this
	 * persistence environment.
	 *
	 * @param in
	 *            The data input to receive the data from.
	 * @param persistenceManager
	 *            The persistence manager.
	 * @param transaction
	 *            The transaction.
	 * @throws IOException
	 * @throws ProtocolException
	 */
	public void restoreStatementsClean(DataInput in, Transaction transaction, int version) throws IOException, ProtocolException
	{
		if (version != 0)
			throw new VersionException(version);
		IntegerProtocol integerProtocol = new IntegerProtocol(0);
		UUIDProtocol uuidProtocol = new UUIDProtocol(0);
		StatementProtocol statementProtocol = new StatementProtocol(0, persistenceManager, transaction);
		StatementAuthorityDelegateTreeProtocol statementAuthorityProtocol = new StatementAuthorityDelegateTreeProtocol(0, persistenceManager, transaction);
		NullableProtocol<StatementAuthority> nullableStatementAuthorityProtocol = new NullableProtocol<>(0, statementAuthorityProtocol);
		StatementLocalProtocol statementLocalProtocol = new StatementLocalProtocol(0, persistenceManager, transaction);
		NullableProtocol<StatementLocal> nullableStatementLocalProtocol = new NullableProtocol<>(0, statementLocalProtocol);
		int recvd = 0;
		try
		{
			{
				Set<RootContext> old = new HashSet<RootContext>(persistenceManager.rootContexts(transaction).values());
				int n = integerProtocol.recv(in);
				for (int i = 0; i < n; i++)
				{
					Statement st = statementProtocol.recv(in);
					nullableStatementAuthorityProtocol.recv(in);
					nullableStatementLocalProtocol.recv(in);
					recvd++;
					if (recvd % 1000 == 0)
						logger.info("--> restore:" + recvd);
					old.remove(st);
				}
				for (RootContext rootCtx : old)
				{
					rootCtx.deleteCascade(transaction);
				}
			}
			while (true)
			{
				UUID uuidCtx = uuidProtocol.recv(in);
				try
				{
					Context ctx = (Context) persistenceManager.getStatement(transaction, uuidCtx);
					Set<Statement> old = new HashSet<Statement>(ctx.localStatements(transaction).values());
					int n = integerProtocol.recv(in);
					for (int i = 0; i < n; i++)
					{
						Statement st = statementProtocol.recv(in);
						nullableStatementAuthorityProtocol.recv(in);
						nullableStatementLocalProtocol.recv(in);
						recvd++;
						if (recvd % 1000 == 0)
							logger.info("--> restore:" + recvd);
						old.remove(st);
					}
					for (Statement st : old)
					{
						try
						{
							if (ctx.localStatements(transaction).containsKey(st.getVariable()))
								ctx.deleteStatementCascade(transaction, st);
						}
						catch (StatementNotInContextException e)
						{
							throw new Error(e);
						}
					}
				}
				catch (ClassCastException e)
				{
					throw new ProtocolException(e);
				}
			}
		}
		catch (EOFException e)
		{
		}
		logger.info("--> restore:" + recvd);
	}

	/**
	 * Receives a full data set of a persistence environment through a
	 * {@link DataInput}. Merges the received information with any that is
	 * already present on this persistence environment.
	 *
	 * @param in
	 *            The data input to receive the data from.
	 * @param restoreCancellerImpl
	 * @param persistenceManager
	 *            The persistence manager.
	 * @throws IOException
	 * @throws ProtocolException
	 */
	public void restoreStatements(DataInput in, int version, ListenableAborter aborter) throws IOException, ProtocolException
	{
		if (version != 0)
			throw new VersionException(version);
		IntegerProtocol integerProtocol = new IntegerProtocol(0);
		UUIDProtocol uuidProtocol = new UUIDProtocol(0);
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
		NullableProtocol<StatementAuthority> nullableStatementAuthorityProtocol = new NullableProtocol<>(0, statementAuthorityProtocol);
		StatementLocalProtocol statementLocalProtocol = new StatementLocalProtocol(0, persistenceManager, transaction);
		NullableProtocol<StatementLocal> nullableStatementLocalProtocol = new NullableProtocol<>(0, statementLocalProtocol);
		int recvd = 0;
		try
		{
			{
				int n = integerProtocol.recv(in);
				for (int i = 0; i < n; i++)
				{
					statementProtocol.recv(in);
					nullableStatementAuthorityProtocol.recv(in);
					nullableStatementLocalProtocol.recv(in);
					recvd++;
					if (recvd % 1000 == 0)
						logger.info("--> restore:" + recvd);
				}
			}
			while (true)
			{
				uuidProtocol.recv(in);
				try
				{
					int n = integerProtocol.recv(in);
					for (int i = 0; i < n; i++)
					{
						statementProtocol.recv(in);
						nullableStatementAuthorityProtocol.recv(in);
						nullableStatementLocalProtocol.recv(in);
						recvd++;
						if (recvd % 1000 == 0)
						{
							logger.info("--> restore:" + recvd);
							transaction.commit();
							transaction = persistenceManager.beginTransaction();
							aborterListener.setTransaction(transaction);
							statementProtocol = new StatementProtocol(0, persistenceManager, transaction);
							statementAuthorityProtocol = new StatementAuthorityDelegateTreeProtocol(0, persistenceManager, transaction);
							nullableStatementAuthorityProtocol = new NullableProtocol<>(0, statementAuthorityProtocol);
							statementLocalProtocol = new StatementLocalProtocol(0, persistenceManager, transaction);
							nullableStatementLocalProtocol = new NullableProtocol<>(0, statementLocalProtocol);
						}
					}
				}
				catch (ClassCastException e)
				{
					throw new ProtocolException(e);
				}
			}
		}
		catch (EOFException e)
		{
			transaction.commit();
		}
		finally
		{
			aborter.removeListener(aborterListener);
			transaction.abort();
		}
		logger.info("--> restore:" + recvd);
	}

}
