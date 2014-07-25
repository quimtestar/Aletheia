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
import java.util.Collections;

import aletheia.model.authority.PackedSignatureRequest;
import aletheia.persistence.Transaction;

public class PackedRequestSignatureRequestTreeNode extends RequestSignatureRequestTreeNode
{
	public PackedRequestSignatureRequestTreeNode(SignatureRequestTreeModel signatureRequestTreeModel, ContextSignatureRequestTreeNode parent,
			PackedSignatureRequest packedSignatureRequest)
	{
		super(signatureRequestTreeModel, parent, packedSignatureRequest);
	}

	@Override
	public ContextSignatureRequestTreeNode getParent()
	{
		return super.getParent();
	}

	@Override
	protected PackedSignatureRequest getSignatureRequest()
	{
		return (PackedSignatureRequest) super.getSignatureRequest();
	}

	@Override
	protected Collection<? extends SignatureRequestTreeNode> childNodeCollection(Transaction transaction)
	{
		return Collections.emptyList();
	}

	@Override
	protected PackedRequestSignatureRequestTreeNodeRenderer buildRenderer(SignatureRequestJTree signatureRequestJTree)
	{
		return new PackedRequestSignatureRequestTreeNodeRenderer(signatureRequestJTree, this);
	}

}
