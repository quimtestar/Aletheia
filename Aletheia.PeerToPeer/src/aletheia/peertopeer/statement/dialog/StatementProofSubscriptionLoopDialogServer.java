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
package aletheia.peertopeer.statement.dialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import aletheia.model.authority.StatementAuthority;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.statement.RemoteSubscription;
import aletheia.peertopeer.statement.message.AvailableProofsMessage;
import aletheia.peertopeer.statement.message.StatementsSubscribeMessage;
import aletheia.protocol.ProtocolException;

public class StatementProofSubscriptionLoopDialogServer extends StatementProofSubscriptionLoopDialog
{

	public StatementProofSubscriptionLoopDialogServer(Phase phase)
	{
		super(phase);
	}

	private void dialogateAvailableProofsServer() throws IOException, ProtocolException, InterruptedException
	{
		RemoteSubscription remoteSubscription = getRemoteSubscription();
		StatementsSubscribeMessage statementSubscribeMessage = recvMessage(StatementsSubscribeMessage.class);
		List<UUID> statementUuids = new ArrayList<>();
		for (UUID uuid : statementSubscribeMessage.getSubscribedUuids())
		{
			Statement st = getPersistenceManager().getStatement(getTransaction(), uuid);
			if (st != null)
			{
				if (!(st instanceof RootContext))
					remoteSubscription.addProof(getTransaction(), st);
				StatementAuthority stAuth = st.getAuthority(getTransaction());
				if ((stAuth != null) && stAuth.isSignedProof())
					statementUuids.add(uuid);
			}
		}
		for (UUID uuid : statementSubscribeMessage.getUnsubscribedUuids())
		{
			Statement st = getPersistenceManager().getStatement(getTransaction(), uuid);
			if (st != null)
			{
				if (!(st instanceof RootContext))
					remoteSubscription.removeProof(getTransaction(), st);
			}
		}
		sendMessage(new AvailableProofsMessage(statementUuids));
	}

	@Override
	protected void dialogate() throws IOException, ProtocolException, InterruptedException
	{
		dialogateAvailableProofsServer();
		setSending(true);
		setReceiving(false);
	}

}
