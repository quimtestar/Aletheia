/*******************************************************************************
 * Copyright (c) 2014, 2016 Quim Testar.
 *
 * This file is part of the Aletheia Proof Assistant.
 *
 * The Aletheia Proof Assistant is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The Aletheia Proof Assistant is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with the Aletheia Proof Assistant.
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package aletheia.peertopeer.statement.message;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import aletheia.model.authority.DelegateAuthorizer;
import aletheia.model.authority.DelegateTreeNode;
import aletheia.model.authority.DelegateTreeRootNode;
import aletheia.model.authority.DelegateTreeRootNode.DateConsistenceException;
import aletheia.model.authority.DelegateTreeRootNode.DuplicateSuccessorException;
import aletheia.model.authority.DelegateTreeRootNode.SuccessorEntry;
import aletheia.model.authority.DelegateTreeSubNode;
import aletheia.model.authority.Person;
import aletheia.model.authority.SignatureVerifyException;
import aletheia.model.authority.SignatureVersionException;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.peertopeer.base.message.AbstractUUIDInfoMessage;
import aletheia.peertopeer.base.message.MessageCode;
import aletheia.peertopeer.base.message.MessageSubProtocolInfo;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.protocol.Exportable;
import aletheia.protocol.ExportableProtocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.collection.ListProtocol;
import aletheia.protocol.collection.MapProtocol;
import aletheia.protocol.enumerate.ByteExportableEnum;
import aletheia.protocol.enumerate.ByteExportableEnumProtocol;
import aletheia.protocol.enumerate.ExportableEnumInfo;
import aletheia.protocol.primitive.DateProtocol;
import aletheia.protocol.primitive.IntegerProtocol;
import aletheia.protocol.primitive.StringProtocol;
import aletheia.protocol.primitive.UUIDProtocol;
import aletheia.security.model.SignatureData;
import aletheia.security.protocol.SignatureDataProtocol;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionList;
import aletheia.utilities.collections.CloseableIterator;
import aletheia.utilities.collections.Filter;
import aletheia.utilities.collections.FilteredEntryMap;

@MessageSubProtocolInfo(subProtocolClass = DelegateTreeInfoMessage.SubProtocol.class)
public class DelegateTreeInfoMessage extends AbstractUUIDInfoMessage<DelegateTreeInfoMessage.DelegateTreeRootNodeInfo>
{

	public static abstract class InfoUpdateException extends Exception
	{
		private static final long serialVersionUID = -7470011085830405236L;

	}

	public static class MissingDependencyException extends InfoUpdateException
	{
		private static final long serialVersionUID = 901784881140975217L;

	}

	static abstract class DelegateTreeNodeInfo implements Exportable
	{
		private final Map<UUID, DelegateAuthorizerInfo> delegateAuthorizers;
		private final Map<String, DelegateTreeSubNodeInfo> subNodes;

		private DelegateTreeNodeInfo(Map<UUID, DelegateAuthorizerInfo> delegateAuthorizers, Map<String, DelegateTreeSubNodeInfo> subNodes)
		{
			super();
			this.delegateAuthorizers = delegateAuthorizers;
			this.subNodes = subNodes;
		}

		private DelegateTreeNodeInfo(Transaction transaction, DelegateTreeNode delegateTreeNode)
		{
			super();
			this.delegateAuthorizers = new HashMap<>();
			this.subNodes = new HashMap<>();
			for (DelegateAuthorizer da : delegateTreeNode.localDelegateAuthorizerMap(transaction).values())
			{
				if (da.isSigned())
					delegateAuthorizers.put(da.getDelegateUuid(), new SignedDelegateAuthorizerInfo(transaction, da));
				else
					delegateAuthorizers.put(da.getDelegateUuid(), new UnsignedDelegateAuthorizerInfo());
			}
			for (DelegateTreeSubNode n : delegateTreeNode.localDelegateTreeSubNodeMap(transaction).values())
				subNodes.put(n.getPrefix().getName(), new DelegateTreeSubNodeInfo(transaction, n));
		}

		protected Map<UUID, DelegateAuthorizerInfo> getDelegateAuthorizers()
		{
			return delegateAuthorizers;
		}

		protected Map<String, DelegateTreeSubNodeInfo> getSubNodes()
		{
			return subNodes;
		}

		protected void update(PersistenceManager persistenceManager, Transaction transaction, DelegateTreeNode delegateTreeNode)
				throws MissingDependencyException, SignatureVerifyException, SignatureVersionException
		{
			for (Map.Entry<UUID, DelegateAuthorizerInfo> e : delegateAuthorizers.entrySet())
			{
				Person delegate = persistenceManager.getPerson(transaction, e.getKey());
				if (delegate == null)
					throw new MissingDependencyException();
				delegateTreeNode.getOrCreateDelegateAuthorizerNoSign(transaction, delegate);
			}
			for (DelegateAuthorizer delegateAuthorizer : delegateTreeNode.localDelegateAuthorizerMap(transaction).values())
				if (!delegateAuthorizers.containsKey(delegateAuthorizer.getDelegateUuid()))
					delegateTreeNode.deleteDelegateAuthorizerNoSign(transaction, delegateAuthorizer);
			for (Map.Entry<String, DelegateTreeSubNodeInfo> e : subNodes.entrySet())
			{
				try
				{
					DelegateTreeSubNode delegateTreeSubNode = delegateTreeNode.getOrCreateSubNodeNoSign(transaction, e.getKey());
					e.getValue().update(persistenceManager, transaction, delegateTreeSubNode);
				}
				catch (InvalidNameException e1)
				{
					throw new RuntimeException(e1);
				}
			}
			for (DelegateTreeSubNode delegateTreeSubNode : delegateTreeNode.localDelegateTreeSubNodeMap(transaction).values())
				if (!subNodes.containsKey(delegateTreeSubNode.getPrefix().getName()))
					delegateTreeNode.deleteSubNodeNoSign(transaction, delegateTreeSubNode.getPrefix());
			delegateTreeNode.update(transaction);
		}

		protected void delegateUuidDependencies(PersistenceManager persistenceManager, Transaction transaction, Set<UUID> delegateUuidDependencies)
		{
			for (UUID uuid : delegateAuthorizers.keySet())
				if (persistenceManager.getPerson(transaction, uuid) == null)
					delegateUuidDependencies.add(uuid);
			for (DelegateTreeSubNodeInfo subNode : subNodes.values())
				subNode.delegateUuidDependencies(persistenceManager, transaction, delegateUuidDependencies);
		}

		protected boolean fullyUpdated(PersistenceManager persistenceManager, Transaction transaction, DelegateTreeNode delegateTreeNode)
		{
			{
				CloseableIterator<DelegateAuthorizer> iterator = delegateTreeNode.localDelegateAuthorizerMap(transaction).values().iterator();
				try
				{
					while (iterator.hasNext())
					{
						DelegateAuthorizer delegateAuthorizer = iterator.next();
						DelegateAuthorizerInfo daInfo = delegateAuthorizers.get(delegateAuthorizer.getDelegateUuid());
						if (daInfo == null)
							return false;
						if (daInfo instanceof UnsignedDelegateAuthorizerInfo)
						{
							if (delegateAuthorizer.isSigned())
								return false;
						}
						else if (daInfo instanceof SignedDelegateAuthorizerInfo)
						{
							SignedDelegateAuthorizerInfo sdaInfo = (SignedDelegateAuthorizerInfo) daInfo;
							if (sdaInfo.getSignatureDate() != null && (delegateAuthorizer.getSignatureDate() == null
									|| delegateAuthorizer.getSignatureDate().compareTo(sdaInfo.getSignatureDate()) > 0))
								return false;
						}
						else
							throw new Error();
					}
				}
				finally
				{
					iterator.close();
				}
			}
			{
				CloseableIterator<DelegateTreeSubNode> iterator = delegateTreeNode.localDelegateTreeSubNodeMap(transaction).values().iterator();
				try
				{
					while (iterator.hasNext())
					{
						DelegateTreeSubNode delegateTreeSubNode = iterator.next();
						DelegateTreeSubNodeInfo subNodeInfo = subNodes.get(delegateTreeSubNode.getPrefix().getName());
						if (subNodeInfo == null)
							return false;
						if (!subNodeInfo.fullyUpdated(persistenceManager, transaction, delegateTreeSubNode))
							return false;
					}
				}
				finally
				{
					iterator.close();
				}
			}
			return true;
		}

		@ProtocolInfo(availableVersions = 0)
		private abstract static class MyProtocol<I extends DelegateTreeNodeInfo> extends ExportableProtocol<I>
		{
			private final static UUIDProtocol uuidProtocol = new UUIDProtocol(0);
			private final static DelegateAuthorizerInfo.MyProtocol delegateAuthorizerInfoProtocol = new DelegateAuthorizerInfo.MyProtocol(0);
			private final static MapProtocol<UUID, DelegateAuthorizerInfo> delegateAuthorizerInfoMapProtocol = new MapProtocol<>(0, uuidProtocol,
					delegateAuthorizerInfoProtocol);
			private final static StringProtocol stringProtocol = new StringProtocol(0);
			private final static DelegateTreeSubNodeInfo.MyProtocol delegateTreeSubNodeInfoProtocol = new DelegateTreeSubNodeInfo.MyProtocol(0);
			private final static MapProtocol<String, DelegateTreeSubNodeInfo> subNodesMapProtocol = new MapProtocol<>(0, stringProtocol,
					delegateTreeSubNodeInfoProtocol);

			protected MyProtocol(int requiredVersion)
			{
				super(0);
				checkVersionAvailability(MyProtocol.class, requiredVersion);
			}

			@Override
			public void send(DataOutput out, I delegateTreeNodeInfo) throws IOException
			{
				delegateAuthorizerInfoMapProtocol.send(out, delegateTreeNodeInfo.getDelegateAuthorizers());
				subNodesMapProtocol.send(out, delegateTreeNodeInfo.getSubNodes());
			}

			protected abstract I recv(Map<UUID, DelegateAuthorizerInfo> delegateAuthorizers, Map<String, DelegateTreeSubNodeInfo> subNodes, DataInput in)
					throws IOException, ProtocolException;

			@Override
			public I recv(DataInput in) throws IOException, ProtocolException
			{
				Map<UUID, DelegateAuthorizerInfo> delegateAuthorizers = delegateAuthorizerInfoMapProtocol.recv(in);
				Map<String, DelegateTreeSubNodeInfo> subNodes = subNodesMapProtocol.recv(in);
				return recv(delegateAuthorizers, subNodes, in);
			}

			@Override
			public void skip(DataInput in) throws IOException, ProtocolException
			{
				delegateAuthorizerInfoMapProtocol.skip(in);
				subNodesMapProtocol.skip(in);
			}

		}

	}

	public static class DelegateTreeRootNodeInfo extends DelegateTreeNodeInfo
	{
		private static class SuccessorEntryInfo implements Exportable
		{
			private final UUID successorUuid;
			private final Date signatureDate;
			private final int signatureVersion;
			private final SignatureData signatureData;

			private SuccessorEntryInfo(UUID successorUuid, Date signatureDate, int signatureVersion, SignatureData signatureData)
			{
				super();
				this.successorUuid = successorUuid;
				this.signatureDate = signatureDate;
				this.signatureVersion = signatureVersion;
				this.signatureData = signatureData;
			}

			private SuccessorEntryInfo(DelegateTreeRootNode.SuccessorEntry successorEntry)
			{
				super();
				this.successorUuid = successorEntry.getSuccessorUuid();
				this.signatureDate = successorEntry.getSignatureDate();
				this.signatureVersion = successorEntry.getSignatureVersion();
				this.signatureData = successorEntry.getSignatureData();

			}

			public UUID getSuccessorUuid()
			{
				return successorUuid;
			}

			public Date getSignatureDate()
			{
				return signatureDate;
			}

			public int getSignatureVersion()
			{
				return signatureVersion;
			}

			public SignatureData getSignatureData()
			{
				return signatureData;
			}

			@ProtocolInfo(availableVersions = 0)
			private static class MyProtocol extends ExportableProtocol<SuccessorEntryInfo>
			{
				private final UUIDProtocol uuidProtocol = new UUIDProtocol(0);
				private final DateProtocol dateProtocol = new DateProtocol(0);
				private final IntegerProtocol integerProtocol = new IntegerProtocol(0);
				private final SignatureDataProtocol signatureDataProtocol = new SignatureDataProtocol(0);

				protected MyProtocol(int requiredVersion)
				{
					super(0);
					checkVersionAvailability(MyProtocol.class, requiredVersion);
				}

				@Override
				public void send(DataOutput out, SuccessorEntryInfo successorEntryInfo) throws IOException
				{
					uuidProtocol.send(out, successorEntryInfo.successorUuid);
					dateProtocol.send(out, successorEntryInfo.signatureDate);
					integerProtocol.send(out, successorEntryInfo.signatureVersion);
					signatureDataProtocol.send(out, successorEntryInfo.signatureData);
				}

				@Override
				public SuccessorEntryInfo recv(DataInput in) throws IOException, ProtocolException
				{
					UUID successorUuid = uuidProtocol.recv(in);
					Date signatureDate = dateProtocol.recv(in);
					int signatureVersion = integerProtocol.recv(in);
					SignatureData signatureData = signatureDataProtocol.recv(in);
					return new SuccessorEntryInfo(successorUuid, signatureDate, signatureVersion, signatureData);
				}

				@Override
				public void skip(DataInput in) throws IOException, ProtocolException
				{
					uuidProtocol.skip(in);
					dateProtocol.skip(in);
					integerProtocol.skip(in);
					signatureDataProtocol.skip(in);
				}

			}

		}

		private final List<SuccessorEntryInfo> successorEntryInfoList;
		private final int successorIndex;
		private final Date signatureDate;
		private final int signatureVersion;
		private final SignatureData signatureData;

		private DelegateTreeRootNodeInfo(Map<UUID, DelegateAuthorizerInfo> delegateAuthorizers, Map<String, DelegateTreeSubNodeInfo> subNodes,
				List<SuccessorEntryInfo> successorEntryInfoList, int successorIndex, Date signatureDate, int signatureVersion, SignatureData signatureData)
		{
			super(delegateAuthorizers, subNodes);
			this.successorEntryInfoList = successorEntryInfoList;
			this.successorIndex = successorIndex;
			this.signatureDate = signatureDate;
			this.signatureVersion = signatureVersion;
			this.signatureData = signatureData;
		}

		public DelegateTreeRootNodeInfo(Transaction transaction, DelegateTreeRootNode delegateTreeRootNode)
		{
			super(transaction, delegateTreeRootNode);
			this.successorEntryInfoList = new BijectionList<>(new Bijection<DelegateTreeRootNode.SuccessorEntry, SuccessorEntryInfo>()
			{

				@Override
				public SuccessorEntryInfo forward(DelegateTreeRootNode.SuccessorEntry successorEntry)
				{
					return new SuccessorEntryInfo(successorEntry);
				}

				@Override
				public SuccessorEntry backward(SuccessorEntryInfo output)
				{
					throw new UnsupportedOperationException();
				}
			}, delegateTreeRootNode.successorEntries());
			this.successorIndex = delegateTreeRootNode.getSuccessorIndex();
			this.signatureDate = delegateTreeRootNode.getSignatureDate();
			this.signatureVersion = delegateTreeRootNode.getSignatureVersion();
			this.signatureData = delegateTreeRootNode.getSignatureData();
		}

		public List<SuccessorEntryInfo> getSuccessorEntryInfoList()
		{
			return successorEntryInfoList;
		}

		public int getSuccessorIndex()
		{
			return successorIndex;
		}

		public Date getSignatureDate()
		{
			return signatureDate;
		}

		public int getSignatureVersion()
		{
			return signatureVersion;
		}

		public SignatureData getSignatureData()
		{
			return signatureData;
		}

		private abstract class UpdateCommand
		{
			abstract void update(PersistenceManager persistenceManager, Transaction transaction, DelegateTreeRootNode delegateTreeRootNode)
					throws SignatureVerifyException, DateConsistenceException, DuplicateSuccessorException, MissingDependencyException,
					SignatureVersionException;

			abstract void successorUuidDependencies(PersistenceManager persistenceManager, Transaction transaction, Set<UUID> successorUuidDependencies);

			abstract void delegateUuidDependencies(PersistenceManager persistenceManager, Transaction transaction, Set<UUID> delegateUuidDependencies);

		}

		private abstract class SuccessorEntryUpdateCommand extends UpdateCommand
		{
			final SuccessorEntryInfo successorEntryInfo;

			private SuccessorEntryUpdateCommand(SuccessorEntryInfo successsorEntryInfo)
			{
				super();
				this.successorEntryInfo = successsorEntryInfo;
			}

			@Override
			void successorUuidDependencies(PersistenceManager persistenceManager, Transaction transaction, Set<UUID> successorUuidDependencies)
			{
				if (persistenceManager.getPerson(transaction, successorEntryInfo.getSuccessorUuid()) == null)
					successorUuidDependencies.add(successorEntryInfo.getSuccessorUuid());
			}

			@Override
			void delegateUuidDependencies(PersistenceManager persistenceManager, Transaction transaction, Set<UUID> delegateUuidDependencies)
			{
			}

		}

		private class UpdateSuccessorEntryUpdateCommand extends SuccessorEntryUpdateCommand
		{
			final int position;

			private UpdateSuccessorEntryUpdateCommand(SuccessorEntryInfo successorEntryInfo, int position)
			{
				super(successorEntryInfo);
				this.position = position;
			}

			@Override
			void update(PersistenceManager persistenceManager, Transaction transaction, DelegateTreeRootNode delegateTreeRootNode)
					throws SignatureVerifyException, DateConsistenceException, DuplicateSuccessorException, SignatureVersionException
			{
				delegateTreeRootNode.updateSuccessorEntriesSet(transaction, position, successorEntryInfo.getSuccessorUuid(),
						successorEntryInfo.getSignatureDate(), successorEntryInfo.getSignatureVersion(), successorEntryInfo.getSignatureData());
			}
		}

		private class AddSuccessorEntryUpdateCommand extends SuccessorEntryUpdateCommand
		{
			private AddSuccessorEntryUpdateCommand(SuccessorEntryInfo successorEntryInfo)
			{
				super(successorEntryInfo);
			}

			@Override
			void update(PersistenceManager persistenceManager, Transaction transaction, DelegateTreeRootNode delegateTreeRootNode)
					throws SignatureVerifyException, DateConsistenceException, DuplicateSuccessorException, SignatureVersionException
			{
				delegateTreeRootNode.updateSuccessorEntriesAdd(transaction, successorEntryInfo.getSuccessorUuid(), successorEntryInfo.getSignatureDate(),
						successorEntryInfo.getSignatureVersion(), successorEntryInfo.getSignatureData());
			}

		}

		private class DelegateTreeRootNodeUpdateCommand extends UpdateCommand
		{
			@Override
			void update(PersistenceManager persistenceManager, Transaction transaction, DelegateTreeRootNode delegateTreeRootNode)
					throws SignatureVerifyException, MissingDependencyException, DateConsistenceException, SignatureVersionException
			{
				DelegateTreeRootNodeInfo.super.update(persistenceManager, transaction, delegateTreeRootNode);
				delegateTreeRootNode.update(transaction, getSuccessorIndex(), getSignatureDate(), getSignatureVersion(), getSignatureData());
			}

			@Override
			void successorUuidDependencies(PersistenceManager persistenceManager, Transaction transaction, Set<UUID> successorUuidDependencies)
			{
			}

			@Override
			void delegateUuidDependencies(PersistenceManager persistenceManager, Transaction transaction, Set<UUID> delegateUuidDependencies)
			{
				DelegateTreeRootNodeInfo.super.delegateUuidDependencies(persistenceManager, transaction, delegateUuidDependencies);
			}
		}

		private class CleanSuccessorEntriesUpdateCommand extends UpdateCommand
		{
			@Override
			void update(PersistenceManager persistenceManager, Transaction transaction, DelegateTreeRootNode delegateTreeRootNode)
			{
				delegateTreeRootNode.cleanSuccessorEntries(transaction);
			}

			@Override
			void successorUuidDependencies(PersistenceManager persistenceManager, Transaction transaction, Set<UUID> successorUuidDependencies)
			{
			}

			@Override
			void delegateUuidDependencies(PersistenceManager persistenceManager, Transaction transaction, Set<UUID> delegateUuidDependencies)
			{
			}

		}

		private Collection<UpdateCommand> updateCommands(PersistenceManager persistenceManager, Transaction transaction, StatementAuthority statementAuthority)
		{
			Collection<UpdateCommand> updateCommands = new ArrayList<>();
			DelegateTreeRootNode delegateTreeRootNode = statementAuthority.getDelegateTreeRootNode(transaction);
			ListIterator<SuccessorEntry> listIterator = null;
			if (delegateTreeRootNode != null)
				listIterator = delegateTreeRootNode.successorEntries().listIterator();
			boolean forceUpdate = false;
			boolean forceNotUpdate = false;
			for (SuccessorEntryInfo successorEntryInfo : getSuccessorEntryInfoList())
			{
				if (listIterator != null)
				{
					if (listIterator.hasNext())
					{
						SuccessorEntry successorEntry = listIterator.next();
						if (successorEntryInfo.getSignatureDate() == null || (successorEntry.getSignatureDate() != null
								&& successorEntryInfo.getSignatureDate().compareTo(successorEntry.getSignatureDate()) < 0))
						{
							forceNotUpdate = true;
							break;
						}
						if (successorEntryInfo.getSignatureDate() == null
								|| successorEntryInfo.getSignatureDate().compareTo(successorEntry.getSignatureDate()) > 0)
						{
							if (successorEntry.updatable(successorEntryInfo.getSuccessorUuid(), successorEntryInfo.getSignatureDate(),
									successorEntryInfo.getSignatureData()))
							{
								int position = listIterator.previousIndex();
								updateCommands.add(new UpdateSuccessorEntryUpdateCommand(successorEntryInfo, position));
								if (delegateTreeRootNode != null && delegateTreeRootNode.getSuccessorIndex() >= position)
									forceUpdate = true;
								listIterator = null;
							}
						}
					}
					else
					{
						if (successorEntryInfo.getSignatureDate() == null || (delegateTreeRootNode != null && delegateTreeRootNode.getSignatureDate() != null
								&& successorEntryInfo.getSignatureDate().compareTo(delegateTreeRootNode.getSignatureDate()) < 0))
						{
							forceNotUpdate = true;
							break;
						}
						else
							updateCommands.add(new AddSuccessorEntryUpdateCommand(successorEntryInfo));
						listIterator = null;
					}
				}
				else
					updateCommands.add(new AddSuccessorEntryUpdateCommand(successorEntryInfo));
			}
			if (!forceUpdate && !forceNotUpdate && listIterator != null && listIterator.hasNext())
			{
				SuccessorEntry successorEntry = listIterator.next();
				if (getSignatureDate() != null
						&& (successorEntry.getSignatureDate() == null || getSignatureDate().compareTo(successorEntry.getSignatureDate()) >= 0))
					forceUpdate = true;
			}
			if (forceUpdate || (!forceNotUpdate && (delegateTreeRootNode == null || delegateTreeRootNode.getSignatureDate() == null
					|| getSignatureDate().compareTo(delegateTreeRootNode.getSignatureDate()) > 0)))
				updateCommands.add(new DelegateTreeRootNodeUpdateCommand());
			else
				updateCommands.add(new CleanSuccessorEntriesUpdateCommand());
			return updateCommands;
		}

		private void update(PersistenceManager persistenceManager, Transaction transaction, StatementAuthority statementAuthority)
				throws SignatureVerifyException, MissingDependencyException, DateConsistenceException, DuplicateSuccessorException, SignatureVersionException
		{
			DelegateTreeRootNode delegateTreeRootNode = statementAuthority.getOrCreateDelegateTreeRootNodeNoSign(transaction);
			for (UpdateCommand updateCommand : updateCommands(persistenceManager, transaction, statementAuthority))
				updateCommand.update(persistenceManager, transaction, delegateTreeRootNode);
		}

		protected void successorUuidDependencies(PersistenceManager persistenceManager, Transaction transaction, StatementAuthority statementAuthority,
				Set<UUID> successorUuidDependencies)
		{
			for (UpdateCommand updateCommand : updateCommands(persistenceManager, transaction, statementAuthority))
				updateCommand.successorUuidDependencies(persistenceManager, transaction, successorUuidDependencies);

		}

		protected void delegateUuidDependencies(PersistenceManager persistenceManager, Transaction transaction, StatementAuthority statementAuthority,
				Set<UUID> delegateUuidDependencies)
		{
			for (UpdateCommand updateCommand : updateCommands(persistenceManager, transaction, statementAuthority))
				updateCommand.delegateUuidDependencies(persistenceManager, transaction, delegateUuidDependencies);
		}

		protected boolean fullyUpdated(PersistenceManager persistenceManager, Transaction transaction, DelegateTreeRootNode delegateTreeRootNode)
		{
			if (getSignatureDate() != null
					&& (delegateTreeRootNode.getSignatureDate() == null || delegateTreeRootNode.getSignatureDate().compareTo(getSignatureDate()) > 0))
				return false;
			return super.fullyUpdated(persistenceManager, transaction, delegateTreeRootNode);
		}

		@ProtocolInfo(availableVersions = 0)
		private static class MyProtocol extends DelegateTreeNodeInfo.MyProtocol<DelegateTreeRootNodeInfo>
		{
			private final SuccessorEntryInfo.MyProtocol successorEntryInfoProtocol = new SuccessorEntryInfo.MyProtocol(0);
			private final ListProtocol<SuccessorEntryInfo> successorEntryInfoListProtocol = new ListProtocol<>(0, successorEntryInfoProtocol);
			private final IntegerProtocol integerProtocol = new IntegerProtocol(0);
			private final DateProtocol dateProtocol = new DateProtocol(0);
			private final SignatureDataProtocol signatureDataProtocol = new SignatureDataProtocol(0);

			protected MyProtocol(int requiredVersion)
			{
				super(0);
				checkVersionAvailability(MyProtocol.class, requiredVersion);
			}

			@Override
			public void send(DataOutput out, DelegateTreeRootNodeInfo delegateTreeRootNodeInfo) throws IOException
			{
				super.send(out, delegateTreeRootNodeInfo);
				successorEntryInfoListProtocol.send(out, delegateTreeRootNodeInfo.getSuccessorEntryInfoList());
				integerProtocol.send(out, delegateTreeRootNodeInfo.getSuccessorIndex());
				dateProtocol.send(out, delegateTreeRootNodeInfo.getSignatureDate());
				integerProtocol.send(out, delegateTreeRootNodeInfo.getSignatureVersion());
				signatureDataProtocol.send(out, delegateTreeRootNodeInfo.getSignatureData());
			}

			@Override
			public void skip(DataInput in) throws IOException, ProtocolException
			{
				super.skip(in);
				successorEntryInfoListProtocol.skip(in);
				integerProtocol.skip(in);
				dateProtocol.skip(in);
				integerProtocol.skip(in);
				signatureDataProtocol.skip(in);
			}

			@Override
			protected DelegateTreeRootNodeInfo recv(Map<UUID, DelegateAuthorizerInfo> delegateAuthorizers, Map<String, DelegateTreeSubNodeInfo> subNodes,
					DataInput in) throws IOException, ProtocolException
			{
				List<SuccessorEntryInfo> successorEntryInfoList = successorEntryInfoListProtocol.recv(in);
				int successorIndex = integerProtocol.recv(in);
				Date signatureDate = dateProtocol.recv(in);
				int signatureVersion = integerProtocol.recv(in);
				SignatureData signatureData = signatureDataProtocol.recv(in);
				return new DelegateTreeRootNodeInfo(delegateAuthorizers, subNodes, successorEntryInfoList, successorIndex, signatureDate, signatureVersion,
						signatureData);
			}

		}

	}

	static class DelegateTreeSubNodeInfo extends DelegateTreeNodeInfo
	{
		private DelegateTreeSubNodeInfo(Map<UUID, DelegateAuthorizerInfo> delegateAuthorizers, Map<String, DelegateTreeSubNodeInfo> subNodes)
		{
			super(delegateAuthorizers, subNodes);
		}

		private DelegateTreeSubNodeInfo(Transaction transaction, DelegateTreeSubNode delegateTreeSubNode)
		{
			super(transaction, delegateTreeSubNode);
		}

		@ProtocolInfo(availableVersions = 0)
		private static class MyProtocol extends DelegateTreeNodeInfo.MyProtocol<DelegateTreeSubNodeInfo>
		{

			protected MyProtocol(int requiredVersion)
			{
				super(0);
				checkVersionAvailability(MyProtocol.class, requiredVersion);
			}

			@Override
			protected DelegateTreeSubNodeInfo recv(Map<UUID, DelegateAuthorizerInfo> delegateAuthorizers, Map<String, DelegateTreeSubNodeInfo> subNodes,
					DataInput in)
			{
				return new DelegateTreeSubNodeInfo(delegateAuthorizers, subNodes);
			}

		}

	}

	abstract static class DelegateAuthorizerInfo implements Exportable
	{
		private DelegateAuthorizerInfo()
		{
		}

		private DelegateAuthorizerInfo(Transaction transaction, DelegateAuthorizer delegateAuthorizer)
		{

		}

		@ProtocolInfo(availableVersions = 0)
		private static class MyProtocol extends ExportableProtocol<DelegateAuthorizerInfo>
		{
			@ExportableEnumInfo(availableVersions = 0)
			private enum Type implements ByteExportableEnum<Type>
			{
				Unsigned((byte) 0), Signed((byte) 1),;

				private final byte code;

				private Type(byte code)
				{
					this.code = code;
				}

				@Override
				public Byte getCode(int version)
				{
					return code;
				}

			}

			private final ByteExportableEnumProtocol<Type> typeProtocol = new ByteExportableEnumProtocol<>(0, Type.class, 0);
			private final UnsignedDelegateAuthorizerInfo.SubProtocol unsignedDelegateAuthorizerInfoSubProtocol = new UnsignedDelegateAuthorizerInfo.SubProtocol(
					0);
			private final SignedDelegateAuthorizerInfo.SubProtocol signedDelegateAuthorizerInfoSubProtocol = new SignedDelegateAuthorizerInfo.SubProtocol(0);

			protected MyProtocol(int requiredVersion)
			{
				super(0);
				checkVersionAvailability(MyProtocol.class, requiredVersion);
			}

			@Override
			public void send(DataOutput out, DelegateAuthorizerInfo delegateAuthorizerInfo) throws IOException
			{
				if (delegateAuthorizerInfo instanceof UnsignedDelegateAuthorizerInfo)
				{
					typeProtocol.send(out, Type.Unsigned);
					unsignedDelegateAuthorizerInfoSubProtocol.send(out, (UnsignedDelegateAuthorizerInfo) delegateAuthorizerInfo);
				}
				else if (delegateAuthorizerInfo instanceof SignedDelegateAuthorizerInfo)
				{
					typeProtocol.send(out, Type.Signed);
					signedDelegateAuthorizerInfoSubProtocol.send(out, (SignedDelegateAuthorizerInfo) delegateAuthorizerInfo);
				}
				else
					throw new Error();
			}

			@Override
			public DelegateAuthorizerInfo recv(DataInput in) throws IOException, ProtocolException
			{
				Type type = typeProtocol.recv(in);
				switch (type)
				{
				case Unsigned:
					return unsignedDelegateAuthorizerInfoSubProtocol.recv(in);
				case Signed:
					return signedDelegateAuthorizerInfoSubProtocol.recv(in);
				default:
					throw new Error();
				}
			}

			@Override
			public void skip(DataInput in) throws IOException, ProtocolException
			{
				Type type = typeProtocol.recv(in);
				switch (type)
				{
				case Unsigned:
					unsignedDelegateAuthorizerInfoSubProtocol.skip(in);
					break;
				case Signed:
					signedDelegateAuthorizerInfoSubProtocol.skip(in);
					break;
				default:
					throw new Error();
				}
			}

		}

		@ProtocolInfo(availableVersions = 0)
		private abstract static class SubProtocol<I extends DelegateAuthorizerInfo> extends ExportableProtocol<I>
		{

			protected SubProtocol(int requiredVersion)
			{
				super(0);
				checkVersionAvailability(SubProtocol.class, requiredVersion);
			}

			@Override
			public abstract void send(DataOutput out, I t) throws IOException;

			@Override
			public abstract I recv(DataInput in) throws IOException, ProtocolException;

			@Override
			public abstract void skip(DataInput in) throws IOException, ProtocolException;

		}

	}

	private static class UnsignedDelegateAuthorizerInfo extends DelegateAuthorizerInfo
	{
		private UnsignedDelegateAuthorizerInfo()
		{
			super();
		}

		private UnsignedDelegateAuthorizerInfo(Transaction transaction, DelegateAuthorizer delegateAuthorizer)
		{
			super(transaction, delegateAuthorizer);
		}

		@ProtocolInfo(availableVersions = 0)
		public static class SubProtocol extends DelegateAuthorizerInfo.SubProtocol<UnsignedDelegateAuthorizerInfo>
		{

			protected SubProtocol(int requiredVersion)
			{
				super(0);
				checkVersionAvailability(SubProtocol.class, requiredVersion);
			}

			@Override
			public void send(DataOutput out, UnsignedDelegateAuthorizerInfo t) throws IOException
			{
			}

			@Override
			public void skip(DataInput in) throws IOException, ProtocolException
			{
			}

			@Override
			public UnsignedDelegateAuthorizerInfo recv(DataInput in) throws IOException, ProtocolException
			{
				return new UnsignedDelegateAuthorizerInfo();
			}

		}

	}

	static class SignedDelegateAuthorizerInfo extends DelegateAuthorizerInfo
	{
		private final UUID authorizerUuid;
		private final Date signatureDate;
		private final SignatureData signatureData;

		private SignedDelegateAuthorizerInfo(UUID authorizerUuid, Date signatureDate, SignatureData signatureData)
		{
			super();
			this.authorizerUuid = authorizerUuid;
			this.signatureDate = signatureDate;
			this.signatureData = signatureData;
		}

		private SignedDelegateAuthorizerInfo(Transaction transaction, DelegateAuthorizer delegateAuthorizer)
		{
			super(transaction, delegateAuthorizer);
			this.authorizerUuid = delegateAuthorizer.getAuthorizerUuid();
			this.signatureDate = delegateAuthorizer.getSignatureDate();
			this.signatureData = delegateAuthorizer.getSignatureData();
		}

		public UUID getAuthorizerUuid()
		{
			return authorizerUuid;
		}

		public Date getSignatureDate()
		{
			return signatureDate;
		}

		public SignatureData getSignatureData()
		{
			return signatureData;
		}

		boolean updatable(DelegateAuthorizer delegateAuthorizer)
		{
			if (delegateAuthorizer == null)
				return true;
			if (getSignatureDate() == null)
				return false;
			if (delegateAuthorizer.getSignatureDate() == null)
				return true;
			if (getSignatureDate().compareTo(delegateAuthorizer.getSignatureDate()) <= 0)
				return false;
			return true;
		}

		@ProtocolInfo(availableVersions = 0)
		public static class SubProtocol extends DelegateAuthorizerInfo.SubProtocol<SignedDelegateAuthorizerInfo>
		{
			private final UUIDProtocol uuidProtocol = new UUIDProtocol(0);
			private final DateProtocol dateProtocol = new DateProtocol(0);
			private final SignatureDataProtocol signatureDataProtocol = new SignatureDataProtocol(0);

			protected SubProtocol(int requiredVersion)
			{
				super(0);
				checkVersionAvailability(SubProtocol.class, requiredVersion);
			}

			@Override
			public void send(DataOutput out, SignedDelegateAuthorizerInfo signedDelegateAuthorizerInfo) throws IOException
			{
				uuidProtocol.send(out, signedDelegateAuthorizerInfo.getAuthorizerUuid());
				dateProtocol.send(out, signedDelegateAuthorizerInfo.getSignatureDate());
				signatureDataProtocol.send(out, signedDelegateAuthorizerInfo.getSignatureData());
			}

			@Override
			public void skip(DataInput in) throws IOException, ProtocolException
			{
				uuidProtocol.skip(in);
				dateProtocol.skip(in);
				signatureDataProtocol.skip(in);
			}

			@Override
			public SignedDelegateAuthorizerInfo recv(DataInput in) throws IOException, ProtocolException
			{
				UUID authorizerUuid = uuidProtocol.recv(in);
				Date signatureDate = dateProtocol.recv(in);
				SignatureData signatureData = signatureDataProtocol.recv(in);
				return new SignedDelegateAuthorizerInfo(authorizerUuid, signatureDate, signatureData);
			}

		}

	}

	public static class Entry extends AbstractUUIDInfoMessage.Entry<DelegateTreeRootNodeInfo>
	{
		public Entry(UUID uuid, DelegateTreeRootNodeInfo value)
		{
			super(uuid, value);
		}

		public Entry(UUID uuid)
		{
			super(uuid, null);
		}

	}

	public DelegateTreeInfoMessage(Collection<? extends AbstractUUIDInfoMessage.Entry<DelegateTreeRootNodeInfo>> entries)
	{
		super(entries);
	}

	public Collection<UUID> successorUuidDependencies(PersistenceManager persistenceManager, Transaction transaction)
	{
		Set<UUID> successorUuidDependencies = new HashSet<>();
		for (AbstractUUIDInfoMessage.Entry<DelegateTreeRootNodeInfo> e : getEntries())
		{
			StatementAuthority statementAuthority = persistenceManager.getStatementAuthority(transaction, e.getKey());
			if (statementAuthority != null)
				e.getValue().successorUuidDependencies(persistenceManager, transaction, statementAuthority, successorUuidDependencies);
		}
		return successorUuidDependencies;
	}

	public Collection<UUID> delegateUuidDependencies(PersistenceManager persistenceManager, Transaction transaction)
	{
		Set<UUID> delegateUuidDependencies = new HashSet<>();
		for (AbstractUUIDInfoMessage.Entry<DelegateTreeRootNodeInfo> e : getEntries())
		{
			StatementAuthority statementAuthority = persistenceManager.getStatementAuthority(transaction, e.getKey());
			if (statementAuthority != null)
				e.getValue().delegateUuidDependencies(persistenceManager, transaction, statementAuthority, delegateUuidDependencies);
		}
		return delegateUuidDependencies;
	}

	public void update(PersistenceManager persistenceManager, Transaction transaction)
			throws SignatureVerifyException, MissingDependencyException, DateConsistenceException, DuplicateSuccessorException, SignatureVersionException
	{
		for (AbstractUUIDInfoMessage.Entry<DelegateTreeRootNodeInfo> e : getEntries())
		{
			if (e.getValue() != null)
			{
				StatementAuthority statementAuthority = persistenceManager.getStatementAuthority(transaction, e.getKey());
				if (statementAuthority != null)
					e.getValue().update(persistenceManager, transaction, statementAuthority);
			}
		}
	}

	public Map<UUID, DelegateTreeRootNodeInfo> filterFullyUpdatedMap(final PersistenceManager persistenceManager, final Transaction transaction)
	{
		return new FilteredEntryMap<>(new Filter<Map.Entry<UUID, DelegateTreeRootNodeInfo>>()
		{

			@Override
			public boolean filter(Map.Entry<UUID, DelegateTreeRootNodeInfo> e)
			{
				UUID contextUuid = e.getKey();
				DelegateTreeRootNodeInfo delegateTreeRootNodeInfo = e.getValue();
				DelegateTreeRootNode delegateTreeRootNode = persistenceManager.getDelegateTreeRootNode(transaction, contextUuid);
				if (delegateTreeRootNode == null)
					return false;
				return delegateTreeRootNodeInfo.fullyUpdated(persistenceManager, transaction, delegateTreeRootNode);

			}
		}, getMap());
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends AbstractUUIDInfoMessage.SubProtocol<DelegateTreeRootNodeInfo, DelegateTreeInfoMessage>
	{
		private final DelegateTreeRootNodeInfo.MyProtocol delegateTreeRootNodeInfoProtocol;

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(0, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
			this.delegateTreeRootNodeInfoProtocol = new DelegateTreeRootNodeInfo.MyProtocol(0);
		}

		@Override
		protected void sendValue(UUID uuid, DataOutput out, DelegateTreeRootNodeInfo v) throws IOException
		{
			delegateTreeRootNodeInfoProtocol.send(out, v);
		}

		@Override
		protected DelegateTreeRootNodeInfo recvValue(UUID uuid, DataInput in) throws IOException, ProtocolException
		{
			return delegateTreeRootNodeInfoProtocol.recv(in);
		}

		@Override
		protected void skipValue(DataInput in) throws IOException, ProtocolException
		{
			delegateTreeRootNodeInfoProtocol.skip(in);
		}

		@Override
		public DelegateTreeInfoMessage recv(DataInput in) throws IOException, ProtocolException
		{
			return new DelegateTreeInfoMessage(recvEntries(in));
		}

	}

}
