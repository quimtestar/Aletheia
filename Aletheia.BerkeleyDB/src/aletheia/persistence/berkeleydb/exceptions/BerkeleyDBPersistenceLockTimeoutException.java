/*******************************************************************************
 * Copyright (c) 2014, 2020 Quim Testar.
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
package aletheia.persistence.berkeleydb.exceptions;

import java.util.Collection;

import com.sleepycat.je.LockTimeoutException;

import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.exceptions.PersistenceLockTimeoutException;
import aletheia.utilities.collections.AdaptedCollection;
import aletheia.utilities.collections.ArrayAsList;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCollection;
import aletheia.utilities.collections.FilteredCollection;
import aletheia.utilities.collections.NotNullFilter;

public class BerkeleyDBPersistenceLockTimeoutException extends PersistenceLockTimeoutException
{
	private static final long serialVersionUID = 8409925429410693331L;

	private static Collection<Transaction> transactions(final BerkeleyDBPersistenceManager persistenceManager, long[] ids)
	{
		return new FilteredCollection<>(new NotNullFilter<Transaction>(),

				new AdaptedCollection<Transaction>(new BijectionCollection<>(new Bijection<Long, BerkeleyDBTransaction>()
				{

					@Override
					public BerkeleyDBTransaction forward(Long id)
					{
						return persistenceManager.openedTransactionsMap().get(id);
					}

					@Override
					public Long backward(BerkeleyDBTransaction transaction)
					{
						return transaction.getDbTransactionId();
					}
				}, new ArrayAsList<Long>(ids))));
	}

	public BerkeleyDBPersistenceLockTimeoutException(BerkeleyDBPersistenceManager persistenceManager, LockTimeoutException cause)
	{
		super(cause, transactions(persistenceManager, cause.getOwnerTxnIds()), transactions(persistenceManager, cause.getWaiterTxnIds()));
	}

}
