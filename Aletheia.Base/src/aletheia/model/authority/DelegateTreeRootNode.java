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

import java.io.DataOutput;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import aletheia.model.authority.PrivatePerson.PrivateSignatoryException;
import aletheia.model.authority.StatementAuthority.StateListener;
import aletheia.model.identifier.RootNamespace;
import aletheia.model.security.SignatureData;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.entities.authority.DelegateTreeRootNodeEntity;
import aletheia.protocol.Exportable;
import aletheia.protocol.primitive.DateProtocol;
import aletheia.protocol.primitive.UUIDProtocol;
import aletheia.protocol.security.MessageDigestDataProtocol;
import aletheia.security.signerverifier.Signer;
import aletheia.security.signerverifier.Verifier;
import aletheia.utilities.collections.AdaptedCloseableIterable;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCloseableIterable;
import aletheia.utilities.collections.BijectionList;
import aletheia.utilities.collections.CloseableCollection;
import aletheia.utilities.collections.CloseableIterable;
import aletheia.utilities.collections.CloseableIterator;
import aletheia.utilities.collections.CloseableMap;
import aletheia.utilities.collections.CombinedCloseableIterable;
import aletheia.utilities.collections.CombinedCollection;
import aletheia.utilities.collections.DifferenceCollection;
import aletheia.utilities.collections.TrivialCloseableIterable;
import aletheia.utilities.collections.UnionCloseableIterable;

public class DelegateTreeRootNode extends DelegateTreeNode
{
	private final static int signingSignatureVersion = 0;

	private Set<UUID> oldSuccessorUuids;

	public DelegateTreeRootNode(PersistenceManager persistenceManager, DelegateTreeRootNodeEntity entity)
	{
		super(persistenceManager, entity);
		this.oldSuccessorUuids = new HashSet<>(successorUuids());
	}

	@Override
	public DelegateTreeRootNodeEntity getEntity()
	{
		return (DelegateTreeRootNodeEntity) super.getEntity();
	}

	private DelegateTreeRootNode(PersistenceManager persistenceManager, StatementAuthority statementAuthority)
	{
		super(persistenceManager, DelegateTreeRootNodeEntity.class, statementAuthority.getStatementUuid());
		getEntity().setPrefix(RootNamespace.instance);
		setSuccessorIndex(-1);
		setSignatureVersion(-1);
		this.oldSuccessorUuids = null;
	}

	public static DelegateTreeRootNode create(PersistenceManager persistenceManager, Transaction transaction, StatementAuthority statementAuthority)
			throws NoPrivateDataForAuthorException, DateConsistenceException
	{
		DelegateTreeRootNode delegateTreeRootNode = new DelegateTreeRootNode(persistenceManager, statementAuthority);
		delegateTreeRootNode.persistenceUpdateSign(transaction);
		return delegateTreeRootNode;
	}

	public static DelegateTreeRootNode createNoSign(PersistenceManager persistenceManager, Transaction transaction, StatementAuthority statementAuthority)
	{
		DelegateTreeRootNode delegateTreeRootNode = new DelegateTreeRootNode(persistenceManager, statementAuthority);
		delegateTreeRootNode.persistenceUpdate(transaction);
		return delegateTreeRootNode;
	}

	@Override
	public RootNamespace getPrefix()
	{
		return (RootNamespace) super.getPrefix();
	}

	public int getSuccessorIndex()
	{
		return getEntity().getSuccessorIndex();
	}

	private void setSuccessorIndex(int successorIndex)
	{
		getEntity().setSuccessorIndex(successorIndex);
	}

	@Override
	public CloseableMap<Person, DelegateAuthorizer> delegateAuthorizerMap(Transaction transaction)
	{
		return localDelegateAuthorizerMap(transaction);
	}

	public class SuccessorEntry implements Exportable
	{
		private final static int defaultSignatureVersion = 0;

		private final DelegateTreeRootNodeEntity.SuccessorEntryEntity entity;

		public SuccessorEntry(DelegateTreeRootNodeEntity.SuccessorEntryEntity entity)
		{
			this.entity = entity;
		}

		private SuccessorEntry(UUID successorUuid)
		{
			this.entity = instantiateSuccessorEntryEntity();
			setSuccessorUuid(successorUuid);
			setSignatureVersion(defaultSignatureVersion);
		}

