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
package aletheia.model.authority;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.PersistenceListener;
import aletheia.persistence.PersistenceListenerManager.Listeners;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.entities.authority.SignatureRequestEntity;
import aletheia.protocol.Exportable;
import aletheia.protocol.PersistentExportableProtocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.VersionProtocol;
import aletheia.protocol.authority.DelegateTreeRootNodeWithAuthorizersProtocol;
import aletheia.protocol.authority.PersonProtocol;
import aletheia.protocol.authority.StatementAuthorityProtocol;
import aletheia.protocol.enumerate.ByteExportableEnum;
import aletheia.protocol.enumerate.ByteExportableEnumProtocol;
import aletheia.protocol.enumerate.ExportableEnumInfo;
import aletheia.protocol.primitive.ByteArrayProtocol;
import aletheia.protocol.primitive.UUIDProtocol;
import aletheia.protocol.statement.StatementProtocol;
import aletheia.utilities.collections.CombinedSet;
import aletheia.utilities.io.SegmentedInputStream;
import aletheia.utilities.io.SegmentedOutputStream;

public abstract class SignatureRequest implements Exportable
{
	private final PersistenceManager persistenceManager;
	private final SignatureRequestEntity entity;

	public SignatureRequest(PersistenceManager persistenceManager, SignatureRequestEntity entity)
	{
		this.persistenceManager = persistenceManager;
		this.entity = entity;
	}

	protected SignatureRequest(PersistenceManager persistenceManager, Class<? extends SignatureRequestEntity> entityClass, UUID uuid, Date creationDate,
			List<UUID> contextUuidPath)
	{
		this.persistenceManager = persistenceManager;
		this.entity = persistenceManager.instantiateSignatureRequestEntity(entityClass);
		setUuid(uuid);
		setCreationDate(creationDate);
		setContextUuidPath(contextUuidPath);
	}

	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	public SignatureRequestEntity getEntity()
	{
		return entity;
	}

	protected void persistenceUpdate(Transaction transaction)
	{
		persistenceManager.putSignatureRequest(transaction, this);
		Iterable<StateListener> listeners = getStateListeners();
		synchronized (listeners)
		{
			for (StateListener l : listeners)
				l.signatureRequestModified(transaction, this);
		}

	}

	protected boolean persistenceUpdateNoOverwrite(Transaction transaction)
	{
		return persistenceManager.putSignatureRequestNoOverwrite(transaction, this);
	}

	public UUID getUuid()
	{
		return getEntity().getUuid();
	}

	private void setUuid(UUID uuid)
	{
		getEntity().setUuid(uuid);
	}

	public Date getCreationDate()
	{
		return getEntity().getCreationDate();
	}

	private void setCreationDate(Date creationDate)
	{
		getEntity().setCreationDate(creationDate);
	}

	protected List<UUID> getContextUuidPath()
	{
		return getEntity().getContextUuidPath();
	}

	private void clearContextUuidPath()
	{
		getContextUuidPath().clear();
	}

	private void setContextUuidPath(List<UUID> contextUuidPath)
	{
		clearContextUuidPath();
		getContextUuidPath().addAll(contextUuidPath);
	}

	public List<UUID> contextUuidPath()
	{
		return Collections.unmodifiableList(getContextUuidPath());
	}

	public UUID getContextUuid()
	{
		List<UUID> list = getContextUuidPath();
		if (list.size() <= 0)
			return null;
		return list.get(list.size() - 1);
	}

	public Context getContext(Transaction transaction)
	{
		UUID uuid = getContextUuid();
		if (uuid == null)
			return null;
		return getPersistenceManager().getContext(transaction, uuid);
	}

	public abstract static class SignatureRequestException extends Exception
	{
		private static final long serialVersionUID = 6806655710135327539L;

		public SignatureRequestException()
		{
			super();
		}

		public SignatureRequestException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public SignatureRequestException(String message)
		{
			super(message);
		}

		public SignatureRequestException(Throwable cause)
		{
			super(cause);
		}
	}

