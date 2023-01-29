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
import java.util.Collections;

import aletheia.model.authority.UnpackedSignatureRequest;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

public class StatementSignatureRequestTreeNode extends SignatureRequestTreeNode
{
	private final Statement statement;

	public StatementSignatureRequestTreeNode(SignatureRequestTreeModel signatureRequestTreeModel, UnpackedRequestSignatureRequestTreeNode parent,
			Statement statement)
	{
		super(signatureRequestTreeModel, parent);
		this.statement = statement;
		statement.addStateListener(getModel().getNodeStateListener());
		statement.addAuthorityStateListener(getModel().getNodeStateListener());
	}

	@Override
	public UnpackedRequestSignatureRequestTreeNode getParent()
	{
		return (UnpackedRequestSignatureRequestTreeNode) super.getParent();
	}

	protected Statement getStatement()
	{
		return statement;
	}

	@Override
	protected StatementSignatureRequestTreeNodeRenderer buildRenderer(SignatureRequestJTree signatureRequestJTree)
	{
		return new StatementSignatureRequestTreeNodeRenderer(signatureRequestJTree, this);
	}

	public UnpackedSignatureRequest getUnpackedSignatureRequest()
	{
		return getParent().getSignatureRequest();
	}

	@Override
	protected Collection<? extends SignatureRequestTreeNode> childNodeCollection(Transaction transaction)
	{
		return Collections.emptyList();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((statement == null) ? 0 : statement.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj) || (getClass() != obj.getClass()))
			return false;
		StatementSignatureRequestTreeNode other = (StatementSignatureRequestTreeNode) obj;
		if (statement == null)
		{
			if (other.statement != null)
				return false;
		}
		else if (!statement.equals(other.statement))
			return false;
		return true;
	}

}
