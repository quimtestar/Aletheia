/*******************************************************************************
 * Copyright (c) 2014, 2018 Quim Testar.
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

import aletheia.gui.lookandfeel.AletheiaTheme;
import aletheia.model.authority.PackedSignatureRequest;
import aletheia.persistence.Transaction;

public class PackedRequestSignatureRequestTreeNodeRenderer extends RequestSignatureRequestTreeNodeRenderer
{
	private static final long serialVersionUID = 3122901078574068484L;

	public PackedRequestSignatureRequestTreeNodeRenderer(SignatureRequestJTree signatureRequestJTree, PackedRequestSignatureRequestTreeNode node)
	{
		super(signatureRequestJTree, node);
		addClosedBoxLabel(AletheiaTheme.Key.packedSignetureRequest);
		addSpaceLabel();
		addSignatureRequestLabels(AletheiaTheme.Key.packedSignetureRequest);
		Transaction transaction = beginTransaction();
		try
		{
			PackedSignatureRequest packedSignatureRequest = getSignatureRequest().refresh(transaction);
			if (packedSignatureRequest != null)
			{
				addSpaceLabel();
				addOpenSquareBracket(AletheiaTheme.Key.packedSignetureRequest);
				addSpaceLabel();
				addDateLabel(packedSignatureRequest.getPackingDate(), AletheiaTheme.Key.packedSignetureRequest);
				addSpaceLabel();
				addByteSize(packedSignatureRequest.getData().length, AletheiaTheme.Key.packedSignetureRequest);
				addSpaceLabel();
				addCloseSquareBracket(AletheiaTheme.Key.packedSignetureRequest);
			}
		}
		finally
		{
			transaction.abort();
		}

	}

	@Override
	protected PackedRequestSignatureRequestTreeNode getNode()
	{
		return (PackedRequestSignatureRequestTreeNode) super.getNode();
	}

	@Override
	protected PackedSignatureRequest getSignatureRequest()
	{
		return (PackedSignatureRequest) super.getSignatureRequest();
	}

}
