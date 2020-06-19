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
package aletheia.model.authority.protocol;

import aletheia.model.authority.DelegateTreeNode;
import aletheia.model.authority.DelegateTreeSubNode;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;

@ProtocolInfo(availableVersions = 0)
public class DelegateTreeSubNodeProtocol extends DelegateTreeNodeProtocol<DelegateTreeSubNode>
{
	private final DelegateTreeNode parent;
	private final String name;

	private DelegateTreeSubNode delegateTreeSubNode;

	public DelegateTreeSubNodeProtocol(int requiredVersion, PersistenceManager persistenceManager, Transaction transaction, DelegateTreeNode parent,
			String name)
	{
		super(0, persistenceManager, transaction);
		checkVersionAvailability(DelegateTreeSubNodeProtocol.class, requiredVersion);
		this.parent = parent;
		this.name = name;
		this.delegateTreeSubNode = null;
	}

	public DelegateTreeSubNodeProtocol(int requiredVersion, PersistenceManager persistenceManager, Transaction transaction)
	{
		this(requiredVersion, persistenceManager, transaction, null, null);
	}

	@Override
	protected DelegateTreeSubNode obtainDelegateTreeNode() throws ProtocolException
	{
		try
		{
			if ((delegateTreeSubNode == null) && (parent != null))
				delegateTreeSubNode = parent.getOrCreateSubNodeNoSign(getTransaction(), name);
			return delegateTreeSubNode;
		}
		catch (InvalidNameException e)
		{
			throw new ProtocolException(e);
		}
	}

}
