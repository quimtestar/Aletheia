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
package aletheia.model.authority.protocol;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import aletheia.model.authority.PackedSignatureRequest;
import aletheia.model.authority.PackedSignatureRequest.CollisionPackedSignatureRequestException;
import aletheia.model.authority.SignatureRequest;
import aletheia.model.authority.SignatureRequest.PackedBuilder;
import aletheia.model.authority.UnpackedSignatureRequest;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.protocol.PersistentExportableProtocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.collection.ListProtocol;
import aletheia.protocol.collection.SetProtocol;
import aletheia.protocol.primitive.ByteArrayProtocol;
import aletheia.protocol.primitive.DateProtocol;
import aletheia.protocol.primitive.NullableProtocol;
import aletheia.protocol.primitive.UUIDProtocol;

@ProtocolInfo(availableVersions = 0)
public class SignatureRequestProtocol extends PersistentExportableProtocol<SignatureRequest>
{
	private final UUIDProtocol uuidProtocol;
	private final DateProtocol dateProtocol;
	private final ListProtocol<UUID> uuidListProtocol;
	private final NullableProtocol<UUID> nullableUuidProtocol;
	private final SetProtocol<UUID> uuidSetProtocol;
	private final ByteArrayProtocol byteArrayProtocol;

	public SignatureRequestProtocol(int requiredVersion, PersistenceManager persistenceManager, Transaction transaction)
	{
		super(0, persistenceManager, transaction);
		checkVersionAvailability(SignatureRequestProtocol.class, requiredVersion);
		this.uuidProtocol = new UUIDProtocol(0);
		this.dateProtocol = new DateProtocol(0);
		this.uuidListProtocol = new ListProtocol<>(0, uuidProtocol);
		this.nullableUuidProtocol = new NullableProtocol<>(0, uuidProtocol);
		this.uuidSetProtocol = new SetProtocol<>(0, uuidProtocol);
		this.byteArrayProtocol = new ByteArrayProtocol(0);
	}

	@Override
	public void send(DataOutput out, SignatureRequest signatureRequest) throws IOException
	{
		uuidProtocol.send(out, signatureRequest.getUuid());
		dateProtocol.send(out, signatureRequest.getCreationDate());
		uuidListProtocol.send(out, signatureRequest.contextUuidPath());
		if (signatureRequest instanceof PackedSignatureRequest)
		{
			PackedSignatureRequest packedSignatureRequest = (PackedSignatureRequest) signatureRequest;
			dateProtocol.send(out, packedSignatureRequest.getPackingDate());
			nullableUuidProtocol.send(out, packedSignatureRequest.getRootContextSignatureUuid());
			byteArrayProtocol.send(out, packedSignatureRequest.getData());
			byteArrayProtocol.send(out, new byte[0]);
			uuidSetProtocol.send(out, packedSignatureRequest.getDependencyUuids());
		}
		else if (signatureRequest instanceof UnpackedSignatureRequest)
		{
			UnpackedSignatureRequest unpackedSignatureRequest = (UnpackedSignatureRequest) signatureRequest;
			dateProtocol.send(out, new Date());
			nullableUuidProtocol.send(out, unpackedSignatureRequest.rootContextSignatureUuid(getTransaction()));
			PackedBuilder packedBuilder = unpackedSignatureRequest.packedBuilder(getTransaction(), out);
			uuidSetProtocol.send(out, packedBuilder.getDependencyUuids());
		}
	}

	public class CollisionPackedSignatureRequestProtocolException extends ProtocolException
	{
		private static final long serialVersionUID = 1846964383620079151L;

		private CollisionPackedSignatureRequestProtocolException(CollisionPackedSignatureRequestException cause)
		{
			super(cause);
		}

		@Override
		public synchronized CollisionPackedSignatureRequestException getCause()
		{
			return (CollisionPackedSignatureRequestException) super.getCause();
		}
	}

	@Override
	public PackedSignatureRequest recv(DataInput in) throws IOException, ProtocolException
	{
		UUID uuid = uuidProtocol.recv(in);
		Date creationDate = dateProtocol.recv(in);
		List<UUID> contextUuidPath = uuidListProtocol.recv(in);
		Date packingDate = dateProtocol.recv(in);
		UUID rootContextSignatureUuid = nullableUuidProtocol.recv(in);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while (true)
		{
			byte[] segment = byteArrayProtocol.recv(in);
			if (segment == null || segment.length <= 0)
				break;
			baos.write(segment);
		}
		byte[] data = baos.toByteArray();
		Set<UUID> dependencyUuids = uuidSetProtocol.recv(in);
		try
		{
			return PackedSignatureRequest.create(getPersistenceManager(), getTransaction(), uuid, creationDate, contextUuidPath, packingDate,
					rootContextSignatureUuid, dependencyUuids, data);
		}
		catch (CollisionPackedSignatureRequestException e)
		{
			throw new CollisionPackedSignatureRequestProtocolException(e);
		}
	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		uuidProtocol.skip(in);
		dateProtocol.skip(in);
		uuidListProtocol.skip(in);
		dateProtocol.skip(in);
		nullableUuidProtocol.skip(in);
		uuidSetProtocol.skip(in);
		byteArrayProtocol.skip(in);
	}

}
