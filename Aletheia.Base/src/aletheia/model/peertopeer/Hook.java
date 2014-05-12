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
package aletheia.model.peertopeer;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.UUID;

import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.entities.peertopeer.HookEntity;
import aletheia.protocol.Exportable;
import aletheia.protocol.net.InetSocketAddressProtocol;
import aletheia.security.utilities.SecurityUtilities;

public class Hook implements Exportable
{
	private final PersistenceManager persistenceManager;
	private final HookEntity entity;

	public Hook(PersistenceManager persistenceManager, HookEntity entity)
	{
		this.persistenceManager = persistenceManager;
		this.entity = entity;
	}

	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	public HookEntity getEntity()
	{
		return entity;
	}

	private static UUID inetSocketAddressToUuid(InetSocketAddress inetSocketAddress)
	{
		return SecurityUtilities.instance.objectToUUID(inetSocketAddress, new InetSocketAddressProtocol(0));
	}

	protected Hook(PersistenceManager persistenceManager, InetSocketAddress inetSocketAddress, Date lastSuccessfulConnectionDate, int failedConnectionAttempts)
	{
		this.persistenceManager = persistenceManager;
		this.entity = persistenceManager.instantiateHookEntity(HookEntity.class);
		setUuid(inetSocketAddressToUuid(inetSocketAddress));
		setInetSocketAddress(inetSocketAddress);
		setLastSuccessfulConnectionDate(lastSuccessfulConnectionDate);
		setFailedConnectionAttempts(failedConnectionAttempts);
		updatePriority();
	}

	private static Hook create(PersistenceManager persistenceManager, Transaction transaction, InetSocketAddress inetSocketAddress,
			Date lastSuccessfulConnectionDate, int failedConnectionAttempts)
	{
		Hook hook = new Hook(persistenceManager, inetSocketAddress, lastSuccessfulConnectionDate, failedConnectionAttempts);
		hook.persistenceUpdate(transaction);
		return hook;
	}

	public static Hook create(PersistenceManager persistenceManager, Transaction transaction, InetSocketAddress inetSocketAddress)
	{
		return create(persistenceManager, transaction, inetSocketAddress, new Date(), 0);
	}

	public UUID getUuid()
	{
		return entity.getUuid();
	}

	protected void setUuid(UUID uuid)
	{
		entity.setUuid(uuid);
	}

	public InetSocketAddress getInetSocketAddress()
	{
		return entity.getInetSocketAddress();
	}

	protected void setInetSocketAddress(InetSocketAddress inetSocketAddress)
	{
		entity.setInetSocketAddress(inetSocketAddress);
	}

	public long getPriority()
	{
		return entity.getPriority();
	}

	protected void setPriority(long priority)
	{
		entity.setPriority(priority);
	}

	public Date getLastSuccessfulConnectionDate()
	{
		return entity.getLastSuccessfulConnectionDate();
	}

	protected void setLastSuccessfulConnectionDate(Date lastSuccessfulConnectionDate)
	{
		entity.setLastSuccessfulConnectionDate(lastSuccessfulConnectionDate);
	}

	public int getFailedConnectionAttempts()
	{
		return entity.getFailedConnectionAttempts();
	}

	protected void setFailedConnectionAttempts(int failedConnectionAttempts)
	{
		entity.setFailedConnectionAttempts(failedConnectionAttempts);
	}

	public static long computePriority(Date date, int failedConnectionAttempts)
	{
		return -date.getTime() + failedConnectionAttempts * 1000 * 60 * 60 * 24;
	}

	protected long computePriority()
	{
		return computePriority(getLastSuccessfulConnectionDate(), getFailedConnectionAttempts());
	}

	protected void updatePriority()
	{
		setPriority(computePriority());
	}

	protected void persistenceUpdate(Transaction transaction)
	{
		persistenceManager.putHook(transaction, this);
	}

	public void delete(Transaction transaction)
	{
		persistenceManager.deleteHook(transaction, this);
	}

	public void failedConnection(Transaction transaction)
	{
		setFailedConnectionAttempts(getFailedConnectionAttempts() + 1);
		updatePriority();
		persistenceUpdate(transaction);
	}

	public void successfulConnection(Transaction transaction)
	{
		setLastSuccessfulConnectionDate(new Date());
		setFailedConnectionAttempts(0);
		updatePriority();
		persistenceUpdate(transaction);
	}

	@Override
	public String toString()
	{
		return getInetSocketAddress() + " [priority: " + getPriority() + "]" + " [lastSuccessfulConnectionDate: " + getLastSuccessfulConnectionDate() + "]"
				+ " [failedConnectionAttempts: " + getFailedConnectionAttempts() + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + getUuid().hashCode();
		result = prime * result + getInetSocketAddress().hashCode();
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
		Hook other = (Hook) obj;
		if (!getUuid().equals(other.getUuid()))
			return false;
		if (!getInetSocketAddress().equals(other.getInetSocketAddress()))
			return false;
		return true;
	}

}
