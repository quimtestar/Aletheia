/*******************************************************************************
 * Copyright (c) 2014, 2016 Quim Testar.
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

import java.util.Stack;
import java.util.UUID;

import aletheia.model.local.ContextLocal.StateListener;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.entities.local.StatementLocalEntity;
import aletheia.protocol.Exportable;

public class StatementLocal implements Exportable
{
	private final PersistenceManager persistenceManager;
	private final StatementLocalEntity entity;

	private boolean oldSubscribeProof;

	public StatementLocal(PersistenceManager persistenceManager, StatementLocalEntity entity)
	{
		this.persistenceManager = persistenceManager;
		this.entity = entity;
		this.oldSubscribeProof = entity.isSubscribeProof();
	}

	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	public StatementLocalEntity getEntity()
	{
		return entity;
	}

	protected StatementLocal(PersistenceManager persistenceManager, Class<? extends StatementLocalEntity> entityClass, Statement statement)
	{
		this.persistenceManager = persistenceManager;
		this.entity = persistenceManager.instantiateStatementLocalEntity(entityClass);
		this.entity.setStatementUuid(statement.getUuid());
		this.entity.setContextUuid(statement.getContextUuid());
		this.oldSubscribeProof = entity.isSubscribeProof();
	}

	protected StatementLocal(PersistenceManager persistenceManager, Statement statement)
	{
		this(persistenceManager, StatementLocalEntity.class, statement);
	}

	public static StatementLocal create(PersistenceManager persistenceManager, Transaction transaction, Statement statement)
	{
		if (statement instanceof Context)
			return ContextLocal.create(persistenceManager, transaction, (Context) statement);
		else
		{
			StatementLocal statementLocal = new StatementLocal(persistenceManager, statement);
			statementLocal.persistenceUpdate(transaction);
			return statementLocal;
		}
	}

	public void persistenceUpdate(Transaction transaction)
	{
		persistenceManager.putStatementLocal(transaction, this);
		if (oldSubscribeProof != isSubscribeProof())
		{
			if (isSubscribeProof())
			{
				if (!(this instanceof RootContextLocal))
					getContextLocal(transaction).setSubscribeStatements(transaction, true);
			}
			notifyListenersSubscribeProofChanged(transaction);
			oldSubscribeProof = isSubscribeProof();
		}
	}

	protected void notifyListenersSubscribeProofChanged(Transaction transaction)
	{
		ContextLocal contextLocal = getContextLocal(transaction);
		Iterable<StateListener> listeners = contextLocal.stateListeners();
		synchronized (listeners)
		{
			for (StateListener stateListener : listeners)
				stateListener.subscribeProofChanged(transaction, contextLocal, this, isSubscribeProof());
		}
	}

	public UUID getStatementUuid()
	{
		return entity.getStatementUuid();
	}

	public UUID getContextUuid()
	{
		return entity.getContextUuid();
	}

	public boolean isSubscribeProof()
	{
		return entity.isSubscribeProof();
	}

	public void setSubscribeProof(boolean subscribeProof)
	{
		entity.setSubscribeProof(subscribeProof);
	}

	public void setSubscribeProof(Transaction transaction, boolean subscribeProof)
	{
		setSubscribeProof(subscribeProof);
		persistenceUpdate(transaction);
	}

	public StatementLocal refresh(Transaction transaction)
	{
		return persistenceManager.getStatementLocal(transaction, getStatementUuid());
	}

	public ContextLocal getContextLocal(Transaction transaction)
	{
		return (ContextLocal) persistenceManager.getStatementLocal(transaction, getContextUuid());
	}

	public Statement getStatement(Transaction transaction)
	{
		return persistenceManager.getStatement(transaction, getStatementUuid());
	}

	public void delete(Transaction transaction)
	{
		Stack<StatementLocal> stack = new Stack<>();
		stack.push(this);
		while (!stack.isEmpty())
		{
			StatementLocal sl = stack.peek();
			boolean pushed = false;
			if (sl instanceof ContextLocal)
			{
				ContextLocal cl = (ContextLocal) sl;
				for (StatementLocal sl_ : cl.statementLocalSet(transaction))
				{
					stack.push(sl_);
					pushed = true;
				}
			}
			if (!pushed)
			{
				stack.pop();
				if (sl.isSubscribeProof())
				{
					sl.setSubscribeProof(false);
					sl.notifyListenersSubscribeProofChanged(transaction);
				}
				if (sl instanceof ContextLocal)
				{
					ContextLocal cl = (ContextLocal) sl;
					if (cl.isSubscribeStatements())
					{
						cl.setSubscribeStatements(false);
						cl.notifyListenersSubscribeStatementsChanged(transaction);
					}
				}
				getPersistenceManager().deleteStatementLocal(transaction, sl);
			}
		}
	}

	@Override
	public int hashCode()
	{
		return getStatementUuid().hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof StatementLocal))
			return false;
		StatementLocal other = (StatementLocal) obj;
		return getStatementUuid().equals(other.getStatementUuid());
	}

	@Override
	public String toString()
	{
		Transaction transaction = persistenceManager.beginDirtyTransaction();
		try
		{
			return toString(transaction);
		}
		finally
		{
			transaction.abort();
		}
	}

	public String toString(Transaction transaction)
	{
		Statement statement = getStatement(transaction);
		String strIdentifier;
		if (statement == null)
			strIdentifier = "*null*";
		else
			strIdentifier = statement.getVariable().toString(statement.parentVariableToIdentifier(transaction));
		return strIdentifier + " subscribeProof: " + isSubscribeProof();
	}

}
