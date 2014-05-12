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

import aletheia.model.authority.SignatureRequest;
import aletheia.model.statement.Context;
import aletheia.persistence.Transaction;
import aletheia.persistence.collections.statement.SubContextsSet;

public class ActualContextSignatureRequestTreeNode extends ContextSignatureRequestTreeNode
{
	private final Context context;

	public ActualContextSignatureRequestTreeNode(SignatureRequestTreeModel signatureRequestTreeModel, SignatureRequestTreeNode parent, Context context)
	{
		super(signatureRequestTreeModel, parent, context.getUuid());
		this.context = context;
		context.addStateListener(getModel().getNodeStateListener());
		context.addNomenclatorListener(getModel().getNodeStateListener());
	}

	@Override
	public Context getContext()
	{
		return context;
	}

	private boolean hasRequests(Transaction transaction, Context context)
	{
		return !context.unpackedSignatureRequestSetByPath(transaction).isEmpty();
	}

	protected SubContextsSet subContexts(Transaction transaction)
	{
		return context.subContexts(transaction);
	}

	public ActualContextSignatureRequestTreeNode actualContextNode(Transaction transaction, Context context)
	{
		if (!this.context.localStatements(transaction).containsKey(context.getVariable()))
			return null;
		if (!hasRequests(transaction, context))
			return null;
		return makeActualContextNode(context);
	}

	public RequestSignatureRequestTreeNode requestNode(Transaction transaction, SignatureRequest signatureRequest)
	{
		if (!signatureRequest.getContextUuid().equals(context.getUuid()))
			return null;
		return makeRequestNode(signatureRequest);
	}

	@Override
	protected ActualContextSignatureRequestTreeNodeRenderer buildRenderer(SignatureRequestJTree signatureRequestJTree)
	{
		return new ActualContextSignatureRequestTreeNodeRenderer(signatureRequestJTree, this);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((context == null) ? 0 : context.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActualContextSignatureRequestTreeNode other = (ActualContextSignatureRequestTreeNode) obj;
		if (context == null)
		{
			if (other.context != null)
				return false;
		}
		else if (!context.equals(other.context))
			return false;
		return true;
	}

}
