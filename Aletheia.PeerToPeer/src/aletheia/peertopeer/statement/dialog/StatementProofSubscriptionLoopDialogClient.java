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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import aletheia.model.local.ContextLocal;
import aletheia.model.local.RootContextLocal;
import aletheia.model.local.StatementLocal;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.statement.PendingPersistentDataChanges;
import aletheia.peertopeer.statement.RemoteSubscription;
import aletheia.peertopeer.statement.Subscription.SubContextSubscription;
import aletheia.peertopeer.statement.message.AvailableProofsMessage;
import aletheia.peertopeer.statement.message.StatementsSubscribeMessage;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.collections.AdaptedSet;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCollection;

public class StatementProofSubscriptionLoopDialogClient extends StatementProofSubscriptionLoopDialog
{

	public StatementProofSubscriptionLoopDialogClient(Phase phase)
	{
		super(phase);
	}

	private AvailableProofsMessage dialogateAvailableProofsClient(Set<StatementLocal> subscribedProofStatementLocals)
			throws IOException, ProtocolException, InterruptedException
	{
		Bijection<StatementLocal, UUID> statementLocalUuidBijection = new Bijection<StatementLocal, UUID>()
		{

			@Override
			public UUID forward(StatementLocal input)
			{
				return input.getStatementUuid();
			}

			@Override
			public StatementLocal backward(UUID output)
			{
				throw new UnsupportedOperationException();
			}
		};
		RemoteSubscription remoteSubscription = getRemoteSubscription();
		PendingPersistentDataChanges pendingStatementLocalChanges = getPendingPersistentDataChanges();
		Set<UUID> subscribedUuids = new HashSet<>();
		Set<RootContextLocal> pendingSubscribedProofRootContextLocals = pendingStatementLocalChanges.dumpPendingSubscribedProofRootContextLocals();
		if (pendingSubscribedProofRootContextLocals != null)
		{
			subscribedProofStatementLocals.addAll(pendingSubscribedProofRootContextLocals);
			subscribedUuids
					.addAll(new BijectionCollection<>(statementLocalUuidBijection, new AdaptedSet<StatementLocal>(pendingSubscribedProofRootContextLocals)));
		}
		Set<UUID> unsubscribedUuids = new HashSet<>();
		Set<RootContextLocal> pendingUnsubscribedProofRootContextLocals = pendingStatementLocalChanges.dumpPendingUnsubscribedProofRootContextLocals();
		if (pendingUnsubscribedProofRootContextLocals != null)
		{
			unsubscribedUuids
					.addAll(new BijectionCollection<>(statementLocalUuidBijection, new AdaptedSet<StatementLocal>(pendingUnsubscribedProofRootContextLocals)));
		}
		Set<UUID> contextUuids = remoteSubscription.rootContextUuids();
		while (!contextUuids.isEmpty())
		{
			Set<UUID> contextUuids_ = new HashSet<>();
			for (UUID contextUuid : contextUuids)
			{
				SubContextSubscription subContextSubscription = remoteSubscription.subContextSubscriptions().get(contextUuid);
				if (subContextSubscription != null)
					contextUuids_.addAll(subContextSubscription.contextUuids());
				ContextLocal ctxLocal = getPersistenceManager().getContextLocal(getTransaction(), contextUuid);
				if (ctxLocal != null)
				{
					Set<StatementLocal> pendingSubscribedProofStatementLocals = pendingStatementLocalChanges
							.dumpPendingSubscribedProofStatementLocals(ctxLocal);
					if (pendingSubscribedProofStatementLocals != null)
					{
						subscribedProofStatementLocals.addAll(pendingSubscribedProofStatementLocals);
						subscribedUuids.addAll(new BijectionCollection<>(statementLocalUuidBijection, pendingSubscribedProofStatementLocals));
					}
					Set<StatementLocal> pendingUnsubscribedProofStatementLocals = pendingStatementLocalChanges
							.dumpPendingUnsubscribedProofStatementLocals(ctxLocal);
					if (pendingUnsubscribedProofStatementLocals != null)
					{
						unsubscribedUuids.addAll(new BijectionCollection<>(statementLocalUuidBijection, pendingUnsubscribedProofStatementLocals));
					}
				}
			}
			contextUuids = contextUuids_;
		}
		sendMessage(new StatementsSubscribeMessage(subscribedUuids, unsubscribedUuids));
		return recvMessage(AvailableProofsMessage.class);
	}

	@Override
	protected void dialogate() throws IOException, ProtocolException, InterruptedException
	{
		Set<StatementLocal> subscribedProofStatementLocals = new HashSet<>();
		try
		{
			AvailableProofsMessage availableProofsMessage = dialogateAvailableProofsClient(subscribedProofStatementLocals);
			for (UUID uuid : availableProofsMessage.getUuids())
				getStatementStack().push(new StatementStackEntry(uuid));
			setSending(false);
			setReceiving(true);
		}
		catch (Exception e)
		{
			getPendingPersistentDataChanges().subscribeProofStatementsChanged(getTransaction(), subscribedProofStatementLocals, true);
			throw e;
		}
	}

}
