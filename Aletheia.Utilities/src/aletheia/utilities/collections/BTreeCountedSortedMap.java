/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
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
package aletheia.utilities.collections;

import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.Stack;

import aletheia.utilities.CastComparator;
import aletheia.utilities.MiscUtilities;

/**
 * Implementation of a {@link CountedSortedMap} with a
 * <a href="http://en.wikipedia.org/wiki/Btree">B-tree</a>.
 *
 * @author Quim Testar
 */
public class BTreeCountedSortedMap<K, V> implements CountedSortedMap<K, V>
{
	private static class BTree
	{
		private final int order;
		private final Comparator<Object> comparator;

		private abstract class Node
		{
			private Object[] keys;
			private Object[] values;
			private int nKeys;
			private int load;

			private Node()
			{
				keys = new Object[2 * order];
				values = new Object[2 * order];
				nKeys = 0;
				load = 0;
			}

			class SearchResult
			{
				private final int index;
				private final Object key;
				@SuppressWarnings("unused")
				private final Object value;

				private SearchResult(int index, Object key, Object value)
				{
					super();
					this.index = index;
					this.key = key;
					this.value = value;
				}
			}

			protected SearchResult search(Object k)
			{
				int from = 0;
				int to = getNKeys();
				while (true)
				{
					int pivot = (from + to) / 2;
					if (pivot >= to)
						return new SearchResult(pivot, null, null);
					Object k_ = keys[pivot];
					int c = keyCompare(k, k_);
					if (c < 0)
						to = pivot;
					else if (c > 0)
						from = pivot + 1;
					else
						return new SearchResult(pivot, k_, values[pivot]);
				}
			}

			protected int getNKeys()
			{
				return nKeys;
			}

			protected void setNKeys(int nKeys)
			{
				this.nKeys = nKeys;
			}

			protected Object getKey(int i)
			{
				return keys[i];
			}

			@SuppressWarnings("unused")
			protected Object setKey(int i, Object key)
			{
				Object old = keys[i];
				keys[i] = key;
				return old;
			}

			protected Object getValue(int i)
			{
				return values[i];
			}

			protected Object setValue(int i, Object value)
			{
				Object old = values[i];
				values[i] = value;
				return old;
			}

			protected Object set(int i, Object key, Object value)
			{
				keys[i] = key;
				Object old = values[i];
				values[i] = value;
				return old;
			}

			protected boolean full()
			{
				return getNKeys() >= 2 * order;
			}

			protected void insert(int position, Object key, Object value)
			{
				for (int i = position; i < getNKeys(); i++)
				{
					Object key_ = getKey(i);
					Object value_ = getValue(i);
					set(i, key, value);
					key = key_;
					value = value_;
				}
				set(getNKeys(), key, value);
				setNKeys(getNKeys() + 1);
				updateLoad();
			}

			protected void delete(int position)
			{
				for (int i = position; i < getNKeys() - 1; i++)
					set(i, getKey(i + 1), getValue(i + 1));
				setNKeys(getNKeys() - 1);
				updateLoad();
			}

			protected abstract Node makeBrother();

			protected class SplitResult
			{
				private final Node left;
				private final Object midKey;
				private final Object midValue;
				private final Node right;

				protected SplitResult(Node left, Object midKey, Object midValue, Node right)
				{
					super();
					this.left = left;
					this.midKey = midKey;
					this.midValue = midValue;
					this.right = right;
				}

				protected Node getLeft()
				{
					return left;
				}

				protected Object getMidKey()
				{
					return midKey;
				}

				protected Object getMidValue()
				{
					return midValue;
				}

				protected Node getRight()
				{
					return right;
				}

			}

			protected SplitResult split(int position, Object key, Object value)
			{
				return splitToBrother(position, key, value, makeBrother());
			}

			protected SplitResult splitToBrother(int position, Object key, Object value, Node brother)
			{
				for (int i = position; i < order; i++)
				{
					Object key_ = getKey(i);
					Object value_ = getValue(i);
					set(i, key, value);
					key = key_;
					value = value_;
				}
				Object midKey;
				Object midValue;
				if (position <= order)
				{
					midKey = key;
					midValue = value;
					key = getKey(order);
					value = getValue(order);
				}
				else
				{
					midKey = getKey(order);
					midValue = getValue(order);
				}
				for (int i = order; i < getNKeys(); i++)
				{
					if (i + 1 < position)
						brother.set(i - order, getKey(i + 1), getValue(i + 1));
					else
					{
						brother.set(i - order, key, value);
						if (i + 1 < getNKeys())
						{
							key = getKey(i + 1);
							value = getValue(i + 1);
						}
					}
				}
				int bNKeys = getNKeys() - order;
				setNKeys(order);
				brother.setNKeys(bNKeys);
				updateLoad();
				brother.updateLoad();
				return new SplitResult(this, midKey, midValue, brother);
			}

			@Deprecated
			protected void trace(PrintStream out, int indent, Object before, Object after)
			{
				for (int i = 0; i < indent; i++)
					out.print("   ");
				if (before != null)
					out.print(before.toString() + " ");
				out.print(toString());
				if (after != null)
					out.print(" " + after.toString());
				out.print(" (" + getLoad() + ")");
				out.println();
			}

			@Override
			public String toString()
			{
				StringBuilder sb = new StringBuilder();
				sb.append("[");
				for (int i = 0; i < getNKeys(); i++)
				{
					if (i > 0)
						sb.append(", ");
					sb.append(getKey(i).toString());
				}
				sb.append("]");
				return sb.toString();
			}

			protected void fusion(Object key, Object value, Node right)
			{
				set(getNKeys(), key, value);
				for (int i = 0; i < right.getNKeys(); i++)
					set(getNKeys() + i + 1, right.getKey(i), right.getValue(i));
				setNKeys(getNKeys() + 1 + right.getNKeys());
				updateLoad();
			}

			protected boolean isEmpty()
			{
				return getNKeys() == 0;
			}

			protected int getLoad()
			{
				return load;
			}

			private void setLoad(int load)
			{
				this.load = load;
			}

			protected int calcLoad()
			{
				return getNKeys();
			}

			protected boolean updateLoad()
			{
				int load = calcLoad();
				if (getLoad() != load)
				{
					setLoad(load);
					return true;
				}
				else
					return false;
			}

		}

		private class BranchNode extends Node
		{
			private Node[] children;

			private BranchNode()
			{
				super();
				children = new Node[2 * order + 1];
			}

			protected Node getChild(int i)
			{
				return children[i];
			}

			protected void setChild(int i, Node child)
			{
				children[i] = child;
			}

			@Override
			protected BranchNode makeBrother()
			{
				return new BranchNode();
			}

			@Override
			protected void insert(int position, Object key, Object value)
			{
				throw new UnsupportedOperationException();
			}

			protected void insert(int position, Object key, Object value, Node child)
			{
				for (int i = position + 1; i <= getNKeys(); i++)
				{
					Node child_ = getChild(i);
					setChild(i, child);
					child = child_;
				}
				setChild(getNKeys() + 1, child);
				super.insert(position, key, value);
			}

			@Override
			protected void delete(int position)
			{
				for (int i = position + 1; i <= getNKeys() - 1; i++)
					setChild(i, getChild(i + 1));
				super.delete(position);
			}

			@Override
			protected SplitResult split(int position, Object key, Object value)
			{
				throw new UnsupportedOperationException();
			}

