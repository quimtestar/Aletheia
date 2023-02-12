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

import aletheia.gui.lookandfeel.AletheiaTheme;
import aletheia.model.authority.DelegateTreeSubNode;

public class DelegateTreeModelBranchSubNodeRenderer extends DelegateTreeModelBranchNodeRenderer
{

	private static final long serialVersionUID = -2618152538453936261L;

	public DelegateTreeModelBranchSubNodeRenderer(DelegateTreeJTree delegateTreeJTree, DelegateTreeModelBranchSubNode delegateTreeModelNode)
	{
		super(delegateTreeJTree, delegateTreeModelNode);
		DelegateTreeSubNode delegateTreeSubNode = delegateTreeModelNode.getDelegateTreeNode();
		addTextLabel(delegateTreeSubNode.getPrefix().getName(), AletheiaTheme.Key.delegateTree);
		setToolTipText(delegateTreeSubNode.getPrefix().qualifiedName());
	}

	@Override
	protected DelegateTreeModelBranchSubNode getDelegateTreeModelNode()
	{
		return (DelegateTreeModelBranchSubNode) super.getDelegateTreeModelNode();
	}

}
