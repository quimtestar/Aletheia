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
package aletheia.gui.catalogjtree;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import aletheia.model.catalog.SubCatalog;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

public class SubCatalogTreeNode extends CatalogTreeNode
{
	private final CatalogTreeNode parent;

	public SubCatalogTreeNode(CatalogTreeModel model, CatalogTreeNode parent, SubCatalog catalog)
	{
		super(model, catalog);
		Transaction transaction = model.beginTransaction();
		try
		{
			this.parent = parent;
			Statement statement = catalog.statement(transaction);
			if (statement != null)
				model.listenTo(statement);
		}
		finally
		{
			transaction.abort();
		}
	}

	@Override
	public SubCatalog getCatalog()
	{
		return (SubCatalog) super.getCatalog();
	}

	@Override
	public TreeNode getParent()
	{
		return parent;
	}

	@Override
	protected SubCatalogJTreeNodeRenderer buildRenderer(CatalogJTree catalogJTree)
	{
		return new SubCatalogJTreeNodeRenderer(catalogJTree, this);
	}

	@Override
	public TreePath path()
	{
		return parent.path().pathByAddingChild(this);
	}

}