			@Override
			protected SplitResult splitToBrother(int position, Object key, Object value, Node brother)
			{
				throw new UnsupportedOperationException();
			}

			protected SplitResult split(int position, Object key, Object value, Node child)
			{
				return splitToBrother(position, key, value, child, makeBrother());
			}

			protected SplitResult splitToBrother(int position, Object key, Object value, Node child, BranchNode brother)
			{
				for (int i = position + 1; i <= order; i++)
				{
					Node child_ = getChild(i);
					setChild(i, child);
					child = child_;
				}
				for (int i = order + 1; i <= getNKeys() + 1; i++)
				{
					if (i <= position)
						brother.setChild(i - order - 1, getChild(i));
					else
					{
						brother.setChild(i - order - 1, child);
						if (i <= getNKeys())
							child = getChild(i);
					}
				}
				return super.splitToBrother(position, key, value, brother);
			}

			@Override
			protected void fusion(Object key, Object value, Node right)
			{
				for (int i = 0; i <= right.getNKeys(); i++)
					setChild(getNKeys() + i + 1, ((BranchNode) right).getChild(i));
				super.fusion(key, value, right);
			}

			@Override
			protected int calcLoad()
			{
				int load = super.calcLoad();
				for (int i = 0; i <= getNKeys(); i++)
					load += getChild(i).getLoad();
				return load;
			}

		}

		private class LeafNode extends Node
		{
			private LeafNode()
			{
				super();
			}

			@Override
			protected LeafNode makeBrother()
			{
				return new LeafNode();
			}

		}

		private Node rootNode;

		public BTree(int order, Comparator<Object> comparator)
		{
			this.order = order;
			this.comparator = comparator;
			this.rootNode = new LeafNode();
		}

		@SuppressWarnings("unchecked")
		private int keyCompare(Object k1, Object k2)
		{
			if (comparator == null)
				return ((Comparable<Object>) k1).compareTo(k2);
			else
				return comparator.compare(k1, k2);
		}

		private abstract class BranchStep
		{
			private final Node node;

			private BranchStep(Node node)
			{
				this.node = node;
			}

			protected Node getNode()
			{
				return node;
			}

			@Override
			public abstract String toString();

		}

		private class MidBranchStep extends BranchStep
		{
			private final int numChild;

			private MidBranchStep(BranchNode node, int numChild)
			{
				super(node);
				this.numChild = numChild;
			}

			@Override
			protected BranchNode getNode()
			{
				return (BranchNode) super.getNode();
			}

			protected int getNumChild()
			{
				return numChild;
			}

			@Override
			public String toString()
			{
				Node node = getNode();
				StringBuilder sb = new StringBuilder();
				sb.append("[");
				for (int i = 0; i < node.getNKeys(); i++)
				{
					if (i == getNumChild())
					{
						if (i > 0)
							sb.append(" ");
						sb.append("(*) ");
					}
					else if (i > 0)
						sb.append(", ");
					sb.append(node.getKey(i).toString());
				}
				if (node.getNKeys() == getNumChild())
					sb.append(" (*)");
				sb.append("]");
				return sb.toString();
			}

		}

		private class FinalBranchStep extends BranchStep
		{
			private final int position;
			private boolean found;

			private FinalBranchStep(Node node, int position, boolean found)
			{
				super(node);
				this.position = position;
				this.found = found;
			}

			protected int getPosition()
			{
				return position;
			}

			protected boolean isFound()
			{
				return found;
			}

			protected Object getKey()
			{
				return found ? getNode().getKey(position) : null;
			}

			protected Object getValue()
			{
				return found ? getNode().getValue(position) : null;
			}

			protected Object setValue(Object value)
			{
				if (!found)
					throw new IllegalStateException();
				return getNode().setValue(position, value);
			}

			@Override
			public String toString()
			{
				Node node = getNode();
				StringBuilder sb = new StringBuilder();
				sb.append("[");
				for (int i = 0; i < node.getNKeys(); i++)
				{
					if (i > 0)
						sb.append(", ");
					if (i == getPosition())
						sb.append("*");
					sb.append(node.getKey(i).toString());
					if (i == getPosition() && isFound())
						sb.append("*");
				}
				if (node.getNKeys() == getPosition() && !isFound())
					sb.append("*");
				sb.append("]");
				return sb.toString();
			}

		}

		private class Branch extends ArrayDeque<BranchStep> implements Comparable<Branch>
		{
			private static final long serialVersionUID = -6415836596967802045L;

			private FinalBranchStep getFinalBranchStep()
			{
				return (FinalBranchStep) peek();
			}

			private boolean isFound()
			{
				return getFinalBranchStep().isFound();
			}

			private Object getKey()
			{
				return getFinalBranchStep().getKey();
			}

			private Object getValue()
			{
				return getFinalBranchStep().getValue();
			}

			@SuppressWarnings("unused")
			private Object setValue(Object value)
			{
				return getFinalBranchStep().setValue(value);
			}

			private void search(Node node, Object key)
			{
				while (true)
				{
					Node.SearchResult sr = node.search(key);
					if (node instanceof LeafNode)
					{
						push(new FinalBranchStep(node, sr.index, sr.key != null));
						break;
					}
					else if (node instanceof BranchNode)
					{
						BranchNode branchNode = (BranchNode) node;
						if (sr.key == null)
						{
							push(new MidBranchStep(branchNode, sr.index));
							node = branchNode.getChild(sr.index);
						}
						else
						{
							push(new FinalBranchStep(branchNode, sr.index, sr.key != null));
							break;
						}
					}
					else
						throw new Error();

				}
			}

			private void first(Node node)
			{
				while (true)
				{
					if (node instanceof LeafNode)
					{
						if (node.isEmpty())
							throw new NoSuchElementException();
						push(new FinalBranchStep(node, 0, true));
						break;
					}
					else if (node instanceof BranchNode)
					{
						BranchNode branchNode = (BranchNode) node;
						push(new MidBranchStep(branchNode, 0));
						node = branchNode.getChild(0);
					}
					else
						throw new Error();
				}
			}

			private void last(Node node)
			{
				while (true)
				{
					if (node instanceof LeafNode)
					{
						if (node.isEmpty())
							throw new NoSuchElementException();
						push(new FinalBranchStep(node, node.getNKeys() - 1, true));
						break;
					}
					else if (node instanceof BranchNode)
					{
						BranchNode branchNode = (BranchNode) node;
						push(new MidBranchStep(branchNode, node.getNKeys()));
						node = branchNode.getChild(node.getNKeys());
					}
					else
						throw new Error();

				}
			}

			private void updateLoad()
			{
				for (BranchStep step : this)
					if (!step.getNode().updateLoad())
						break;
			}

			private boolean atBeginning()
			{
				if (isEmpty())
					return BTree.this.isEmpty();
				else
				{
					for (BranchStep step : this)
					{
						if (step instanceof MidBranchStep)
						{
							if (((MidBranchStep) step).getNumChild() != 0)
								return false;
						}
						else if (step instanceof FinalBranchStep)
						{
							if ((step.getNode() instanceof BranchNode) || ((FinalBranchStep) step).getPosition() != 0
									|| (!step.getNode().isEmpty() && !((FinalBranchStep) step).isFound()))
								return false;
						}
						else
							throw new Error();
					}
					return true;
				}
			}

