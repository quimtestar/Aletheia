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

import java.awt.event.KeyEvent;

import javax.swing.JLabel;

import org.apache.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.authority.UnpackedSignatureRequest;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

public class StatementSignatureRequestTreeNodeRenderer extends SignatureRequestTreeNodeRenderer
{
	private static final long serialVersionUID = -397830765452814828L;
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
				UnpackedSignatureRequest unpackedSignatureRequest = getNode().getUnpackedSignatureRequest();
				Statement statement = getNode().getStatement();
				if (unpackedSignatureRequest != null && statement != null)
				{
					try
					{
						getPersistentJTree().removeStatementFromUnpackedSignatureRequest(unpackedSignatureRequest, statement);
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

	public StatementSignatureRequestTreeNodeRenderer(SignatureRequestJTree signatureRequestJTree, StatementSignatureRequestTreeNode node)
	{
		super(signatureRequestJTree, node);
		Transaction transaction = beginTransaction();
		try
		{
			Statement statement = getStatement().refresh(transaction);
			if (statement != null)
			{
				StatementAuthority statementAuthority = statement.getAuthority(transaction);
				addProofLabel(statement);
				if (statementAuthority != null)
					addSignatureStatusSymbolLabel(statementAuthority.signatureStatus());
				else
					addSpaceLabel();
				addSpaceLabel();
				addStatementReference(transaction, statement);
			}
		}
		finally
		{
			transaction.abort();
		}
	}

	@Override
	protected Listener makeListener()
	{
		return new Listener();
	}

	@Override
	protected StatementSignatureRequestTreeNode getNode()
	{
		return (StatementSignatureRequestTreeNode) super.getNode();
	}

	protected Statement getStatement()
	{
		return getNode().getStatement();
	}

	protected StatementAuthority getStatementAuthority(Transaction transaction)
	{
		return getStatement().getAuthority(transaction);
	}

	private JLabel addProofLabel(Statement statement)
	{
		JLabel jLabel;
		if (statement.isProved())
			jLabel = addTickLabel();
		else
			jLabel = addQuestionMarkLabel();
		return jLabel;
	}

}
