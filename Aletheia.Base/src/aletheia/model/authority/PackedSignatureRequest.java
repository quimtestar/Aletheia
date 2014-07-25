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

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import aletheia.model.authority.UnpackedSignatureRequest.UnpackedSignatureRequestException;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.entities.authority.PackedSignatureRequestEntity;
import aletheia.protocol.ProtocolException;

public class PackedSignatureRequest extends SignatureRequest
{

	public static abstract class PackedSignatureRequestException extends SignatureRequestException
	{

		private static final long serialVersionUID = -3462704710792872971L;

		public PackedSignatureRequestException()
		{
			super();
		}

		public PackedSignatureRequestException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public PackedSignatureRequestException(String message)
		{
			super(message);
		}

		public PackedSignatureRequestException(Throwable cause)
		{
			super(cause);
		}
	}

	public PackedSignatureRequest(PersistenceManager persistenceManager, PackedSignatureRequestEntity entity)
	{
		super(persistenceManager, entity);
	}

	protected PackedSignatureRequest(PersistenceManager persistenceManager, UUID uuid, Date creationDate, List<UUID> contextUuidPath, Date packingDate,
			UUID rootContextSignatureUuid, Collection<UUID> dependencyUuids, byte[] data)
	{
		super(persistenceManager, PackedSignatureRequestEntity.class, uuid, creationDate, contextUuidPath);
		setPackingDate(packingDate);
		setRootContextSignatureUuid(rootContextSignatureUuid);
		setDependencyUuids(dependencyUuids);
		setData(data);
	}

	public static class CollisionPackedSignatureRequestException extends PackedSignatureRequestException
	{
		private static final long serialVersionUID = 9071578111248551429L;
	}

	public static PackedSignatureRequest create(PersistenceManager persistenceManager, Transaction transaction, UUID uuid, Date creationDate,
			List<UUID> contextUuidPath, Date packingDate, UUID rootContextSignatureUuid, Collection<UUID> dependencyUuids, byte[] data)
			throws CollisionPackedSignatureRequestException
	{
		PackedSignatureRequest packedSignatureRequest = new PackedSignatureRequest(persistenceManager, uuid, creationDate, contextUuidPath, packingDate,
				rootContextSignatureUuid, dependencyUuids, data);
		if (!packedSignatureRequest.persistenceUpdateNoOverwrite(transaction))
			throw new CollisionPackedSignatureRequestException();
		packedSignatureRequest.notifySignatureRequestAdded(transaction);
		return packedSignatureRequest;
	}

	@Override
	public PackedSignatureRequestEntity getEntity()
	{
		return (PackedSignatureRequestEntity) super.getEntity();
	}

	public Date getPackingDate()
	{
		return getEntity().getPackingDate();
	}

	private void setPackingDate(Date packingDate)
	{
		getEntity().setPackingDate(packingDate);
	}

	public UUID getRootContextSignatureUuid()
	{
		return getEntity().getRootContextSignatureUuid();
	}

	private void setRootContextSignatureUuid(UUID rootContextSignatureUuid)
	{
		getEntity().setRootContextSignatureUuid(rootContextSignatureUuid);
	}

	public Set<UUID> getDependencyUuids()
	{
		return Collections.unmodifiableSet(getEntity().getDependencyUuids());
	}

	private void clearDependencyUuids()
	{
		getEntity().getDependencyUuids().clear();
	}

	private void setDependencyUuids(Collection<UUID> dependencyUuids)
	{
		clearDependencyUuids();
		getEntity().getDependencyUuids().addAll(dependencyUuids);
	}

	public byte[] getData()
	{
		return getEntity().getData();
	}

	private void setData(byte[] data)
	{
		getEntity().setData(data);
	}

	public class UnpackPackedSignatureRequestException extends PackedSignatureRequestException
	{

		private static final long serialVersionUID = 3517411288450238978L;

		public UnpackPackedSignatureRequestException()
		{
			super();
		}

		public UnpackPackedSignatureRequestException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public UnpackPackedSignatureRequestException(String message)
		{
			super(message);
		}

		public UnpackPackedSignatureRequestException(Throwable cause)
		{
			super(cause);
		}
	}

	public UnpackedSignatureRequest unpack(Transaction transaction) throws UnpackPackedSignatureRequestException
	{
		try
		{
			UnpackedSignatureRequest unpackedSignatureRequest = UnpackedSignatureRequest.create(getPersistenceManager(), transaction, getUuid(),
					getCreationDate(), getContextUuidPath());
			UnpackedBuilder unpackedBuilder = new ByteArrayUnpackedBuilder(getPersistenceManager(), transaction, getData());
			for (Statement statement : unpackedBuilder.getStatementSet())
				unpackedSignatureRequest.addStatement(transaction, statement);
			return unpackedSignatureRequest;
		}
		catch (ProtocolException | UnpackedSignatureRequestException e)
		{
			throw new UnpackPackedSignatureRequestException(e);
		}
	}

	public boolean unpackable(Transaction transaction)
	{
		RootContextAuthority rootContextAuthority = getPersistenceManager().getRootContextAuthorityBySignatureUuid(transaction, getRootContextSignatureUuid());
		if (rootContextAuthority == null)
			return false;
		if (getContextUuidPath().isEmpty())
			return false;
		if (!rootContextAuthority.getStatementUuid().equals(getContextUuidPath().get(0)))
			return false;
		UUID ctxUuid = null;
		for (UUID uuid : getContextUuidPath())
		{
			if (ctxUuid == null)
			{
				if (!rootContextAuthority.getStatementUuid().equals(uuid))
					return false;
			}
			else
			{
				Context ctx = getPersistenceManager().getContext(transaction, uuid);
				if (ctx == null)
					return false;
				if (!ctxUuid.equals(ctx.getContextUuid()))
					return false;
			}
			ctxUuid = uuid;
		}
		for (UUID uuid : getDependencyUuids())
		{
			Statement st = getPersistenceManager().getStatement(transaction, uuid);
			if (st == null || !getContextUuidPath().contains(st.getContextUuid()))
				return false;
		}
		return true;
	}

	@Override
	public PackedSignatureRequest refresh(Transaction transaction)
	{
		SignatureRequest signatureRequest = super.refresh(transaction);
		return signatureRequest instanceof PackedSignatureRequest ? (PackedSignatureRequest) signatureRequest : null;
	}

}
