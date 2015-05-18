package aletheia.gui.contextjtree.statementsorter;

import aletheia.model.statement.Statement;

public class SingletonStatementSorter<S extends Statement> extends StatementSorter<S>
{
	private final S statement;

	public SingletonStatementSorter(S statement)
	{
		this.statement = statement;
	}

	public S getStatement()
	{
		return statement;
	}

}
