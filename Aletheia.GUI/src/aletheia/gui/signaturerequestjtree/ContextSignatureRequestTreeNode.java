/*******************************************************************************
 * Copyright (c) 2014, 2023 Quim Testar.
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

import aletheia.model.authority.SignatureRequest;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.AdaptedCollection;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCloseableCollection;
import aletheia.utilities.collections.CloseableCollection;
import aletheia.utilities.collections.CombinedCollection;

public abstract class ContextSignatureRequestTreeNode extends SignatureRequestTreeNode
{
	private final UUID contextUuid;

	public ContextSignatureRequestTreeNode(SignatureRequestTreeModel signatureRequestTreeModel, SignatureRequestTreeNode parent, UUID contextUuid)
	{
		super(signatureRequestTreeModel, parent);
		this.contextUuid = contextUuid;
	}

	public UUID getContextUuid()
	{
		return contextUuid;
	}

	protected Collection<UUID> signatureRequestSubContextUuidsCollection(Transaction transaction)
	{
		return getPersistenceManager().signatureRequestContextSubContextUuidsCollection(transaction, contextUuid);
	}

	protected Collection<ContextSignatureRequestTreeNode> childContextNodeCollection(Transaction transaction)
	{
		return childContextNodeCollection(transaction, signatureRequestSubContextUuidsCollection(transaction));
	}

	protected CloseableCollection<SignatureRequest> childSignatureRequests(Transaction transaction)
	{
		return getPersistenceManager().signatureRequestContextCreationDateCollection(transaction, contextUuid);
	}

	protected RequestSignatureRequestTreeNode makeRequestNode(SignatureRequest signatureRequest)
	{
		return getModel().makeRequestNode(this, signatureRequest);
	}

	protected CloseableCollection<RequestSignatureRequestTreeNode> childSignatureRequestNodeCollection(Transaction transaction)
	{
		return new BijectionCloseableCollection<>(new Bijection<SignatureRequest, RequestSignatureRequestTreeNode>()
		{

			@Override
			public RequestSignatureRequestTreeNode forward(SignatureRequest signatureRequest)
			{
				return makeRequestNode(signatureRequest);
			}

			@Override
			public SignatureRequest backward(RequestSignatureRequestTreeNode output)
			{
				throw new UnsupportedOperationException();
			}

		}, childSignatureRequests(transaction));
	}

	@Override
	protected Collection<SignatureRequestTreeNode> childNodeCollection(Transaction transaction)
	{
		return new CombinedCollection<>(new AdaptedCollection<>(childContextNodeCollection(transaction)),
				new AdaptedCollection<>(childSignatureRequestNodeCollection(transaction)));
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((contextUuid == null) ? 0 : contextUuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj) || (getClass() != obj.getClass()))
			return false;
		ContextSignatureRequestTreeNode other = (ContextSignatureRequestTreeNode) obj;
		if (contextUuid == null)
		{
			if (other.contextUuid != null)
				return false;
		}
		else if (!contextUuid.equals(other.contextUuid))
			return false;
		return true;
	}

}
