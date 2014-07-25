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

import java.io.DataOutput;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

import aletheia.model.security.SignatureData;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.entities.authority.StatementAuthoritySignatureEntity;
import aletheia.protocol.Exportable;
import aletheia.protocol.primitive.DateProtocol;
import aletheia.protocol.primitive.NullableProtocol;
import aletheia.security.signerverifier.Signer;
import aletheia.security.signerverifier.Verifier;

public class StatementAuthoritySignature implements Exportable
{
	private final static int signingSignatureVersion = 0;

	private final PersistenceManager persistenceManager;
	private final StatementAuthoritySignatureEntity entity;

	public StatementAuthoritySignature(PersistenceManager persistenceManager, StatementAuthoritySignatureEntity entity)
	{
		this.persistenceManager = persistenceManager;
		this.entity = entity;
	}

	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	private StatementAuthoritySignature(PersistenceManager persistenceManager, StatementAuthority statementAuthority, UUID authorizerUuid)
	{
		this.persistenceManager = persistenceManager;
		this.entity = persistenceManager.instantiateStatementAuthoritySignatureEntity(StatementAuthoritySignatureEntity.class);
		this.entity.setStatementUuid(statementAuthority.getStatementUuid());
		this.setAuthorizerUuid(authorizerUuid);
		this.setSignatureVersion(-1);
	}

	public StatementAuthoritySignatureEntity getEntity()
	{
		return entity;
	}

	public static StatementAuthoritySignature create(PersistenceManager persistenceManager, Transaction transaction, StatementAuthority statementAuthority,
			PrivateSignatory authorizer)
	{
		StatementAuthoritySignature statementAuthoritySignature = new StatementAuthoritySignature(persistenceManager, statementAuthority, authorizer.getUuid());
		statementAuthoritySignature.sign(authorizer, transaction);
		statementAuthoritySignature.persistenceUpdate(transaction);
		statementAuthoritySignature.checkValidSignature(transaction);
		statementAuthoritySignature.updateRootContextAuthoritySignatureUuid(transaction);
		Iterable<StatementAuthority.StateListener> stateListeners = statementAuthority.stateListeners();
		synchronized (stateListeners)
		{
			for (StatementAuthority.StateListener l : stateListeners)
				l.signatureAdded(transaction, statementAuthority, statementAuthoritySignature);
		}
		return statementAuthoritySignature;
	}

	public static StatementAuthoritySignature create(PersistenceManager persistenceManager, Transaction transaction, StatementAuthority statementAuthority,
			UUID authorizerUuid, Date signatureDate, int signatureVersion, SignatureData signatureData) throws SignatureVerifyException,
			SignatureVersionException
	{
		StatementAuthoritySignature statementAuthoritySignature = new StatementAuthoritySignature(persistenceManager, statementAuthority, authorizerUuid);
		statementAuthoritySignature.setSignatureDate(signatureDate);
		statementAuthoritySignature.setSignatureVersion(signatureVersion);
		statementAuthoritySignature.setSignatureData(signatureData);
		statementAuthoritySignature.verify(transaction);
		statementAuthoritySignature.persistenceUpdate(transaction);
		statementAuthoritySignature.checkValidSignature(transaction);
		statementAuthoritySignature.updateRootContextAuthoritySignatureUuid(transaction);
		Iterable<StatementAuthority.StateListener> stateListeners = statementAuthority.stateListeners();
		synchronized (stateListeners)
		{
			for (StatementAuthority.StateListener l : stateListeners)
				l.signatureAdded(transaction, statementAuthority, statementAuthoritySignature);
		}
		return statementAuthoritySignature;
	}

	private void updateRootContextAuthoritySignatureUuid(Transaction transaction)
	{
		StatementAuthority statementAuthority = getStatementAuthority(transaction);
		if (statementAuthority instanceof RootContextAuthority)
		{
			RootContextAuthority rootContextAuthority = (RootContextAuthority) statementAuthority;
			rootContextAuthority.updateSignatureUuid(transaction);
			rootContextAuthority.persistenceUpdate(transaction);
		}
	}