	public void delete(Transaction transaction)
	{
		persistenceManager.deleteSignatureRequest(transaction, this);
		Iterable<StateListener> listeners = getStateListeners();
		synchronized (listeners)
		{
			for (StateListener l : listeners)
				l.signatureRequestRemoved(transaction, this);
		}
	}

	protected abstract static class Builder
	{
		protected final static int version = 2;

		@ExportableEnumInfo(availableVersions = 0)
		protected enum RegisterType implements ByteExportableEnum<Enum<RegisterType>>
		{
			//@formatter:off
			Statement((byte)0),
			StatementAuthority((byte)1),
			Person((byte)2),
			RequestedStatement((byte)4),
			DelegateTreeNode((byte)5),
			;
			//@formatter:on

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

			@ProtocolInfo(availableVersions = 0)
			private static class Protocol extends ByteExportableEnumProtocol<RegisterType>
			{
				public Protocol(int requiredVersion)
				{
					super(0, RegisterType.class, 0);
					checkVersionAvailability(Protocol.class, requiredVersion);
				}
			}

		}

		private final PersistenceManager persistenceManager;
		private final Transaction transaction;
		private final VersionProtocol versionProtocol;
		private final RegisterType.Protocol registerTypeProtocol;
		private final StatementProtocol statementProtocol;
		private final StatementAuthorityProtocol statementAuthorityProtocol;
		private final PersonProtocol personProtocol;
		private final ByteArrayProtocol byteArrayProtocol;

		@ProtocolInfo(availableVersions = 0)
		protected class MyDelegateTreeRootNodeProtocol extends PersistentExportableProtocol<DelegateTreeRootNode>
		{
			private final UUIDProtocol uuidProtocol;

			public MyDelegateTreeRootNodeProtocol(int requiredVersion)
			{
				super(0, persistenceManager, transaction);
				checkVersionAvailability(MyDelegateTreeRootNodeProtocol.class, requiredVersion);
				this.uuidProtocol = new UUIDProtocol(0);
			}

			@Override
			public void send(DataOutput out, DelegateTreeRootNode dtrn) throws IOException
			{
				uuidProtocol.send(out, dtrn.getStatementUuid());
				DelegateTreeRootNodeWithAuthorizersProtocol delegateTreeRootNodeProtocol = new DelegateTreeRootNodeWithAuthorizersProtocol(0,
						getPersistenceManager(), getTransaction(), dtrn.getStatementAuthority(getTransaction()));
				delegateTreeRootNodeProtocol.send(out, dtrn);
			}

			@Override
			public DelegateTreeRootNode recv(DataInput in) throws IOException, ProtocolException
			{
				UUID statementUuid = uuidProtocol.recv(in);
				StatementAuthority statementAuthority = getPersistenceManager().getStatementAuthority(getTransaction(), statementUuid);
				if (statementAuthority == null)
					throw new ProtocolException();
				DelegateTreeRootNodeWithAuthorizersProtocol delegateTreeRootNodeProtocol = new DelegateTreeRootNodeWithAuthorizersProtocol(0,
						getPersistenceManager(), getTransaction(), statementAuthority);
				return delegateTreeRootNodeProtocol.recv(in);
			}

			@Override
			public void skip(DataInput in) throws IOException, ProtocolException
			{
				uuidProtocol.skip(in);
				new DelegateTreeRootNodeWithAuthorizersProtocol(0, getPersistenceManager(), getTransaction(), null).skip(in);
			}
		}

		private final MyDelegateTreeRootNodeProtocol myDelegateTreeRootNodeProtocol;

		public Builder(PersistenceManager persistenceManager, Transaction transaction)
		{
			this.persistenceManager = persistenceManager;
			this.transaction = transaction;
			this.versionProtocol = new VersionProtocol();
			this.registerTypeProtocol = new RegisterType.Protocol(0);
			this.statementProtocol = new StatementProtocol(2, persistenceManager, transaction);
			this.statementAuthorityProtocol = new StatementAuthorityProtocol(0, persistenceManager, transaction);
			this.personProtocol = new PersonProtocol(0, persistenceManager, transaction);
			this.byteArrayProtocol = new ByteArrayProtocol(0);
			this.myDelegateTreeRootNodeProtocol = new MyDelegateTreeRootNodeProtocol(0);
		}

