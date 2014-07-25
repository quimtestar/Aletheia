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
package aletheia.persistence.berkeleydb.entities.misc;

import aletheia.persistence.entities.misc.PersistenceSecretKeySingletonEntity;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity(version = 1)
public class BerkeleyDBPersistenceSecretKeySingletonEntity implements PersistenceSecretKeySingletonEntity
{
	@PrimaryKey
	private final boolean mark = true;

	private byte[] salt;
	private int verificationVersion;
	private byte[] verification;

	@Override
	public byte[] getSalt()
	{
		return salt;
	}

	@Override
	public void setSalt(byte[] salt)
	{
		this.salt = salt;
	}

	@Override
	public int getVerificationVersion()
	{
		return verificationVersion;
	}

	@Override
	public void setVerificationVersion(int verificationVersion)
	{
		this.verificationVersion = verificationVersion;
	}

	@Override
	public byte[] getVerification()
	{
		return verification;
	}

	@Override
	public void setVerification(byte[] verification)
	{
		this.verification = verification;
	}

}
