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
package aletheia.model.local.protocol;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

import aletheia.model.local.ContextLocal;
import aletheia.model.local.StatementLocal;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.protocol.PersistentExportableProtocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.BooleanProtocol;
import aletheia.protocol.primitive.UUIDProtocol;

@ProtocolInfo(availableVersions = 0)
public class StatementLocalProtocol extends PersistentExportableProtocol<StatementLocal>
{
	private final UUIDProtocol uuidProtocol;
	private final StatementLocalCodeProtocol statementLocalCodeProtocol;
	private final BooleanProtocol booleanProtocol;

	public StatementLocalProtocol(int requiredVersion, PersistenceManager persistenceManager, Transaction transaction)
	{
		super(0, persistenceManager, transaction);
		checkVersionAvailability(StatementLocalProtocol.class, requiredVersion);
		this.uuidProtocol = new UUIDProtocol(0);
		this.statementLocalCodeProtocol = new StatementLocalCodeProtocol(0);
		this.booleanProtocol = new BooleanProtocol(0);
	}

	@Override
	public void send(DataOutput out, StatementLocal statementLocal) throws IOException
	{
		uuidProtocol.send(out, statementLocal.getStatementUuid());
		StatementLocalCode code = StatementLocalCode.classMap().get(statementLocal.getClass());
		statementLocalCodeProtocol.send(out, code);
		switch (code)
		{
		case _StatementLocal:
			sendStatementLocal(out, statementLocal);
			break;
		case _ContextLocal:
			sendContextLocal(out, (ContextLocal) statementLocal);
			break;
		default:
			throw new Error();
		}
	}

	private void sendStatementLocal(DataOutput out, StatementLocal statementLocal) throws IOException
	{
		booleanProtocol.send(out, statementLocal.isSubscribeProof());
	}

	private void sendContextLocal(DataOutput out, ContextLocal contextLocal) throws IOException
	{
		sendStatementLocal(out, contextLocal);
		booleanProtocol.send(out, contextLocal.isSubscribeStatements());
	}

	@Override
	public StatementLocal recv(DataInput in) throws IOException, ProtocolException
	{
		UUID uuid = uuidProtocol.recv(in);
		Statement statement = getPersistenceManager().getStatement(getTransaction(), uuid);
		if (statement == null)
			throw new ProtocolException();
		StatementLocalCode code = statementLocalCodeProtocol.recv(in);
		StatementLocal statementLocal;
		switch (code)
		{
		case _StatementLocal:
			statementLocal = recvStatementLocal(in, statement);
			break;
		case _ContextLocal:
			statementLocal = recvContextLocal(in, statement);
			break;
		default:
			throw new ProtocolException();
		}
		statementLocal.persistenceUpdate(getTransaction());
		return statementLocal;
	}

	private StatementLocal recvStatementLocal(DataInput in, Statement statement) throws IOException
	{
		StatementLocal statementLocal = statement.getOrCreateLocal(getTransaction());
		boolean subscribeProof = booleanProtocol.recv(in);
		statementLocal.setSubscribeProof(subscribeProof);
		return statementLocal;
	}

	private ContextLocal recvContextLocal(DataInput in, Statement statement) throws ProtocolException, IOException
	{
		if (!(statement instanceof Context))
			throw new ProtocolException();
		StatementLocal statementLocal = recvStatementLocal(in, statement);
		if (!(statementLocal instanceof ContextLocal))
			throw new ProtocolException();
		ContextLocal contextLocal = (ContextLocal) statementLocal;
		boolean subscribeStatements = booleanProtocol.recv(in);
		contextLocal.setSubscribeStatements(subscribeStatements);
		return contextLocal;
	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		uuidProtocol.skip(in);
		StatementLocalCode statementLocalCode = statementLocalCodeProtocol.recv(in);
		switch (statementLocalCode)
		{
		case _StatementLocal:
			skipStatementLocal(in);
			break;
		case _ContextLocal:
			skipContextLocal(in);
			break;
		default:
			throw new ProtocolException();
		}
	}

	private void skipStatementLocal(DataInput in) throws IOException
	{
		booleanProtocol.skip(in);
	}

	private void skipContextLocal(DataInput in) throws IOException
	{
		skipStatementLocal(in);
		booleanProtocol.skip(in);
	}

}