	private void signatureDataOut(DataOutput out, Transaction transaction) throws SignatureVersionException
	{
		if (getSignatureVersion() != 0)
			throw new SignatureVersionException();
		try
		{
			NullableProtocol<Date> nullableDateProtocol = new NullableProtocol<Date>(0, new DateProtocol(0));
			getStatementAuthority(transaction).signatureDataOutStatementAuthoritySignature(out, transaction, getSignatureVersion());
			nullableDateProtocol.send(out, getSignatureDate());
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

	}

	private void persistenceUpdate(Transaction transaction)
	{
		persistenceManager.putStatementAuthoritySignature(transaction, this);
	}

	public UUID getStatementUuid()
	{
		return entity.getStatementUuid();
	}

	public UUID getAuthorizerUuid()
	{
		return entity.getAuthorizerUuid();
	}

	private void setAuthorizerUuid(UUID authorizerUuid)
	{
		entity.setAuthorizerUuid(authorizerUuid);
	}

	public Date getSignatureDate()
	{
		return getEntity().getSignatureDate();
	}

	private void setSignatureDate(Date signatureDate)
	{
		getEntity().setSignatureDate(signatureDate);
	}

	private void setSignatureDate()
	{
		setSignatureDate(new Date());
	}

	private void setSignatureVersion(int signatureVersion)
	{
		entity.setSignatureVersion(signatureVersion);
	}

	public int getSignatureVersion()
	{
		return entity.getSignatureVersion();
	}

	public SignatureData getSignatureData()
	{
		return entity.getSignatureData();
	}

	private void setSignatureData(SignatureData signatureData)
	{
		entity.setSignatureData(signatureData);
		updateSignatureUuid();
	}

	public UUID getSignatureUuid()
	{
		return entity.getSignatureUuid();
	}

	private void setSignatureUuid(UUID signatureUuid)
	{
		entity.setSignatureUuid(signatureUuid);
	}

	private void updateSignatureUuid()
	{
		SignatureData signatureData = getSignatureData();
		if (signatureData == null)
			setSignatureUuid(null);
		else
			setSignatureUuid(signatureData.uuid());
	}

	public StatementAuthority getStatementAuthority(Transaction transaction)
	{
		return persistenceManager.getStatementAuthority(transaction, getStatementUuid());
	}

	public Statement getStatement(Transaction transaction)
	{
		return persistenceManager.getStatement(transaction, getStatementUuid());
	}

	public Signatory getAuthorizer(Transaction transaction)
	{
		return persistenceManager.getSignatory(transaction, getAuthorizerUuid());
	}

	private void sign(PrivateSignatory authorizer, Transaction transaction)
	{
		try
		{
			setSignatureDate();
			Signer signer = authorizer.signer();
			setSignatureVersion(signingSignatureVersion);
			try
			{
				signatureDataOut(signer.dataOutput(), transaction);
			}
			catch (SignatureVersionException e)
			{
				throw new Error("signingSignatureVersion must be supported", e);
			}
			setSignatureData(signer.sign());
		}
		catch (InvalidKeyException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{

		}
	}

	private void verify(Transaction transaction) throws SignatureVerifyException, SignatureVersionException
	{
		try
		{
			Signatory signatory = getAuthorizer(transaction);
			SignatureData signatureData = getSignatureData();
			Verifier verifier = signatory.verifier(signatureData);
			signatureDataOut(verifier.dataOutput(), transaction);
			if (!verifier.verify())
				throw new SignatureVerifyException();
		}
		catch (InvalidKeyException | IncompleteDataSignatureException | NoSuchAlgorithmException e)
		{
			throw new SignatureVerifyException(e);
		}
		finally
		{

		}
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof StatementAuthoritySignature))
			return false;
		return getEntity().equals(obj);
	}

	public boolean isValid()
	{
		return entity.isValid();
	}

	private void setValid(Transaction transaction, boolean valid)
	{
		entity.setValid(valid);
		persistenceUpdate(transaction);
	}

	public void checkValidSignature(Transaction transaction)
	{
		boolean oldValidSignature = isValid();
		boolean newValidSignature = calcValidSignature(transaction);
		if (newValidSignature != oldValidSignature)
		{
			setValid(transaction, newValidSignature);
			getStatementAuthority(transaction).checkValidSignature(transaction);
		}
	}

	private boolean calcValidSignature(Transaction transaction)
	{
		Statement statement = getStatement(transaction);
		StatementAuthority statementAuthority = getStatementAuthority(transaction);
		Signatory authorizer = getAuthorizer(transaction);
		if (statement instanceof RootContext)
		{
			if (statementAuthority.getAuthorUuid().equals(getAuthorizerUuid()))
				return true;
		}
		else
		{
			StatementAuthority parent = statement.getContext(transaction).getAuthority(transaction);
			if (parent != null)
			{
				DelegateAuthorizer da = parent.delegateAuthorizerByAuthorizerMap(transaction, statement.prefix(transaction)).get(authorizer);
				if (da != null)
				{
					if (da.isSigned() && !da.revokedSignatureUuids().contains(getSignatureUuid()))
					{
						DelegateTreeRootNode rn = da.getDelegateTreeRootNode(transaction);
						if (rn.isSigned())
							return true;
					}
				}
			}
		}
		return false;
	}

	public void delete(Transaction transaction)
	{
		getStatementAuthority(transaction).deleteSignature(transaction, getAuthorizerUuid());
	}

	@Override
	public String toString()
	{
		Transaction transaction = persistenceManager.beginDirtyTransaction();
		try
		{
			return toString(transaction);
		}
		finally
		{
			transaction.abort();
		}
	}

	public String toString(Transaction transaction)
	{
		Statement statement = getStatement(transaction);
		if (statement == null)
			return "*null*";
		return statement.getVariable().toString(statement.parentVariableToIdentifier(transaction)) + ": " + getAuthorizer(transaction).toString() + ": "
				+ getSignatureDate() + ": " + getSignatureData() + ": " + isValid();
	}

}
