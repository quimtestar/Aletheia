package aletheia.gui.contextjtree.sorter.old;

import java.util.NoSuchElementException;

import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.Namespace;
import aletheia.model.identifier.NodeNamespace;
import aletheia.model.identifier.RootNamespace;
import aletheia.model.statement.Statement;
import aletheia.persistence.collections.statement.SortedStatements;
import aletheia.utilities.MiscUtilities;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCloseableIterable;
import aletheia.utilities.collections.CloseableIterable;
import aletheia.utilities.collections.CloseableIterator;
import aletheia.utilities.collections.CombinedCloseableIterable;

public class GroupSorter<S extends Statement> extends Sorter implements CloseableIterable<Sorter>
{
	private final static int minGroupingSize = 0;
	private final static int minSubGroupSize = 2;

	private final SortedStatements<S> sortedStatements;
	private final Bijection<S, Sorter> singletonBijection;

	protected GroupSorter(GroupSorter<S> group, Namespace prefix, SortedStatements<S> sortedStatements)
	{
		super(group, prefix);
		this.sortedStatements = sortedStatements;
		this.singletonBijection = new Bijection<S, Sorter>()
		{

			@Override
			public Sorter forward(S statement)
			{
				return new StatementSorter(GroupSorter.this, statement);
			}

			@Override
			public S backward(Sorter output)
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public CloseableIterator<Sorter> iterator()
	{
		if (sortedStatements.smaller(minGroupingSize + 1))
			return new BijectionCloseableIterable<S, Sorter>(singletonBijection, sortedStatements).iterator();
		else
		{
			CloseableIterable<Sorter> assumptionIterable = new BijectionCloseableIterable<S, Sorter>(singletonBijection,
					sortedStatements.headSet(RootNamespace.instance.initiator()));
			SortedStatements<S> nonAssumptions = sortedStatements.tailSet(RootNamespace.instance.initiator());
			final SortedStatements<S> identified = nonAssumptions.headSet(RootNamespace.instance.terminator());
			CloseableIterable<Sorter> identifiedIterable = new CloseableIterable<Sorter>()
			{

				@Override
				public CloseableIterator<Sorter> iterator()
				{
					return new CloseableIterator<Sorter>()
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
						public Sorter next()
						{
							if (iterator != null)
							{
								S st = iterator.next();
								if (!iterator.hasNext())
									iterator = null;
								return new StatementSorter(GroupSorter.this, st);
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
											return new StatementSorter(GroupSorter.this, st);
										}
										else
											return new GroupSorter<S>(GroupSorter.this, prefix, sub);
									}
								}
								iterator = identified.identifierSet(id).iterator();
								Statement st = iterator.next();
								if (!iterator.hasNext())
									iterator = null;
								prev = next;
								next = MiscUtilities.firstFromCloseableIterable(identified.postIdentifierSet(id));
								return new StatementSorter(GroupSorter.this, st);
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
											return new StatementSorter(GroupSorter.this, st);
										}
										else
											return new GroupSorter<S>(GroupSorter.this, prefix, sub);
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
			CloseableIterable<Sorter> nonIdentifiedIterable = new BijectionCloseableIterable<S, Sorter>(singletonBijection,
					nonAssumptions.tailSet(RootNamespace.instance.terminator()));
			return new CombinedCloseableIterable<Sorter>(assumptionIterable, new CombinedCloseableIterable<Sorter>(identifiedIterable,
					nonIdentifiedIterable)).iterator();
		}
	}

}
