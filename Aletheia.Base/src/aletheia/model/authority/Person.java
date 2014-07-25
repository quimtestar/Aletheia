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
import java.util.Date;
import java.util.UUID;

import aletheia.model.security.SignatureData;
import aletheia.persistence.PersistenceListener;
import aletheia.persistence.PersistenceListenerManager.Listeners;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.collections.authority.DelegateAuthorizerSetByDelegate;
import aletheia.persistence.collections.authority.DelegateTreeRootNodeSetBySuccessor;
import aletheia.persistence.collections.authority.StatementAuthoritySetByAuthor;
import aletheia.persistence.entities.authority.PersonEntity;
import aletheia.protocol.Exportable;
import aletheia.protocol.primitive.DateProtocol;
import aletheia.protocol.primitive.NullableProtocol;
import aletheia.protocol.primitive.StringProtocol;
import aletheia.security.signerverifier.Verifier;

public class Person implements Exportable
{
	private final PersistenceManager persistenceManager;
	private final PersonEntity entity;

	public Person(PersistenceManager persistenceManager, PersonEntity entity)
	{
		this.persistenceManager = persistenceManager;
		this.entity = entity;
	}

	protected Person(PersistenceManager persistenceManager, Class<? extends PersonEntity> entityClass, UUID uuid, String nick)
	{
		this.persistenceManager = persistenceManager;
		this.entity = persistenceManager.instantiatePersonEntity(entityClass);
		this.entity.setUuid(uuid);
		if (nick == null)
			throw new IllegalArgumentException();
		this.entity.setNick(nick);
		this.entity.setSignatureVersion(-1);
	}

	private Person(PersistenceManager persistenceManager, UUID uuid, String nick)
	{
		this(persistenceManager, PersonEntity.class, uuid, nick);
	}

	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	public PersonEntity getEntity()
	{
		return entity;
	}

	public static abstract class PersonCreationException extends AuthorityException
	{

		private static final long serialVersionUID = -1109184992559746355L;

		public PersonCreationException()
		{
			super();
		}

		public PersonCreationException(Throwable cause)
		{
			super(cause);
		}

	}

	public static class NoSignatoryException extends PersonCreationException
	{

		private static final long serialVersionUID = 516941393830476230L;

	}

	public static class SignatureVerifyPersonCreationException extends PersonCreationException
	{

		private static final long serialVersionUID = 7018190794886318036L;

		public SignatureVerifyPersonCreationException(SignatureVerifyException cause)
		{
			super(cause);
		}
	}

	public static class NickCollisionException extends PersonCreationException
	{

		private static final long serialVersionUID = -7149744962633728653L;

	}

	protected static <P extends Person> P create(PersistenceManager persistenceManager, Transaction transaction, P person, String name, String email,
			Date signatureDate, int signatureVersion, SignatureData signatureData) throws PersonCreationException
	{
		person.setName(name);
		person.setEmail(email);
		person.setSignatureDate(signatureDate);
		person.setSignatureVersion(signatureVersion);
		person.setSignatureData(signatureData);
		try
		{
			person.verify(transaction);
		}
		catch (SignatureVerifyException e)
		{
			throw new SignatureVerifyPersonCreationException(e);
		}
		person.updateOrphanSince(transaction);
		person.persistenceUpdate(transaction);
		Listeners<AddStateListener> listeners = persistenceManager.getListenerManager().getPersonAddStateListeners();
		synchronized (listeners)
		{
			for (AddStateListener l : listeners)
				l.personAdded(transaction, person);
		}
		return person;
	}

	public static Person create(PersistenceManager persistenceManager, Transaction transaction, Signatory signatory, String nick, String name, String email,
			Date signatureDate, int signatureVersion, SignatureData signatureData) throws PersonCreationException
	{
		if (signatory instanceof PrivateSignatory)
			return PrivatePerson.create(persistenceManager, transaction, (PrivateSignatory) signatory, nick, name, email, signatureDate, signatureVersion,
					signatureData);
		else
			return create(persistenceManager, transaction, new Person(persistenceManager, signatory.getUuid(), nick), name, email, signatureDate,
					signatureVersion, signatureData);
	}

