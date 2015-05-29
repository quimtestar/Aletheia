package aletheia.gui.contextjtree.node;

import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.TreeNode;

import aletheia.gui.contextjtree.ContextJTree;
import aletheia.gui.contextjtree.ContextJTreeModel;
import aletheia.gui.contextjtree.renderer.ContextJTreeNodeRenderer;
import aletheia.gui.contextjtree.sorter.GroupSorter;
import aletheia.gui.contextjtree.sorter.Sorter;
import aletheia.model.statement.Statement;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionIterator;
import aletheia.utilities.collections.BufferedList;
import aletheia.utilities.collections.IteratorEnumeration;


public class GroupSorterContextJTreeNode<S extends Statement> extends SorterContextJTreeNode
{
	private BufferedList<Sorter> sorterList;
	
	public GroupSorterContextJTreeNode(ContextJTreeModel model,GroupSorter<S> groupSorter)
	{
		super(model,groupSorter);
		this.sorterList=null;
	}

	public GroupSorter<S> getSorter()
	{
		return (GroupSorter<S>) getSorter();
	}
	
	private synchronized List<Sorter> getSorterList()
	{
		//TODO El Sorter té una transacció associada i això no està bé. La transacció s'hauria de generar aquí.
		if (sorterList==null)
			sorterList=new BufferedList<Sorter>(getSorter());
		return sorterList;
	}

	@Override
	public ContextJTreeNode getChildAt(int childIndex)
	{
		return getModel().nodeMap().get(getSorterList().get(childIndex));
	}

	@Override
	public int getChildCount()
	{
		return getSorterList().size();
	}

	@Override
	public int getIndex(TreeNode node)
	{
		if (!(node instanceof SorterContextJTreeNode))
			return -1;
		return getSorterList().indexOf(node);
	}

	@Override
	public boolean getAllowsChildren()
	{
		return true;
	}

	@Override
	public boolean isLeaf()
	{
		return false;
	}

	@Override
	public Enumeration<? extends ContextJTreeNode> children()
	{
		return new IteratorEnumeration<SorterContextJTreeNode>(new BijectionIterator<Sorter,SorterContextJTreeNode>(new Bijection<Sorter,SorterContextJTreeNode>(){

			@Override
			public SorterContextJTreeNode forward(Sorter sorter)
			{
				return getModel().nodeMap().get(sorter);
			}

			@Override
			public Sorter backward(SorterContextJTreeNode sorterContextJTreeNode)
			{
				return sorterContextJTreeNode.getSorter();
			}},getSorterList().iterator()));
	}

	@Override
	protected ContextJTreeNodeRenderer buildRenderer(ContextJTree contextJTree)
	{
		getSorter().getPrefix();
		throw new UnsupportedOperationException(); //TODO
	}
	
	
	
	
	

}
