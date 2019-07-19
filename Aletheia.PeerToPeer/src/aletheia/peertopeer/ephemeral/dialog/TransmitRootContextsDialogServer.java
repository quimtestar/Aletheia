/*******************************************************************************
 * Copyright (c) 2014, 2016 Quim Testar.
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
package aletheia.peertopeer.ephemeral.dialog;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.ephemeral.message.RootContextStatementSignaturesResponseMessage;
import aletheia.peertopeer.statement.message.PersonResponseMessage;
import aletheia.peertopeer.statement.message.StatementResponseMessage;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.io.NonBlockingSocketChannelStream.TimeoutException;

public class TransmitRootContextsDialogServer extends TransmitRootContextsDialog
{
	private final Map<UUID, RootContext> rootContexts;

	public TransmitRootContextsDialogServer(Phase phase)
	{
		super(phase);
		this.rootContexts = new HashMap<>();
	}

	public Map<UUID, RootContext> getRootContexts()
	{
		return rootContexts;
	}

	@Override
	protected void dialogate() throws IOException, ProtocolException, InterruptedException, TimeoutException
	{
		RootContextStatementSignaturesResponseMessage rootContextStatementSignaturesResponseMessage = recvMessage(
				RootContextStatementSignaturesResponseMessage.class);
		dialogatePersonStatementRequestMessage(rootContextStatementSignaturesResponseMessage, rootContexts);
		recvMessage(PersonResponseMessage.class);
		StatementResponseMessage statementResponseMessage = recvMessage(StatementResponseMessage.class);
		for (Statement statement : statementResponseMessage.getMap().values())
		{
			if (!statement.isValidSignature(getTransaction()))
				throw new ProtocolException();
			if (statement instanceof RootContext)
			{
				RootContext rootCtx = (RootContext) statement;
				rootContexts.put(rootCtx.getSignatureUuid(getTransaction()), rootCtx);
			}
		}
	}

}