		protected PersistenceManager getPersistenceManager()
		{
			return persistenceManager;
		}

		protected Transaction getTransaction()
		{
			return transaction;
		}

		protected VersionProtocol getVersionProtocol()
		{
			return versionProtocol;
		}

		protected RegisterType.Protocol getRegisterTypeProtocol()
		{
			return registerTypeProtocol;
		}

		protected StatementProtocol getStatementProtocol()
		{
			return statementProtocol;
		}

		protected StatementAuthorityProtocol getStatementAuthorityProtocol()
		{
			return statementAuthorityProtocol;
		}

		protected PersonProtocol getPersonProtocol()
		{
			return personProtocol;
		}

		protected ByteArrayProtocol getByteArrayProtocol()
		{
			return byteArrayProtocol;
		}

		protected MyDelegateTreeRootNodeProtocol getMyDelegateTreeRootNodeProtocol()
		{
			return myDelegateTreeRootNodeProtocol;
		}

	}

	public abstract class PackedBuilder extends Builder
	{
		private final Set<Statement> statementSet;
		private final Set<Person> sentPersons;
		private final Set<Statement> sentStatements;
		private final Set<UUID> dependencyUuids;

		protected PackedBuilder(PersistenceManager persistenceManager, Transaction transaction, Set<Statement> statementSet)
		{
			super(persistenceManager, transaction);
			this.statementSet = statementSet;
			this.sentPersons = new HashSet<>();
			this.sentStatements = new HashSet<>();
			this.dependencyUuids = new HashSet<>();
		}

		public Set<UUID> getDependencyUuids()
		{
			return dependencyUuids;
		}

		private void sendPerson(DataOutput out, Person person) throws IOException
		{
			if (!sentPersons.contains(person))
			{
				getRegisterTypeProtocol().send(out, RegisterType.Person);
				getPersonProtocol().send(out, person);
				sentPersons.add(person);
			}
		}

		private void sendStatementAuthority(DataOutput out, StatementAuthority statementAuthority) throws IOException
		{
			sendPerson(out, statementAuthority.getAuthor(getTransaction()));
			getRegisterTypeProtocol().send(out, RegisterType.StatementAuthority);
			getStatementAuthorityProtocol().send(out, statementAuthority);
			DelegateTreeRootNode delegateTreeRootNode = statementAuthority.getDelegateTreeRootNode(getTransaction());
			if (delegateTreeRootNode != null)
				sendDelegateTreeRootNode(out, delegateTreeRootNode);
		}

		private void sendDelegateTreeRootNode(DataOutput out, DelegateTreeRootNode delegateTreeRootNode) throws IOException
		{
			if (delegateTreeRootNode.isSigned())
			{
				for (DelegateAuthorizer da : delegateTreeRootNode.delegateAuthorizersRecursive(getTransaction()))
					sendPerson(out, da.getDelegate(getTransaction()));
				getRegisterTypeProtocol().send(out, RegisterType.DelegateTreeNode);
				getMyDelegateTreeRootNodeProtocol().send(out, delegateTreeRootNode);
			}
		}

		private void sendStatement(DataOutput out, Statement statement, boolean requested) throws IOException
		{
			if (!sentStatements.contains(statement))
			{
				getRegisterTypeProtocol().send(out, requested ? RegisterType.RequestedStatement : RegisterType.Statement);
				getStatementProtocol().send(out, statement);
				StatementAuthority statementAuthority = statement.getAuthority(getTransaction());
				if (statementAuthority != null)
					sendStatementAuthority(out, statementAuthority);
				sentStatements.add(statement);
			}

		}

