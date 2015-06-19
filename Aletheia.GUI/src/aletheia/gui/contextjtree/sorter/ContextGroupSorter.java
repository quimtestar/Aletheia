package aletheia.gui.contextjtree.sorter;

import aletheia.model.statement.Context;
import aletheia.persistence.Transaction;

public class ContextGroupSorter extends StatementGroupSorter
{
	private final ContextSorter contextSorter;

	protected ContextGroupSorter(ContextSorter contextSorter)
	{
		super(null, null, contextSorter.getStatement());
		this.contextSorter = contextSorter;
	}

	public ContextSorter getContextSorter()
	{
		return contextSorter;
	}

	@Override
	public StatementGroupSorter getGroup()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Context getStatement(Transaction transaction)
	{
		return getContext();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((contextSorter == null) ? 0 : contextSorter.hashCode());
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
		ContextGroupSorter other = (ContextGroupSorter) obj;
		if (contextSorter == null)
		{
			if (other.contextSorter != null)
				return false;
		}
		else if (!contextSorter.equals(other.contextSorter))
			return false;
		return true;
	}

	@Override
	public boolean degenerate(Transaction transaction)
	{
		return false;
	}

}
