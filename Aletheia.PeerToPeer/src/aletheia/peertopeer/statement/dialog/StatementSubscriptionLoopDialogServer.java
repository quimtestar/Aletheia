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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import aletheia.model.local.ContextLocal;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.statement.message.StatementsSubscribeConfirmationMessage;
import aletheia.peertopeer.statement.message.StatementsSubscribeMessage;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.collections.AdaptedCollection;
import aletheia.utilities.collections.BijectionCollection;

public class StatementSubscriptionLoopDialogServer extends StatementSubscriptionLoopDialog
{

	public StatementSubscriptionLoopDialogServer(Phase phase)
	{
		super(phase);
	}

	private Set<Context> dialogateStatementsSubscribeServer() throws IOException, ProtocolException, InterruptedException
	{
		StatementsSubscribeMessage statementsSubscribeMessage = recvMessage(StatementsSubscribeMessage.class);
		Set<Context> subscribe = new HashSet<Context>();
		for (UUID uuid : statementsSubscribeMessage.getSubscribedUuids())
		{
			Context context = getPersistenceManager().getContext(getTransaction(), uuid);
			if (context != null)
			{
				ContextLocal contextLocal = context.getLocal(getTransaction());
				if (contextLocal != null && contextLocal.isSubscribeStatements())
					subscribe.add(context);
			}
		}
		for (UUID uuid : statementsSubscribeMessage.getUnsubscribedUuids())
		{
			Context context = getPersistenceManager().getContext(getTransaction(), uuid);
			if (context != null)
				getRemoteSubscription().removeContext(getTransaction(), context);
		}
		Collection<UUID> subscribeUuids = new BijectionCollection<Statement, UUID>(getStatementUuidBijection(), new AdaptedCollection<Statement>(subscribe));
		sendMessage(new StatementsSubscribeConfirmationMessage(subscribeUuids));
		return subscribe;
	}

	@Override
	protected void dialogate() throws IOException, ProtocolException, InterruptedException
	{
		Set<Context> contexts = dialogateStatementsSubscribeServer();
		if (!contexts.isEmpty())
			dialogateStatementSubscriptions(contexts);

	}
}