			private boolean atEnd()
			{
				if (isEmpty())
					return BTree.this.isEmpty();
				else
				{
					for (BranchStep step : this)
					{
						if (step instanceof MidBranchStep)
						{
							if (((MidBranchStep) step).getNumChild() < step.getNode().getNKeys())
								return false;
						}
						else if (step instanceof FinalBranchStep)
						{
							if ((step.getNode() instanceof BranchNode) || ((FinalBranchStep) step).getPosition() < step.getNode().getNKeys() - 1
									|| (!step.getNode().isEmpty() && !((FinalBranchStep) step).isFound()))
								return false;
						}
						else
							throw new Error();
					}
					return true;
				}
			}

			private void unFind()
			{
				if (isFound())
				{
					FinalBranchStep fbs = (FinalBranchStep) pop();
					push(new FinalBranchStep(fbs.getNode(), fbs.getPosition(), false));
				}
			}

			private void forward()
			{
				if (isEmpty())
					first(rootNode);
				else
				{
					FinalBranchStep fbs = (FinalBranchStep) pop();
					if (fbs.getNode() instanceof LeafNode)
					{
						int nextPos = fbs.isFound() ? fbs.getPosition() + 1 : fbs.getPosition();
						if (nextPos < fbs.getNode().getNKeys())
							push(new FinalBranchStep(fbs.getNode(), nextPos, true));
						else
						{
							while (!isEmpty())
							{
								MidBranchStep mbs = (MidBranchStep) pop();
								int pos = mbs.getNumChild();
								if (pos < mbs.getNode().getNKeys())
								{
									push(new FinalBranchStep(mbs.getNode(), pos, true));
									return;
								}
							}
							throw new NoSuchElementException();
						}
					}
					else if (fbs.getNode() instanceof BranchNode)
					{
						BranchNode branchNode = (BranchNode) fbs.getNode();
						int pos = fbs.getPosition() + 1;
						push(new MidBranchStep(branchNode, pos));
						first(branchNode.getChild(pos));
					}
					else
						throw new Error();
				}
			}

			private void backward()
			{
				if (isEmpty())
					last(rootNode);
				else
				{
					FinalBranchStep fbs = (FinalBranchStep) pop();
					if (fbs.getNode() instanceof LeafNode)
					{
						int prevPos = fbs.getPosition() - 1;
						if (prevPos >= 0)
							push(new FinalBranchStep(fbs.getNode(), prevPos, true));
						else
						{
							while (!isEmpty())
							{
								MidBranchStep mbs = (MidBranchStep) pop();
								int pos = mbs.getNumChild() - 1;
								if (pos >= 0)
								{
									push(new FinalBranchStep(mbs.getNode(), pos, true));
									return;
								}
							}
							throw new NoSuchElementException();
						}
					}
					else if (fbs.getNode() instanceof BranchNode)
					{
						BranchNode branchNode = (BranchNode) fbs.getNode();
						int pos = fbs.getPosition();
						push(new MidBranchStep(branchNode, pos));
						last(branchNode.getChild(pos));
					}
					else
						throw new Error();
				}
			}

			private void byOrdinal(Node node, int ordinal)
			{
				if (ordinal < 0 || ordinal >= node.getLoad())
					throw new NoSuchElementException();
				loop: while (true)
				{
					if (node instanceof LeafNode)
					{
						LeafNode leafNode = (LeafNode) node;
						push(new FinalBranchStep(leafNode, ordinal, true));
						return;
					}
					else if (node instanceof BranchNode)
					{
						BranchNode branchNode = (BranchNode) node;
						for (int i = 0; i < branchNode.getNKeys(); i++)
						{
							if (ordinal < branchNode.getChild(i).getLoad())
							{
								push(new MidBranchStep(branchNode, i));
								node = branchNode.getChild(i);
								continue loop;
							}
							else if (ordinal == branchNode.getChild(i).getLoad())
							{
								push(new FinalBranchStep(branchNode, i, true));
								return;
							}
							ordinal -= branchNode.getChild(i).getLoad() + 1;
						}
						push(new MidBranchStep(branchNode, branchNode.getNKeys()));
						node = branchNode.getChild(branchNode.getNKeys());
					}
					else
						throw new Error();
				}
			}

			private int ordinal()
			{
				int ordinal = 0;
				for (BranchStep step : this)
				{
					if (step instanceof FinalBranchStep)
					{
						FinalBranchStep finalBranchStep = (FinalBranchStep) step;
						ordinal += finalBranchStep.getPosition();
						if (step.getNode() instanceof BranchNode)
						{
							BranchNode branchNode = (BranchNode) step.getNode();
							for (int i = 0; i <= finalBranchStep.getPosition(); i++)
								ordinal += branchNode.getChild(i).getLoad();
						}
					}
					else if (step instanceof MidBranchStep)
					{
						MidBranchStep midBranchStep = (MidBranchStep) step;
						ordinal += midBranchStep.getNumChild();
						BranchNode branchNode = (BranchNode) step.getNode();
						for (int i = 0; i < midBranchStep.getNumChild(); i++)
							ordinal += branchNode.getChild(i).getLoad();
					}
				}
				return ordinal;
			}

			@Override
			public int compareTo(Branch o)
			{
				Iterator<BranchStep> i1 = this.descendingIterator();
				Iterator<BranchStep> i2 = o.descendingIterator();
				while (i1.hasNext() && i2.hasNext())
				{
					BranchStep step1 = i1.next();
					BranchStep step2 = i2.next();
					if (step1 instanceof MidBranchStep)
					{
						MidBranchStep mbs1 = (MidBranchStep) step1;
						if (step2 instanceof MidBranchStep)
						{
							MidBranchStep mbs2 = (MidBranchStep) step2;
							int c = Integer.compare(mbs1.getNumChild(), mbs2.getNumChild());
							if (c != 0)
								return c;
						}
						else if (step2 instanceof FinalBranchStep)
						{
							FinalBranchStep fbs2 = (FinalBranchStep) step2;
							int c = Integer.compare(mbs1.getNumChild(), fbs2.getPosition());
							if (c == 0)
								return -1;
							return c;
						}
						else
							throw new Error();
					}
					else if (step1 instanceof FinalBranchStep)
					{
						FinalBranchStep fbs1 = (FinalBranchStep) step1;
						if (step2 instanceof MidBranchStep)
						{
							MidBranchStep mbs2 = (MidBranchStep) step2;
							int c = Integer.compare(fbs1.getPosition(), mbs2.getNumChild());
							if (c == 0)
								return +1;
							return c;
						}
						else if (step2 instanceof FinalBranchStep)
						{
							FinalBranchStep fbs2 = (FinalBranchStep) step2;
							int c = Integer.compare(fbs1.getPosition(), fbs2.getPosition());
							if (c != 0)
								return c;
						}
						else
							throw new Error();
					}
				}
				return 0;
			}

			private Object prevKey()
			{
				FinalBranchStep fbs = getFinalBranchStep();
				return fbs.getNode().getKey(fbs.getPosition());
			}

