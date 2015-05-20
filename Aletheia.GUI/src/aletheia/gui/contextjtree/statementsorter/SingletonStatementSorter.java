package aletheia.gui.contextjtree.statementsorter;

import aletheia.model.statement.Statement;

public class SingletonStatementSorter<S extends Statement> extends StatementSorter<S>
{
	private final S statement;

	protected SingletonStatementSorter(GroupStatementSorter<S> group, S statement)
	{
		super(group, statement.getIdentifier());
		this.statement = statement;
	}

	public S getStatement()
	{
		return statement;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((statement == null) ? 0 : statement.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("rawtypes")
		SingletonStatementSorter other = (SingletonStatementSorter) obj;
		if (statement == null)
		{
			if (other.statement != null)
				return false;
		}
		else if (!statement.equals(other.statement))
			return false;
		return true;
	}

}