		private void sendStatementDeps(DataOutput out, Statement statement) throws IOException
		{
			Stack<Statement> stack0 = new Stack<>();
			stack0.add(statement);
			Stack<Statement> stack1 = new Stack<>();
			while (!stack0.isEmpty())
			{
				Statement st = stack0.pop();
				if (!sentStatements.contains(st))
				{
					stack1.push(st);
					for (Statement dep : st.dependencies(getTransaction()))
					{
						if (statementSet.contains(dep))
							stack0.push(dep);
						else
							dependencyUuids.add(dep.getUuid());
					}
				}
			}
			while (!stack1.isEmpty())
			{
				Statement st = stack1.pop();
				sendStatement(out, st, true);
			}
		}

		private void sendContextContents(DataOutput out, Context context) throws IOException
		{
			class StackEntry
			{
				final Context context;
				final Set<Statement> frontDependencies;
				final CombinedSet<Statement> combinedDependencies;

				StackEntry(Context context, Set<Statement> backDependencies)
				{
					this.context = context;
					this.frontDependencies = new HashSet<>();
					this.combinedDependencies = new CombinedSet<>(frontDependencies, backDependencies);
				}
			}
			Stack<StackEntry> stack = new Stack<>();
			stack.push(new StackEntry(context, statementSet));
			while (!stack.isEmpty())
			{
				StackEntry se = stack.pop();
				for (Statement statement : se.context.localDependencySortedStatements(getTransaction()))
				{
					boolean satisfied = true;
					Collection<UUID> depUuids = new ArrayList<>();
					for (Statement dep : statement.dependencies(getTransaction()))
					{
						if (!se.combinedDependencies.contains(dep))
						{
							StatementAuthority depAuth = dep.getAuthority(getTransaction());
							if (depAuth == null || !depAuth.isSignedDependencies())
							{
								satisfied = false;
								break;
							}
							depUuids.add(dep.getUuid());
						}
					}
					if (satisfied)
					{
						dependencyUuids.addAll(depUuids);
						sendStatement(out, statement, false);
						se.frontDependencies.add(statement);
						if (statement instanceof Context)
							stack.push(new StackEntry((Context) statement, se.combinedDependencies));
					}
				}
			}
		}

		protected void send(DataOutput out) throws IOException
		{
			getVersionProtocol().send(out, version);
			for (Statement st : statementSet)
				sendStatementDeps(out, st);
			for (Statement st : statementSet)
				if (st instanceof Context)
					sendContextContents(out, (Context) st);
		}

	}

	public class DataOutputPackedBuilder extends PackedBuilder
	{
		private static final int segmentSize = 64 * 1024;

		public DataOutputPackedBuilder(PersistenceManager persistenceManager, Transaction transaction, Set<Statement> statementSet, final DataOutput dataOutput)
				throws IOException
		{
			super(persistenceManager, transaction, statementSet);
			SegmentedOutputStream sos = new SegmentedOutputStream(segmentSize)
			{
				@Override
				protected void segment(byte[] b, int off, int len) throws IOException
				{
					getByteArrayProtocol().send(dataOutput, b, off, len);
				}

				@Override
				public void close() throws IOException
				{
					super.close();
					getByteArrayProtocol().send(dataOutput, new byte[0]);
				}

			};
			DataOutputStream out = new DataOutputStream(new GZIPOutputStream(sos));
			try
			{
				send(out);
			}
			finally
			{
				out.close();
			}
		}
	}

	protected abstract class UnpackedBuilder extends Builder
	{
		private final Set<Statement> statementSet;

		protected UnpackedBuilder(PersistenceManager persistenceManager, Transaction transaction)
		{
			super(persistenceManager, transaction);
			this.statementSet = new HashSet<>();
		}

		protected Set<Statement> getStatementSet()
		{
			return statementSet;
		}

