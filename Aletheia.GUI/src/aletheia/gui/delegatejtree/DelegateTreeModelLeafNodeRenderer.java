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
package aletheia.gui.delegatejtree;

import aletheia.gui.common.renderer.PersonLabelRenderer;
import aletheia.model.authority.DelegateAuthorizer;
import aletheia.model.authority.Person;
import aletheia.persistence.Transaction;

public class DelegateTreeModelLeafNodeRenderer extends DelegateTreeModelNodeRenderer
{
	private static final long serialVersionUID = 6479812889291862637L;
	private final PersonLabelRenderer personLabelRenderer;

	public DelegateTreeModelLeafNodeRenderer(DelegateTreeJTree delegateTreeJTree, DelegateTreeModelLeafNode delegateTreeModelNode)
	{
		super(delegateTreeJTree, delegateTreeModelNode);
		DelegateAuthorizer delegateAuthorizer = delegateTreeModelNode.getDelegateAuthorizer();
		Transaction transaction = delegateTreeJTree.getModel().beginTransaction();
		try
		{
			Person person = delegateAuthorizer.getDelegate(transaction);
			this.personLabelRenderer = new PersonLabelRenderer(person);
			add(personLabelRenderer);
		}
		finally
		{
			transaction.abort();
		}
	}

	@Override
	protected DelegateTreeModelLeafNode getDelegateTreeModelNode()
	{
		return (DelegateTreeModelLeafNode) super.getDelegateTreeModelNode();
	}

	@Override
	public void setSelected(boolean selected)
	{
		super.setSelected(selected);
		personLabelRenderer.setSelected(selected);
	}

}
