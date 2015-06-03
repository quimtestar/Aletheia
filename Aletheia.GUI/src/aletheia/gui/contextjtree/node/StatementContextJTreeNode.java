package aletheia.gui.contextjtree.node;

import javax.swing.tree.TreePath;

import aletheia.model.statement.Statement;

public interface StatementContextJTreeNode
{
	public Statement getStatement();

	public TreePath path();
}
