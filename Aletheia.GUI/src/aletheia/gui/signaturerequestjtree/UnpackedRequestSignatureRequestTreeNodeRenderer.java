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

import java.awt.Color;

import aletheia.model.authority.UnpackedSignatureRequest;

public class UnpackedRequestSignatureRequestTreeNodeRenderer extends RequestSignatureRequestTreeNodeRenderer
{
	private static final long serialVersionUID = -4298224728936519579L;
	private final static Color defaultColor = Color.blue;

	public UnpackedRequestSignatureRequestTreeNodeRenderer(SignatureRequestJTree signatureRequestJTree, UnpackedRequestSignatureRequestTreeNode node)
	{
		super(signatureRequestJTree, node);
		addOpenBoxLabel(defaultColor);
		addSpaceLabel();
		addSignatureRequestLabels(defaultColor);
	}

	@Override
	protected UnpackedRequestSignatureRequestTreeNode getNode()
	{
		return (UnpackedRequestSignatureRequestTreeNode) super.getNode();
	}

	@Override
	protected UnpackedSignatureRequest getSignatureRequest()
	{
		return (UnpackedSignatureRequest) super.getSignatureRequest();
	}

}
