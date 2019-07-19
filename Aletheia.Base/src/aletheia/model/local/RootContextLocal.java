/*******************************************************************************
 * Copyright (c) 2014, 2018 Quim Testar.
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

import aletheia.model.statement.RootContext;
import aletheia.persistence.PersistenceListener;
import aletheia.persistence.PersistenceListenerManager.Listeners;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.entities.local.RootContextLocalEntity;

public class RootContextLocal extends ContextLocal
{

	public RootContextLocal(PersistenceManager persistenceManager, RootContextLocalEntity entity)
	{
		super(persistenceManager, entity);
	}

	protected RootContextLocal(PersistenceManager persistenceManager, RootContext context)
	{
		super(persistenceManager, RootContextLocalEntity.class, context);
	}

	@Override
	public RootContextLocalEntity getEntity()
	{
		return (RootContextLocalEntity) super.getEntity();
	}

	public static RootContextLocal create(PersistenceManager persistenceManager, Transaction transaction, RootContext rootContext)
	{
		RootContextLocal rootContextLocal = new RootContextLocal(persistenceManager, rootContext);
		rootContextLocal.persistenceUpdate(transaction);
		return rootContextLocal;
	}

	@Override
	public ContextLocal getContextLocal(Transaction transaction)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public RootContext getStatement(Transaction transaction)
	{
		return (RootContext) super.getStatement(transaction);
	}

	public interface StateListener extends PersistenceListener
	{
		public default void subscribeProofChanged(Transaction transaction, RootContextLocal rootContextLocal, boolean subscribed)
		{
		}

		public default void subscribeStatementsChanged(Transaction transaction, RootContextLocal rootContextLocal, boolean subscribed)
		{
		}
	}

	@Override
	protected void notifyListenersSubscribeStatementsChanged(Transaction transaction)
	{
		Listeners<StateListener> listeners = getPersistenceManager().getListenerManager().getRootContextLocalStateListeners();
		synchronized (listeners)
		{
			for (StateListener stateListener : listeners)
				stateListener.subscribeStatementsChanged(transaction, this, isSubscribeStatements());
		}
	}

	@Override
	protected void notifyListenersSubscribeProofChanged(Transaction transaction)
	{
		Listeners<StateListener> listeners = getPersistenceManager().getListenerManager().getRootContextLocalStateListeners();
		synchronized (listeners)
		{
			for (StateListener stateListener : listeners)
				stateListener.subscribeProofChanged(transaction, this, isSubscribeProof());
		}
	}

}