	public PrivatePerson toPrivatePerson(Transaction transaction, PrivateSignatory signatory) throws PersonCreationException
	{
		if (this instanceof PrivatePerson)
			return (PrivatePerson) this;
		else
			return PrivatePerson.create(persistenceManager, transaction, signatory, getNick(), getName(), getEmail(), getSignatureDate(),
					getSignatureVersion(), getSignatureData());
	}

	public UUID getUuid()
	{
		return entity.getUuid();
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
		Person other = (Person) obj;
		if (!getUuid().equals(other.getUuid()))
			return false;
		return true;
	}

	public String getNick()
	{
		return getEntity().getNick();
	}

	protected void setNick(String nick)
	{
		if (nick == null)
			throw new IllegalArgumentException();
		getEntity().setNick(nick);
		clearSignature();
	}

	public String getName()
	{
		return getEntity().getName();
	}

	protected void setName(String name)
	{
		getEntity().setName(name);
		clearSignature();
	}

	public String getEmail()
	{
		return getEntity().getEmail();
	}

	protected void setEmail(String email)
	{
		getEntity().setEmail(email);
		clearSignature();
	}

	public Date getSignatureDate()
	{
		return getEntity().getSignatureDate();
	}

	protected void setSignatureDate(Date signatureDate)
	{
		getEntity().setSignatureDate(signatureDate);
	}

	public int getSignatureVersion()
	{
		return getEntity().getSignatureVersion();
	}

	protected void setSignatureVersion(int signatureVersion)
	{
		getEntity().setSignatureVersion(signatureVersion);
	}

	public SignatureData getSignatureData()
	{
		return getEntity().getSignatureData();
	}

	protected void setSignatureData(SignatureData signatureData)
	{
		getEntity().setSignatureData(signatureData);
	}

	public Date getOrphanSince()
	{
		return getEntity().getOrphanSince();
	}

	protected void setOrphanSince(Date orphanSince)
	{
		getEntity().setOrphanSince(orphanSince);
	}

	public boolean isOrphan()
	{
		return getOrphanSince() != null;
	}

	public void persistenceUpdate(Transaction transaction)
	{
		persistenceManager.putPerson(transaction, this);
		Iterable<StateListener> listeners = getStateListeners();
		synchronized (listeners)
		{
			for (StateListener l : listeners)
				l.personModified(transaction, this);
		}
	}

	public Signatory getSignatory(Transaction transaction)
	{
		return persistenceManager.getSignatory(transaction, getUuid());
	}

