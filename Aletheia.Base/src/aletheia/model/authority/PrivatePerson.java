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
package aletheia.model.authority;

import java.security.InvalidKeyException;
import java.util.Date;
import java.util.UUID;

import aletheia.persistence.PersistenceListenerManager.Listeners;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.entities.authority.PrivatePersonEntity;
import aletheia.security.model.SignatureData;
import aletheia.security.signerverifier.Signer;

public class PrivatePerson extends Person
{
	private final static int signingSignatureVersion = 0;

	public PrivatePerson(PersistenceManager persistenceManager, PrivatePersonEntity entity)
	{
		super(persistenceManager, entity);
	}

	protected PrivatePerson(PersistenceManager persistenceManager, UUID uuid, String nick)
	{
		super(persistenceManager, PrivatePersonEntity.class, uuid, nick);
	}

	public static PrivatePerson create(PersistenceManager persistenceManager, Transaction transaction, String nick)
	{
		PrivateSignatory signatory = PrivateSignatory.create(persistenceManager, transaction);
		PrivatePerson person = new PrivatePerson(persistenceManager, signatory.getUuid(), nick);
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

	public static PrivatePerson create(PersistenceManager persistenceManager, Transaction transaction, PrivateSignatory signatory, String nick, String name,
			String email, Date signatureDate, int signatureVersion, SignatureData signatureData) throws PersonCreationException
	{
		PrivatePerson old = persistenceManager.privatePersonsByNick(transaction).get(nick);
		if (old != null && !signatory.getUuid().equals(old.getUuid()))
			throw new NickCollisionException();
		return create(persistenceManager, transaction, new PrivatePerson(persistenceManager, signatory.getUuid(), nick), name, email, signatureDate,
				signatureVersion, signatureData);
	}

	@Override
	public void setNick(String nick)
	{
		super.setNick(nick);
	}

	@Override
	public void setName(String name)
	{
		super.setName(name);
	}

	@Override
	public void setEmail(String email)
	{
		super.setEmail(email);
	}

	public static class NoPrivateSignatoryException extends PersonCreationException
	{

		private static final long serialVersionUID = 6608776370601231512L;

	}

	public static PrivatePerson fromPerson(PersistenceManager persistenceManager, Transaction transaction, Person person) throws PersonCreationException
	{
		Signatory signatory = person.getSignatory(transaction);
		if (!(signatory instanceof PrivateSignatory))
			throw new NoPrivateSignatoryException();
		PrivateSignatory privateSignatory = (PrivateSignatory) signatory;
		return create(persistenceManager, transaction, privateSignatory, person.getNick(), person.getName(), person.getEmail(), person.getSignatureDate(),
				person.getSignatureVersion(), person.getSignatureData());
	}

	@Override
	public PrivatePersonEntity getEntity()
	{
		return (PrivatePersonEntity) super.getEntity();
	}

	public class PrivateSignatoryException extends AuthorityException
	{
		private static final long serialVersionUID = 8903545211803412856L;

		private PrivateSignatoryException()
		{
			super("Private signatory's data not decrypted. Maybe try entering a passphrase?");
		}

	}

	public PrivateSignatory getPrivateSignatory(Transaction transaction) throws PrivateSignatoryException
	{
		Signatory signatory = getSignatory(transaction);
		if (!(signatory instanceof PrivateSignatory))
			throw new PrivateSignatoryException();
		return (PrivateSignatory) signatory;
	}

	private void setSignatureDate()
	{
		setSignatureDate(new Date());
	}

	public void sign(Transaction transaction) throws IncompleteDataSignatureException
	{
		try
		{
			PrivateSignatory signatory = getPrivateSignatory(transaction);
			Signer signer = signatory.signer();
			setSignatureDate();
			setSignatureVersion(signingSignatureVersion);
			signatureDataOut(signer.dataOutput());
			setSignatureData(signer.sign());
		}
		catch (PrivateSignatoryException e)
		{
			throw new IncompleteDataSignatureException(e);
		}
		catch (InvalidKeyException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public PrivatePerson refresh(Transaction transaction)
	{
		return (PrivatePerson) super.refresh(transaction);
	}

}
