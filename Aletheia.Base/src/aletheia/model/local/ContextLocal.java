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
package aletheia.model.local;

import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.persistence.PersistenceListener;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.collections.local.StatementLocalSet;
import aletheia.persistence.collections.local.SubscribeProofStatementLocalSet;
import aletheia.persistence.collections.local.SubscribeStatementsContextLocalSet;
import aletheia.persistence.entities.local.ContextLocalEntity;

public class ContextLocal extends StatementLocal
{
	private boolean oldSubscribeStatements;

	public ContextLocal(PersistenceManager persistenceManager, ContextLocalEntity entity)
	{
		super(persistenceManager, entity);
		this.oldSubscribeStatements = entity.isSubscribeStatements();
	}

	@Override
	public ContextLocalEntity getEntity()
	{
		return (ContextLocalEntity) super.getEntity();
	}

	protected ContextLocal(PersistenceManager persistenceManager, Class<? extends ContextLocalEntity> entityClass, Context context)
	{
		super(persistenceManager, entityClass, context);
		this.oldSubscribeStatements = isSubscribeStatements();
	}

	protected ContextLocal(PersistenceManager persistenceManager, Context context)
	{
		this(persistenceManager, ContextLocalEntity.class, context);
	}

	public static ContextLocal create(PersistenceManager persistenceManager, Transaction transaction, Context context)
	{
		if (context instanceof RootContext)
			return RootContextLocal.create(persistenceManager, transaction, (RootContext) context);
		else
		{
			ContextLocal contextLocal = new ContextLocal(persistenceManager, context);
			contextLocal.persistenceUpdate(transaction);
			return contextLocal;
		}
	}

	public boolean isSubscribeStatements()
	{
		return getEntity().isSubscribeStatements();
	}

	public void setSubscribeStatements(boolean subscribeStatements)
	{
		getEntity().setSubscribeStatements(subscribeStatements);
	}

	public void setSubscribeStatements(Transaction transaction, boolean subscribeStatements)
	{
		setSubscribeStatements(subscribeStatements);
		persistenceUpdate(transaction);
	}

	@Override
	public ContextLocal refresh(Transaction transaction)
	{
		return (ContextLocal) super.refresh(transaction);
	}

	public SubscribeProofStatementLocalSet subscribeProofStatementLocalSet(Transaction transaction)
	{
		return getPersistenceManager().subscribeProofStatementLocalSet(transaction, this);
	}

	public SubscribeStatementsContextLocalSet subscribeStatementsContextLocalSet(Transaction transaction)
	{
		return getPersistenceManager().subscribeStatementsContextLocalSet(transaction, this);
	}

	@Override
	public Context getStatement(Transaction transaction)
	{
		return (Context) super.getStatement(transaction);
	}

	public StatementLocalSet statementLocalSet(Transaction transaction)
	{
		return getPersistenceManager().statementLocalSet(transaction, this);
	}

	public interface StateListener extends PersistenceListener
	{
		public void subscribeProofChanged(Transaction transaction, ContextLocal contextLocal, StatementLocal statementLocal, boolean subscribed);

		public void subscribeStatementsChanged(Transaction transaction, ContextLocal contextLocal, ContextLocal contextLocal_, boolean subscribed);
	}

	public void addStateListener(StateListener stateListener)
	{
		getPersistenceManager().getListenerManager().getContextLocalStateListeners().add(getStatementUuid(), stateListener);
	}

	public void removeStateListener(StateListener stateListener)
	{
		getPersistenceManager().getListenerManager().getContextLocalStateListeners().remove(getStatementUuid(), stateListener);
	}

	/**
	 * Return the set of listeners of this {@link ContextLocal}.
	 * 
	 * @return The set of listeners.
	 */
	protected Iterable<StateListener> stateListeners()
	{
		return getPersistenceManager().getListenerManager().getContextLocalStateListeners().iterable(getStatementUuid());
	}

	@Override
	public void persistenceUpdate(Transaction transaction)
	{
		super.persistenceUpdate(transaction);
		if (oldSubscribeStatements != isSubscribeStatements())
		{
			if (isSubscribeStatements())
			{
				if (!(this instanceof RootContextLocal))
					getContextLocal(transaction).setSubscribeStatements(transaction, true);
			}
			else
			{
				for (ContextLocal subContextLocal : subscribeStatementsContextLocalSet(transaction))
					subContextLocal.setSubscribeStatements(transaction, false);
				for (StatementLocal statementLocal : subscribeProofStatementLocalSet(transaction))
					statementLocal.setSubscribeProof(transaction, false);
			}
			notifyListenersSubscribeStatementsChanged(transaction);
			oldSubscribeStatements = isSubscribeStatements();
		}
	}

	protected void notifyListenersSubscribeStatementsChanged(Transaction transaction)
	{
		ContextLocal contextLocal = getContextLocal(transaction);
		Iterable<StateListener> listeners = contextLocal.stateListeners();
		synchronized (listeners)
		{
			for (StateListener stateListener : listeners)
				stateListener.subscribeStatementsChanged(transaction, contextLocal, this, isSubscribeStatements());
		}
	}

	@Override
	public String toString(Transaction transaction)
	{
		return super.toString(transaction) + " subscribeStatements: " + isSubscribeStatements();
	}

}
