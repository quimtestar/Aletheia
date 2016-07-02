/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
 *
 * This file is part of the Aletheia Proof Assistant.
 *
 * The Aletheia Proof Assistant is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The Aletheia Proof Assistant is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with the Aletheia Proof Assistant.
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package aletheia.peertopeer.statement.message;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import aletheia.model.authority.RootContextAuthority;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.authority.StatementAuthoritySignature;
import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.Namespace;
import aletheia.model.security.SignatureData;
import aletheia.persistence.Transaction;
import aletheia.protocol.Exportable;
import aletheia.protocol.ExportableProtocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.collection.CollectionProtocol;
import aletheia.protocol.namespace.NamespaceProtocol;
import aletheia.protocol.primitive.BooleanProtocol;
import aletheia.protocol.primitive.DateProtocol;
import aletheia.protocol.primitive.NullableProtocol;
import aletheia.protocol.primitive.UUIDProtocol;
import aletheia.protocol.security.SignatureDataProtocol;
import aletheia.utilities.collections.CloseableCollection;
import aletheia.utilities.collections.TrivialCloseableCollection;

public class StatementAuthoritySubMessage implements Exportable
{

	public static class LastSignatureSubMessage implements Exportable
	{
		private final UUID authorizerUuid;
		private final Date signatureDate;
		private final SignatureData signatureData;

		private LastSignatureSubMessage(UUID authorizerUuid, Date signatureDate, SignatureData signatureData)
		{
			super();
			this.authorizerUuid = authorizerUuid;
			this.signatureDate = signatureDate;
			this.signatureData = signatureData;
		}

		public LastSignatureSubMessage(StatementAuthoritySignature statementAuthoritySignature)
		{
			this(statementAuthoritySignature.getAuthorizerUuid(), statementAuthoritySignature.getSignatureDate(),
					statementAuthoritySignature.getSignatureData());
		}

		public UUID getAuthorizerUuid()
		{
			return authorizerUuid;
		}

		public Date getSignatureDate()
		{
			return signatureDate;
		}

		public SignatureData getSignatureData()
		{
			return signatureData;
		}

		@ProtocolInfo(availableVersions = 0)
		public static class SubProtocol extends ExportableProtocol<LastSignatureSubMessage>
		{
			private final UUIDProtocol uuidProtocol;
			private final DateProtocol dateProtocol;
			private final SignatureDataProtocol signatureDataProtocol;

			public SubProtocol(int requiredVersion)
			{
				super(0);
				checkVersionAvailability(SubProtocol.class, requiredVersion);
				this.uuidProtocol = new UUIDProtocol(0);
				this.dateProtocol = new DateProtocol(0);
				this.signatureDataProtocol = new SignatureDataProtocol(0);
			}

			@Override
			public void send(DataOutput out, LastSignatureSubMessage lastSignatureSubMessage) throws IOException
			{
				uuidProtocol.send(out, lastSignatureSubMessage.getAuthorizerUuid());
				dateProtocol.send(out, lastSignatureSubMessage.getSignatureDate());
				signatureDataProtocol.send(out, lastSignatureSubMessage.getSignatureData());
			}

			@Override
			public LastSignatureSubMessage recv(DataInput in) throws IOException, ProtocolException
			{
				UUID authorizerUuid = uuidProtocol.recv(in);
				Date signatureDate = dateProtocol.recv(in);
				SignatureData signatureData = signatureDataProtocol.recv(in);
				return new LastSignatureSubMessage(authorizerUuid, signatureDate, signatureData);
			}

			@Override
			public void skip(DataInput in) throws IOException, ProtocolException
			{
				uuidProtocol.skip(in);
				dateProtocol.skip(in);
				signatureDataProtocol.skip(in);
			}

		}

	}

	private final UUID statementUuid;

	private final boolean isRootContext;

	private final Collection<UUID> personDependencies;

	private final UUID contextUuid;

	private final Identifier identifier;

	private final LastSignatureSubMessage lastSignatureSubMessage;

	private StatementAuthoritySubMessage(UUID statementUuid, boolean isRootContext, Collection<UUID> personDependencies, UUID contextUuid,
			Identifier identifier, LastSignatureSubMessage lastSignatureSubMessage)
	{
		super();
		this.statementUuid = statementUuid;
		this.isRootContext = isRootContext;
		this.personDependencies = personDependencies;
		this.contextUuid = contextUuid;
		this.identifier = identifier;
		this.lastSignatureSubMessage = lastSignatureSubMessage;
	}

	public class NoValidSignature extends Exception
	{
		private static final long serialVersionUID = -4659347616745073774L;

	}

