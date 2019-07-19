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
package aletheia.peertopeer.statement.dialog;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import aletheia.model.local.StatementLocal;
import aletheia.model.statement.Statement;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.statement.StatementUuidBijection;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.Bijection;

public abstract class StatementProofDialog extends StatementDialog
{
	private final StatementUuidBijection statementUuidBijection;
	private final Bijection<UUID, Statement> uuidStatementBijection;

	public static class StatementStackEntry
	{
		private final UUID statementUuid;

		protected static abstract class SubscriptionDependencies
		{
			protected boolean isInfinite()
			{
				return this instanceof InfiniteSubscriptionDependencies;
			}

			protected abstract boolean anyStillSubscribed(PersistenceManager persistenceManager, Transaction transaction);

			protected abstract SubscriptionDependencies combine(SubscriptionDependencies subscriptionDependencies);

			@Override
			public int hashCode()
			{
				return 1;
			}

			@Override
			public boolean equals(Object obj)
			{
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				return true;
			}

		}

		protected static class SetSubscriptionDependencies extends SubscriptionDependencies
		{
			private final Set<UUID> set;

			private SetSubscriptionDependencies(Set<UUID> set)
			{
				this.set = set;
			}

			private SetSubscriptionDependencies(UUID statementUuid)
			{
				this(new HashSet<>(Arrays.asList(statementUuid)));
			}

			protected Set<UUID> getSet()
			{
				return set;
			}

			@Override
			protected boolean anyStillSubscribed(PersistenceManager persistenceManager, Transaction transaction)
			{
				Iterator<UUID> iterator = set.iterator();
				while (iterator.hasNext())
				{
					UUID uuid = iterator.next();
					StatementLocal statementLocal = persistenceManager.getStatementLocal(transaction, uuid);
					if (statementLocal != null && statementLocal.isSubscribeProof())
						return true;
					iterator.remove();
				}
				return false;
			}

			@Override
			protected SubscriptionDependencies combine(SubscriptionDependencies subscriptionDependencies)
			{
				if (subscriptionDependencies == null)
					return this;
				else if (subscriptionDependencies instanceof SetSubscriptionDependencies)
				{
					SetSubscriptionDependencies setSubscriptionDependencies = (SetSubscriptionDependencies) subscriptionDependencies;
					Set<UUID> combinedSet = new HashSet<>(set);
					combinedSet.addAll(setSubscriptionDependencies.getSet());
					if (combinedSet.size() < 20)
						return new SetSubscriptionDependencies(combinedSet);
					else
						return new InfiniteSubscriptionDependencies();
				}
				else if (subscriptionDependencies instanceof InfiniteSubscriptionDependencies)
				{
					return subscriptionDependencies;
				}
				else
					throw new Error();
			}

			@Override
			public int hashCode()
			{
				final int prime = 31;
				int result = super.hashCode();
				result = prime * result + ((set == null) ? 0 : set.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj)
			{
				if (this == obj)
					return true;
				if (!super.equals(obj))
					return false;
				if (getClass() != obj.getClass())
					return false;
				SetSubscriptionDependencies other = (SetSubscriptionDependencies) obj;
				if (set == null)
				{
					if (other.set != null)
						return false;
				}
				else if (!set.equals(other.set))
					return false;
				return true;
			}

		}

		protected static class InfiniteSubscriptionDependencies extends SubscriptionDependencies
		{
			private InfiniteSubscriptionDependencies()
			{

			}

			@Override
			protected boolean anyStillSubscribed(PersistenceManager persistenceManager, Transaction transaction)
			{
				return true;
			}

			@Override
			protected SubscriptionDependencies combine(SubscriptionDependencies subscriptionDependencies)
			{
				return this;
			}

		}

		private final SubscriptionDependencies subscriptionDependencies;

		protected StatementStackEntry(UUID statementUuid, SubscriptionDependencies subscriptionDependencies)
		{
			super();
			this.statementUuid = statementUuid;
			this.subscriptionDependencies = subscriptionDependencies;
		}

		protected StatementStackEntry(UUID statementUuid)
		{
			this(statementUuid, new SetSubscriptionDependencies(statementUuid));
		}

		protected UUID getStatementUuid()
		{
			return statementUuid;
		}

		protected SubscriptionDependencies getSubscriptionDependencies()
		{
			return subscriptionDependencies;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((statementUuid == null) ? 0 : statementUuid.hashCode());
			result = prime * result + ((subscriptionDependencies == null) ? 0 : subscriptionDependencies.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			StatementStackEntry other = (StatementStackEntry) obj;
			if (statementUuid == null)
			{
				if (other.statementUuid != null)
					return false;
			}
			else if (!statementUuid.equals(other.statementUuid))
				return false;
			if (subscriptionDependencies == null)
			{
				if (other.subscriptionDependencies != null)
					return false;
			}
			else if (!subscriptionDependencies.equals(other.subscriptionDependencies))
				return false;
			return true;
		}

	}

	private final Stack<StatementStackEntry> statementStack;

	private boolean sending;
	private boolean receiving;

	public StatementProofDialog(Phase phase, Stack<StatementStackEntry> statementStack, boolean sending, boolean receiving)
	{
		super(phase);
		this.statementUuidBijection = new StatementUuidBijection(getPersistenceManager(), getTransaction());
		this.uuidStatementBijection = statementUuidBijection.inverse();
		this.statementStack = statementStack;
		this.sending = sending;
		this.receiving = receiving;
	}

	public StatementProofDialog(Phase phase)
	{
		this(phase, new Stack<StatementStackEntry>(), false, false);
	}

	protected StatementUuidBijection getStatementUuidBijection()
	{
		return statementUuidBijection;
	}

	protected Bijection<UUID, Statement> getUuidStatementBijection()
	{
		return uuidStatementBijection;
	}

	public Stack<StatementStackEntry> getStatementStack()
	{
		return statementStack;
	}

	public boolean isSending()
	{
		return sending;
	}

	protected void setSending(boolean sending)
	{
		this.sending = sending;
	}

	public boolean isReceiving()
	{
		return receiving;
	}

	protected void setReceiving(boolean receiving)
	{
		this.receiving = receiving;
	}

}
