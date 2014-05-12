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
package aletheia.peertopeer.statement.message;

import java.util.Collection;

import aletheia.peertopeer.base.message.AbstractUUIDPersistentInfoMessage;
import aletheia.peertopeer.base.message.MessageCode;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.protocol.ProtocolInfo;

public abstract class DelegateTreeDependencyResponseMessage<V> extends AbstractUUIDPersistentInfoMessage<V>
{

	public DelegateTreeDependencyResponseMessage(Collection<? extends aletheia.peertopeer.base.message.AbstractUUIDPersistentInfoMessage.Entry<V>> entries)
	{
		super(entries);
	}

	@ProtocolInfo(availableVersions = 0)
	public abstract static class SubProtocol<V, M extends DelegateTreeDependencyResponseMessage<V>> extends AbstractUUIDPersistentInfoMessage.SubProtocol<V, M>
	{
		public SubProtocol(int requiredVersion, PersistenceManager persistenceManager, Transaction transaction, MessageCode messageCode)
		{
			super(0, persistenceManager, transaction, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
		}

	}

}
