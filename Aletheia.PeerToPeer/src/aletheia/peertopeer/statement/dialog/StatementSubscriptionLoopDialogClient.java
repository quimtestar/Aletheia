/*******************************************************************************
 * Copyright (c) 2014, 2019 Quim Testar.
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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import aletheia.model.local.ContextLocal;
import aletheia.model.local.RootContextLocal;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.statement.PendingPersistentDataChanges;
import aletheia.peertopeer.statement.RemoteSubscription;
import aletheia.peertopeer.statement.Subscription;
import aletheia.peertopeer.statement.message.StatementsSubscribeConfirmationMessage;
import aletheia.peertopeer.statement.message.StatementsSubscribeMessage;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.collections.AdaptedSet;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionSet;
import aletheia.utilities.collections.CastBijection;
import aletheia.utilities.collections.ComposedBijection;

public class StatementSubscriptionLoopDialogClient extends StatementSubscriptionLoopDialog
{

	public StatementSubscriptionLoopDialogClient(Phase phase)
	{
		super(phase);
	}

	private Set<Context> dialogateStatementsSubscribeClient(Set<ContextLocal> subscribedContextLocals, Set<ContextLocal> unsubscribedContextLocals)
			throws IOException, ProtocolException, InterruptedException
	{
		Bijection<ContextLocal, UUID> contextLocalUuidBijection = new Bijection<>()
		{

			@Override
			public UUID forward(ContextLocal ctxLocal)
			{
				return ctxLocal.getStatementUuid();
			}

			@Override
			public ContextLocal backward(UUID uuid)
			{
				throw new UnsupportedOperationException();
			}
		};

		Set<UUID> subscribeUuids = new HashSet<>();
		Set<UUID> unsubscribeUuids = new HashSet<>();
		PendingPersistentDataChanges pendingStatementLocalChanges = getPendingPersistentDataChanges();
		RemoteSubscription remoteSubscription = getRemoteSubscription();
		Set<ContextLocal> pendingUnsubscribedContextLocals;
		{
			Set<RootContextLocal> set = pendingStatementLocalChanges.dumpPendingUnsubscribedRootContextLocals();
			if (set != null)
			{
				subscribedContextLocals.addAll(set);
				pendingUnsubscribedContextLocals = new AdaptedSet<>(set);
			}
			else
				pendingUnsubscribedContextLocals = Collections.emptySet();
		}
		Set<ContextLocal> pendingSubscribedContextLocals;
		{
			Set<RootContextLocal> set = pendingStatementLocalChanges.dumpPendingSubscribedRootContextLocals();
			if (set != null)
			{
				subscribedContextLocals.addAll(set);
				pendingSubscribedContextLocals = new AdaptedSet<>(set);
			}
			else
				pendingSubscribedContextLocals = Collections.emptySet();
		}
		Set<UUID> contextUuids = remoteSubscription.rootContextUuids();
		while (!contextUuids.isEmpty())
		{
			Set<UUID> contextUuids_ = new HashSet<>();
			Set<ContextLocal> pendingUnsubscribedContextLocals_ = new HashSet<>();
			Set<ContextLocal> pendingSubscribedContextLocals_ = new HashSet<>();
			subscribeUuids.addAll(new BijectionSet<>(contextLocalUuidBijection, pendingSubscribedContextLocals));
			for (UUID contextUuid : contextUuids)
			{
				ContextLocal ctxLocal = getPersistenceManager().getContextLocal(getTransaction(), contextUuid);
				if (ctxLocal != null)
				{
					if (pendingUnsubscribedContextLocals.contains(ctxLocal))
						unsubscribeUuids.add(contextUuid);
					Subscription.SubContextSubscription subcontextSubscription = remoteSubscription.subContextSubscriptions().get(contextUuid);
					if (subcontextSubscription != null)
						contextUuids_.addAll(subcontextSubscription.contextUuids());
					{
						Set<ContextLocal> set = pendingStatementLocalChanges.dumpPendingUnsubscribedContextLocals(ctxLocal);
						if (set != null)
						{
							unsubscribedContextLocals.addAll(set);
							pendingUnsubscribedContextLocals_.addAll(set);
						}
					}
					{
						Set<ContextLocal> set = pendingStatementLocalChanges.dumpPendingSubscribedContextLocals(ctxLocal);
						if (set != null)
						{
							subscribedContextLocals.addAll(set);
							pendingSubscribedContextLocals_.addAll(set);
						}
					}
				}
			}
			contextUuids = contextUuids_;
			pendingUnsubscribedContextLocals = pendingUnsubscribedContextLocals_;
			pendingSubscribedContextLocals = pendingSubscribedContextLocals_;
		}
		subscribeUuids.addAll(new BijectionSet<>(contextLocalUuidBijection, pendingSubscribedContextLocals));

		sendMessage(new StatementsSubscribeMessage(subscribeUuids, unsubscribeUuids));
		StatementsSubscribeConfirmationMessage statementsSubscribeConfirmationMessage = recvMessage(StatementsSubscribeConfirmationMessage.class);
		subscribeUuids.retainAll(statementsSubscribeConfirmationMessage.getUuids());
		return new BijectionSet<>(new ComposedBijection<>(new CastBijection<Statement, Context>(), getStatementUuidBijection().inverse()), subscribeUuids);
	}

	@Override
	protected void dialogate() throws IOException, ProtocolException, InterruptedException
	{
		Set<ContextLocal> subscribedContextLocals = new HashSet<>();
		Set<ContextLocal> unsubscribedContextLocals = new HashSet<>();
		try
		{
			Set<Context> contexts = dialogateStatementsSubscribeClient(subscribedContextLocals, unsubscribedContextLocals);
			if (!contexts.isEmpty())
				dialogateStatementSubscriptions(contexts);
		}
		catch (Exception e)
		{
			getPendingPersistentDataChanges().subscribeStatementsChanged(getTransaction(), subscribedContextLocals, true);
			getPendingPersistentDataChanges().subscribeStatementsChanged(getTransaction(), unsubscribedContextLocals, false);
			throw e;
		}
	}

}
