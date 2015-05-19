package aletheia.gui.contextjtree.statementsorter;

import java.util.NoSuchElementException;

import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.Namespace;
import aletheia.model.identifier.NodeNamespace;
import aletheia.model.identifier.RootNamespace;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.collections.statement.SortedStatements;
import aletheia.utilities.MiscUtilities;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCloseableIterable;
import aletheia.utilities.collections.CloseableIterable;
import aletheia.utilities.collections.CloseableIterator;
import aletheia.utilities.collections.CombinedCloseableIterable;

public class GroupStatementSorter<S extends Statement> extends StatementSorter<S> implements CloseableIterable<StatementSorter<S>>
{
	private final static int minGroupingSize = 0;
	private final static int minSubGroupSize = 2;

	private final Context context;
	private final Namespace prefix;
	private final SortedStatements<S> sortedStatements;
	private final Bijection<S, StatementSorter<S>> singletonBijection;

	protected GroupStatementSorter(Context context, Namespace prefix, SortedStatements<S> sortedStatements)
	{
		this.context = context;
		this.prefix = prefix;
		this.sortedStatements = sortedStatements;
		this.singletonBijection = new Bijection<S, StatementSorter<S>>()
		{

			@Override
			public StatementSorter<S> forward(S statement)
			{
				return new SingletonStatementSorter<S>(statement);
			}

			@Override
			public S backward(StatementSorter<S> output)
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	public Context getContext()
	{
		return context;
	}

	public Namespace getPrefix()
	{
		return prefix;
	}

	@Override
	public CloseableIterator<StatementSorter<S>> iterator()
	{
		if (sortedStatements.smaller(minGroupingSize + 1))
			return new BijectionCloseableIterable<S, StatementSorter<S>>(singletonBijection, sortedStatements).iterator();
		else
		{
			CloseableIterable<StatementSorter<S>> assumptionIterable = new BijectionCloseableIterable<S, StatementSorter<S>>(singletonBijection,
					sortedStatements.headSet(RootNamespace.instance.initiator()));
			SortedStatements<S> nonAssumptions = sortedStatements.tailSet(RootNamespace.instance.initiator());
			final SortedStatements<S> identified = nonAssumptions.headSet(RootNamespace.instance.terminator());
			CloseableIterable<StatementSorter<S>> identifiedIterable = new CloseableIterable<StatementSorter<S>>()
			{

				@Override
				public CloseableIterator<StatementSorter<S>> iterator()
				{
					return new CloseableIterator<StatementSorter<S>>()
					{
						S next;
						{
							if (identified.isEmpty())
								next = null;
							else
								next = identified.first();
						}
						S prev = null;
						CloseableIterator<S> iterator = null;

						@Override
						public boolean hasNext()
						{
							return next != null || (iterator != null && iterator.hasNext());
						}

						@Override
						public StatementSorter<S> next()
						{
							if (iterator != null)
							{
								S st = iterator.next();
								if (!iterator.hasNext())
									iterator = null;
								return new SingletonStatementSorter<S>(st);
							}
							if (next == null)
								throw new NoSuchElementException();
							Identifier id = next.getIdentifier();
							if (prev == null)
							{
								for (NodeNamespace prefix : id.prefixList())
								{
									SortedStatements<S> tail = identified.tailSet(prefix.terminator());
									if (!tail.isEmpty())
									{
										prev = next;
										next = tail.first();
										SortedStatements<S> sub = identified.subSet(id, prefix.terminator());
										if (sub.smaller(minSubGroupSize))
										{
											iterator = sub.iterator();
											S st = iterator.next();
											if (!iterator.hasNext())
												iterator = null;
											return new SingletonStatementSorter<S>(st);
										}
										else
											return new GroupStatementSorter<S>(context, prefix, sub);
									}
								}
								iterator = identified.identifierSet(id).iterator();
								S st = iterator.next();
								if (!iterator.hasNext())
									iterator = null;
								prev = next;
								next = MiscUtilities.firstFromCloseableIterable(identified.postIdentifierSet(id));
								return new SingletonStatementSorter<S>(st);
							}
							else
							{
								for (NodeNamespace prefix : id.prefixList())
								{
									if (prefix.isPrefixOf(prev.getIdentifier()))
										continue;
									SortedStatements<S> sub = identified.subSet(id, prefix.terminator());
									if (!sub.isEmpty())
									{
										SortedStatements<S> tail = identified.tailSet(prefix.terminator());
										prev = next;
										if (tail.isEmpty())
											next = null;
										else
											next = tail.first();
										if (sub.smaller(minSubGroupSize))
										{
											iterator = sub.iterator();
											S st = iterator.next();
											if (!iterator.hasNext())
												iterator = null;
											return new SingletonStatementSorter<S>(st);
										}
										else
											return new GroupStatementSorter<S>(context, prefix, sub);
									}
								}
								throw new RuntimeException();
							}
						}

						@Override
						public void close()
						{
							if (iterator != null)
								iterator.close();
						}

					};
				}

			};
			CloseableIterable<StatementSorter<S>> nonIdentifiedIterable = new BijectionCloseableIterable<S, StatementSorter<S>>(singletonBijection,
					nonAssumptions.tailSet(RootNamespace.instance.terminator()));
			return new CombinedCloseableIterable<StatementSorter<S>>(assumptionIterable, new CombinedCloseableIterable<StatementSorter<S>>(identifiedIterable,
					nonIdentifiedIterable)).iterator();
		}
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((context == null) ? 0 : context.hashCode());
		result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
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
		GroupStatementSorter other = (GroupStatementSorter) obj;
		if (context == null)
		{
			if (other.context != null)
				return false;
		}
		else if (!context.equals(other.context))
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
