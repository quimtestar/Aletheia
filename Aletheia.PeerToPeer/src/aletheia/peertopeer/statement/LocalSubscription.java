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

import java.util.Map;
import java.util.UUID;

import aletheia.model.local.ContextLocal;
import aletheia.model.local.RootContextLocal;
import aletheia.model.local.StatementLocal;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.collections.local.SubscribeStatementsRootContextLocalSet;
import aletheia.utilities.collections.AbstractCloseableMap;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCloseableSet;
import aletheia.utilities.collections.CloseableMap;
import aletheia.utilities.collections.CloseableSet;

public class LocalSubscription implements Subscription
{
	private final PersistenceManager persistenceManager;
	private final Transaction transaction;

	public LocalSubscription(PersistenceManager persistenceManager, Transaction transaction)
	{
		this.persistenceManager = persistenceManager;
		this.transaction = transaction;
	}

	@Override
	public CloseableSet<UUID> rootContextUuids()
	{

		SubscribeStatementsRootContextLocalSet subscribeStatementsRootContextLocalSet = persistenceManager.subscribeStatementsRootContextLocalSet(transaction);
		return new BijectionCloseableSet<RootContextLocal, UUID>(new Bijection<RootContextLocal, UUID>()
		{

			@Override
			public UUID forward(RootContextLocal rootContextLocal)
			{
				return rootContextLocal.getStatementUuid();
			}

			@Override
			public RootContextLocal backward(UUID uuid)
			{
				return persistenceManager.getRootContextLocal(transaction, uuid);
			}
		}, subscribeStatementsRootContextLocalSet);
	}

	public class LocalSubContextSubscription implements SubContextSubscription
	{

		private final ContextLocal contextLocal;

		private LocalSubContextSubscription(ContextLocal contextLocal)
		{
			this.contextLocal = contextLocal;
		}

		@Override
		public CloseableSet<UUID> contextUuids()
		{
			return new BijectionCloseableSet<>(new Bijection<ContextLocal, UUID>()
			{

				@Override
				public UUID forward(ContextLocal contextLocal)
				{
					return contextLocal.getStatementUuid();
				}

				@Override
				public ContextLocal backward(UUID uuid)
				{
					return persistenceManager.getContextLocal(transaction, uuid);
				}
			}, contextLocal.subscribeStatementsContextLocalSet(transaction));
		}

		@Override
		public CloseableSet<UUID> proofUuids()
		{
			return new BijectionCloseableSet<>(new Bijection<StatementLocal, UUID>()
			{

				@Override
				public UUID forward(StatementLocal statementLocal)
				{
					return statementLocal.getStatementUuid();
				}

				@Override
				public StatementLocal backward(UUID uuid)
				{
					return persistenceManager.getStatementLocal(transaction, uuid);
				}
			}, contextLocal.subscribeProofStatementLocalSet(transaction));
		}

	}

	@Override
	public CloseableMap<UUID, LocalSubContextSubscription> subContextSubscriptions()
	{
		return new AbstractCloseableMap<UUID, LocalSubContextSubscription>()
		{

			@Override
			public boolean containsKey(Object key)
			{
				return getContextLocal(key) != null;
			}

			private ContextLocal getContextLocal(Object key)
			{
				if (!(key instanceof UUID))
					return null;
				UUID uuid = (UUID) key;
				return persistenceManager.getContextLocal(transaction, uuid);
			}

			@Override
			public LocalSubContextSubscription get(Object key)
			{
				ContextLocal contextLocal = getContextLocal(key);
				if (contextLocal == null)
					return null;
				return new LocalSubContextSubscription(contextLocal);
			}

			@Override
			public CloseableSet<Map.Entry<UUID, LocalSubContextSubscription>> entrySet()
			{
				return new BijectionCloseableSet<>(new Bijection<ContextLocal, Map.Entry<UUID, LocalSubContextSubscription>>()
				{

					@Override
					public Map.Entry<UUID, LocalSubContextSubscription> forward(final ContextLocal contextLocal)
					{
						return new Map.Entry<UUID, LocalSubContextSubscription>()
						{

							@Override
							public UUID getKey()
							{
								return contextLocal.getStatementUuid();
							}

							@Override
							public LocalSubContextSubscription getValue()
							{
								return new LocalSubContextSubscription(contextLocal);
							}

							@Override
							public LocalSubContextSubscription setValue(LocalSubContextSubscription value)
							{
								throw new UnsupportedOperationException();
							}

						};
					}

					@Override
					public ContextLocal backward(Map.Entry<UUID, LocalSubContextSubscription> entry)
					{
						return persistenceManager.getContextLocal(transaction, entry.getKey());
					}
				}, persistenceManager.statementLocalSetMap(transaction).keySet());

			}
		};
	}

}