	protected void signatureDataOut(DataOutput out) throws IncompleteDataSignatureException
	{
		if (getSignatureVersion() != 0)
			throw new RuntimeException();
		try
		{
			StringProtocol stringProtocol = new StringProtocol(0);
			NullableProtocol<String> nullableStringProtocol = new NullableProtocol<>(0, stringProtocol);
			if (getNick() == null)
				throw new IncompleteDataSignatureException();
			stringProtocol.send(out, getNick());
			nullableStringProtocol.send(out, getName());
			nullableStringProtocol.send(out, getEmail());
			DateProtocol dateProtocol = new DateProtocol(0);
			if (getSignatureDate() == null)
				throw new IncompleteDataSignatureException();
			dateProtocol.send(out, getSignatureDate());
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void verify(Transaction transaction) throws SignatureVerifyException
	{
		try
		{
			Signatory signatory = getSignatory(transaction);
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
		return getSignatureDate() != null && getSignatureData() != null;
	}

	private void clearSignature()
	{
		setSignatureDate(null);
		setSignatureData(null);
	}

	@Override
	public String toString()
	{
		Transaction transaction = persistenceManager.beginDirtyTransaction();
		try
		{
			return toString(transaction);
		}
		finally
		{
			transaction.abort();
		}
	}

	public String toString(Transaction transaction)
	{
		Signatory signatory = getSignatory(transaction);
		if (signatory == null)
			return "*null*";
		return signatory.toString() + ": " + getNick() + ": " + getEmail() + ": " + getName();
	}

	public void delete(Transaction transaction)
	{
		persistenceManager.deletePerson(transaction, this);
		Signatory signatory = getSignatory(transaction);
		if (signatory != null)
			signatory.deleteIfOrphan(transaction);
		Iterable<StateListener> listeners = clearStateListeners();
		synchronized (listeners)
		{
			for (StateListener l : listeners)
				l.personRemoved(transaction, this);
		}
	}

	public interface AddStateListener extends PersistenceListener
	{
		public void personAdded(Transaction transaction, Person person);
	}

	public interface StateListener extends PersistenceListener
	{
		public void personModified(Transaction transaction, Person person);

		public void personRemoved(Transaction transaction, Person person);
	}

	public Iterable<StateListener> getStateListeners()
	{
		return persistenceManager.getListenerManager().getPersonStateListeners().iterable(getUuid());
	}

	public void addStateListener(StateListener listener)
	{
		persistenceManager.getListenerManager().getPersonStateListeners().add(getUuid(), listener);
	}

	public void removeStateListener(StateListener listener)
	{
		persistenceManager.getListenerManager().getPersonStateListeners().remove(getUuid(), listener);
	}

	protected Iterable<StateListener> clearStateListeners()
	{
		return persistenceManager.getListenerManager().getPersonStateListeners().clear(getUuid());
	}

	public Person refresh(Transaction transaction)
	{
		return persistenceManager.getPerson(transaction, getUuid());
	}

	public StatementAuthoritySetByAuthor statementAuthoritySetByAuthor(Transaction transaction)
	{
		return persistenceManager.statementAuthoritySetByAuthor(transaction, this);
	}

	public DelegateAuthorizerSetByDelegate delegateAuthorizerSetByDelegate(Transaction transaction)
	{
		return persistenceManager.delegateAuthorizerSetByDelegate(transaction, this);
	}

	public DelegateTreeRootNodeSetBySuccessor delegateTreeRootNodeSetBySuccessor(Transaction transaction)
	{
		return persistenceManager.delegateTreeRootNodeSetBySuccessor(transaction, this);
	}

	public void update(Transaction transaction, String nick, String name, String email, Date signatureDate, int signatureVersion, SignatureData signatureData)
			throws SignatureVerifyException
	{
		setNick(nick);
		setName(name);
		setEmail(email);
		setSignatureDate(signatureDate);
		setSignatureVersion(signatureVersion);
		setSignatureData(signatureData);
		if (signatureData != null)
			verify(transaction);
		persistenceUpdate(transaction);
	}

	private boolean orphan(Transaction transaction)
	{
		return statementAuthoritySetByAuthor(transaction).isEmpty() && delegateAuthorizerSetByDelegate(transaction).isEmpty()
				&& delegateTreeRootNodeSetBySuccessor(transaction).isEmpty();
	}

	protected synchronized boolean updateOrphanSince(Transaction transaction)
	{
		if (orphan(transaction))
		{
			if (getOrphanSince() == null)
			{
				setOrphanSince(new Date());
				return true;
			}
			else
				return false;
		}
		else
		{
			if (getOrphanSince() != null)
			{
				setOrphanSince(null);
				return true;
			}
			else
				return false;
		}
	}

	public synchronized void checkOrphanity(Transaction transaction)
	{
		if (updateOrphanSince(transaction))
			persistenceUpdate(transaction);
	}

	private static final int orphanityMaxAge = 7 * 24 * 60 * 60 * 1000; //millis;

	public static void deleteOldNonPrivateOrphans(PersistenceManager persistenceManager, Transaction transaction)
	{
		for (Person person : persistenceManager.personsOrphanSinceSortedSet(transaction).olderThanSet(new Date(System.currentTimeMillis() - orphanityMaxAge)))
			if (!(person instanceof PrivatePerson))
				person.delete(transaction);
	}

}
