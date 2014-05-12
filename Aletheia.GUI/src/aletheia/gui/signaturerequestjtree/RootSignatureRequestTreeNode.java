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
package aletheia.gui.signaturerequestjtree;

import java.util.Collection;
import java.util.UUID;

import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.persistence.Transaction;

public class RootSignatureRequestTreeNode extends SignatureRequestTreeNode
{
	public RootSignatureRequestTreeNode(SignatureRequestTreeModel signatureRequestTreeModel)
	{
		super(signatureRequestTreeModel, null);
	}

	@Override
	public SignatureRequestTreeNode getParent()
	{
		throw new UnsupportedOperationException();
	}

	private boolean hasRequests(Transaction transaction, RootContext rootContext)
	{
		return !rootContext.unpackedSignatureRequestSetByPath(transaction).isEmpty();
	}

	private ActualContextSignatureRequestTreeNode makeActualContextNode(RootContext rootContext)
	{
		return getModel().makeActualContextNode(rootContext);
	}

	public ActualContextSignatureRequestTreeNode contextNode(Transaction transaction, RootContext rootContext)
	{
		if (!hasRequests(transaction, rootContext))
			return null;
		return makeActualContextNode(rootContext);
	}

	@Override
	protected SignatureRequestTreeNodeRenderer buildRenderer(SignatureRequestJTree signatureRequestJTree)
	{
		return new RootSignatureRequestTreeNodeRenderer(signatureRequestJTree, this);
	}

	@Override
	protected Context getContext()
	{
		return null;
	}

	protected Collection<UUID> signatureRequestSubContextUuidsCollection(Transaction transaction)
	{
		return getPersistenceManager().signatureRequestContextSubContextUuidsCollection(transaction);
	}

	@Override
	protected Collection<ContextSignatureRequestTreeNode> childNodeCollection(Transaction transaction)
	{
		return childContextNodeCollection(transaction, signatureRequestSubContextUuidsCollection(transaction));
	}

}
