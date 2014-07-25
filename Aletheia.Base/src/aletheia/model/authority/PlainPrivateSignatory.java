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

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.UUID;

import aletheia.persistence.PersistenceManager;
import aletheia.persistence.entities.authority.PlainPrivateSignatoryEntity;
import aletheia.security.utilities.SecurityUtilities;

public class PlainPrivateSignatory extends PrivateSignatory
{

	protected PlainPrivateSignatory(PersistenceManager persistenceManager, UUID uuid, PublicKey publicKey, String signatureAlgorithm, PrivateKey privateKey)
			throws KeysDontMatchException
	{
		super(persistenceManager, PlainPrivateSignatoryEntity.class, uuid, publicKey, signatureAlgorithm);
		if (!SecurityUtilities.instance.checkKeyPair(signatureAlgorithm, privateKey, publicKey))
			throw new KeysDontMatchException();
		getEntity().setPrivateKey(privateKey);
	}

	public PlainPrivateSignatory(PersistenceManager persistenceManager, PlainPrivateSignatoryEntity entity)
	{
		super(persistenceManager, entity);
	}

	@Override
	public PlainPrivateSignatoryEntity getEntity()
	{
		return (PlainPrivateSignatoryEntity) super.getEntity();
	}

	@Override
	public PrivateKey getPrivateKey()
	{
		return getEntity().getPrivateKey();
	}

}
