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

public abstract class RequestSignatureRequestTreeNode extends SignatureRequestTreeNode
{
	private final SignatureRequest signatureRequest;

	public RequestSignatureRequestTreeNode(SignatureRequestTreeModel signatureRequestTreeModel, ContextSignatureRequestTreeNode parent,
			SignatureRequest signatureRequest)
	{
		super(signatureRequestTreeModel, parent);
		this.signatureRequest = signatureRequest;
		signatureRequest.addStateListener(getModel().getNodeStateListener());
	}

	@Override
	public ContextSignatureRequestTreeNode getParent()
	{
		return (ContextSignatureRequestTreeNode) super.getParent();
	}

	protected SignatureRequest getSignatureRequest()
	{
		return signatureRequest;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((signatureRequest == null) ? 0 : signatureRequest.hashCode());
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
		RequestSignatureRequestTreeNode other = (RequestSignatureRequestTreeNode) obj;
		if (signatureRequest == null)
		{
			if (other.signatureRequest != null)
				return false;
		}
		else if (!signatureRequest.equals(other.signatureRequest))
			return false;
		return true;
	}

}
