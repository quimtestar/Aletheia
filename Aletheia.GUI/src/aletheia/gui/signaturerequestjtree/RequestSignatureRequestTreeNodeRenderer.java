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
import java.awt.event.KeyEvent;

import org.apache.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.model.authority.SignatureRequest;

public class RequestSignatureRequestTreeNodeRenderer extends SignatureRequestTreeNodeRenderer
{
	private static final long serialVersionUID = 4218146842946975144L;

	private final static Logger logger = LoggerManager.logger();

	protected class Listener extends SignatureRequestTreeNodeRenderer.Listener
	{

		@Override
		public void keyPressed(KeyEvent ev)
		{
			super.keyPressed(ev);
			switch (ev.getKeyCode())
			{
			case KeyEvent.VK_DELETE:
			{
				SignatureRequest signatureRequest = getNode().getSignatureRequest();
				if (signatureRequest != null)
				{
					try
					{
						getPersistentJTree().deleteSignatureRequest(signatureRequest);
					}
					catch (InterruptedException e)
					{
						logger.error(e.getMessage(), e);
					}
				}
				break;
			}
			}

		}

	}

	public RequestSignatureRequestTreeNodeRenderer(SignatureRequestJTree signatureRequestJTree, RequestSignatureRequestTreeNode node)
	{
		super(signatureRequestJTree, node);
	}

	@Override
	protected Listener makeListener()
	{
		return new Listener();
	}

	@Override
	protected RequestSignatureRequestTreeNode getNode()
	{
		return (RequestSignatureRequestTreeNode) super.getNode();
	}

	protected SignatureRequest getSignatureRequest()
	{
		return getNode().getSignatureRequest();
	}

	protected void addSignatureRequestLabels(Color color)
	{
		addUUIDLabel(getSignatureRequest().getUuid(), color);
		addSpaceLabel();
		addDateLabel(getSignatureRequest().getCreationDate(), color);
	}
}
