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
package aletheia.model.misc;

import aletheia.persistence.PersistenceListener;
import aletheia.persistence.PersistenceListenerManager.Listeners;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.entities.misc.PersistenceSecretKeySingletonEntity;

public class PersistenceSecretKeySingleton
{
	public interface StateListener extends PersistenceListener
	{
		public void persistenceSecretKeySingletonInserted(Transaction transaction, PersistenceSecretKeySingleton persistenceSecretKeySingleton);

		public void persistenceSecretKeySingletonDeleted(Transaction transaction, PersistenceSecretKeySingleton persistenceSecretKeySingleton);

	}

	public static class PersistenceSecretKeySingletonException extends Exception
	{
		private static final long serialVersionUID = -312555166850561985L;

	}

	public static PersistenceSecretKeySingleton lock(PersistenceManager persistenceManager, Transaction transaction)
	{
		boolean locked = persistenceManager.lockPersistenceSecretKeySingleton(transaction);
		if (locked)
			return persistenceManager.getPersistenceSecretKeySingleton(transaction);
		else
			return null;
	}

	public static class PersistenceSecretKeySingletonCollisionException extends PersistenceSecretKeySingletonException
	{
		private static final long serialVersionUID = -668229887028800191L;

	}

	public static PersistenceSecretKeySingleton create(PersistenceManager persistenceManager, Transaction transaction, byte[] salt, int verificationVersion,
			byte[] verification) throws PersistenceSecretKeySingletonCollisionException
	{
		if (persistenceManager.lockPersistenceSecretKeySingleton(transaction))
			throw new PersistenceSecretKeySingletonCollisionException();
		PersistenceSecretKeySingleton persistenceSecretKeySingleton = new PersistenceSecretKeySingleton(persistenceManager, salt, verificationVersion,
				verification);
		persistenceSecretKeySingleton.persistenceUpdate(transaction);
		Listeners<StateListener> listeners = persistenceManager.getListenerManager().getPersistenceSecretKeySingletonStateListeners();
		synchronized (listeners)
		{
			for (StateListener l : listeners)
				l.persistenceSecretKeySingletonInserted(transaction, persistenceSecretKeySingleton);
		}
		return persistenceSecretKeySingleton;
	}

	private final PersistenceManager persistenceManager;
	private final PersistenceSecretKeySingletonEntity entity;

	public PersistenceSecretKeySingleton(PersistenceManager persistenceManager, PersistenceSecretKeySingletonEntity entity)
	{
		this.persistenceManager = persistenceManager;
		this.entity = entity;
	}

	private PersistenceSecretKeySingleton(PersistenceManager persistenceManager, byte[] salt, int verificationVersion, byte[] verification)
	{
		this.persistenceManager = persistenceManager;
		this.entity = persistenceManager.instantiatePersistenceSecretKeySingletonEntity(PersistenceSecretKeySingletonEntity.class);
		this.entity.setSalt(salt);
		this.entity.setVerificationVersion(verificationVersion);
		this.entity.setVerification(verification);
	}

	public PersistenceSecretKeySingletonEntity getEntity()
	{
		return entity;
	}

	public byte[] getSalt()
	{
		return getEntity().getSalt();
	}

	public int getVerificationVersion()
	{
		return getEntity().getVerificationVersion();
	}

	public byte[] getVerification()
	{
		return getEntity().getVerification();
	}

	private void persistenceUpdate(Transaction transaction)
	{
		persistenceManager.putPersistenceSecretKeySingleton(transaction, this);

	}

	public void delete(Transaction transaction)
	{
		persistenceManager.deletePersistenceSecretKeySingleton(transaction);
		Listeners<StateListener> listeners = persistenceManager.getListenerManager().getPersistenceSecretKeySingletonStateListeners();
		synchronized (listeners)
		{
			for (StateListener l : listeners)
				l.persistenceSecretKeySingletonInserted(transaction, this);
		}
	}

}
