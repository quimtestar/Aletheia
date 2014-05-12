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

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.UUID;

import aletheia.model.security.SignatureData;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.collections.authority.StatementAuthoritySignatureSetByAuthorizer;
import aletheia.persistence.collections.authority.StatementAuthoritySignatureSetByAuthorizerAndSignatureUuid;
import aletheia.persistence.entities.authority.SignatoryEntity;
import aletheia.protocol.Exportable;
import aletheia.security.signerverifier.BufferedVerifier;
import aletheia.security.signerverifier.Verifier;

public class Signatory implements Exportable
{
	private final PersistenceManager persistenceManager;
	private final SignatoryEntity entity;

	protected Signatory(PersistenceManager persistenceManager, Class<? extends SignatoryEntity> entityClass, UUID uuid, PublicKey publicKey)
	{
		super();
		this.persistenceManager = persistenceManager;
		this.entity = persistenceManager.instantiateSignatoryEntity(entityClass);
		this.entity.setUuid(uuid);
		this.entity.setPublicKey(publicKey);
	}

	protected Signatory(PersistenceManager persistenceManager, UUID uuid, PublicKey publicKey)
	{
		this(persistenceManager, SignatoryEntity.class, uuid, publicKey);
	}

	public Signatory(PersistenceManager persistenceManager, SignatoryEntity entity)
	{
		this.persistenceManager = persistenceManager;
		this.entity = entity;
	}

	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	public SignatoryEntity getEntity()
	{
		return entity;
	}

	public static Signatory create(PersistenceManager persistenceManager, Transaction transaction, UUID uuid, PublicKey publicKey)
	{
		Signatory signatory = new Signatory(persistenceManager, uuid, publicKey);
		signatory.persistenceUpdate(transaction);
		return signatory;
	}

	public UUID getUuid()
	{
		return entity.getUuid();
	}

	public PublicKey getPublicKey()
	{
		return entity.getPublicKey();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + getUuid().hashCode();
		result = prime * result + getPublicKey().hashCode();
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
		Signatory other = (Signatory) obj;
		if (!getUuid().equals(other.getUuid()))
			return false;
		if (!getPublicKey().equals(other.getPublicKey()))
			return false;
		return true;
	}

	protected void persistenceUpdate(Transaction transaction)
	{
		persistenceManager.putSignatory(transaction, this);
	}

	public Verifier verifier(SignatureData signatureData) throws InvalidKeyException, NoSuchAlgorithmException, IncompleteDataSignatureException
	{

		if (signatureData == null)
			throw new IncompleteDataSignatureException();
		return new BufferedVerifier(getPublicKey(), signatureData);
	}

	public StatementAuthoritySignatureSetByAuthorizer statementAuthoritySignatures(Transaction transaction)
	{
		return persistenceManager.statementAuthoritySignatureSetByAuthorizer(transaction, this);
	}

	@Override
	public String toString()
	{
		return getUuid().toString();
	}

	private void delete(Transaction transaction)
	{
		for (StatementAuthoritySignature sas : statementAuthoritySignatureSetByAuthorizer(transaction))
			sas.delete(transaction);
		persistenceManager.deleteSignatory(transaction, this);
	}

	public Person person(Transaction transaction)
	{
		return persistenceManager.persons(transaction).get(this);
	}

	public DelegateAuthorizer delegateAuthorizer(Transaction transaction)
	{
		return persistenceManager.delegateAuthorizerByAuthorizerMap(transaction).get(this);
	}

	public StatementAuthoritySignatureSetByAuthorizer statementAuthoritySignatureSetByAuthorizer(Transaction transaction)
	{
		return persistenceManager.statementAuthoritySignatureSetByAuthorizer(transaction, this);
	}

	public StatementAuthoritySignatureSetByAuthorizerAndSignatureUuid statementAuthoritySignatureSetByAuthorizerAndSignatureUuid(Transaction transaction,
			UUID signatureUuid)
	{
		return persistenceManager.statementAuthoritySignatureSetByAuthorizerAndSignatureUuid(transaction, this, signatureUuid);
	}

	private boolean isOrphan(Transaction transaction)
	{
		return person(transaction) == null && delegateAuthorizer(transaction) == null;
	}

	public boolean deleteIfOrphan(Transaction transaction)
	{
		if (isOrphan(transaction))
		{
			delete(transaction);
			return true;
		}
		else
			return false;
	}

}
