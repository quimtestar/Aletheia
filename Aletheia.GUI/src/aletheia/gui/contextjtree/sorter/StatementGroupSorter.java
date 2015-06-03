package aletheia.gui.contextjtree.sorter;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;
import aletheia.persistence.collections.statement.LocalSortedStatements;

public class StatementGroupSorter extends GroupSorter<Statement>
{
	private final Context context;

	protected StatementGroupSorter(StatementGroupSorter group, Identifier prefix, Context context)
	{
		super(group, prefix);
		this.context = context;
	}

	@Override
	public StatementGroupSorter getGroup()
	{
		return (StatementGroupSorter) super.getGroup();
	}

	public Context getContext()
	{
		return context;
	}

	@Override
	public Statement getStatement(Transaction transaction)
	{
		if (getPrefix() == null)
			return null;
		return context.localIdentifierToStatement(transaction).get(getPrefix());
	}

	@Override
	protected LocalSortedStatements sortedStatements(Transaction transaction)
	{
		LocalSortedStatements sortedStatements = context.localSortedStatements(transaction);
		if (getPrefix() != null)
			return sortedStatements.subSet(getPrefix(), getPrefix().terminator());
		else
			return sortedStatements;

	}

	@Override
	protected StatementGroupSorter subGroupSorter(Identifier prefix)
	{
		return new StatementGroupSorter(this, prefix, context);
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
		StatementGroupSorter other = (StatementGroupSorter) obj;
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