			private Object nextKey()
			{
				Iterator<BranchStep> iterator = iterator();
				FinalBranchStep fbs = (FinalBranchStep) iterator.next();
				Node node = fbs.getNode();
				if (!fbs.isFound())
				{
					if (fbs.getPosition() < node.getNKeys())
						return node.getKey(fbs.getPosition());
					else
					{
						while (iterator.hasNext())
						{
							MidBranchStep mbs = (MidBranchStep) iterator.next();
							if (mbs.getNumChild() < mbs.getNode().getNKeys())
								return mbs.getNode().getKey(mbs.getNumChild());
						}
						throw new NoSuchElementException();
					}
				}
				else
				{
					if (node instanceof LeafNode)
					{
						if (fbs.getPosition() + 1 < node.getNKeys())
							return node.getKey(fbs.getPosition() + 1);
						else
						{
							while (iterator.hasNext())
							{
								MidBranchStep mbs = (MidBranchStep) iterator.next();
								if (mbs.getNumChild() < mbs.getNode().getNKeys())
									return mbs.getNode().getKey(mbs.getNumChild());
							}
							throw new NoSuchElementException();
						}
					}
					else if (node instanceof BranchNode)
					{
						BranchNode branchNode = (BranchNode) node;
						Node child = branchNode.getChild(fbs.getPosition() + 1);
						while (true)
						{
							if (child instanceof LeafNode)
								return child.getKey(0);
							else if (child instanceof BranchNode)
								child = ((BranchNode) child).getChild(0);
							else
								throw new Error();
						}
					}
					else
						throw new Error();
				}

			}

			@Override
			public String toString()
			{
				StringBuilder sb = new StringBuilder();
				sb.append("[");
				boolean first = true;
				for (Iterator<BranchStep> iterator = descendingIterator(); iterator.hasNext();)
				{
					if (!first)
						sb.append(", ");
					else
						first = false;
					BranchStep step = iterator.next();
					sb.append(step.toString());
				}
				sb.append("]");
				return sb.toString();
			}

		}

		private Branch branchSearch(Object key)
		{
			return branchSearch(rootNode, key);
		}

		private Branch branchSearch(Node node, Object key)
		{
			Branch branch = new Branch();
			branch.search(node, key);
			return branch;
		}

		private Branch branchFirst(Node node)
		{
			Branch branch = new Branch();
			branch.first(node);
			return branch;
		}

		private Branch branchFirst()
		{
			return branchFirst(rootNode);
		}

		private Branch branchLast(Node node)
		{
			Branch branch = new Branch();
			branch.last(node);
			return branch;
		}

		private Branch branchLast()
		{
			return branchLast(rootNode);
		}

		private Branch branchByOrdinal(int ordinal)
		{
			return branchByOrdinal(rootNode, ordinal);
		}

		private Branch branchByOrdinal(Node node, int ordinal)
		{
			Branch branch = new Branch();
			branch.byOrdinal(node, ordinal);
			return branch;
		}

		private Object insert(Object key, Object value)
		{
			Branch branch = branchSearch(key);
			FinalBranchStep fbs = (FinalBranchStep) branch.pop();
			if (fbs.isFound())
				return fbs.getNode().setValue(fbs.position, value);
			else
			{
				if (!fbs.getNode().full())
				{
					fbs.getNode().insert(fbs.position, key, value);
					branch.updateLoad();
					return null;
				}
				else
				{
					Node node = fbs.getNode();
					Node.SplitResult splitResult = node.split(fbs.position, key, value);
					while (!branch.isEmpty())
					{
						MidBranchStep mbs = (MidBranchStep) branch.pop();
						if (!mbs.getNode().full())
						{
							mbs.getNode().insert(mbs.getNumChild(), splitResult.getMidKey(), splitResult.getMidValue(), splitResult.getRight());
							branch.updateLoad();
							return null;
						}
						else
							splitResult = mbs.getNode().split(mbs.getNumChild(), splitResult.getMidKey(), splitResult.getMidValue(), splitResult.getRight());
					}
					BranchNode rootNode_ = new BranchNode();
					rootNode_.setNKeys(1);
					rootNode_.set(0, splitResult.getMidKey(), splitResult.getMidValue());
					rootNode_.setChild(0, splitResult.getLeft());
					rootNode_.setChild(1, splitResult.getRight());
					rootNode_.updateLoad();
					rootNode = rootNode_;
					return null;
				}
			}
		}

		private Object get(Object key)
		{
			try
			{
				return branchSearch(key).getValue();
			}
			catch (ClassCastException e)
			{
				return null;
			}
		}

		private Object branchRemove(Branch branch)
		{
			FinalBranchStep fbs = (FinalBranchStep) branch.peek();
			if (!fbs.isFound())
				return null;
			Branch branchExtra;
			if (fbs.getNode() instanceof BranchNode)
			{
				BranchNode branchNode = (BranchNode) fbs.getNode();
				branchExtra = branchLast(branchNode.getChild(fbs.getPosition()));
				FinalBranchStep fbsExtra = (FinalBranchStep) branchExtra.peek();
				branchNode.set(fbs.getPosition(), fbsExtra.getKey(), fbsExtra.getValue());
			}
			else
				branchExtra = new Branch();
			Deque<BranchStep> combined = new CombinedDeque<>(branchExtra, branch);
			int position = ((FinalBranchStep) combined.peek()).getPosition();
			while (!combined.isEmpty())
			{
				BranchStep step = combined.pop();
				step.getNode().delete(position);
				if (step.getNode().getNKeys() < order && !combined.isEmpty())
				{
					BranchStep parentStep = combined.peek();
					int nc;
					if (parentStep instanceof FinalBranchStep)
						nc = ((FinalBranchStep) parentStep).getPosition();
					else if (parentStep instanceof MidBranchStep)
						nc = ((MidBranchStep) parentStep).getNumChild();
					else
						throw new Error();
					BranchNode parent = (BranchNode) parentStep.getNode();
					Node lb = null;
					int lbn = 0;
					if (nc > 0)
					{
						lb = parent.getChild(nc - 1);
						lbn = lb.getNKeys();
					}
					Node rb = null;
					int rbn = 0;
					if (nc < parent.getNKeys())
					{
						rb = parent.getChild(nc + 1);
						rbn = rb.getNKeys();
					}
					Node left = null;
					Node right = null;
					int mid;
					if (lbn > rbn)
					{
						left = lb;
						right = step.getNode();
						mid = nc - 1;
					}
					else
					{
						left = step.getNode();
						right = rb;
						mid = nc;
					}
					if ((left.getNKeys() + right.getNKeys()) / 2 >= order)
					{
						rotate(left, parent, mid, right);
						break;
					}
					else
						left.fusion(parent.getKey(mid), parent.getValue(mid), right);
					position = mid;
				}
				else if ((step.getNode().getNKeys() == 0) && (step.getNode() instanceof BranchNode))
				{
					rootNode = ((BranchNode) step.getNode()).getChild(0);
					break;
				}
				else
					break;
			}
			branchExtra.updateLoad();
			branch.updateLoad();
			return fbs.getValue();
		}

		private Object remove(Object key)
		{
			return branchRemove(branchSearch(key));
		}

		private void rotate(Node left, BranchNode parent, int mid, Node right)
		{
			if ((left instanceof BranchNode) && (right instanceof BranchNode))
				rotate_((BranchNode) left, parent, mid, (BranchNode) right);
			else if ((left instanceof LeafNode) && (right instanceof LeafNode))
				rotate_(left, parent, mid, right);
			else
				throw new RuntimeException();
		}

