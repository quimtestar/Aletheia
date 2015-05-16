package aletheia.gui.contextjtree.statementsorter;

import aletheia.model.statement.Statement;

public class SingletonStatementSorter extends StatementSorter
{
	private final Statement statement;

	public SingletonStatementSorter(Statement statement)
	{
		this.statement = statement;
	}

	public Statement getStatement()
	{
		return statement;
	}

}
