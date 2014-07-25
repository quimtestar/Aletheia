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
package aletheia.persistence.berkeleydb.collections.local;

import aletheia.model.local.ContextLocal;
import aletheia.model.local.RootContextLocal;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.collections.local.SubscribeStatementsRootContextLocalSet;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCloseableSet;

public class BerkeleyDBSubscribeStatementsRootContextLocalSet extends BijectionCloseableSet<ContextLocal, RootContextLocal> implements
SubscribeStatementsRootContextLocalSet
{
	private final BerkeleyDBPersistenceManager persistenceManager;
	private final BerkeleyDBTransaction transaction;

	public BerkeleyDBSubscribeStatementsRootContextLocalSet(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction)
	{
		super(new Bijection<ContextLocal, RootContextLocal>()
				{

			@Override
			public ContextLocal backward(RootContextLocal output)
			{
				return output;
			}

			@Override
			public RootContextLocal forward(ContextLocal input)
			{
				return (RootContextLocal) input;
			}
				}, new BerkeleyDBSubscribeStatementsContextLocalSet(persistenceManager, transaction));

		this.persistenceManager = persistenceManager;
		this.transaction = transaction;
	}

	@Override
	public BerkeleyDBPersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	@Override
	public BerkeleyDBTransaction getTransaction()
	{
		return transaction;
	}

}
