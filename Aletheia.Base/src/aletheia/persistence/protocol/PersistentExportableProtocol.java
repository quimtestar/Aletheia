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
package aletheia.persistence.protocol;

import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.protocol.Exportable;
import aletheia.protocol.ExportableProtocol;
import aletheia.protocol.ProtocolInfo;

/**
 * Extension of the {@link ExportableProtocol} for objects stored into the
 * persistent environment. A single transaction will be used for all the
 * send/receive operations.
 *
 * @param <E>
 *            The class of objects to export.
 */
@ProtocolInfo(availableVersions = 0)
public abstract class PersistentExportableProtocol<E extends Exportable> extends ExportableProtocol<E>
{
	private final PersistenceManager persistenceManager;
	private final Transaction transaction;

	/**
	 * Creates a new protocol for a given persistence manager and transaction to
	 * use
	 *
	 * @param persistenceManager
	 *            The persistence manager.
	 * @param transaction
	 *            The transaction.
	 */
	public PersistentExportableProtocol(int requiredVersion, PersistenceManager persistenceManager, Transaction transaction)
	{
		super(0);
		checkVersionAvailability(PersistentExportableProtocol.class, requiredVersion);
		this.persistenceManager = persistenceManager;
		this.transaction = transaction;
	}

	/**
	 * The persistent manager associated to this protocol.
	 *
	 * @return The persistent manager.
	 */
	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	/**
	 * The persistent transaction associated to this protocol.
	 *
	 * @return The transaction.
	 */
	public Transaction getTransaction()
	{
		return transaction;
	}

}
