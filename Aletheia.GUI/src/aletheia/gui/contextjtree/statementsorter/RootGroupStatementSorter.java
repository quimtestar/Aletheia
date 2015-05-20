package aletheia.gui.contextjtree.statementsorter;

import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.collections.statement.SortedStatements;

public abstract class RootGroupStatementSorter<S extends Statement> extends GroupStatementSorter<S>
{
	private final Context context;

	protected RootGroupStatementSorter(Context context, SortedStatements<S> sortedStatements)
	{
		super(null, null, sortedStatements);
		this.context = context;
	}

	@Override
	public Context getContext()
	{
		return context;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((context == null) ? 0 : context.hashCode());
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
		RootGroupStatementSorter other = (RootGroupStatementSorter) obj;
		if (context == null)
		{
			if (other.context != null)
				return false;
		}
		else if (!context.equals(other.context))
			return false;
		return true;
	}

}
