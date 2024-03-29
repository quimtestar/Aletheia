/*******************************************************************************
 * Copyright (c) 2015, 2023 Quim Testar.
 *
 * This file is part of the Aletheia Proof Assistant.
 *
 * The Aletheia Proof Assistant is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * The Aletheia Proof Assistant is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with the Aletheia Proof Assistant. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package aletheia.gui.contextjtree.sorter;

import java.util.Collections;
import java.util.NoSuchElementException;

import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.Namespace;
import aletheia.model.identifier.NodeNamespace;
import aletheia.model.identifier.RootNamespace;
import aletheia.model.statement.Assumption;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;
import aletheia.persistence.collections.statement.SortedStatements;
import aletheia.utilities.MiscUtilities;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCloseableIterator;
import aletheia.utilities.collections.CloseableIterable;
import aletheia.utilities.collections.CloseableIterator;
import aletheia.utilities.collections.CombinedCloseableIterator;
import aletheia.utilities.collections.TrivialCloseableIterable;

public abstract class GroupSorter<S extends Statement> extends Sorter
{
	private final static int minGroupingSize = 0;
	private final static int minSubGroupSize = 2;

	private final Identifier prefix;

	private final Bijection<S, Sorter> statementSorterBijection;

	protected GroupSorter(GroupSorter<S> group, Identifier prefix)
	{
		super(group);
		this.prefix = prefix;
		if (group != null && group.getPrefix() != null && (prefix == null || !group.getPrefix().isPrefixOf(prefix)))
			throw new IllegalArgumentException("Inconsistent prefix.");
		this.statementSorterBijection = new Bijection<>()
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

	@Override
	public Identifier getPrefix()
	{
		return prefix;
	}

	@Override
	public String toString()
	{
		return "[prefix:" + prefix + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj) || (getClass() != obj.getClass()))
			return false;
		GroupSorter<? extends Statement> other = (GroupSorter<?>) obj;
		if (prefix == null)
		{
			if (other.prefix != null)
				return false;
		}
		else if (!prefix.equals(other.prefix))
			return false;
		return true;
	}

	private StatementSorter statementSorter(Statement statement)
	{
		return StatementSorter.newStatementSorter(this, statement);
	}

	public abstract SortedStatements<S> sortedStatements(Transaction transaction);

	@Override
	public SortedStatements<S> statements(Transaction transaction)
	{
		return sortedStatements(transaction);
	}

	protected abstract GroupSorter<S> subGroupSorter(Identifier prefix);

	public CloseableIterable<Sorter> iterable(Transaction transaction)
	{
		final SortedStatements<S> sortedStatements = sortedStatements(transaction);
		return new CloseableIterable<>()
		{

			@Override
			public CloseableIterator<Sorter> iterator()
			{
				if (sortedStatements.smaller(minGroupingSize + 1))
					return new BijectionCloseableIterator<>(statementSorterBijection, sortedStatements.iterator());
				else
				{
					SortedStatements<S> assumptions = sortedStatements.headSet(RootNamespace.instance.initiator());
					SortedStatements<S> nonAssumptions = sortedStatements.tailSet(RootNamespace.instance.initiator());
					final SortedStatements<S> identified = nonAssumptions.headSet(RootNamespace.instance.terminator());
					SortedStatements<S> nonIdentified = nonAssumptions.tailSet(RootNamespace.instance.terminator());

					CloseableIterator<Sorter> assumptionIterator = new BijectionCloseableIterator<>(statementSorterBijection, assumptions.iterator());
					CloseableIterator<Sorter> identifiedIterator = null;
					if (!(assumptions.isEmpty() && nonIdentified.isEmpty()) && !identified.smaller(2))
					{
						Namespace prefix = identified.first().getIdentifier().commonPrefix(identified.last().getIdentifier());
						if (prefix instanceof NodeNamespace)
							identifiedIterator = new TrivialCloseableIterable<>(
									Collections.<Sorter> singleton(subGroupSorter(((NodeNamespace) prefix).asIdentifier()))).iterator();
					}
					if (identifiedIterator == null)
						identifiedIterator = new CloseableIterator<>()
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

							@Override
							public void remove()
							{
								throw new UnsupportedOperationException();
							}

						};
					CloseableIterator<Sorter> nonIdentifiedIterator = new BijectionCloseableIterator<>(statementSorterBijection, nonIdentified.iterator());
					return new CombinedCloseableIterator<>(assumptionIterator, new CombinedCloseableIterator<>(identifiedIterator, nonIdentifiedIterator));
				}
			};

		};
	}

	private GroupSorter<S> getByPrefix(Transaction transaction, NodeNamespace nodeNamespace)
	{
		SortedStatements<S> sortedStatements = sortedStatements(transaction);
		SortedStatements<S> assumptions = sortedStatements.headSet(RootNamespace.instance.initiator());
		SortedStatements<S> nonAssumptions = sortedStatements.tailSet(RootNamespace.instance.initiator());
		SortedStatements<S> identified = nonAssumptions.headSet(RootNamespace.instance.terminator());
		SortedStatements<S> nonIdentified = nonAssumptions.tailSet(RootNamespace.instance.terminator());

		if (!(assumptions.isEmpty() && nonIdentified.isEmpty()) && !identified.smaller(2))
		{
			Namespace prefix = identified.first().getIdentifier().commonPrefix(identified.last().getIdentifier());
			if (prefix instanceof NodeNamespace)
			{
				if (getPrefix() != null && !getPrefix().isPrefixOf(prefix))
					return null;
				return subGroupSorter(((NodeNamespace) prefix).asIdentifier());
			}
		}
		for (NodeNamespace prefix : nodeNamespace.prefixList())
		{
			Identifier iPrefix = prefix.asIdentifier();
			if (!identified.headSet(iPrefix).isEmpty() || !identified.tailSet(iPrefix.terminator()).isEmpty())
			{
				if (getPrefix() != null && !getPrefix().isPrefixOf(iPrefix))
					return null;
				GroupSorter<S> subGroupSorter = subGroupSorter(iPrefix);
				if (subGroupSorter.sortedStatements(transaction).smaller(minSubGroupSize))
					return null;
				return subGroupSorter;
			}
		}
		return null;
	}

	public Sorter findByPrefix(Transaction transaction, NodeNamespace nodeNamespace)
	{
		GroupSorter<S> gs = this;
		while (true)
		{
			GroupSorter<S> gs_ = gs.getByPrefix(transaction, nodeNamespace);
			if (gs_ == null)
			{
				S s = gs.sortedStatements(transaction).get(nodeNamespace.asIdentifier());
				if (s == null)
					return null;
				return gs.statementSorter(s);
			}
			if (nodeNamespace.equals(gs_.getPrefix()))
				return gs_;
			gs = gs_;
		}
	}

	private Sorter getByStatementByPrefix(Transaction transaction, Statement statement)
	{
		GroupSorter<S> groupSorter = null;
		if (statement.getIdentifier() != null)
			groupSorter = getByPrefix(transaction, statement.getIdentifier());
		if (groupSorter != null)
			return groupSorter;
		return statementSorter(statement);
	}

	public Sorter getByStatement(Transaction transaction, Statement statement)
	{
		SortedStatements<S> set = sortedStatements(transaction);
		if (!set.contains(statement))
			return null;
		if (set.smaller(minGroupingSize + 1) || statement instanceof Assumption || statement.getIdentifier() == null)
			return statementSorter(statement);
		return getByStatementByPrefix(transaction, statement);
	}

	@SuppressWarnings("unchecked")
	public StatementSorter getByStatementDeep(Transaction transaction, Statement statement)
	{
		SortedStatements<S> set = sortedStatements(transaction);
		if (!set.contains(statement))
			return null;
		if (set.smaller(minGroupingSize + 1) || statement instanceof Assumption || statement.getIdentifier() == null)
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

	public boolean degenerate(Transaction transaction)
	{
		return sortedStatements(transaction).smaller(minSubGroupSize);
	}

}
