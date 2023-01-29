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

import java.util.Arrays;

import com.sleepycat.persist.model.Persistent;

import aletheia.persistence.entities.authority.EncryptedPrivateSignatoryEntity;

@Persistent(version = 1)
public class BerkeleyDBEncryptedPrivateSignatoryEntity extends BerkeleyDBPrivateSignatoryEntity implements EncryptedPrivateSignatoryEntity
{
	private int version;
	private byte[] bytes;

	@Override
	public int getVersion()
	{
		return version;
	}

	@Override
	public void setVersion(int version)
	{
		this.version = version;
	}

	@Override
	public byte[] getBytes()
	{
		return bytes;
	}

	@Override
	public void setBytes(byte[] bytes)
	{
		this.bytes = bytes;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(bytes);
		result = prime * result + version;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj) || (getClass() != obj.getClass()))
			return false;
		BerkeleyDBEncryptedPrivateSignatoryEntity other = (BerkeleyDBEncryptedPrivateSignatoryEntity) obj;
		if (!Arrays.equals(bytes, other.bytes))
			return false;
		if (version != other.version)
			return false;
		return true;
	}

}
