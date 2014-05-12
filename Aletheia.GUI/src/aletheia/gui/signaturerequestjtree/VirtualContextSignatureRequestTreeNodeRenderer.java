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

import java.util.UUID;

public class VirtualContextSignatureRequestTreeNodeRenderer extends SignatureRequestTreeNodeRenderer
{

	private static final long serialVersionUID = -9106038056242199196L;

	public VirtualContextSignatureRequestTreeNodeRenderer(SignatureRequestJTree signatureRequestJTree, VirtualContextSignatureRequestTreeNode node)
	{
		super(signatureRequestJTree, node);
		addUUIDLabel(getContextUuid());
	}

	@Override
	protected VirtualContextSignatureRequestTreeNode getNode()
	{
		return (VirtualContextSignatureRequestTreeNode) super.getNode();
	}

	protected UUID getContextUuid()
	{
		return getNode().getContextUuid();
	}

}