		private void rotate_(BranchNode left, BranchNode parent, int mid, BranchNode right)
		{
			int lnk = (left.getNKeys() + right.getNKeys()) / 2;
			if (lnk < left.getNKeys())
			{
				List<Node> children = new ArrayList<>();
				for (int i = lnk + 1; i <= left.getNKeys(); i++)
				{
					children.add(right.getChild(i - lnk - 1));
					right.setChild(i - lnk - 1, left.getChild(i));
				}
				for (int i = left.getNKeys() - lnk, j = 0; j < children.size(); i++, j++)
				{
					if (i <= right.getNKeys())
						children.add(right.getChild(i));
					right.setChild(i, children.get(j));
				}
			}
			else
			{
				for (int i = left.getNKeys() + 1; i <= lnk; i++)
					left.setChild(i, right.getChild(i - left.getNKeys() - 1));
				for (int i = 0; i <= left.getNKeys() + right.getNKeys() - lnk; i++)
					right.setChild(i, right.getChild(lnk + i - left.getNKeys()));
			}
			rotate_((Node) left, parent, mid, (Node) right);
		}

		private void rotate_(Node left, BranchNode parent, int mid, Node right)
		{
			int lnk = (left.getNKeys() + right.getNKeys()) / 2;
			if (lnk < left.getNKeys())
			{
				List<Object> keys = new ArrayList<>();
				List<Object> values = new ArrayList<>();
				keys.add(parent.getKey(mid));
				values.add(parent.getValue(mid));
				parent.set(mid, left.getKey(lnk), left.getValue(lnk));
				for (int i = lnk + 1; i < left.getNKeys(); i++)
				{
					keys.add(right.getKey(i - lnk - 1));
					values.add(right.getValue(i - lnk - 1));
					right.set(i - lnk - 1, left.getKey(i), left.getValue(i));
				}
				for (int i = left.getNKeys() - lnk - 1, j = 0; j < keys.size(); i++, j++)
				{
					if (i < right.getNKeys())
					{
						keys.add(right.getKey(i));
						values.add(right.getValue(i));
					}
					right.set(i, keys.get(j), values.get(j));
				}
			}
			else
			{
				left.set(left.getNKeys(), parent.getKey(mid), parent.getValue(mid));
				for (int i = left.getNKeys() + 1; i < lnk; i++)
					left.set(i, right.getKey(i - left.getNKeys() - 1), right.getValue(i - left.getNKeys() - 1));
				parent.set(mid, right.getKey(lnk - left.getNKeys() - 1), right.getValue(lnk - left.getNKeys() - 1));
				for (int i = 0; i < left.getNKeys() + right.getNKeys() - lnk; i++)
					right.set(i, right.getKey(lnk + i - left.getNKeys()), right.getValue(lnk + i - left.getNKeys()));
			}
			right.setNKeys(left.getNKeys() + right.getNKeys() - lnk);
			left.setNKeys(lnk);
			left.updateLoad();
			right.updateLoad();
		}

		private FinalBranchStep removeByOrdinal(int ordinal)
		{
			Branch branch = branchByOrdinal(ordinal);
			FinalBranchStep fbs = (FinalBranchStep) branch.peek();
			branchRemove(branch);
			return fbs;
		}

		private boolean isEmpty()
		{
			return rootNode.isEmpty();
		}

		private Object firstKey()
		{
			return branchFirst(rootNode).getKey();
		}

		private Object lastKey()
		{
			return branchLast(rootNode).getKey();
		}

		private int size()
		{
			return rootNode.getLoad();
		}

		private void clear()
		{
			rootNode = new LeafNode();
		}

		private boolean containsKey(Object key)
		{
			try
			{
				return branchSearch(key).isFound();
			}
			catch (ClassCastException e)
			{
				return false;
			}
		}

		private class BranchIterator implements CountedIterator<Branch>
		{
			private final Branch branch;

			public BranchIterator()
			{
				this.branch = new Branch();
			}

			protected Branch getBranch()
			{
				return branch;
			}

			@Override
			public boolean hasNext()
			{
				return !branch.atEnd();
			}

			@Override
			public Branch next()
			{
				branch.forward();
				return branch;
			}

			@Override
			public void remove()
			{
				Object key = ((FinalBranchStep) branch.peek()).getKey();
				BTree.this.branchRemove(branch);
				if (!branch.isEmpty())
				{
					if (branch.peek() instanceof MidBranchStep)
					{
						MidBranchStep step = (MidBranchStep) branch.peek();
						branch.search(step.getNode().getChild(step.getNumChild()), key);
					}
					else if (branch.peek() instanceof FinalBranchStep)
						branch.unFind();
					else
						throw new Error();
				}
				else
					branch.search(rootNode, key);

			}

			public boolean hasPrevious()
			{
				return !branch.atBeginning();
			}

			public Branch previous()
			{
				branch.backward();
				return branch;
			}

			@Override
			public int ordinal()
			{
				return branch.ordinal();
			}

			@SuppressWarnings("unused")
			protected void search(Object key)
			{
				branch.clear();
				branch.search(rootNode, key);
			}

			@SuppressWarnings("unused")
			protected void first()
			{
				branch.clear();
				branch.first(rootNode);
			}

			@SuppressWarnings("unused")
			protected void last()
			{
				branch.clear();
				branch.last(rootNode);
			}

		}

		private class IntervalBranchIterator extends BranchIterator
		{
			private final Object fromKey;
			private final Object toKey;

			private IntervalBranchIterator(Object fromKey, Object toKey)
			{
				super();
				this.fromKey = fromKey;
				this.toKey = toKey;
				if (fromKey != null)
				{
					getBranch().search(rootNode, fromKey);
					getBranch().unFind();
				}
			}

			private boolean checkHighEndpoint()
			{
				return toKey == null || keyCompare(getBranch().nextKey(), toKey) < 0;
			}

			@Override
			public boolean hasNext()
			{
				if (!super.hasNext())
					return false;
				if (!checkHighEndpoint())
					return false;
				return true;
			}

			@Override
			public Branch next()
			{
				if (!checkHighEndpoint())
					throw new NoSuchElementException();
				return super.next();
			}

			private boolean checkLowEndpoint()
			{
				return fromKey == null || keyCompare(fromKey, getBranch().prevKey()) < 0;
			}

			@Override
			public boolean hasPrevious()
			{
				if (!super.hasPrevious())
					return false;
				if (!checkLowEndpoint())
					return false;
				return true;
			}

			@Override
			public BTree.Branch previous()
			{
				if (!checkLowEndpoint())
					throw new NoSuchElementException();
				return super.previous();
			}

			@Override
			public int ordinal()
			{
				return super.ordinal() - ordinalOfKey(fromKey);
			}

		}

		private int ordinalOfKey(Object key)
		{
			try
			{
				return branchSearch(key).ordinal();
			}
			catch (ClassCastException e)
			{
				return -1;
			}
		}

		private Branch branchGreaterOrEqual(Object key)
		{
			if (key == null)
				return branchFirst();
			else
			{
				BTree.Branch branch = branchSearch(key);
				if (!branch.isFound())
					branch.forward();
				return branch;
			}
		}

		private Branch branchLesser(Object key)
		{
			if (key == null)
				return branchLast();
			else
			{
				BTree.Branch branch = branchSearch(key);
				branch.backward();
				return branch;
			}
		}

		private Object greaterOrEqualKey(Object key)
		{
			return branchGreaterOrEqual(key).getKey();
		}

		private Object lesserKey(Object key)
		{
			return branchLesser(key).getKey();
		}