		protected void receive(DataInput in) throws IOException, ProtocolException
		{
			int version_ = getVersionProtocol().recv(in);
			if (version_ != version)
				throw new ProtocolException();
			while (true)
			{
				try
				{
					RegisterType type = getRegisterTypeProtocol().recv(in);
					switch (type)
					{
					case Statement:
						getStatementProtocol().recv(in);
						break;
					case RequestedStatement:
						statementSet.add(getStatementProtocol().recv(in));
						break;
					case StatementAuthority:
						getStatementAuthorityProtocol().recv(in);
						break;
					case Person:
						getPersonProtocol().recv(in);
						break;
					case DelegateTreeNode:
						getMyDelegateTreeRootNodeProtocol().recv(in);
						break;
					default:
						throw new Error();
					}
				}
				catch (EOFException e)
				{
					break;
				}
			}
		}

	}

	protected class DataInputUnpackedBuilder extends UnpackedBuilder
	{
		public DataInputUnpackedBuilder(PersistenceManager persistenceManager, Transaction transaction, final DataInput dataInput)
				throws ProtocolException, IOException
		{
			super(persistenceManager, transaction);
			class ExceptionCapsule extends RuntimeException
			{
				private static final long serialVersionUID = 8080643179801688789L;

				public ExceptionCapsule(ProtocolException cause)
				{
					super(cause);
				}

				@Override
				public synchronized ProtocolException getCause()
				{
					return (ProtocolException) super.getCause();
				}
			}
			SegmentedInputStream sis = new SegmentedInputStream()
			{
				@Override
				public byte[] segment() throws IOException
				{
					try
					{
						return getByteArrayProtocol().recv(dataInput);
					}
					catch (ProtocolException e)
					{
						throw new ExceptionCapsule(e);
					}
				}
			};
			DataInputStream in = new DataInputStream(new GZIPInputStream(sis));
			try
			{
				receive(in);
			}
			catch (ExceptionCapsule e)
			{
				throw e.getCause();
			}
			finally
			{
				in.close();
			}
		}

	}

	protected class ByteArrayUnpackedBuilder extends UnpackedBuilder
	{

		protected ByteArrayUnpackedBuilder(PersistenceManager persistenceManager, Transaction transaction, final byte[] data) throws ProtocolException
		{
			super(persistenceManager, transaction);
			SegmentedInputStream sis = new SegmentedInputStream()
			{
				private boolean sent = false;

				@Override
				public byte[] segment() throws IOException
				{
					if (!sent)
					{
						sent = true;
						return data;
					}
					else
						return null;
				}
			};
			try
			{
				DataInputStream in = new DataInputStream(new GZIPInputStream(sis));
				try
				{
					receive(in);
				}
				finally
				{
					in.close();
				}
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	}

	public interface AddStateListener extends PersistenceListener
	{
		void signatureRequestAdded(Transaction transaction, SignatureRequest signatureRequest);
	}

	public interface StateListener extends PersistenceListener
	{
		void signatureRequestModified(Transaction transaction, SignatureRequest signatureRequest);

		void signatureRequestRemoved(Transaction transaction, SignatureRequest signatureRequest);
	}

	protected void notifySignatureRequestAdded(Transaction transaction)
	{
		Listeners<AddStateListener> listeners = persistenceManager.getListenerManager().getSignatureRequestAddStateListeners();
		synchronized (listeners)
		{
			for (AddStateListener l : listeners)
				l.signatureRequestAdded(transaction, this);
		}
	}

	public Iterable<StateListener> getStateListeners()
	{
		return persistenceManager.getListenerManager().getSignatureRequestStateListeners().iterable(getUuid());
	}

	protected Iterable<StateListener> clearStateListeners()
	{
		return persistenceManager.getListenerManager().getSignatureRequestStateListeners().clear(getUuid());
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + getUuid().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SignatureRequest other = (SignatureRequest) obj;
		if (!getUuid().equals(other.getUuid()))
			return false;
		return true;
	}

	public void addStateListener(StateListener listener)
	{
		persistenceManager.getListenerManager().getSignatureRequestStateListeners().add(getUuid(), listener);
	}

	public void removeStateListener(StateListener listener)
	{
		persistenceManager.getListenerManager().getSignatureRequestStateListeners().remove(getUuid(), listener);
	}

	public SignatureRequest refresh(Transaction transaction)
	{
		return getPersistenceManager().getSignatureRequest(transaction, getUuid());
	}

}
