package aletheia.gui.contextjtree.sorter.old;

import aletheia.model.statement.Statement;

public class StatementSorter extends Sorter
{
	private final Statement statement;

	protected StatementSorter(GroupSorter<? extends Statement> group, Statement statement)
	{
		super(group, statement.getIdentifier());
		this.statement = statement;
	}

	public Statement getStatement()
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
		StatementSorter other = (StatementSorter) obj;
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
