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
package aletheia.persistence;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.UUID;

import aletheia.model.authority.Person;
import aletheia.model.authority.SignatureRequest;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.local.ContextLocal;
import aletheia.model.local.RootContextLocal;
import aletheia.model.nomenclator.Nomenclator;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;

public class PersistenceListenerManager
{

	public class Listeners<L extends PersistenceListener> implements Iterable<L>
	{
		private final Collection<L> listeners;

		private Listeners()
		{
			this.listeners = new LinkedHashSet<L>();
		}

		public synchronized void add(L listener)
		{
			listeners.add(listener);
		}

		public synchronized void remove(L listener)
		{
			listeners.remove(listener);
		}

		public synchronized void clear()
		{
			listeners.clear();
		}

		@Override
		public synchronized Iterator<L> iterator()
		{
			return listeners.iterator();
		}

		public synchronized boolean isEmpty()
		{
			return listeners.isEmpty();
		}
	}

	private final Listeners<RootContext.TopStateListener> rootContextTopStateListeners;
	private final Listeners<RootContextLocal.StateListener> rootContextLocalStateListeners;
	private final Listeners<Person.AddStateListener> personAddStateListeners;
	private final Listeners<SignatureRequest.AddStateListener> signatureRequestAddStateListeners;

	public class ListenersByUuid<L extends PersistenceListener>
	{
		private class MyListeners extends Listeners<L>
		{
			private UUID uuid;

			private MyListeners(UUID uuid)
			{
				this.uuid = uuid;
			}

			@Override
			public synchronized void remove(L listener)
			{
				super.remove(listener);
				if (isEmpty())
				{
					synchronized (ListenersByUuid.this)
					{
						map.remove(uuid);
					}
				}
			}

			@Override
			public synchronized void clear()
			{
				super.clear();
				synchronized (ListenersByUuid.this)
				{
					map.remove(uuid);
				}
			}

		}

		private final Map<UUID, MyListeners> map;

		public ListenersByUuid()
		{
			this.map = new HashMap<UUID, MyListeners>();
		}

		public synchronized void add(UUID uuid, L listener)
		{
			MyListeners listeners = map.get(uuid);
			if (listeners == null)
			{
				listeners = new MyListeners(uuid);
				map.put(uuid, listeners);
			}
			listeners.add(listener);
		}

		public synchronized void remove(UUID uuid, L listener)
		{
			Listeners<L> listeners = map.get(uuid);
			if (listeners != null)
				listeners.remove(listener);
		}

		public synchronized Iterable<L> iterable(UUID uuid)
		{
			Listeners<L> listeners = map.get(uuid);
			if (listeners == null)
				return Collections.emptyList();
			return listeners;
		}

		public synchronized Iterable<L> clear(UUID uuid)
		{
			Listeners<L> listeners = map.get(uuid);
			if (listeners == null)
				return Collections.emptyList();
			return listeners;
		}

	}

	private final ListenersByUuid<Nomenclator.Listener> subNomenclatorListeners;
	private final ListenersByUuid<Nomenclator.Listener> rootNomenclatorListeners;
	private final ListenersByUuid<Statement.StateListener> statementStateListeners;
	private final ListenersByUuid<ContextLocal.StateListener> contextLocalStateListeners;
	private final ListenersByUuid<StatementAuthority.StateListener> statementAuthorityStateListeners;
	private final ListenersByUuid<Person.StateListener> personStateListeners;
	private final ListenersByUuid<SignatureRequest.StateListener> signatureRequestStateListeners;

	public PersistenceListenerManager()
	{
		this.rootContextTopStateListeners = new Listeners<RootContext.TopStateListener>();
		this.rootContextLocalStateListeners = new Listeners<RootContextLocal.StateListener>();
		this.personAddStateListeners = new Listeners<Person.AddStateListener>();
		this.signatureRequestAddStateListeners = new Listeners<SignatureRequest.AddStateListener>();

		this.subNomenclatorListeners = new ListenersByUuid<Nomenclator.Listener>();
		this.rootNomenclatorListeners = new ListenersByUuid<Nomenclator.Listener>();
		this.statementStateListeners = new ListenersByUuid<Statement.StateListener>();
		this.contextLocalStateListeners = new ListenersByUuid<ContextLocal.StateListener>();
		this.statementAuthorityStateListeners = new ListenersByUuid<StatementAuthority.StateListener>();
		this.personStateListeners = new ListenersByUuid<Person.StateListener>();
		this.signatureRequestStateListeners = new ListenersByUuid<SignatureRequest.StateListener>();

	}

	public Listeners<RootContext.TopStateListener> getRootContextTopStateListeners()
	{
		return rootContextTopStateListeners;
	}

	public Listeners<RootContextLocal.StateListener> getRootContextLocalStateListeners()
	{
		return rootContextLocalStateListeners;
	}

	public Listeners<Person.AddStateListener> getPersonAddStateListeners()
	{
		return personAddStateListeners;
	}

	public Listeners<SignatureRequest.AddStateListener> getSignatureRequestAddStateListeners()
	{
		return signatureRequestAddStateListeners;
	}

	public ListenersByUuid<Nomenclator.Listener> getSubNomenclatorListeners()
	{
		return subNomenclatorListeners;
	}

	public ListenersByUuid<Nomenclator.Listener> getRootNomenclatorListeners()
	{
		return rootNomenclatorListeners;
	}

	public ListenersByUuid<Statement.StateListener> getStatementStateListeners()
	{
		return statementStateListeners;
	}

	public ListenersByUuid<ContextLocal.StateListener> getContextLocalStateListeners()
	{
		return contextLocalStateListeners;
	}

	public ListenersByUuid<StatementAuthority.StateListener> getStatementAuthorityStateListeners()
	{
		return statementAuthorityStateListeners;
	}

	public ListenersByUuid<Person.StateListener> getPersonStateListeners()
	{
		return personStateListeners;
	}

	public ListenersByUuid<SignatureRequest.StateListener> getSignatureRequestStateListeners()
	{
		return signatureRequestStateListeners;
	}

}