		@Deprecated
		private void trace(PrintStream out)
		{
			class StackEntry
			{
				final Node node;
				final int depth;
				final Object before;
				final Object after;

				public StackEntry(Node node, int depth, Object before, Object after)
				{
					super();
					this.node = node;
					this.depth = depth;
					this.before = before;
					this.after = after;
				}
			}
			Stack<StackEntry> stack = new Stack<>();
			stack.push(new StackEntry(rootNode, 0, null, null));
			while (!stack.isEmpty())
			{
				StackEntry se = stack.pop();
				se.node.trace(out, se.depth, se.before, se.after);
				if (se.node instanceof BranchNode)
				{
					BranchNode bnode = (BranchNode) se.node;
					for (int i = bnode.getNKeys(); i >= 0; i--)
						stack.push(new StackEntry(bnode.getChild(i), se.depth + 1, i > 0 ? bnode.getKey(i - 1) : null,
								i < bnode.getNKeys() ? bnode.getKey(i) : null));
				}
			}
		}

		@Deprecated
		private void sanityCheck()
		{
			class StackEntry
			{
				final Object before;
				final Node node;
				final Object after;

				public StackEntry(Object before, Node node, Object after)
				{
					super();
					this.before = before;
					this.node = node;
					this.after = after;
				}
			}
			Stack<StackEntry> stack = new Stack<>();
			stack.push(new StackEntry(null, rootNode, null));
			while (!stack.isEmpty())
			{
				StackEntry se = stack.pop();
				if (se.before != null)
					if (keyCompare(se.before, se.node.getKey(0)) >= 0)
						throw new RuntimeException(se.before.toString() + ", " + se.node.getKey(0).toString());
				for (int i = 1; i < se.node.getNKeys(); i++)
				{
					if (keyCompare(se.node.getKey(i - 1), se.node.getKey(i)) >= 0)
						throw new RuntimeException(se.node.getKey(i - 1).toString() + ", " + se.node.getKey(i).toString());
				}
				if (se.after != null)
					if (keyCompare(se.node.getKey(se.node.getNKeys() - 1), se.after) >= 0)
						throw new RuntimeException(se.node.getKey(se.node.getNKeys() - 1).toString() + ", " + se.after.toString());
				if (se.node instanceof BranchNode)
				{
					BranchNode branchNode = (BranchNode) se.node;
					stack.push(new StackEntry(se.before, branchNode.getChild(0), branchNode.getKey(0)));
					for (int i = 1; i < se.node.getNKeys(); i++)
						stack.push(new StackEntry(branchNode.getKey(i - 1), branchNode.getChild(i), branchNode.getKey(i)));
					stack.push(new StackEntry(branchNode.getKey(branchNode.getNKeys() - 1), branchNode.getChild(branchNode.getNKeys()), se.after));
				}
				if (se.node.calcLoad() != se.node.getLoad())
					throw new RuntimeException(se.node.toString());
			}
		}

	}

	private static final int defaultOrder = 16;

	private final BTree bTree;
	private final Comparator<? super K> comparator;

	public BTreeCountedSortedMap(int order, Comparator<? super K> comparator, Map<? extends K, ? extends V> init)
	{
		super();
		if (order <= 0)
			throw new IllegalArgumentException();
		this.bTree = new BTree(order, comparator != null ? new CastComparator<>(comparator) : null);
		this.comparator = comparator;
		if (init != null)
			putAll(init);
	}

	public BTreeCountedSortedMap(Comparator<? super K> comparator, Map<? extends K, ? extends V> init)
	{
		this(defaultOrder, comparator, init);
	}

	public BTreeCountedSortedMap(int order, Map<? extends K, ? extends V> init)
	{
		this(order, (Comparator<? super K>) null, init);
	}

	public BTreeCountedSortedMap(Map<? extends K, ? extends V> init)
	{
		this(defaultOrder, (Comparator<? super K>) null, init);
	}

	public BTreeCountedSortedMap(int order, SortedMap<K, ? extends V> init)
	{
		this(order, init.comparator(), init);
	}

	public BTreeCountedSortedMap(SortedMap<K, ? extends V> init)
	{
		this(defaultOrder, init.comparator(), init);
	}

	public BTreeCountedSortedMap(int order, Comparator<? super K> comparator)
	{
		this(order, comparator, null);
	}

	public BTreeCountedSortedMap(Comparator<? super K> comparator)
	{
		this(defaultOrder, comparator, null);
	}

	public BTreeCountedSortedMap(int order)
	{
		this(order, (Comparator<? super K>) null, null);
	}

	public BTreeCountedSortedMap()
	{
		this(defaultOrder, (Comparator<? super K>) null, null);
	}

	@Override
	public Comparator<? super K> comparator()
	{
		return comparator;
	}

