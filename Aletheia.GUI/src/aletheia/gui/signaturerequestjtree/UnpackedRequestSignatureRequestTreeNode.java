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
import java.util.Set;

import aletheia.model.authority.UnpackedSignatureRequest;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCollection;
import aletheia.utilities.collections.BufferedList;

public class UnpackedRequestSignatureRequestTreeNode extends RequestSignatureRequestTreeNode
{
	public UnpackedRequestSignatureRequestTreeNode(SignatureRequestTreeModel signatureRequestTreeModel, ActualContextSignatureRequestTreeNode parent,
			UnpackedSignatureRequest unpackedSignatureRequest)
	{
		super(signatureRequestTreeModel, parent, unpackedSignatureRequest);
	}

	@Override
	public ActualContextSignatureRequestTreeNode getParent()
	{
		return (ActualContextSignatureRequestTreeNode) super.getParent();
	}

	@Override
	protected UnpackedSignatureRequest getSignatureRequest()
	{
		return (UnpackedSignatureRequest) super.getSignatureRequest();
	}

	protected Set<Statement> statements(Transaction transaction)
	{
		return getSignatureRequest().statements(transaction);
	}

	protected Collection<Statement> sortedStatements(Transaction transaction)
	{
		BufferedList<Statement> list = new BufferedList<Statement>(statements(transaction));
		Collections.sort(list, new StatementComparator(transaction));
		return list;
	}

	public StatementSignatureRequestTreeNode statementNode(Transaction transaction, Statement statement)
	{
		if (!getSignatureRequest().statements(transaction).contains(statement))
			return null;
		return makeNode(statement);
	}

	private StatementSignatureRequestTreeNode makeNode(Statement statement)
	{
		return getModel().makeStatementNode(this, statement);
	}

	@Override
	protected Collection<StatementSignatureRequestTreeNode> childNodeCollection(Transaction transaction)
	{
		return new BijectionCollection<Statement, StatementSignatureRequestTreeNode>(new Bijection<Statement, StatementSignatureRequestTreeNode>()
		{

			@Override
			public StatementSignatureRequestTreeNode forward(Statement statement)
			{
				return makeNode(statement);
			}

			@Override
			public Statement backward(StatementSignatureRequestTreeNode output)
			{
				throw new UnsupportedOperationException();
			}
		}, sortedStatements(transaction));
	}

	@Override
	protected UnpackedRequestSignatureRequestTreeNodeRenderer buildRenderer(SignatureRequestJTree signatureRequestJTree)
	{
		return new UnpackedRequestSignatureRequestTreeNodeRenderer(signatureRequestJTree, this);
	}

}