	public StatementAuthoritySubMessage(Transaction transaction, StatementAuthority statementAuthority) throws NoValidSignature
	{
		super();
		this.statementUuid = statementAuthority.getStatementUuid();
		this.isRootContext = statementAuthority instanceof RootContextAuthority;
		CloseableCollection<UUID> authorPersonDependencies = new TrivialCloseableCollection<>(Collections.singleton(statementAuthority.getAuthorUuid()));
		this.personDependencies = authorPersonDependencies;
		this.contextUuid = statementAuthority.getContextUuid();
		this.identifier = statementAuthority.getStatement(transaction).getIdentifier();
		StatementAuthoritySignature lastValidSignature = statementAuthority.lastValidSignature(transaction);
		if (lastValidSignature == null)
			throw new NoValidSignature();
		this.lastSignatureSubMessage = new LastSignatureSubMessage(lastValidSignature);
	}

	public UUID getStatementUuid()
	{
		return statementUuid;
	}

	public boolean isRootContext()
	{
		return isRootContext;
	}

	public Collection<UUID> getPersonDependencies()
	{
		return personDependencies;
	}

	public UUID getContextUuid()
	{
		return contextUuid;
	}

	public Identifier getIdentifier()
	{
		return identifier;
	}

	public LastSignatureSubMessage getLastSignatureSubMessage()
	{
		return lastSignatureSubMessage;
	}

	@ProtocolInfo(availableVersions = 1)
	public static class SubProtocol extends ExportableProtocol<StatementAuthoritySubMessage>
	{
		private final UUIDProtocol uuidProtocol;
		private final BooleanProtocol booleanProtocol;
		private final CollectionProtocol<UUID> uuidCollectionProtocol;
		private final NullableProtocol<UUID> nullableUuidProtocol;
		private final NamespaceProtocol namespaceProtocol;
		private final NullableProtocol<Namespace> nullableNamespaceProtocol;
		private final LastSignatureSubMessage.SubProtocol lastSignatureMessageDataProtocol;

		public SubProtocol(int requiredVersion)
		{
			super(0);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
			this.uuidProtocol = new UUIDProtocol(0);
			this.booleanProtocol = new BooleanProtocol(0);
			this.uuidCollectionProtocol = new CollectionProtocol<>(0, uuidProtocol);
			this.nullableUuidProtocol = new NullableProtocol<>(0, uuidProtocol);
			this.namespaceProtocol = new NamespaceProtocol(0);
			this.nullableNamespaceProtocol = new NullableProtocol<>(0, namespaceProtocol);
			this.lastSignatureMessageDataProtocol = new LastSignatureSubMessage.SubProtocol(0);
		}

		@Override
		public void send(DataOutput out, StatementAuthoritySubMessage statementAuthoritySubMessage) throws IOException
		{
			uuidProtocol.send(out, statementAuthoritySubMessage.getStatementUuid());
			booleanProtocol.send(out, statementAuthoritySubMessage.isRootContext());
			uuidCollectionProtocol.send(out, statementAuthoritySubMessage.getPersonDependencies());
			nullableUuidProtocol.send(out, statementAuthoritySubMessage.getContextUuid());
			nullableNamespaceProtocol.send(out, statementAuthoritySubMessage.getIdentifier());
			lastSignatureMessageDataProtocol.send(out, statementAuthoritySubMessage.getLastSignatureSubMessage());
		}

		@Override
		public StatementAuthoritySubMessage recv(DataInput in) throws IOException, ProtocolException
		{
			UUID statementUuid = uuidProtocol.recv(in);
			boolean isRootContext = booleanProtocol.recv(in);
			Collection<UUID> personDependencies = uuidCollectionProtocol.recv(in);
			UUID contextUuid = nullableUuidProtocol.recv(in);
			Identifier identifier;
			try
			{
				identifier = (Identifier) nullableNamespaceProtocol.recv(in);
			}
			catch (ClassCastException e)
			{
				throw new ProtocolException(e);
			}
			LastSignatureSubMessage lastSignatureSubMessage = lastSignatureMessageDataProtocol.recv(in);
			return new StatementAuthoritySubMessage(statementUuid, isRootContext, personDependencies, contextUuid, identifier, lastSignatureSubMessage);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			uuidProtocol.skip(in);
			booleanProtocol.skip(in);
			uuidCollectionProtocol.skip(in);
			nullableUuidProtocol.skip(in);
			nullableNamespaceProtocol.skip(in);
			lastSignatureMessageDataProtocol.skip(in);
		}

	}

}
