package aletheia.gui.contextjtree.statementsorter;

import java.util.NoSuchElementException;

import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.Namespace;
import aletheia.model.identifier.NodeNamespace;
import aletheia.model.identifier.RootNamespace;
import aletheia.model.statement.Statement;
import aletheia.persistence.collections.statement.LocalSortedStatements;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCloseableIterable;
import aletheia.utilities.collections.CloseableIterable;
import aletheia.utilities.collections.CloseableIterator;
import aletheia.utilities.collections.CombinedCloseableIterable;

public class GroupStatementSorter extends StatementSorter implements CloseableIterable<StatementSorter>
{
	private final static int minGroupingSize = 0;
	private final static int minSubGroupSize = 2;

	private final LocalSortedStatements localSortedStatements;

	public GroupStatementSorter(LocalSortedStatements localSortedStatements)
	{
		this.localSortedStatements = localSortedStatements;
	}

	public Namespace commonPrefix()
	{
		Identifier i0 = localSortedStatements.first().getIdentifier();
		if (i0 == null)
			return null;
		Identifier i1 = localSortedStatements.last().getIdentifier();
		if (i1 == null)
			return null;
		return i0.commonPrefix(i1);
	}

	@Override
	public CloseableIterator<StatementSorter> iterator()
	{
		Bijection<Statement, StatementSorter> singletonBijection = new Bijection<Statement, StatementSorter>()
		{

			@Override
			public StatementSorter forward(Statement statement)
			{
				return new SingletonStatementSorter(statement);
			}

			@Override
			public Statement backward(StatementSorter output)
			{
				throw new UnsupportedOperationException();
			}
		};

		if (localSortedStatements.smaller(minGroupingSize + 1))
					return new BijectionCloseableIterable<Statement, StatementSorter>(singletonBijection, localSortedStatements).iterator();
		else
		{
			CloseableIterable<StatementSorter> assumptionIterable = new BijectionCloseableIterable<Statement, StatementSorter>(singletonBijection,
					localSortedStatements.headSet(RootNamespace.instance.initiator()));
			LocalSortedStatements nonAssumptions = localSortedStatements.tailSet(RootNamespace.instance.initiator());
			final LocalSortedStatements identified = nonAssumptions.headSet(RootNamespace.instance.terminator());
			CloseableIterable<StatementSorter> identifiedIterable = new CloseableIterable<StatementSorter>()
			{

				@Override
				public CloseableIterator<StatementSorter> iterator()
				{
					return new CloseableIterator<StatementSorter>()
					{
						Statement next;
						{
							if (identified.isEmpty())
								next = null;
							else
								next = identified.first();
						}
						Statement prev = null;
						CloseableIterator<Statement> iterator = null;

						@Override
						public boolean hasNext()
						{
							return next != null || (iterator != null && iterator.hasNext());
						}

						@Override
						public StatementSorter next()
						{
							if (iterator != null)
							{
								Statement st = iterator.next();
								if (!iterator.hasNext())
									iterator = null;
								return new SingletonStatementSorter(st);
							}
							if (next == null)
								throw new NoSuchElementException();
							Identifier id = next.getIdentifier();
							if (prev == null)
							{
								for (NodeNamespace prefix : id.prefixList())
								{
									LocalSortedStatements tail = identified.tailSet(prefix.terminator());
									if (!tail.isEmpty())
									{
										prev = next;
										next = tail.first();
										LocalSortedStatements sub = identified.subSet(id, prefix.terminator());
										if (sub.smaller(minSubGroupSize))
										{
											iterator = sub.iterator();
											Statement st = iterator.next();
											if (!iterator.hasNext())
												iterator = null;
											return new SingletonStatementSorter(st);
										}
										else
											return new GroupStatementSorter(sub);
									}
								}
								Statement st = next;
								CloseableIterator<Statement> iterator = identified.tailSet(next).iterator();
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
								return new SingletonStatementSorter(st);
							}
							else
							{
								for (NodeNamespace prefix : id.prefixList())
								{
									if (prefix.isPrefixOf(prev.getIdentifier()))
										continue;
									LocalSortedStatements sub = identified.subSet(id, prefix.terminator());
									if (!sub.isEmpty())
									{
										LocalSortedStatements tail = identified.tailSet(prefix.terminator());
										prev = next;
										if (tail.isEmpty())
											next = null;
										else
											next = tail.first();
										if (sub.smaller(minSubGroupSize))
										{
											iterator = sub.iterator();
											Statement st = iterator.next();
											if (!iterator.hasNext())
												iterator = null;
											return new SingletonStatementSorter(st);
										}
										else
											return new GroupStatementSorter(sub);
									}
								}
								prev = next;
								next = null;
								LocalSortedStatements tail = identified.tailSet(id);
								if (tail.smaller(minSubGroupSize))
								{
									iterator = tail.iterator();
									Statement st = iterator.next();
									if (!iterator.hasNext())
										iterator = null;
									return new SingletonStatementSorter(st);
								}
								else
									return new GroupStatementSorter(tail);
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
			CloseableIterable<StatementSorter> nonIdentifiedIterable = new BijectionCloseableIterable<Statement, StatementSorter>(singletonBijection,
					nonAssumptions.tailSet(RootNamespace.instance.terminator()));
			return new CombinedCloseableIterable<StatementSorter>(assumptionIterable, new CombinedCloseableIterable<StatementSorter>(identifiedIterable,
					nonIdentifiedIterable)).iterator();
		}
	}

}
