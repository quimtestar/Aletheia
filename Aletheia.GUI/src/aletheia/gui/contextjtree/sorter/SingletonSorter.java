package aletheia.gui.contextjtree.sorter;

import aletheia.model.statement.Statement;

public class SingletonSorter extends Sorter
{
	private final Statement statement;

	protected SingletonSorter(GroupSorter<? extends Statement> group, Statement statement)
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
		SingletonSorter other = (SingletonSorter) obj;
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
