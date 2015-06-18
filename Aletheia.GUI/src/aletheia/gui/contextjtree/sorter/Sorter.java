package aletheia.gui.contextjtree.sorter;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.CloseableSet;

public abstract class Sorter
{
	private final GroupSorter<? extends Statement> group;

	public Sorter(GroupSorter<? extends Statement> group)
	{
		this.group = group;
	}

	public GroupSorter<? extends Statement> getGroup()
	{
		return group;
	}

	public abstract Identifier getPrefix();

	public abstract Statement getStatement(Transaction transaction);

	public abstract CloseableSet<? extends Statement> statements(Transaction transaction);

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Sorter other = (Sorter) obj;
		if (group == null)
		{
			if (other.group != null)
				return false;
		}
		else if (!group.equals(other.group))
			return false;
		return true;
	}

}