		private SuccessorEntry(UUID successorUuid, Date signatureDate, int signatureVersion, SignatureData signatureData)
		{
			this.entity = instantiateSuccessorEntryEntity();
			setSuccessorUuid(successorUuid);
			setSignatureDate(signatureDate);
			setSignatureVersion(signatureVersion);
			setSignatureData(signatureData);
		}

		public synchronized DelegateTreeRootNodeEntity.SuccessorEntryEntity getEntity()
		{
			return entity;
		}

		public synchronized UUID getSuccessorUuid()
		{
			return getEntity().getSuccessorUuid();
		}

		public synchronized Person getSuccessor(Transaction transaction)
		{
			return getPersistenceManager().getPerson(transaction, getSuccessorUuid());
		}

		private synchronized void setSuccessorUuid(UUID successorUuid)
		{
			getEntity().setSuccessorUuid(successorUuid);
		}

		public synchronized Date getSignatureDate()
		{
			return getEntity().getSignatureDate();
		}

		public synchronized void setSignatureDate(Date signatureDate)
		{
			getEntity().setSignatureDate(signatureDate);
		}

		protected synchronized void setSignatureDate()
		{
			setSignatureDate(new Date());
		}

		public synchronized int getSignatureVersion()
		{
			return getEntity().getSignatureVersion();
		}

		protected synchronized void setSignatureVersion(int signatureVersion)
		{
			getEntity().setSignatureVersion(signatureVersion);
		}

		public synchronized SignatureData getSignatureData()
		{
			return getEntity().getSignatureData();
		}

		private synchronized void setSignatureData(SignatureData signatureData)
		{
			getEntity().setSignatureData(signatureData);
		}

