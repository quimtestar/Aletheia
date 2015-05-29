package aletheia.gui.contextjtree.sorter.old;

import aletheia.model.identifier.Namespace;
import aletheia.model.statement.Statement;

public abstract class Sorter
{
	private final GroupSorter<? extends Statement> group;
	private final Namespace prefix;

	public Sorter(GroupSorter<? extends Statement> group, Namespace prefix)
	{
		if (group != null && group.getPrefix() != null && (prefix == null || !group.getPrefix().isPrefixOf(prefix)))
			throw new IllegalArgumentException("Inconsistent prefix.");
		this.group = group;
		this.prefix = prefix;
	}

	public GroupSorter<? extends Statement> getGroup()
	{
		return group;
	}

	public Namespace getPrefix()
	{
		return prefix;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
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
		if (prefix == null)
		{
			if (other.prefix != null)
				return false;
		}
		else if (!prefix.equals(other.prefix))
			return false;
		return true;
	}

}
