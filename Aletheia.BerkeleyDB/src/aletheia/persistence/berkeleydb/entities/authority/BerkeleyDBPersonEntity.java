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
package aletheia.persistence.berkeleydb.entities.authority;

import java.util.Date;
import java.util.UUID;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

import aletheia.model.security.SignatureData;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.entities.authority.PersonEntity;

@Entity(version = 2)
public class BerkeleyDBPersonEntity implements PersonEntity
{
	@PrimaryKey
	private UUIDKey uuidKey;

	public static final String signatoryUuidKey_FieldName = "signatoryUuidKey";
	@SecondaryKey(name = signatoryUuidKey_FieldName, relatedEntity = BerkeleyDBSignatoryEntity.class, relate = Relationship.ONE_TO_ONE)
	private UUIDKey signatoryUuidKey;

	public static final String nick_FieldName = "nick";
	@SecondaryKey(name = nick_FieldName, relate = Relationship.MANY_TO_ONE)
	private String nick;

	private String name;

	private String email;

	private Date signatureDate;

	private int signatureVersion;

	private SignatureData signatureData;

	public static final String orphanSince_FieldName = "orphanSince";
	@SecondaryKey(name = orphanSince_FieldName, relate = Relationship.MANY_TO_ONE)
	private Date orphanSince;

	@Override
	public UUID getUuid()
	{
		return uuidKey.uuid();
	}

	@Override
	public void setUuid(UUID uuid)
	{
		uuidKey = new UUIDKey(uuid);
		signatoryUuidKey = uuidKey;
	}

	@Override
	public String getNick()
	{
		return nick;
	}

	@Override
	public void setNick(String nick)
	{
		this.nick = nick;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public String getEmail()
	{
		return email;
	}

	@Override
	public void setEmail(String email)
	{
		this.email = email;
	}

	@Override
	public Date getSignatureDate()
	{
		return signatureDate;
	}

	@Override
	public void setSignatureDate(Date signatureDate)
	{
		this.signatureDate = signatureDate;
	}

	@Override
	public int getSignatureVersion()
	{
		return signatureVersion;
	}

	@Override
	public void setSignatureVersion(int signatureVersion)
	{
		this.signatureVersion = signatureVersion;
	}

	@Override
	public SignatureData getSignatureData()
	{
		return signatureData;
	}

	@Override
	public void setSignatureData(SignatureData signatureData)
	{
		this.signatureData = signatureData;
	}

	@Override
	public Date getOrphanSince()
	{
		return orphanSince;
	}

	@Override
	public void setOrphanSince(Date orphanSince)
	{
		this.orphanSince = orphanSince;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((nick == null) ? 0 : nick.hashCode());
		result = prime * result + ((orphanSince == null) ? 0 : orphanSince.hashCode());
		result = prime * result + ((signatoryUuidKey == null) ? 0 : signatoryUuidKey.hashCode());
		result = prime * result + ((signatureData == null) ? 0 : signatureData.hashCode());
		result = prime * result + ((signatureDate == null) ? 0 : signatureDate.hashCode());
		result = prime * result + signatureVersion;
		result = prime * result + ((uuidKey == null) ? 0 : uuidKey.hashCode());
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
		BerkeleyDBPersonEntity other = (BerkeleyDBPersonEntity) obj;
		if (email == null)
		{
			if (other.email != null)
				return false;
		}
		else if (!email.equals(other.email))
			return false;
		if (name == null)
		{
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		if (nick == null)
		{
			if (other.nick != null)
				return false;
		}
		else if (!nick.equals(other.nick))
			return false;
		if (orphanSince == null)
		{
			if (other.orphanSince != null)
				return false;
		}
		else if (!orphanSince.equals(other.orphanSince))
			return false;
		if (signatoryUuidKey == null)
		{
			if (other.signatoryUuidKey != null)
				return false;
		}
		else if (!signatoryUuidKey.equals(other.signatoryUuidKey))
			return false;
		if (signatureData == null)
		{
			if (other.signatureData != null)
				return false;
		}
		else if (!signatureData.equals(other.signatureData))
			return false;
		if (signatureDate == null)
		{
			if (other.signatureDate != null)
				return false;
		}
		else if (!signatureDate.equals(other.signatureDate))
			return false;
		if (signatureVersion != other.signatureVersion)
			return false;
		if (uuidKey == null)
		{
			if (other.uuidKey != null)
				return false;
		}
		else if (!uuidKey.equals(other.uuidKey))
			return false;
		return true;
	}

}
