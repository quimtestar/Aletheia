package aletheia.gui.contextjtree.node;

import javax.swing.tree.TreePath;

import aletheia.gui.contextjtree.sorter.Sorter;
import aletheia.gui.contextjtree.sorter.StatementSorter;
import aletheia.model.statement.Statement;

public interface StatementContextJTreeNode
{
	public Statement getStatement();

	public TreePath path();

	public Sorter getSorter();

	public StatementSorter getNodeMapSorter();

	public GroupSorterContextJTreeNode<? extends Statement> getParent();

}
