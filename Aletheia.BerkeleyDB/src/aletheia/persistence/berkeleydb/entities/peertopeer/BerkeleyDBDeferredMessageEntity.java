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
package aletheia.persistence.berkeleydb.entities.peertopeer;

import java.util.Date;
import java.util.UUID;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import aletheia.model.peertopeer.deferredmessagecontent.DeferredMessageContent;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.entities.peertopeer.DeferredMessageEntity;

@Entity
public class BerkeleyDBDeferredMessageEntity implements DeferredMessageEntity
{
	@PrimaryKey
	private UUIDKey uuidKey;

	private UUIDKey recipientUuidKey;

	private Date date;

	private DeferredMessageContent content;

	public BerkeleyDBDeferredMessageEntity()
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
	public UUID getRecipientUuid()
	{
		return recipientUuidKey.uuid();
	}

	@Override
	public void setRecipientUuid(UUID recipientUuid)
	{
		this.recipientUuidKey = new UUIDKey(recipientUuid);
	}

	@Override
	public Date getDate()
	{
		return date;
	}

	@Override
	public void setDate(Date date)
	{
		this.date = date;
	}

	@Override
	public DeferredMessageContent getContent()
	{
		return content;
	}

	@Override
	public void setContent(DeferredMessageContent content)
	{
		this.content = content;
	}

}
