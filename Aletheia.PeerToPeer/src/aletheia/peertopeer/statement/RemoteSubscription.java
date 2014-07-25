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
package aletheia.peertopeer.statement;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.CloseableMap;
import aletheia.utilities.collections.CloseableSet;
import aletheia.utilities.collections.TrivialCloseableMap;
import aletheia.utilities.collections.TrivialCloseableSet;

public class RemoteSubscription implements Subscription
{
	private final PersistenceManager persistenceManager;
	private final Set<UUID> rootContextUuids;
	private final Map<UUID, RemoteSubContextSubscription> subContextSubscriptions;

	public RemoteSubscription(PersistenceManager persistenceManager)
	{
		this.persistenceManager = persistenceManager;
		this.rootContextUuids = new HashSet<UUID>();
		this.subContextSubscriptions = new HashMap<UUID, RemoteSubContextSubscription>();
	}

	protected PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	public class RemoteSubContextSubscription implements SubContextSubscription
	{
		private final Set<UUID> contextUuids;
		private final Set<UUID> proofUuids;

		private RemoteSubContextSubscription()
		{
			this.contextUuids = new HashSet<UUID>();
			this.proofUuids = new HashSet<UUID>();
		}

		@Override
		public CloseableSet<UUID> contextUuids()
		{
			return new TrivialCloseableSet<>(Collections.unmodifiableSet(contextUuids));
		}

		@Override
		public CloseableSet<UUID> proofUuids()
		{
			return new TrivialCloseableSet<>(Collections.unmodifiableSet(proofUuids));
		}

		public void addContextUuid(UUID contextUuid)
		{
			contextUuids.add(contextUuid);
		}

		public void addProofUuid(UUID proofUuid)
		{
			proofUuids.add(proofUuid);
		}

		public void removeContextUuid(UUID contextUuid)
		{
			contextUuids.remove(contextUuid);
		}

		public void removeProofUuid(UUID proofUuid)
		{
			proofUuids.remove(proofUuid);
		}

	}

	@Override
	public CloseableMap<UUID, RemoteSubContextSubscription> subContextSubscriptions()
	{
		return new TrivialCloseableMap<UUID, RemoteSubContextSubscription>(Collections.unmodifiableMap(subContextSubscriptions));
	}

	@Override
	public CloseableSet<UUID> rootContextUuids()
	{
		return new TrivialCloseableSet<>(Collections.unmodifiableSet(rootContextUuids));
	}

	private void addRootContextUuid(UUID rootContextUuid)
	{
		rootContextUuids.add(rootContextUuid);
	}

	private void removeRootContextUuid(UUID rootContextUuid)
	{
		rootContextUuids.remove(rootContextUuid);
	}

	private RemoteSubContextSubscription addSubContextSubscription(Transaction transaction, UUID parentUuid)
	{
		Context ctx = persistenceManager.getContext(transaction, parentUuid);
		Stack<Context> stack = new Stack<Context>();
		while (!subContextSubscriptions.containsKey(ctx.getUuid()))
		{
			if (ctx instanceof RootContext)
			{
				addRootContextUuid(ctx.getUuid());
				subContextSubscriptions.put(ctx.getUuid(), new RemoteSubContextSubscription());
				break;
			}
			stack.push(ctx);
			ctx = ctx.getContext(transaction);
		}
		RemoteSubContextSubscription sub = subContextSubscriptions.get(ctx.getUuid());
		while (!stack.isEmpty())
		{
			Context ctx_ = stack.pop();
			sub.addContextUuid(ctx_.getUuid());
			sub = new RemoteSubContextSubscription();
			subContextSubscriptions.put(ctx_.getUuid(), sub);
			ctx = ctx_;
		}
		return sub;
	}

	public void addContext(Transaction transaction, Context context)
	{
		if (context instanceof RootContext)
		{
			addRootContextUuid(context.getUuid());
		}
		else
		{
			RemoteSubContextSubscription sub = addSubContextSubscription(transaction, context.getContextUuid());
			sub.addContextUuid(context.getUuid());
		}
	}

	public void removeContext(Transaction transaction, Context context)
	{
		if (context instanceof RootContext)
			removeRootContextUuid(context.getUuid());
		else
		{
			RemoteSubContextSubscription sub = subContextSubscriptions.get(context.getContextUuid());
			if (sub != null)
				sub.removeContextUuid(context.getUuid());
		}
		Stack<UUID> stack = new Stack<UUID>();
		stack.push(context.getUuid());
		while (!stack.isEmpty())
		{
			UUID ctxUuid = stack.pop();
			RemoteSubContextSubscription sub = subContextSubscriptions.remove(ctxUuid);
			if (sub != null)
				stack.addAll(sub.contextUuids);
		}
	}

	public void addProof(Transaction transaction, Statement statement)
	{
		RemoteSubContextSubscription sub = addSubContextSubscription(transaction, statement.getContextUuid());
		sub.addProofUuid(statement.getUuid());
	}

	public void removeProof(Transaction transaction, Statement statement)
	{
		RemoteSubContextSubscription sub = subContextSubscriptions.get(statement.getContextUuid());
		if (sub != null)
			sub.removeProofUuid(statement.getUuid());
	}

	public boolean isContextSubscribed(Transaction transaction, Context context)
	{
		if (context instanceof RootContext)
			return rootContextUuids.contains(context.getUuid());
		else
		{
			RemoteSubContextSubscription sub = subContextSubscriptions().get(context.getContextUuid());
			if (sub == null)
				return false;
			return sub.contextUuids.contains(context.getUuid());
		}
	}

	public boolean isProofSubscribed(Transaction transaction, Statement statement)
	{
		if (statement instanceof RootContext)
			return true;
		else
		{
			RemoteSubContextSubscription sub = subContextSubscriptions().get(statement.getContextUuid());
			if (sub == null)
				return false;
			return sub.proofUuids.contains(statement.getUuid());
		}
	}
}
