/*******************************************************************************
 * Copyright (c) 2014, 2018 Quim Testar.
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
import java.util.Date;
import java.util.UUID;

import aletheia.model.authority.Person;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.authority.StatementAuthority.AuthorityWithNoParentException;
import aletheia.model.authority.StatementAuthoritySignature;
import aletheia.model.statement.Statement;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.protocol.PersistentExportableProtocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.collection.NonReturningCollectionProtocol;
import aletheia.protocol.primitive.DateProtocol;
import aletheia.protocol.primitive.UUIDProtocol;

@ProtocolInfo(availableVersions = 0)
public class StatementAuthorityProtocol extends PersistentExportableProtocol<StatementAuthority>
{
	private final UUIDProtocol uuidProtocol;
	private final DateProtocol dateProtocol;

	public StatementAuthorityProtocol(int requiredVersion, PersistenceManager persistenceManager, Transaction transaction)
	{
		super(0, persistenceManager, transaction);
		checkVersionAvailability(StatementAuthorityProtocol.class, requiredVersion);
		this.uuidProtocol = new UUIDProtocol(0);
		this.dateProtocol = new DateProtocol(0);
	}

	@Override
	public void send(DataOutput out, StatementAuthority statementAuthority) throws IOException
	{
		uuidProtocol.send(out, statementAuthority.getStatementUuid());
		uuidProtocol.send(out, statementAuthority.getAuthorUuid());
		dateProtocol.send(out, statementAuthority.getCreationDate());
		StatementAuthoritySignatureProtocol statementAuthoritySignatureProtocol = new StatementAuthoritySignatureProtocol(0, getPersistenceManager(),
				getTransaction(), statementAuthority);
		NonReturningCollectionProtocol<StatementAuthoritySignature> statementAuthoritySignatureCollectionProtocol = new NonReturningCollectionProtocol<>(0,
				statementAuthoritySignatureProtocol);
		statementAuthoritySignatureCollectionProtocol.send(out, statementAuthority.validSignatureMap(getTransaction()).values());
	}

	@Override
	public StatementAuthority recv(DataInput in) throws IOException, ProtocolException
	{
		try
		{
			UUID statementUuid = uuidProtocol.recv(in);
			UUID authorUuid = uuidProtocol.recv(in);
			Date creationDate = dateProtocol.recv(in);
			Statement statement = getPersistenceManager().getStatement(getTransaction(), statementUuid);
			Person author = getPersistenceManager().getPerson(getTransaction(), authorUuid);
			if (author == null)
				throw new ProtocolException();
			StatementAuthority statementAuthority = statement.createAuthorityOverwrite(getTransaction(), author, creationDate);
			StatementAuthoritySignatureProtocol statementAuthoritySignatureProtocol = new StatementAuthoritySignatureProtocol(0, getPersistenceManager(),
					getTransaction(), statementAuthority);
			NonReturningCollectionProtocol<StatementAuthoritySignature> statementAuthoritySignatureCollectionProtocol = new NonReturningCollectionProtocol<>(0,
					statementAuthoritySignatureProtocol);
			statementAuthoritySignatureCollectionProtocol.recv(in);
			statementAuthority = statementAuthority.refresh(getTransaction());
			return statementAuthority;
		}
		catch (AuthorityWithNoParentException e)
		{
			throw new ProtocolException(e);
		}
	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		uuidProtocol.skip(in);
		uuidProtocol.skip(in);
		dateProtocol.skip(in);
		StatementAuthoritySignatureProtocol statementAuthoritySignatureProtocol = new StatementAuthoritySignatureProtocol(0, getPersistenceManager(),
				getTransaction(), null);
		NonReturningCollectionProtocol<StatementAuthoritySignature> statementAuthoritySignatureCollectionProtocol = new NonReturningCollectionProtocol<>(0,
				statementAuthoritySignatureProtocol);
		statementAuthoritySignatureCollectionProtocol.skip(in);
	}

}
