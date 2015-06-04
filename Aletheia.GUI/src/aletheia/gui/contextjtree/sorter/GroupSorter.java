package aletheia.gui.contextjtree.sorter;

import java.util.NoSuchElementException;

import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.NodeNamespace;
import aletheia.model.identifier.RootNamespace;
import aletheia.model.statement.Assumption;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;
import aletheia.persistence.collections.statement.SortedStatements;
import aletheia.utilities.MiscUtilities;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCloseableIterable;
import aletheia.utilities.collections.CloseableIterable;
import aletheia.utilities.collections.CloseableIterator;
import aletheia.utilities.collections.CombinedCloseableIterable;

public abstract class GroupSorter<S extends Statement> extends Sorter
{
	private final static int minGroupingSize = 0;
	private final static int minSubGroupSize = 2;

	private final Bijection<S, Sorter> statementSorterBijection;

	protected GroupSorter(GroupSorter<S> group, Identifier prefix)
	{
		super(group, prefix);
		this.statementSorterBijection = new Bijection<S, Sorter>()
		{

			@Override
			public Sorter forward(S statement)
			{
				return statementSorter(statement);
			}

			@Override
			public S backward(Sorter output)
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	private StatementSorter statementSorter(Statement statement)
	{
		return StatementSorter.newStatementSorter(this, statement);
	}

	protected abstract SortedStatements<S> sortedStatements(Transaction transaction);

	protected abstract GroupSorter<S> subGroupSorter(Identifier prefix);

	public CloseableIterable<Sorter> iterable(Transaction transaction)
	{
		final SortedStatements<S> sortedStatements = sortedStatements(transaction);
		return new CloseableIterable<Sorter>()
		{

			@Override
			public CloseableIterator<Sorter> iterator()
			{
				if (sortedStatements.smaller(minGroupingSize + 1))
					return new BijectionCloseableIterable<S, Sorter>(statementSorterBijection, sortedStatements).iterator();
				else
				{
					CloseableIterable<Sorter> assumptionIterable = new BijectionCloseableIterable<S, Sorter>(statementSorterBijection,
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
										return statementSorter(st);
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
													return statementSorter(st);
												}
												else
													return subGroupSorter(prefix.asIdentifier());
											}
										}
										iterator = identified.identifierSet(id).iterator();
										Statement st = iterator.next();
										if (!iterator.hasNext())
											iterator = null;
										prev = next;
										next = MiscUtilities.firstFromCloseableIterable(identified.postIdentifierSet(id));
										return statementSorter(st);
									}
									else
									{
										for (NodeNamespace prefix : id.prefixList())
										{
											prefix.asIdentifier();
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
													return statementSorter(st);
												}
												else
													return subGroupSorter(prefix.asIdentifier());
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
					CloseableIterable<Sorter> nonIdentifiedIterable = new BijectionCloseableIterable<S, Sorter>(statementSorterBijection,
							nonAssumptions.tailSet(RootNamespace.instance.terminator()));
					return new CombinedCloseableIterable<Sorter>(assumptionIterable, new CombinedCloseableIterable<Sorter>(identifiedIterable,
							nonIdentifiedIterable)).iterator();
				}
			};

		};
	}

	private Sorter getByStatementByPrefix(Transaction transaction, Statement statement)
	{
		SortedStatements<S> set = sortedStatements(transaction).subSet(RootNamespace.instance.initiator(), RootNamespace.instance.terminator());
		for (NodeNamespace prefix : statement.getIdentifier().prefixList())
		{
			Identifier iPrefix = prefix.asIdentifier();
			if (!set.headSet(iPrefix).isEmpty() || !set.tailSet(iPrefix.terminator()).isEmpty())
			{
				GroupSorter<S> subGroupSorter = subGroupSorter(iPrefix);
				if (subGroupSorter.sortedStatements(transaction).smaller(minSubGroupSize))
					return statementSorter(statement);
				return subGroupSorter;
			}
		}
		return null;
	}

	public Sorter getByStatement(Transaction transaction, Statement statement)
	{
		SortedStatements<S> set = sortedStatements(transaction);
		if (!set.contains(statement))
			return null;
		if (set.smaller(minGroupingSize + 1))
			return statementSorter(statement);
		if (statement instanceof Assumption || statement.getIdentifier() == null)
			return statementSorter(statement);
		return getByStatementByPrefix(transaction, statement);
	}

	@SuppressWarnings("unchecked")
	public StatementSorter getByStatementDeep(Transaction transaction, Statement statement)
	{
		SortedStatements<S> set = sortedStatements(transaction);
		if (!set.contains(statement))
			return null;
		if (set.smaller(minGroupingSize + 1))
			return statementSorter(statement);
		if (statement instanceof Assumption || statement.getIdentifier() == null)
			return statementSorter(statement);
		GroupSorter<S> groupSorter = this;
		while (true)
		{
			Sorter sorter = groupSorter.getByStatementByPrefix(transaction, statement);
			if (sorter instanceof StatementSorter)
				return (StatementSorter) sorter;
			else if (sorter instanceof GroupSorter)
				groupSorter = (GroupSorter<S>) sorter;
			else
				throw new Error();
		}
	}

}
