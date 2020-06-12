/*******************************************************************************
 * Copyright (c) 2014, 2016 Quim Testar.
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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import aletheia.model.authority.DelegateAuthorizer;
import aletheia.model.authority.DelegateTreeNode;
import aletheia.model.authority.Person;
import aletheia.model.authority.Signatory;
import aletheia.model.authority.SignatureVerifyException;
import aletheia.model.authority.SignatureVersionException;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.protocol.PersistentExportableProtocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.collection.CollectionProtocol;
import aletheia.protocol.primitive.DateProtocol;
import aletheia.protocol.primitive.IntegerProtocol;
import aletheia.protocol.primitive.NullableProtocol;
import aletheia.protocol.primitive.UUIDProtocol;
import aletheia.security.model.SignatureData;
import aletheia.security.protocol.SignatureDataProtocol;

@ProtocolInfo(availableVersions = 0)
public class DelegateAuthorizerProtocol extends PersistentExportableProtocol<DelegateAuthorizer>
{
	private final DelegateTreeNode delegateTreeNode;
	private final UUIDProtocol uuidProtocol;
	private final SignatoryProtocol signatoryProtocol;
	private final NullableProtocol<Signatory> nullableSignatoryProtocol;
	private final CollectionProtocol<UUID> uuidCollectionProtocol;
	private final DateProtocol dateProtocol;
	private final NullableProtocol<Date> nullableDateProtocol;
	private final SignatureDataProtocol signatureDataProtocol;
	private final IntegerProtocol integerProtocol;
	private final NullableProtocol<SignatureData> nullableSignatureDataProtocol;

	public DelegateAuthorizerProtocol(int requiredVersion, PersistenceManager persistenceManager, Transaction transaction, DelegateTreeNode delegateTreeNode)
	{
		super(0, persistenceManager, transaction);
		checkVersionAvailability(DelegateAuthorizerProtocol.class, requiredVersion);
		this.delegateTreeNode = delegateTreeNode;
		this.uuidProtocol = new UUIDProtocol(0);
		this.signatoryProtocol = new SignatoryProtocol(0, persistenceManager, transaction);
		this.nullableSignatoryProtocol = new NullableProtocol<>(0, this.signatoryProtocol);
		this.uuidCollectionProtocol = new CollectionProtocol<>(0, this.uuidProtocol);
		this.dateProtocol = new DateProtocol(0);
		this.nullableDateProtocol = new NullableProtocol<>(0, dateProtocol);
		this.signatureDataProtocol = new SignatureDataProtocol(0);
		this.integerProtocol = new IntegerProtocol(0);
		this.nullableSignatureDataProtocol = new NullableProtocol<>(0, signatureDataProtocol);
	}

	@Override
	public void send(DataOutput out, DelegateAuthorizer delegateAuthorizer) throws IOException
	{
		uuidProtocol.send(out, delegateAuthorizer.getDelegateUuid());
		nullableSignatoryProtocol.send(out, delegateAuthorizer.getAuthorizer(getTransaction()));
		uuidCollectionProtocol.send(out, delegateAuthorizer.revokedSignatureUuids());
		nullableDateProtocol.send(out, delegateAuthorizer.getSignatureDate());
		integerProtocol.send(out, delegateAuthorizer.getSignatureVersion());
		nullableSignatureDataProtocol.send(out, delegateAuthorizer.getSignatureData());
	}

	@Override
	public DelegateAuthorizer recv(DataInput in) throws IOException, ProtocolException
	{
		UUID delegateUuid = uuidProtocol.recv(in);
		Signatory authorizer = nullableSignatoryProtocol.recv(in);
		Collection<UUID> revokedSignatureUuids = uuidCollectionProtocol.recv(in);
		Date signatureDate = nullableDateProtocol.recv(in);
		int signatureVersion = integerProtocol.recv(in);
		SignatureData signatureData = nullableSignatureDataProtocol.recv(in);
		Person delegate = getPersistenceManager().getPerson(getTransaction(), delegateUuid);
		if (delegate == null)
			throw new ProtocolException();
		try
		{
			return delegateTreeNode.updateDelegateAuthorizer(getTransaction(), delegate, authorizer, revokedSignatureUuids, signatureDate, signatureVersion,
					signatureData);
		}
		catch (SignatureVerifyException | SignatureVersionException e)
		{
			throw new ProtocolException(e);
		}
	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		uuidProtocol.skip(in);
		nullableSignatoryProtocol.skip(in);
		uuidCollectionProtocol.skip(in);
		nullableDateProtocol.skip(in);
		integerProtocol.skip(in);
		nullableSignatureDataProtocol.skip(in);
	}

}
