/*******************************************************************************
 * Copyright (c) 2014, 2020 Quim Testar.
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
package aletheia.persistence.berkeleydb.entities.peertopeer;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.UUID;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.entities.peertopeer.HookEntity;

@Entity(version = 0)
public class BerkeleyDBHookEntity implements HookEntity
{
	@PrimaryKey
	private UUIDKey uuidKey;

	private InetSocketAddress inetSocketAddress;

	public final static String priority_FieldName = "priority";

	@SecondaryKey(name = priority_FieldName, relate = Relationship.MANY_TO_ONE)
	private long priority;

	private Date lastSuccessfulConnectionDate;
	private int failedConnectionAttempts;

	public BerkeleyDBHookEntity()
	{
	}

	@Override
	public UUID getUuid()
	{
		return uuidKey.uuid();
	}

	@Override
	public void setUuid(UUID uuid)
	{
		this.uuidKey = new UUIDKey(uuid);
	}

	@Override
	public InetSocketAddress getInetSocketAddress()
	{
		return inetSocketAddress;
	}

	@Override
	public void setInetSocketAddress(InetSocketAddress inetSocketAddress)
	{
		this.inetSocketAddress = inetSocketAddress;

	}

	@Override
	public long getPriority()
	{
		return priority;
	}

	@Override
	public void setPriority(long priority)
	{
		this.priority = priority;
	}

	@Override
	public Date getLastSuccessfulConnectionDate()
	{
		return lastSuccessfulConnectionDate;
	}

	@Override
	public void setLastSuccessfulConnectionDate(Date lastSuccessfulConnectionDate)
	{
		this.lastSuccessfulConnectionDate = lastSuccessfulConnectionDate;
	}

	@Override
	public int getFailedConnectionAttempts()
	{
		return failedConnectionAttempts;
	}

	@Override
	public void setFailedConnectionAttempts(int failedConnectionAttempts)
	{
		this.failedConnectionAttempts = failedConnectionAttempts;
	}

}
