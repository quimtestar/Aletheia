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
package aletheia.protocol.authority;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import aletheia.model.authority.Signatory;
import aletheia.model.authority.SignatureVerifyException;
import aletheia.model.authority.SignatureVersionException;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.authority.StatementAuthoritySignature;
import aletheia.model.security.SignatureData;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.protocol.PersistentExportableProtocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.DateProtocol;
import aletheia.protocol.primitive.IntegerProtocol;
import aletheia.protocol.primitive.UUIDProtocol;
import aletheia.protocol.security.SignatureDataProtocol;

@ProtocolInfo(availableVersions = 0)
public class StatementAuthoritySignatureProtocol extends PersistentExportableProtocol<StatementAuthoritySignature>
{
	private final StatementAuthority statementAuthority;
	private final UUIDProtocol uuidProtocol;
	private final DateProtocol dateProtocol;
	private final IntegerProtocol integerProtocol;
	private final SignatureDataProtocol signatureDataProtocol;

	public StatementAuthoritySignatureProtocol(int requiredVersion, PersistenceManager persistenceManager, Transaction transaction,
			StatementAuthority statementAuthority)
	{
		super(0, persistenceManager, transaction);
		checkVersionAvailability(StatementAuthoritySignatureProtocol.class, requiredVersion);
		this.statementAuthority = statementAuthority;
		this.uuidProtocol = new UUIDProtocol(0);
		this.dateProtocol = new DateProtocol(0);
		this.integerProtocol = new IntegerProtocol(0);
		this.signatureDataProtocol = new SignatureDataProtocol(0);
	}

	@Override
	public void send(DataOutput out, StatementAuthoritySignature statementAuthoritySignature) throws IOException
	{
		uuidProtocol.send(out, statementAuthoritySignature.getAuthorizerUuid());
		dateProtocol.send(out, statementAuthoritySignature.getSignatureDate());
		integerProtocol.send(out, statementAuthoritySignature.getSignatureVersion());
		signatureDataProtocol.send(out, statementAuthoritySignature.getSignatureData());
	}

	@Override
	public StatementAuthoritySignature recv(DataInput in) throws IOException, ProtocolException
	{
		UUID authorizerUuid = uuidProtocol.recv(in);
		Date signatureDate = dateProtocol.recv(in);
		int signatureVersion = integerProtocol.recv(in);
		SignatureData signatureData = signatureDataProtocol.recv(in);
		Signatory authorizer = getPersistenceManager().getSignatory(getTransaction(), authorizerUuid);
		if (authorizer == null)
			throw new ProtocolException();
		StatementAuthoritySignature statementAuthoritySignature = statementAuthority.signatureMap(getTransaction()).get(authorizer);
		boolean same = statementAuthoritySignature != null && statementAuthoritySignature.getSignatureDate().equals(signatureDate)
				&& statementAuthoritySignature.getSignatureVersion() != signatureVersion
				&& statementAuthoritySignature.getSignatureData().equals(signatureData);
		if (!same)
		{
			try
			{
				statementAuthoritySignature = statementAuthority.createSignature(getTransaction(), authorizer, signatureDate, signatureVersion, signatureData);
			}
			catch (SignatureVerifyException | SignatureVersionException e)
			{
				throw new ProtocolException(e);
			}
		}
		return statementAuthoritySignature;
	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		uuidProtocol.skip(in);
		dateProtocol.skip(in);
		integerProtocol.skip(in);
		signatureDataProtocol.skip(in);
	}

}
