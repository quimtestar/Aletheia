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

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import aletheia.model.catalog.Catalog;
import aletheia.model.catalog.SubCatalog;
import aletheia.model.identifier.NodeNamespace;
import aletheia.persistence.Transaction;

public abstract class CatalogTreeNode implements TreeNode
{
	private abstract class ChildrenData
	{
		public final List<SubCatalogTreeNode> list;
		public final Map<NodeNamespace, SubCatalogTreeNode> map;

		protected ChildrenData(List<SubCatalogTreeNode> list, Map<NodeNamespace, SubCatalogTreeNode> map)
		{
			super();
			this.list = list;
			this.map = map;
		}

	}

	private class ModifiableChildrenData extends ChildrenData
	{

		protected ModifiableChildrenData()
		{
			super(new ArrayList<SubCatalogTreeNode>(), new HashMap<NodeNamespace, SubCatalogTreeNode>());
		}

		public void add(SubCatalogTreeNode node)
		{
			list.add(node);
			map.put(node.getCatalog().prefix(), node);
		}
	}

	@SuppressWarnings("unused")
	private class EmptyChildrenData extends ChildrenData
	{
		protected EmptyChildrenData()
		{
			super(Collections.<SubCatalogTreeNode> emptyList(), Collections.<NodeNamespace, SubCatalogTreeNode> emptyMap());
		}
	}

	private final CatalogTreeModel model;
	private final Catalog catalog;
	private SoftReference<ModifiableChildrenData> refChildren;
	private SoftReference<CatalogJTreeNodeRenderer> rendererRef;

	public CatalogTreeNode(CatalogTreeModel model, Catalog catalog)
	{
		super();
		this.model = model;
		this.catalog = catalog;
		this.refChildren = null;
		cleanRenderer();
	}

	protected CatalogTreeModel getModel()
	{
		return model;
	}

	public Catalog getCatalog()
	{
		return catalog;
	}

	private ChildrenData getChildren()
	{
		ModifiableChildrenData children = null;
		if (refChildren != null)
			children = refChildren.get();
		if (children == null)
		{
			children = new ModifiableChildrenData();
			if (catalog != null)
			{
				Transaction transaction = getModel().beginTransaction();
				try
				{
					for (SubCatalog subc : catalog.subCatalogs(transaction))
					{
						SubCatalogTreeNode node = new SubCatalogTreeNode(getModel(), this, subc);
						children.add(node);
					}
				}
				finally
				{
					transaction.abort();
				}
			}
			refChildren = new SoftReference<ModifiableChildrenData>(children);
		}
		return children;
	}

	public boolean childrenLoaded()
	{
		return refChildren != null && refChildren.get() != null;
	}

	@Override
	public Enumeration<SubCatalogTreeNode> children()
	{

		final Iterator<SubCatalogTreeNode> iterator = getChildren().list.iterator();
		return new Enumeration<SubCatalogTreeNode>()
		{

			@Override
			public boolean hasMoreElements()
			{
				return iterator.hasNext();
			}

			@Override
			public SubCatalogTreeNode nextElement()
			{
				return iterator.next();
			}

		};
	}

	@Override
	public boolean getAllowsChildren()
	{
		return true;
	}

	@Override
	public SubCatalogTreeNode getChildAt(int i)
	{
		return getChildren().list.get(i);
	}

	public SubCatalogTreeNode getChild(NodeNamespace ns)
	{
		return getChildren().map.get(ns);
	}

	@Override
	public int getChildCount()
	{
		return getChildren().list.size();
	}

	@Override
	public int getIndex(TreeNode o)
	{
		return getChildren().list.indexOf(o);
	}

	@Override
	public abstract TreeNode getParent();

	@Override
	public boolean isLeaf()
	{
		if (catalog == null)
			return true;
		Transaction transaction = getModel().beginTransaction();
		try
		{
			return catalog.subCatalogs(transaction).isEmpty();
		}
		finally
		{
			transaction.abort();
		}
	}

	public void cleanRenderer()
	{
		rendererRef = new SoftReference<CatalogJTreeNodeRenderer>(null);
	}

	public CatalogJTreeNodeRenderer renderer(CatalogJTree catalogJTree)
	{
		CatalogJTreeNodeRenderer renderer = rendererRef.get();
		if (renderer == null)
		{
			renderer = buildRenderer(catalogJTree);
			if (renderer == null)
				renderer = new EmptyCatalogJTreeNodeRenderer(catalogJTree, this);
			else
				rendererRef = new SoftReference<CatalogJTreeNodeRenderer>(renderer);
		}
		return renderer;
	}

	protected abstract CatalogJTreeNodeRenderer buildRenderer(CatalogJTree catalogJTree);

	public void rebuildChildren()
	{
		refChildren = null;
	}

	public abstract TreePath path();

	@Override
	public String toString()
	{
		return getCatalog().prefix().toString();
	}

	protected void cleanRenderers()
	{
		cleanRenderer();
		if (refChildren != null)
		{
			for (SubCatalogTreeNode node : refChildren.get().list)
				node.cleanRenderers();
		}
	}

}