	@SuppressWarnings("unchecked")
	@Override
	public V put(K key, V value)
	{
		return (V) bTree.insert(key, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public V get(Object key)
	{
		return (V) bTree.get(key);
	}

	@SuppressWarnings("unchecked")
	@Override
	public V remove(Object key)
	{
		return (V) bTree.remove(key);
	}

	@Override
	public boolean isEmpty()
	{
		return bTree.isEmpty();
	}

	@SuppressWarnings("unchecked")
	@Override
	public K firstKey()
	{
		return (K) bTree.firstKey();
	}

	@SuppressWarnings("unchecked")
	@Override
	public K lastKey()
	{
		return (K) bTree.lastKey();
	}

	@Override
	public int size()
	{
		return bTree.size();
	}

	@Override
	public void clear()
	{
		bTree.clear();
	}

	@Override
	public boolean containsKey(Object key)
	{
		return bTree.containsKey(key);
	}

	private class BranchEntry implements Map.Entry<K, V>
	{
		private final BTree.FinalBranchStep finalBranchStep;

		private BranchEntry(BTree.FinalBranchStep finalBranchStep)
		{
			this.finalBranchStep = finalBranchStep;
		}

		private BranchEntry(BTree.Branch branch)
		{
			this(branch.getFinalBranchStep());
		}

		@SuppressWarnings("unchecked")
		@Override
		public K getKey()
		{
			return (K) finalBranchStep.getKey();
		}

		@SuppressWarnings("unchecked")
		@Override
		public V getValue()
		{
			return (V) finalBranchStep.getValue();
		}

		@SuppressWarnings("unchecked")
		@Override
		public V setValue(V value)
		{
			return (V) finalBranchStep.setValue(value);
		}

		@Override
		public String toString()
		{
			return getKey().toString() + " => " + getValue().toString();
		}
	}

	private abstract class ViewCollection<E> implements CountedIteratorCollection<E>
	{
		private ViewCollection()
		{
		}

		protected abstract Bijection<BTree.Branch, E> branchBijection();

		protected abstract Bijection<E, K> keyBijection();

		@Override
		public abstract int size();

		@Override
		public abstract boolean isEmpty();

		@Override
		public abstract void clear();

		@Override
		public Object[] toArray()
		{
			return MiscUtilities.iterableToArray(this);
		}

		@Override
		public <T> T[] toArray(T[] a)
		{
			return MiscUtilities.iterableToArray(this, a);
		}

		protected abstract CountedIterator<BTree.Branch> branchIterator();

		@Override
		public CountedIterator<E> iterator()
		{
			return new BijectionCountedIterator<>(branchBijection(), branchIterator());
		}

		@Override
		public boolean add(E e)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean containsAll(Collection<?> c)
		{
			for (Object o : c)
				if (!contains(o))
					return false;
			return true;
		}

		@Override
		public boolean addAll(Collection<? extends E> c)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(Collection<?> c)
		{
			boolean change = false;
			for (Iterator<E> i = iterator(); i.hasNext();)
			{
				Object o = i.next();
				if (!c.contains(o))
				{
					i.remove();
					change = true;
				}
			}
			return change;
		}

		@Override
		public boolean removeAll(Collection<?> c)
		{
			boolean change = false;
			for (Object o : c)
				if (remove(o))
					change = true;
			return change;
		}

		protected abstract boolean containsKey(K key);

		@SuppressWarnings("unchecked")
		@Override
		public boolean contains(Object o)
		{
			try
			{
				return containsKey(keyBijection().forward((E) o));
			}
			catch (ClassCastException e)
			{
				return false;
			}

		}

		protected abstract boolean removeKey(K key);

		@SuppressWarnings("unchecked")
		@Override
		public boolean remove(Object o)
		{
			try
			{
				return removeKey(keyBijection().forward((E) o));
			}
			catch (ClassCastException e)
			{
				return false;
			}
		}

	}

	private abstract class GeneralViewCollection<E> extends ViewCollection<E>
	{
		private GeneralViewCollection()
		{
		}

		@Override
		public int size()
		{
			return BTreeCountedSortedMap.this.size();
		}

		@Override
		public boolean isEmpty()
		{
			return BTreeCountedSortedMap.this.isEmpty();
		}

		@Override
		public void clear()
		{
			BTreeCountedSortedMap.this.clear();
		}

		@Override
		protected CountedIterator<BTree.Branch> branchIterator()
		{
			return bTree.new BranchIterator();
		}

		@Override
		protected boolean containsKey(K key)
		{
			return BTreeCountedSortedMap.this.containsKey(key);
		}

		@Override
		protected boolean removeKey(K key)
		{
			BTree.Branch branch = bTree.branchSearch(key);
			if (!branch.isFound())
				return false;
			bTree.branchRemove(branch);
			return true;
		}

	}

	@Override
	public CountedIteratorSet<Entry<K, V>> entrySet()
	{
		class EntrySet extends GeneralViewCollection<Entry<K, V>> implements CountedIteratorSet<Entry<K, V>>
		{
			private Bijection<BTree.Branch, Entry<K, V>> branchBijection = new Bijection<BTree.Branch, Entry<K, V>>()
			{

				@Override
				public Entry<K, V> forward(BTree.Branch branch)
				{
					return new BranchEntry(branch);
				}

				@Override
				public BTree.Branch backward(Entry<K, V> entry)
				{
					throw new UnsupportedOperationException();
				}
			};

			private Bijection<Entry<K, V>, K> keyBijection = new Bijection<Entry<K, V>, K>()
			{

				@Override
				public K forward(Entry<K, V> input)
				{
					return input.getKey();
				}

				@Override
				public Entry<K, V> backward(K output)
				{
					throw new UnsupportedOperationException();
				}
			};

			@Override
			protected Bijection<BTree.Branch, Entry<K, V>> branchBijection()
			{
				return branchBijection;
			}

			@Override
			protected Bijection<Entry<K, V>, K> keyBijection()
			{
				return keyBijection;
			}
		}

		return new EntrySet();
	}

	@Override
	public CountedIteratorSet<K> keySet()
	{
		class KeySet extends GeneralViewCollection<K> implements CountedIteratorSet<K>
		{
			private Bijection<BTree.Branch, K> branchBijection = new Bijection<BTree.Branch, K>()
			{

				@SuppressWarnings("unchecked")
				@Override
				public K forward(BTree.Branch branch)
				{
					return (K) branch.getKey();
				}

				@Override
				public BTree.Branch backward(K key)
				{
					throw new UnsupportedOperationException();
				}
			};

			private Bijection<K, K> keyBijection = new IdentityBijection<>();

			@Override
			protected Bijection<BTree.Branch, K> branchBijection()
			{
				return branchBijection;
			}

			@Override
			protected Bijection<K, K> keyBijection()
			{
				return keyBijection;
			}
		}

		return new KeySet();
	}

	@Override
	public CountedIteratorCollection<V> values()
	{
		class Values extends GeneralViewCollection<V> implements CountedIteratorCollection<V>
		{

			private Bijection<BTree.Branch, V> branchBijection = new Bijection<BTree.Branch, V>()
			{

				@SuppressWarnings("unchecked")
				@Override
				public V forward(BTree.Branch branch)
				{
					return (V) branch.getValue();
				}

				@Override
				public BTree.Branch backward(V value)
				{
					throw new UnsupportedOperationException();
				}
			};

			@Override
			protected Bijection<BTree.Branch, V> branchBijection()
			{
				return branchBijection;
			}

			@Override
			protected Bijection<V, K> keyBijection()
			{
				throw new UnsupportedOperationException();
			}
		}

		return new Values();
	}

	@Override
	public Entry<K, V> get(int ordinal)
	{
		return new BranchEntry(bTree.branchByOrdinal(ordinal));
	}

	@Override
	public Entry<K, V> remove(int ordinal)
	{
		return new BranchEntry(bTree.removeByOrdinal(ordinal));
	}

	@Override
	public int ordinalOfKey(Object key)
	{
		return bTree.ordinalOfKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return values().contains(value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m)
	{
		for (Map.Entry<? extends K, ? extends V> e : m.entrySet())
			put(e.getKey(), e.getValue());
	}

	private class IntervalCountedSortedMap implements CountedSortedMap<K, V>
	{
		private final K fromKey;
		private final K toKey;

		private IntervalCountedSortedMap(K fromKey, K toKey)
		{
			this.fromKey = fromKey;
			this.toKey = toKey;
		}

		@Override
		public Comparator<? super K> comparator()
		{
			return comparator;
		}

		@SuppressWarnings("unchecked")
		@Override
		public K firstKey()
		{
			K key = (K) bTree.greaterOrEqualKey(fromKey);
			if (bTree.keyCompare(key, toKey) >= 0)
				throw new NoSuchElementException();
			return key;
		}

		@SuppressWarnings("unchecked")
		@Override
		public K lastKey()
		{
			K key = (K) bTree.lesserKey(toKey);
			if (bTree.keyCompare(fromKey, key) > 0)
				throw new NoSuchElementException();
			return key;
		}

		private abstract class IntervalViewCollection<E> extends ViewCollection<E>
		{

			@Override
			public int size()
			{
				return IntervalCountedSortedMap.this.size();
			}

			@Override
			public boolean isEmpty()
			{
				return IntervalCountedSortedMap.this.isEmpty();
			}

			@Override
			public void clear()
			{
				IntervalCountedSortedMap.this.clear();
			}

			@Override
			protected CountedIterator<BTree.Branch> branchIterator()
			{
				return bTree.new IntervalBranchIterator(fromKey, toKey);
			}

			@Override
			protected boolean containsKey(K key)
			{
				return IntervalCountedSortedMap.this.containsKey(key);
			}

			@Override
			protected boolean removeKey(K key)
			{
				if (!keyInInterval(key))
					return false;
				BTree.Branch branch = bTree.branchSearch(key);
				if (!branch.isFound())
					return false;
				bTree.branchRemove(branch);
				return true;
			}
		}

		@Override
		public CountedIteratorSet<Entry<K, V>> entrySet()
		{
			class EntrySet extends IntervalViewCollection<Entry<K, V>> implements CountedIteratorSet<Entry<K, V>>
			{
				private Bijection<BTree.Branch, Entry<K, V>> branchBijection = new Bijection<BTree.Branch, Entry<K, V>>()
				{

					@Override
					public Entry<K, V> forward(BTree.Branch branch)
					{
						return new BranchEntry(branch);
					}

					@Override
					public BTree.Branch backward(Entry<K, V> entry)
					{
						throw new UnsupportedOperationException();
					}
				};

				private Bijection<Entry<K, V>, K> keyBijection = new Bijection<Entry<K, V>, K>()
				{

					@Override
					public K forward(Entry<K, V> input)
					{
						return input.getKey();
					}

					@Override
					public Entry<K, V> backward(K output)
					{
						throw new UnsupportedOperationException();
					}
				};

				@Override
				protected Bijection<BTree.Branch, Entry<K, V>> branchBijection()
				{
					return branchBijection;
				}

				@Override
				protected Bijection<Entry<K, V>, K> keyBijection()
				{
					return keyBijection;
				}
			}

			return new EntrySet();
		}

		@Override
		public CountedIteratorSet<K> keySet()
		{
			class KeySet extends IntervalViewCollection<K> implements CountedIteratorSet<K>
			{
				private Bijection<BTree.Branch, K> branchBijection = new Bijection<BTree.Branch, K>()
				{

					@SuppressWarnings("unchecked")
					@Override
					public K forward(BTree.Branch branch)
					{
						return (K) branch.getKey();
					}

					@Override
					public BTree.Branch backward(K key)
					{
						throw new UnsupportedOperationException();
					}
				};

				private Bijection<K, K> keyBijection = new IdentityBijection<>();

				@Override
				protected Bijection<BTree.Branch, K> branchBijection()
				{
					return branchBijection;
				}

				@Override
				protected Bijection<K, K> keyBijection()
				{
					return keyBijection;
				}
			}

			return new KeySet();
		}

		@Override
		public CountedIteratorCollection<V> values()
		{
			class Values extends IntervalViewCollection<V> implements CountedIteratorCollection<V>
			{

				private Bijection<BTree.Branch, V> branchBijection = new Bijection<BTree.Branch, V>()
				{

					@SuppressWarnings("unchecked")
					@Override
					public V forward(BTree.Branch branch)
					{
						return (V) branch.getValue();
					}

					@Override
					public BTree.Branch backward(V value)
					{
						throw new UnsupportedOperationException();
					}
				};

				@Override
				protected Bijection<BTree.Branch, V> branchBijection()
				{
					return branchBijection;
				}

				@Override
				protected Bijection<V, K> keyBijection()
				{
					throw new UnsupportedOperationException();
				}
			}

			return new Values();
		}

		@Override
		public int size()
		{
			return toOrdinal() - fromOrdinal();
		}

		@Override
		public boolean isEmpty()
		{
			return fromOrdinal() >= toOrdinal();
		}

		private boolean keyAboveFromKey(Object key)
		{
			try
			{
				if ((fromKey != null) && (bTree.keyCompare(fromKey, key) > 0))
					return false;
				return true;
			}
			catch (ClassCastException e)
			{
				return false;
			}
		}

		private boolean keyUnderToKey(Object key)
		{
			try
			{
				if ((toKey != null) && (bTree.keyCompare(key, toKey) >= 0))
					return false;
				return true;
			}
			catch (ClassCastException e)
			{
				return false;
			}
		}

		private boolean keyInInterval(Object key)
		{
			if (!keyAboveFromKey(key))
				return false;
			if (!keyUnderToKey(key))
				return false;
			return true;
		}

		@Override
		public boolean containsKey(Object key)
		{
			if (!keyInInterval(key))
				return false;
			return BTreeCountedSortedMap.this.containsKey(key);
		}

		@Override
		public boolean containsValue(Object value)
		{
			return values().contains(value);
		}

		@Override
		public V get(Object key)
		{
			if (!keyInInterval(key))
				return null;
			return BTreeCountedSortedMap.this.get(key);
		}

		@Override
		public V put(K key, V value)
		{
			if (!keyInInterval(key))
				throw new IllegalArgumentException();
			return BTreeCountedSortedMap.this.put(key, value);
		}

		@Override
		public V remove(Object key)
		{
			if (!keyInInterval(key))
				return null;
			return BTreeCountedSortedMap.this.remove(key);
		}

		@Override
		public void putAll(Map<? extends K, ? extends V> m)
		{
			for (Map.Entry<? extends K, ? extends V> e : m.entrySet())
				put(e.getKey(), e.getValue());
		}

		@Override
		public void clear()
		{
			for (Iterator<Entry<K, V>> i = entrySet().iterator(); i.hasNext();)
			{
				i.next();
				i.remove();
			}
		}

		private int fromOrdinal()
		{
			if (fromKey == null)
				return 0;
			return BTreeCountedSortedMap.this.ordinalOfKey(fromKey);
		}

		private int toOrdinal()
		{
			if (toKey == null)
				return Integer.MAX_VALUE;
			return BTreeCountedSortedMap.this.ordinalOfKey(toKey);
		}

		@Override
		public Entry<K, V> get(int ordinal)
		{
			int fromOrdinal = fromOrdinal();
			int toOrdinal = toOrdinal();
			if (fromOrdinal + ordinal >= toOrdinal)
				throw new NoSuchElementException();
			return BTreeCountedSortedMap.this.get(fromOrdinal + ordinal);
		}

		@Override
		public Entry<K, V> remove(int ordinal)
		{
			int fromOrdinal = fromOrdinal();
			int toOrdinal = toOrdinal();
			if (fromOrdinal + ordinal >= toOrdinal)
				throw new NoSuchElementException();
			return BTreeCountedSortedMap.this.remove(fromOrdinal + ordinal);
		}

		@Override
		public int ordinalOfKey(Object o)
		{
			if (!keyAboveFromKey(o))
				return 0;
			if (!keyUnderToKey(o))
				return size();
			return BTreeCountedSortedMap.this.ordinalOfKey(o) - fromOrdinal();
		}

		@Override
		public CountedSortedMap<K, V> subMap(K fromKey, K toKey)
		{
			if (fromKey == null || !keyAboveFromKey(fromKey))
				fromKey = this.fromKey;
			if (toKey == null || !keyUnderToKey(toKey))
				toKey = this.toKey;
			return new IntervalCountedSortedMap(fromKey, toKey);
		}

		@Override
		public CountedSortedMap<K, V> headMap(K toKey)
		{
			return subMap(null, toKey);
		}

		@Override
		public CountedSortedMap<K, V> tailMap(K fromKey)
		{
			return subMap(fromKey, null);
		}

	}

	@Override
	public CountedSortedMap<K, V> subMap(K fromKey, K toKey)
	{
		return new IntervalCountedSortedMap(fromKey, toKey);
	}

	@Override
	public CountedSortedMap<K, V> headMap(K toKey)
	{
		return new IntervalCountedSortedMap(null, toKey);
	}

	@Override
	public CountedSortedMap<K, V> tailMap(K fromKey)
	{
		return new IntervalCountedSortedMap(fromKey, null);
	}

	@Override
	public String toString()
	{
		return MiscUtilities.toString(this);
	}

	@Deprecated
	protected void trace(PrintStream out)
	{
		bTree.trace(out);
	}

	@Deprecated
	protected void sanityCheck()
	{
		bTree.sanityCheck();
	}

}
