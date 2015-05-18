package aletheia.gui.contextjtree.statementsorter;

import java.util.NoSuchElementException;

import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.Namespace;
import aletheia.model.identifier.NodeNamespace;
import aletheia.model.identifier.RootNamespace;
import aletheia.model.statement.Statement;
import aletheia.persistence.collections.statement.SortedStatements;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCloseableIterable;
import aletheia.utilities.collections.CloseableIterable;
import aletheia.utilities.collections.CloseableIterator;
import aletheia.utilities.collections.CombinedCloseableIterable;

public class GroupStatementSorter<S extends Statement> extends StatementSorter<S> implements CloseableIterable<StatementSorter<S>>
{
	private final static int minGroupingSize = 0;
	private final static int minSubGroupSize = 2;

	private final SortedStatements<S> sortedStatements;
	private final Bijection<S, StatementSorter<S>> singletonBijection;

	public GroupStatementSorter(SortedStatements<S> sortedStatements)
	{
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

	public Namespace commonPrefix()
	{
		Identifier i0 = sortedStatements.first().getIdentifier();
		if (i0 == null)
			return null;
		Identifier i1 = sortedStatements.last().getIdentifier();
		if (i1 == null)
			return null;
		return i0.commonPrefix(i1);
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
						Statement prev = null;
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
											return new GroupStatementSorter<S>(sub);
									}
								}
								S st = next;
								CloseableIterator<S> iterator = identified.tailSet(next).iterator();
								try
								{
									iterator.next();
									prev = next;
									if (iterator.hasNext())
										next = iterator.next();
									else
										next = null;
								}
								finally
								{
									iterator.close();
								}
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
											return new GroupStatementSorter<S>(sub);
									}
								}
								prev = next;
								next = null;
								SortedStatements<S> tail = identified.tailSet(id);
								if (tail.smaller(minSubGroupSize))
								{
									iterator = tail.iterator();
									S st = iterator.next();
									if (!iterator.hasNext())
										iterator = null;
									return new SingletonStatementSorter<S>(st);
								}
								else
									return new GroupStatementSorter<S>(tail);
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

}