		private synchronized void signatureDataOut(DataOutput out) throws SignatureVersionException
		{
			if (getSignatureVersion() != 0)
				throw new SignatureVersionException();
			try
			{
				UUIDProtocol uuidProtocol = new UUIDProtocol(0);
				DateProtocol dateProtocol = new DateProtocol(0);
				uuidProtocol.send(out, getSuccessorUuid());
				dateProtocol.send(out, getSignatureDate());
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		private synchronized void sign(PrivateSignatory signatory, Transaction transaction) throws NoPrivateDataForSignatoryException
		{
			try
			{
				Signer signer = signatory.signer();
				setSignatureDate();
				setSignatureVersion(defaultSignatureVersion);
				try
				{
					signatureDataOut(signer.dataOutput());
				}
				catch (SignatureVersionException e)
				{
					throw new Error("signingSignatureVersion must be supported", e);
				}
				setSignatureData(signer.sign());
			}
			catch (InvalidKeyException e)
			{
				throw new RuntimeException(e);
			}

		}

		private synchronized void verify(Transaction transaction, SuccessorEntry antecessor) throws SignatureVerifyException, SignatureVersionException
		{
			try
			{
				Signatory signatory;
				if (antecessor == null)
					signatory = getAuthor(transaction).getSignatory(transaction);
				else
					signatory = antecessor.getSuccessor(transaction).getSignatory(transaction);
				Verifier verifier = signatory.verifier(getSignatureData());
				signatureDataOut(verifier.dataOutput());
				if (!verifier.verify())
					throw new SignatureVerifyException();
			}
			catch (InvalidKeyException | IncompleteDataSignatureException | NoSuchAlgorithmException e)
			{
				throw new SignatureVerifyException(e);
			}
			finally
			{

			}
		}

		public synchronized boolean updatable(UUID successorUuid, Date signatureDate, SignatureData signatureData)
		{
			return !successorUuid.equals(getSuccessorUuid()) || !signatureDate.equals(getSignatureDate()) || !signatureData.equals(getSignatureData());
		}

	}

	private DelegateTreeRootNodeEntity.SuccessorEntryEntity instantiateSuccessorEntryEntity()
	{
		return getEntity().instantiateSuccessorEntryEntity();
	}

	private List<SuccessorEntry> getSuccessorEntries()
	{
		return new BijectionList<>(
				new Bijection<DelegateTreeRootNodeEntity.SuccessorEntryEntity, SuccessorEntry>()
				{

					@Override
					public SuccessorEntry forward(DelegateTreeRootNodeEntity.SuccessorEntryEntity entity)
					{
						return new SuccessorEntry(entity);
					}

					@Override
					public DelegateTreeRootNodeEntity.SuccessorEntryEntity backward(SuccessorEntry successorEntry)
					{
						return successorEntry.getEntity();
					}
				}, getEntity().getSuccessorEntryEntities());
	}

	private Set<UUID> successorUuids()
	{
		return getEntity().successorUuids();
	}

	public List<SuccessorEntry> successorEntries()
	{
		return Collections.unmodifiableList(getSuccessorEntries());
	}

	private synchronized SuccessorEntry lastSuccessorEntry()
	{
		if (getSuccessorEntries().isEmpty())
			return null;
		else
			return getSuccessorEntries().get(getSuccessorEntries().size() - 1);
	}

	public class DateConsistenceException extends AuthorityException
	{
		private static final long serialVersionUID = 4850309249581871699L;

		private DateConsistenceException()
		{
			super("Date consistence error");
		}

	}

	public class DuplicateSuccessorException extends AuthorityException
	{
		private static final long serialVersionUID = 1101499341934219167L;

		private DuplicateSuccessorException()
		{
			super("Duplicate successor");
		}

	}

	private boolean duplicateSuccessorUuid(Transaction transaction, UUID successorUuid)
	{
		if (successorUuids().contains(successorUuid))
			return true;
		if (getAuthor(transaction).getUuid().equals(successorUuid))
			return true;
		return false;
	}

	private synchronized void addSuccessorEntry(Transaction transaction, SuccessorEntry successorEntry)
			throws DateConsistenceException, DuplicateSuccessorException
	{
		if (duplicateSuccessorUuid(transaction, successorEntry.getSuccessorUuid()))
			throw new DuplicateSuccessorException();
		Date lastDate;
		SuccessorEntry lastSuccessorEntry = lastSuccessorEntry();
		if (lastSuccessorEntry == null)
			lastDate = getStatementAuthority(transaction).getCreationDate();
		else
			lastDate = lastSuccessorEntry.getSignatureDate();
		if (lastDate.compareTo(successorEntry.getSignatureDate()) >= 0)
			throw new DateConsistenceException();
		getSuccessorEntries().add(successorEntry);
		persistenceUpdate(transaction);
		notifyListenersSuccessorEntriesChanged(transaction);
	}

	private synchronized SuccessorEntry addSuccessorEntry(Transaction transaction, UUID successorUuid)
			throws NoPrivateDataForSignatoryException, DateConsistenceException, DuplicateSuccessorException
	{
		SuccessorEntry successorEntry = new SuccessorEntry(successorUuid);
		PrivatePerson person = cutSuccessorEntriesToLastPrivatePerson(transaction);
		if (getSuccessorIndex() >= getSuccessorEntries().size())
			sign(transaction);
		try
		{
			successorEntry.sign(person.getPrivateSignatory(transaction), transaction);
		}
		catch (PrivateSignatoryException e)
		{
			throw new NoPrivateDataForAuthorException(e);
		}
		addSuccessorEntry(transaction, successorEntry);
		return successorEntry;
	}

	public synchronized SuccessorEntry updateSuccessorEntriesAdd(Transaction transaction, UUID successorUuid, Date signatureDate, int signatureVersion,
			SignatureData signatureData) throws SignatureVerifyException, DateConsistenceException, DuplicateSuccessorException, SignatureVersionException
	{
		SuccessorEntry successorEntry = new SuccessorEntry(successorUuid, signatureDate, signatureVersion, signatureData);
		successorEntry.verify(transaction, lastSuccessorEntry());
		addSuccessorEntry(transaction, successorEntry);
		return successorEntry;
	}

	public synchronized SuccessorEntry updateSuccessorEntriesSet(Transaction transaction, int position, UUID successorUuid, Date signatureDate,
			int signatureVersion, SignatureData signatureData)
			throws SignatureVerifyException, DateConsistenceException, DuplicateSuccessorException, SignatureVersionException
	{
		List<SuccessorEntry> successorEntries = getSuccessorEntries();
		successorEntries.subList(position, successorEntries.size()).clear();
		return updateSuccessorEntriesAdd(transaction, successorUuid, signatureDate, signatureVersion, signatureData);
	}

	public SuccessorEntry addSuccessorEntry(Transaction transaction, Person successor)
			throws NoPrivateDataForSignatoryException, DateConsistenceException, DuplicateSuccessorException
	{
		return addSuccessorEntry(transaction, successor.getUuid());
	}

	public Date getSignatureDate()
	{
		return getEntity().getSignatureDate();
	}

	protected void setSignatureDate(Date signatureDate)
	{
		getEntity().setSignatureDate(signatureDate);
	}

	protected void setSignatureDate()
	{
		setSignatureDate(new Date());
	}

	public int getSignatureVersion()
	{
		return getEntity().getSignatureVersion();
	}

	private void setSignatureVersion(int signatureVersion)
	{
		getEntity().setSignatureVersion(signatureVersion);
	}

	public SignatureData getSignatureData()
	{
		return getEntity().getSignatureData();
	}

	private void setSignatureData(SignatureData signatureData)
	{
		getEntity().setSignatureData(signatureData);
	}

	protected void signatureDataOut(DataOutput out) throws SignatureVersionException
	{
		if (getSignatureVersion() != 0)
			throw new SignatureVersionException();
		try
		{
			UUIDProtocol uuidProtocol = new UUIDProtocol(0);
			DateProtocol dateProtocol = new DateProtocol(0);
			MessageDigestDataProtocol messageDigestDataProtocol = new MessageDigestDataProtocol(0);
			uuidProtocol.send(out, getStatementUuid());
			dateProtocol.send(out, getSignatureDate());
			messageDigestDataProtocol.send(out, getMessageDigestData());
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public abstract class NoPrivateDataForSignatoryException extends BadSignatoryException
	{
		private static final long serialVersionUID = -4951999618180680347L;

		private NoPrivateDataForSignatoryException(String message)
		{
			super(message);
		}

		private NoPrivateDataForSignatoryException(Throwable cause)
		{
			super(cause);
		}

	}

	public class NoPrivateDataForAuthorException extends NoPrivateDataForSignatoryException
	{
		private static final long serialVersionUID = -5771523023927376399L;

		private NoPrivateDataForAuthorException()
		{
			super("Don't have the private data for this author");
		}

		private NoPrivateDataForAuthorException(Throwable cause)
		{
			super(cause);
		}

	}

	public class NoPrivateDataForSuccessorException extends NoPrivateDataForSignatoryException
	{
		private static final long serialVersionUID = 8181820539920270498L;

		private NoPrivateDataForSuccessorException()
		{
			super("Don't have the private data for this successor");
		}

	}

	private synchronized PrivatePerson cutSuccessorEntriesToFirstPrivatePerson(Transaction transaction) throws NoPrivateDataForAuthorException
	{
		List<SuccessorEntry> successorEntries = getSuccessorEntries();
		Person author = getAuthor(transaction);
		if (author instanceof PrivatePerson)
		{
			successorEntries.clear();
			return (PrivatePerson) author;
		}
		ListIterator<SuccessorEntry> listIterator = successorEntries.listIterator();
		while (listIterator.hasNext())
		{
			SuccessorEntry successorEntry = listIterator.next();
			Person successor = successorEntry.getSuccessor(transaction);
			if (successor instanceof PrivatePerson)
			{
				while (listIterator.hasNext())
				{
					listIterator.next();
					listIterator.remove();
				}
				return (PrivatePerson) successor;
			}
		}
		throw new NoPrivateDataForAuthorException();
	}

	private synchronized PrivatePerson cutSuccessorEntriesToLastPrivatePerson(Transaction transaction) throws NoPrivateDataForAuthorException
	{
		List<SuccessorEntry> successorEntries = getSuccessorEntries();
		ListIterator<SuccessorEntry> listIterator = successorEntries.listIterator(successorEntries.size());
		while (listIterator.hasPrevious())
		{
			SuccessorEntry successorEntry = listIterator.previous();
			Person successor = successorEntry.getSuccessor(transaction);
			if (successor instanceof PrivatePerson)
				return (PrivatePerson) successor;
			listIterator.remove();
		}
		Person author = getAuthor(transaction);
		if (author instanceof PrivatePerson)
			return (PrivatePerson) author;
		throw new NoPrivateDataForAuthorException();
	}

	private void updateMessageDigestDataTree(Transaction transaction) throws SignatureVersionException
	{
		Stack<DelegateTreeNode> stack = new Stack<>();
		stack.addAll(localDelegateTreeSubNodeMap(transaction).values());
		Stack<DelegateTreeNode> stack2 = new Stack<>();
		while (!stack.isEmpty())
		{
			DelegateTreeNode node = stack.pop();
			stack.addAll(node.localDelegateTreeSubNodeMap(transaction).values());
			stack2.push(node);
		}
		while (!stack2.isEmpty())
		{
			DelegateTreeNode node = stack2.pop();
			node.updateMessageDigestData(transaction);
			node.persistenceUpdate(transaction);
		}
		updateMessageDigestData(transaction);
	}

	private synchronized void changeSignatureVersion(Transaction transaction, int signatureVersion) throws SignatureVersionException
	{
		if (getSignatureVersion() != signatureVersion)
		{
			setSignatureVersion(signatureVersion);
			persistenceUpdate(transaction);
			updateMessageDigestDataTree(transaction);
		}
	}

	@Override
	protected synchronized void changeSignatureVersion(Transaction transaction)
	{
		try
		{
			changeSignatureVersion(transaction, signingSignatureVersion);
		}
		catch (SignatureVersionException e)
		{
			throw new Error("signingSignatureVersion must be supported", e);
		}
	}

	private synchronized void sign(Transaction transaction) throws NoPrivateDataForAuthorException
	{
		try
		{
			Signer signer = cutSuccessorEntriesToLastPrivatePerson(transaction).getPrivateSignatory(transaction).signer();
			setSuccessorIndex(getSuccessorEntries().size() - 1);
			setSignatureDate();
			changeSignatureVersion(transaction);
			try
			{
				updateMessageDigestData(transaction);
				signatureDataOut(signer.dataOutput());
			}
			catch (SignatureVersionException e)
			{
				throw new Error("signingSignatureVersion must be supported", e);
			}
			setSignatureData(signer.sign());
		}
		catch (PrivateSignatoryException e)
		{
			throw new NoPrivateDataForAuthorException(e);
		}
		catch (InvalidKeyException e)
		{
			throw new RuntimeException(e);
		}

	}

	public void verify(Transaction transaction) throws SignatureVerifyException, SignatureVersionException
	{
		try
		{
			Signatory signatory;
			int successorIndex = getSuccessorIndex();
			if (successorIndex < 0)
				signatory = getAuthor(transaction).getSignatory(transaction);
			else
				signatory = getSuccessorEntries().get(successorIndex).getSuccessor(transaction).getSignatory(transaction);
			Verifier verifier = signatory.verifier(getSignatureData());
			signatureDataOut(verifier.dataOutput());
			if (!verifier.verify())
				throw new SignatureVerifyException();
		}
		catch (InvalidKeyException | IncompleteDataSignatureException | NoSuchAlgorithmException e)
		{
			throw new SignatureVerifyException(e);
		}
		finally
		{

		}
	}

	public boolean isSigned()
	{
		return getSignatureData() != null;
	}

	public CloseableIterable<DelegateTreeSubNode> delegateTreeSubNodesRecursive(final Transaction transaction)
	{
		return new CloseableIterable<DelegateTreeSubNode>()
		{

			@Override
			public CloseableIterator<DelegateTreeSubNode> iterator()
			{
				final Stack<CloseableIterator<DelegateTreeSubNode>> stack = new Stack<>();
				stack.push(localDelegateTreeSubNodeMap(transaction).values().iterator());
				return new CloseableIterator<DelegateTreeSubNode>()
				{
					private DelegateTreeSubNode next = obtainNext();

					private DelegateTreeSubNode obtainNext()
					{
						while (!stack.isEmpty() && !stack.peek().hasNext())
							stack.pop();
						if (stack.isEmpty())
							return null;
						else
							return stack.peek().next();
					}

					@Override
					public boolean hasNext()
					{
						return next != null;
					}

					@Override
					public DelegateTreeSubNode next()
					{
						if (next == null)
							throw new NoSuchElementException();
						DelegateTreeSubNode next_ = next;
						next = obtainNext();
						return next_;
					}

					@Override
					public void remove()
					{
						throw new UnsupportedOperationException();
					}

					@Override
					public void close()
					{
						while (!stack.isEmpty())
							stack.pop().close();
					}

					@Override
					protected void finalize() throws Throwable
					{
						close();
						super.finalize();
					}
				};
			}

		};
	}

	public CloseableIterable<DelegateTreeNode> delegateTreeNodesRecursive(final Transaction transaction)
	{
		return new CombinedCloseableIterable<>(new TrivialCloseableIterable<>(Collections.<DelegateTreeNode> singleton(this)),
				new AdaptedCloseableIterable<DelegateTreeNode>(delegateTreeSubNodesRecursive(transaction)));
	}

	public CloseableIterable<DelegateAuthorizer> delegateAuthorizersRecursive(final Transaction transaction)
	{
		return new UnionCloseableIterable<>(new BijectionCloseableIterable<>(
				new Bijection<DelegateTreeNode, CloseableCollection<DelegateAuthorizer>>()
				{

					@Override
					public CloseableCollection<DelegateAuthorizer> forward(DelegateTreeNode delegateTreeNode)
					{
						return delegateTreeNode.localDelegateAuthorizerMap(transaction).values();
					}

					@Override
					public DelegateTreeSubNode backward(CloseableCollection<DelegateAuthorizer> output)
					{
						throw new UnsupportedOperationException();
					}
				}, delegateTreeNodesRecursive(transaction)));
	}

	private void checkAuthoritySignatures(Transaction transaction)
	{
		for (DelegateAuthorizer da : delegateAuthorizersRecursive(transaction))
			da.checkAuthoritySignatures(transaction);
	}

	private void notifyListenersDelegateTreeChanged(Transaction transaction)
	{
		Iterable<StateListener> listeners = stateListeners();
		synchronized (listeners)
		{
			StatementAuthority statementAuthority = null;
			boolean first = true;
			for (StateListener l : listeners)
			{
				if (first)
				{
					statementAuthority = getStatementAuthority(transaction);
					first = false;
				}
				l.delegateTreeChanged(transaction, statementAuthority);
			}
		}
	}

	private void notifyListenersSuccessorEntriesChanged(Transaction transaction)
	{
		Iterable<StateListener> listeners = stateListeners();
		synchronized (listeners)
		{
			StatementAuthority statementAuthority = null;
			boolean first = true;
			for (StateListener l : listeners)
			{
				if (first)
				{
					statementAuthority = getStatementAuthority(transaction);
					first = false;
				}
				l.successorEntriesChanged(transaction, statementAuthority);
			}
		}
	}

	public void cutSuccessorEntries(Transaction transaction) throws NoPrivateDataForAuthorException
	{
		cutSuccessorEntriesToFirstPrivatePerson(transaction);
		sign(transaction);
		persistenceUpdate(transaction);
		notifyListenersSuccessorEntriesChanged(transaction);
	}

	private synchronized void checkSignatureDate(Transaction transaction) throws DateConsistenceException
	{
		Date lastDate;
		ListIterator<SuccessorEntry> listIterator;
		if (getSuccessorIndex() < 0)
		{
			lastDate = getStatementAuthority(transaction).getCreationDate();
			listIterator = getSuccessorEntries().listIterator();
		}
		else
		{
			listIterator = getSuccessorEntries().listIterator(getSuccessorIndex());
			lastDate = listIterator.next().getSignatureDate();
		}
		if (lastDate.compareTo(getSignatureDate()) >= 0)
			throw new DateConsistenceException();
		if (listIterator.hasNext())
		{
			Date nextDate = listIterator.next().getSignatureDate();
			if (getSignatureDate().compareTo(nextDate) >= 0)
				throw new DateConsistenceException();
		}

	}

	@Override
	public synchronized void persistenceUpdateSign(Transaction transaction) throws NoPrivateDataForAuthorException, DateConsistenceException
	{
		sign(transaction);
		checkSignatureDate(transaction);
		persistenceUpdate(transaction);
		checkAuthoritySignatures(transaction);
		notifyListenersDelegateTreeChanged(transaction);
	}

	private synchronized boolean cleanSuccessorEntries()
	{
		boolean cleaned = false;
		ListIterator<SuccessorEntry> listIterator;
		if (getSuccessorIndex() < 0)
			listIterator = getSuccessorEntries().listIterator();
		else
			listIterator = getSuccessorEntries().listIterator(getSuccessorIndex() + 1);
		while (listIterator.hasNext())
		{
			SuccessorEntry successorEntry = listIterator.next();
			if (successorEntry.getSignatureDate().compareTo(getSignatureDate()) < 0)
			{
				listIterator.remove();
				cleaned = true;
			}
		}
		return cleaned;
	}

	public boolean cleanSuccessorEntries(Transaction transaction)
	{
		boolean cleaned = cleanSuccessorEntries();
		if (cleaned)
		{
			persistenceUpdate(transaction);
			notifyListenersSuccessorEntriesChanged(transaction);
		}
		return cleaned;
	}

	public synchronized boolean update(Transaction transaction, int successorIndex, Date signatureDate, int signatureVersion, SignatureData signatureData)
			throws SignatureVerifyException, DateConsistenceException, SignatureVersionException
	{
		if (successorIndex != getSuccessorIndex() || !signatureDate.equals(getSignatureDate()) || signatureVersion != getSignatureVersion()
				|| !signatureData.equals(getSignatureData()))
		{
			setSuccessorIndex(successorIndex);
			setSignatureDate(signatureDate);
			changeSignatureVersion(transaction, signatureVersion);
			setSignatureData(signatureData);
			boolean cleaned = cleanSuccessorEntries();
			checkSignatureDate(transaction);
			verify(transaction);
			persistenceUpdate(transaction);
			if (cleaned)
				notifyListenersSuccessorEntriesChanged(transaction);
			checkAuthoritySignatures(transaction);
			notifyListenersDelegateTreeChanged(transaction);
			return true;
		}
		return false;
	}

	@Override
	public void delete(Transaction transaction)
	{
		getStatementAuthority(transaction).deleteDelegateTree(transaction);
	}

	private synchronized void checkSuccessorOrphanityInsert(Transaction transaction)
	{
		Collection<UUID> successorUuids = successorUuids();
		if (oldSuccessorUuids != null)
			successorUuids = new CombinedCollection<>(new DifferenceCollection<>(successorUuids, oldSuccessorUuids),
					new DifferenceCollection<>(oldSuccessorUuids, successorUuids));
		for (UUID successorUuid : successorUuids)
		{
			Person successor = getPersistenceManager().getPerson(transaction, successorUuid);
			if (successor != null)
				successor.checkOrphanity(transaction);
		}
		oldSuccessorUuids = new HashSet<>(successorUuids());
	}

	private synchronized void checkSuccessorOrphanityDelete(Transaction transaction)
	{
		if (oldSuccessorUuids != null)
		{
			for (UUID successorUuid : oldSuccessorUuids)
			{
				Person successor = getPersistenceManager().getPerson(transaction, successorUuid);
				if (successor != null)
					successor.checkOrphanity(transaction);
			}
		}
		oldSuccessorUuids = null;
	}

	@Override
	protected void persistenceUpdate(Transaction transaction)
	{
		super.persistenceUpdate(transaction);
		checkSuccessorOrphanityInsert(transaction);
	}

	@Override
	protected void deleteNoSign(Transaction transaction)
	{
		super.deleteNoSign(transaction);
		checkSuccessorOrphanityDelete(transaction);
	}

	public CloseableIterable<Person> delegatesRecursive(final Transaction transaction)
	{
		return new BijectionCloseableIterable<>(new Bijection<DelegateAuthorizer, Person>()
		{

			@Override
			public Person forward(DelegateAuthorizer delegateAuthorizer)
			{
				return delegateAuthorizer.getDelegate(transaction);
			}

			@Override
			public DelegateAuthorizer backward(Person delegate)
			{
				throw new UnsupportedOperationException();
			}
		}, delegateAuthorizersRecursive(transaction));
	}

	public List<Person> successors(final Transaction transaction)
	{
		return new BijectionList<>(new Bijection<SuccessorEntry, Person>()
		{

			@Override
			public Person forward(SuccessorEntry successorEntry)
			{
				return successorEntry.getSuccessor(transaction);
			}

			@Override
			public SuccessorEntry backward(Person successor)
			{
				throw new UnsupportedOperationException();
			}
		}, successorEntries());
	}

	public CloseableIterable<Person> personDependencies(Transaction transaction)
	{
		return new CombinedCloseableIterable<>(new TrivialCloseableIterable<>(successors(transaction)), delegatesRecursive(transaction));
	}

}
