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
package aletheia.parsergenerator.lexer;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

/**
 * A deterministic finite automaton for regular language recognition.
 * {@link DeterministicAutomaton}s are immutable.
 */
public class DeterministicAutomaton implements Serializable
{
	private static final long serialVersionUID = -3791547470654776727L;

	private final AutomatonState startState;
	private final Set<AutomatonState> acceptStates;
	private final Map<AutomatonState, Map<Character, AutomatonState>> transitions;

	/**
	 * Creates a new deterministic automaton with the specified start state, set
	 * of accepting states and transitions. This method is private and doesn't
	 * make a copy of the parameter data structures, so the caller must be
	 * responsible of the further modifications done on them.
	 * 
	 * @param startState
	 *            The start state.
	 * @param acceptStates
	 *            The set of accepting states.
	 * @param transitions
	 *            The map of transitions.
	 */
	private DeterministicAutomaton(AutomatonState startState, Set<AutomatonState> acceptStates, Map<AutomatonState, Map<Character, AutomatonState>> transitions)
	{
		super();
		this.startState = startState;
		this.acceptStates = acceptStates;
		this.transitions = transitions;
	}

	/**
	 * Creates a new deterministic automaton as a determinized version of a
	 * given generalized (nondeterministic) automaton.
	 * 
	 * @param automaton
	 *            The automaton to determinize.
	 */
	public DeterministicAutomaton(Automaton automaton)
	{
		super();
		Map<Set<AutomatonState>, Map<Character, Set<AutomatonState>>> dtrans = new HashMap<Set<AutomatonState>, Map<Character, Set<AutomatonState>>>();
		Stack<Set<AutomatonState>> stack = new Stack<Set<AutomatonState>>();
		stack.push(automaton.startStates());
		while (!stack.isEmpty())
		{
			Set<AutomatonState> stateSet = stack.pop();
			if (!dtrans.containsKey(stateSet))
			{
				Map<Character, Set<AutomatonState>> map = new TreeMap<Character, Set<AutomatonState>>();
				dtrans.put(stateSet, map);
				for (AutomatonState s : stateSet)
				{
					Map<Character, Set<AutomatonState>> map_ = automaton.next(s);
					if (map_ != null)
					{
						for (Map.Entry<Character, Set<AutomatonState>> e : map_.entrySet())
						{
							Character c = e.getKey();
							Set<AutomatonState> set = map.get(c);
							if (set == null)
							{
								set = new HashSet<AutomatonState>();
								map.put(c, set);
							}
							set.addAll(e.getValue());
						}
					}
				}
				stack.addAll(map.values());
			}
		}
		Set<Set<AutomatonState>> daccept = new HashSet<Set<AutomatonState>>();
		for (Set<AutomatonState> set : dtrans.keySet())
		{
			for (AutomatonState s : set)
			{
				if (automaton.acceptStates().contains(s))
				{
					daccept.add(set);
					break;
				}
			}
		}

		Map<Set<AutomatonState>, AutomatonState> isomorphism = new HashMap<Set<AutomatonState>, AutomatonState>();
		Set<AutomatonState> acceptStates = new HashSet<AutomatonState>();
		for (Set<AutomatonState> set : dtrans.keySet())
		{
			AutomatonState s = new AutomatonState();
			isomorphism.put(set, s);
			if (daccept.contains(set))
				acceptStates.add(s);
		}
		AutomatonState startState = isomorphism.get(automaton.startStates());
		Map<AutomatonState, Map<Character, AutomatonState>> transitions = new HashMap<AutomatonState, Map<Character, AutomatonState>>();
		for (Map.Entry<Set<AutomatonState>, Map<Character, Set<AutomatonState>>> e : dtrans.entrySet())
		{
			Map<Character, AutomatonState> map = new TreeMap<Character, AutomatonState>();
			for (Map.Entry<Character, Set<AutomatonState>> e2 : e.getValue().entrySet())
				map.put(e2.getKey(), isomorphism.get(e2.getValue()));
			transitions.put(isomorphism.get(e.getKey()), map);
		}

		this.startState = startState;
		this.acceptStates = acceptStates;
		this.transitions = transitions;
	}

	/**
	 * The start state of this automaton.
	 * 
	 * @return The start state.
	 */
	public AutomatonState startState()
	{
		return startState;
	}

	/**
	 * The set of accepting states of this automaton.
	 * 
	 * @return The set of accepting states.
	 */
	public Set<AutomatonState> acceptStates()
	{
		return Collections.unmodifiableSet(acceptStates);
	}

	/**
	 * Checks if a given state is a member of the acceptable state set.
	 * 
	 * @param state
	 *            The state.
	 * @return Is it acceptable?
	 */
	public boolean acceptable(AutomatonState state)
	{
		return acceptStates.contains(state);
	}

	/**
	 * Checks if this automaton accepts the empty string. I.e. the start state
	 * is acceptable.
	 * 
	 * @return Does it?
	 */
	public boolean acceptsEmpty()
	{
		return acceptable(startState);
	}

	/**
	 * The transition of a given state and a character.
	 * 
	 * @param state
	 *            The state.
	 * @param c
	 *            The character.
	 * @return The resulting state.
	 */
	public AutomatonState next(AutomatonState state, char c)
	{
		Map<Character, AutomatonState> map = transitions.get(state);
		if (map != null)
			return map.get(c);
		return null;
	}

	/**
	 * The transition map of a given state. A compact form of
	 * {@link #next(AutomatonState, char)}.
	 * 
	 * @param state
	 *            The state.
	 * @return The transition map.
	 */
	public Map<Character, AutomatonState> next(AutomatonState state)
	{
		Map<Character, AutomatonState> map = transitions.get(state);
		if (map != null)
			return Collections.unmodifiableMap(map);
		else
			return Collections.emptyMap();
	}

	/**
	 * Compute the deterministic automaton that accepts the same language of
	 * this one and have the minimal number of states. That minimal automaton is
	 * unique for a language up to isomorphism.
	 * 
	 * @return The minimal deterministic finite automaton.
	 */
	public DeterministicAutomaton minimize()
	{
		Map<AutomatonState, Set<AutomatonState>> partitions = new HashMap<AutomatonState, Set<AutomatonState>>();
		Map<AutomatonState, Set<AutomatonState>> partitionmap = new HashMap<AutomatonState, Set<AutomatonState>>();
		AutomatonState well = new AutomatonState();
		Set<Character> alphabet = new HashSet<Character>();
		{
			Set<AutomatonState> acceptStates = new HashSet<AutomatonState>();
			partitions.put(new AutomatonState(), acceptStates);
			Set<AutomatonState> nonacceptStates = new HashSet<AutomatonState>();
			partitions.put(new AutomatonState(), nonacceptStates);
			for (Map.Entry<AutomatonState, Map<Character, AutomatonState>> e : transitions.entrySet())
			{
				alphabet.addAll(e.getValue().keySet());
				AutomatonState s = e.getKey();
				if (this.acceptStates.contains(s))
				{
					acceptStates.add(s);
					partitionmap.put(s, acceptStates);
				}
				else
				{
					nonacceptStates.add(s);
					partitionmap.put(s, nonacceptStates);
				}
			}
			for (AutomatonState s : this.acceptStates)
			{
				acceptStates.add(s);
				partitionmap.put(s, acceptStates);
			}
			nonacceptStates.add(well);
			partitionmap.put(well, nonacceptStates);
		}

		while (true)
		{
			Map<Set<AutomatonState>, Map<Map<Character, Set<AutomatonState>>, Set<AutomatonState>>> subpartitions = new HashMap<Set<AutomatonState>, Map<Map<Character, Set<AutomatonState>>, Set<AutomatonState>>>();
			for (Set<AutomatonState> part : partitions.values())
			{
				Map<Map<Character, Set<AutomatonState>>, Set<AutomatonState>> successormapinv = new HashMap<Map<Character, Set<AutomatonState>>, Set<AutomatonState>>();
				for (AutomatonState s : part)
				{
					Map<Character, Set<AutomatonState>> destmap = new TreeMap<Character, Set<AutomatonState>>();
					for (Character c : alphabet)
						destmap.put(c, partitionmap.get(well));
					if (s != well)
					{
						for (Map.Entry<Character, AutomatonState> e : this.transitions.get(s).entrySet())
						{
							char c = e.getKey();
							Set<AutomatonState> destpar = partitionmap.get(e.getValue());
							destmap.put(c, destpar);
						}
					}
					Set<AutomatonState> subpar = successormapinv.get(destmap);
					if (subpar == null)
					{
						subpar = new HashSet<AutomatonState>();
						successormapinv.put(destmap, subpar);
					}
					subpar.add(s);
				}
				subpartitions.put(part, successormapinv);
			}
			boolean subparted = false;
			partitions.clear();
			partitionmap.clear();
			for (Map.Entry<Set<AutomatonState>, Map<Map<Character, Set<AutomatonState>>, Set<AutomatonState>>> e : subpartitions.entrySet())
			{
				@SuppressWarnings("unused")
				Set<AutomatonState> part = e.getKey();
				Map<Map<Character, Set<AutomatonState>>, Set<AutomatonState>> successormapinv = e.getValue();
				if (successormapinv.size() > 1)
					subparted = true;
				for (Set<AutomatonState> set : successormapinv.values())
				{
					partitions.put(new AutomatonState(), set);
					for (AutomatonState s : set)
						partitionmap.put(s, set);
				}
			}
			if (!subparted)
				break;
		}

		Map<Set<AutomatonState>, AutomatonState> iso = new HashMap<Set<AutomatonState>, AutomatonState>();
		for (Map.Entry<AutomatonState, Set<AutomatonState>> e : partitions.entrySet())
			if (iso.put(e.getValue(), e.getKey()) != null)
				throw new Error();
		AutomatonState startState_ = iso.get(partitionmap.get(this.startState));
		Set<AutomatonState> acceptStates_ = new HashSet<AutomatonState>();
		for (AutomatonState s : this.acceptStates)
			acceptStates_.add(iso.get(partitionmap.get(s)));
		Map<AutomatonState, Map<Character, AutomatonState>> transitions_ = new HashMap<AutomatonState, Map<Character, AutomatonState>>();
		for (Map.Entry<AutomatonState, Map<Character, AutomatonState>> e : this.transitions.entrySet())
		{
			if (!partitionmap.get(e.getKey()).contains(well))
			{
				Map<Character, AutomatonState> map = transitions_.get(iso.get(partitionmap.get(e.getKey())));
				if (map == null)
				{
					map = new TreeMap<Character, AutomatonState>();
					transitions_.put(iso.get(partitionmap.get(e.getKey())), map);
				}
				for (Map.Entry<Character, AutomatonState> e2 : e.getValue().entrySet())
				{
					if (!partitionmap.get(e2.getValue()).contains(well))
						map.put(e2.getKey(), iso.get(partitionmap.get(e2.getValue())));
				}
			}
		}
		return new DeterministicAutomaton(startState_, acceptStates_, transitions_);
	}

	/**
	 * Gives a generalized (nondeterministic) version of this deterministic
	 * automaton.
	 * 
	 * @return The nondeterministic automaton.
	 */
	public Automaton undeterminize()
	{
		return new Automaton(this);
	}

	/**
	 * Creates the empty deterministic automaton.
	 * 
	 * @return The empty automaton.
	 * 
	 * @see Automaton#empty()
	 */
	public static DeterministicAutomaton empty()
	{
		return Automaton.empty().determinize();
	}

	/**
	 * Create the empty string deterministic automaton.
	 * 
	 * @return The empty string automaton.
	 * 
	 * @see Automaton#emptyString()
	 */
	public static DeterministicAutomaton emptyString()
	{
		return Automaton.emptyString().determinize();
	}

	/**
	 * Create the singleton automaton.
	 * 
	 * @param c
	 *            The character.
	 * @return The singleton automaton.
	 * 
	 * @see Automaton#singleton(char)
	 */
	public static DeterministicAutomaton singleton(char c)
	{
		return Automaton.singleton(c).determinize();
	}

	/**
	 * Creates a deterministic automaton that solely recognizes a given string.
	 * 
	 * @param s
	 *            The string.
	 * @return The automaton.
	 * 
	 * @see Automaton#string(CharSequence)
	 */
	public static DeterministicAutomaton string(CharSequence s)
	{
		return Automaton.string(s).determinize();
	}

	/**
	 * Creates the union automaton of two previously existing automatons.
	 * 
	 * @param a1
	 *            The first automaton to unite.
	 * @param a2
	 *            The second automaton to unite.
	 * @return The union automaton.
	 * 
	 * @see Automaton#union(Automaton, Automaton)
	 */
	public static DeterministicAutomaton union(DeterministicAutomaton a1, DeterministicAutomaton a2)
	{
		return Automaton.union(a1.undeterminize(), a2.undeterminize()).determinize();
	}

	/**
	 * Calls to {@link #union(DeterministicAutomaton, DeterministicAutomaton)}
	 * with this automaton and another one.
	 * 
	 * @param a
	 *            The automaton to unite to this.
	 * @return The resulting automaton.
	 */
	public DeterministicAutomaton union(DeterministicAutomaton a)
	{
		return union(this, a);
	}

	/**
	 * Creates a concatenation automaton of two previously existing automatons.
	 * 
	 * @param a1
	 *            The first automaton to concatenate.
	 * @param a2
	 *            The second automaton to concatenate.
	 * @return The concatenation automaton.
	 * 
	 * @see Automaton#concatenate(Automaton, Automaton)
	 */
	public static DeterministicAutomaton concatenate(DeterministicAutomaton a1, DeterministicAutomaton a2)
	{
		return Automaton.concatenate(a1.undeterminize(), a2.undeterminize()).determinize();
	}

	/**
	 * Calls to
	 * {@link #concatenate(DeterministicAutomaton, DeterministicAutomaton)} with
	 * this automaton and another one.
	 * 
	 * @param a
	 *            The automaton to concatenate to this.
	 * @return The concatenated automaton.
	 */
	public DeterministicAutomaton concatenate(DeterministicAutomaton a)
	{
		return concatenate(this, a);
	}

	/**
	 * Creates the Kleene star of an automaton.
	 * 
	 * @param a
	 *            The source automaton.
	 * @return The Kleene star automaton.
	 * 
	 * @see Automaton#kleene(Automaton)
	 */
	public static DeterministicAutomaton kleene(DeterministicAutomaton a)
	{
		return Automaton.kleene(a.undeterminize()).determinize();
	}

	/**
	 * Calls to {@link #kleene(DeterministicAutomaton)} with this automaton.
	 * 
	 * @return The Kleene star automaton.
	 */
	public DeterministicAutomaton kleene()
	{
		return kleene(this);
	}

	/**
	 * Computes the cartesian product between two automatons. The set of accept
	 * states of the resulting automaton might be:
	 * <ul>
	 * <li><b>If <i>sign</i> is true</b>, the cartesian product between the set
	 * of accept states of a and the set of accept states of b.</li>
	 * <li><b>If <i>sign</i> is false</b>, the cartesian product between the set
	 * of accept states of a and the complement of the set of accept states of
	 * b.</li>
	 * </ul>
	 * 
	 * @param a
	 *            The first automaton.
	 * @param b
	 *            The second automaton.
	 * @param sign
	 *            The sign of the accept states of b to consider.
	 * @return The cartesian product.
	 */
	private static DeterministicAutomaton cartesian(DeterministicAutomaton a, DeterministicAutomaton b, boolean sign)
	{

		class StateConverter
		{
			class StatePair
			{
				public final AutomatonState stateA;
				public final AutomatonState stateB;

				public StatePair(AutomatonState stateA, AutomatonState stateB)
				{
					super();
					this.stateA = stateA;
					this.stateB = stateB;
				}

				@Override
				public int hashCode()
				{
					final int prime = 31;
					int result = 1;
					result = prime * result + ((stateA == null) ? 0 : stateA.hashCode());
					result = prime * result + ((stateB == null) ? 0 : stateB.hashCode());
					return result;
				}

				@Override
				public boolean equals(Object obj)
				{
					if (this == obj)
						return true;
					if (obj == null)
						return false;
					if (getClass() != obj.getClass())
						return false;
					StatePair other = (StatePair) obj;
					if (stateA == null)
					{
						if (other.stateA != null)
							return false;
					}
					else if (!stateA.equals(other.stateA))
						return false;
					if (stateB == null)
					{
						if (other.stateB != null)
							return false;
					}
					else if (!stateB.equals(other.stateB))
						return false;
					return true;
				}
			};

			private final Map<StatePair, AutomatonState> pairToState = new HashMap<StatePair, AutomatonState>();
			private final Map<AutomatonState, StatePair> stateToPair = new HashMap<AutomatonState, StatePair>();

			public AutomatonState getState(AutomatonState stateA, AutomatonState stateB)
			{
				StatePair pair = new StatePair(stateA, stateB);
				AutomatonState state = pairToState.get(pair);
				if (state == null)
				{
					state = new AutomatonState();
					pairToState.put(pair, state);
					stateToPair.put(state, pair);
				}
				return state;
			}

			public AutomatonState getPairA(AutomatonState state)
			{
				StatePair pair = stateToPair.get(state);
				if (pair == null)
					return null;
				return pair.stateA;
			}

			public AutomatonState getPairB(AutomatonState state)
			{
				StatePair pair = stateToPair.get(state);
				if (pair == null)
					return null;
				return pair.stateB;
			}

		}
		StateConverter stateConverter = new StateConverter();
		AutomatonState wellStateB = new AutomatonState();
		AutomatonState startState = stateConverter.getState(a.startState(), b.startState());
		Map<AutomatonState, Map<Character, AutomatonState>> transitions = new HashMap<AutomatonState, Map<Character, AutomatonState>>();
		Stack<AutomatonState> stack = new Stack<AutomatonState>();
		stack.push(startState);
		Set<AutomatonState> acceptStates = new HashSet<AutomatonState>();
		Set<AutomatonState> visited = new HashSet<AutomatonState>();
		while (!stack.isEmpty())
		{
			AutomatonState state = stack.pop();
			if (!visited.contains(state))
			{
				visited.add(state);
				AutomatonState stateA = stateConverter.getPairA(state);
				AutomatonState stateB = stateConverter.getPairB(state);
				if (a.acceptStates().contains(stateA))
				{
					if (b.acceptStates().contains(stateB) == sign)
						acceptStates.add(state);

				}
				Map<Character, AutomatonState> next = new HashMap<Character, AutomatonState>();
				transitions.put(state, next);
				Map<Character, AutomatonState> nextA = a.next(stateA);
				Map<Character, AutomatonState> nextB = b.next(stateB);
				for (Map.Entry<Character, AutomatonState> e : nextA.entrySet())
				{
					char c = e.getKey();
					AutomatonState stateA_ = e.getValue();
					AutomatonState stateB_ = nextB.get(c);
					if (stateB_ == null)
						stateB_ = wellStateB;
					AutomatonState state_ = stateConverter.getState(stateA_, stateB_);
					next.put(c, state_);
					stack.push(state_);
				}
			}
		}
		return new DeterministicAutomaton(startState, acceptStates, transitions);
	}

	/**
	 * The deterministic automaton that accepts the intersection language of two
	 * other automatons.
	 * 
	 * @param a
	 *            The first automaton.
	 * @param b
	 *            The second automaton.
	 * @return The intersection automaton.
	 */
	public static DeterministicAutomaton intersection(DeterministicAutomaton a, DeterministicAutomaton b)
	{
		return cartesian(a, b, true);
	}

	/**
	 * Calls to
	 * {@link #intersection(DeterministicAutomaton, DeterministicAutomaton)}
	 * with this automaton and another one.
	 * 
	 * @param a
	 *            The other automaton.
	 * @return The intersection.
	 */
	public DeterministicAutomaton intersection(DeterministicAutomaton a)
	{
		return intersection(this, a);
	}

	/**
	 * The deterministic automaton that accepts the subtraction language of two
	 * other automatons.
	 * 
	 * @param a
	 *            The first automaton.
	 * @param b
	 *            The second automaton.
	 * @return The subtraction automaton.
	 */
	public static DeterministicAutomaton subtraction(DeterministicAutomaton a, DeterministicAutomaton b)
	{
		return cartesian(a, b, false);
	}

	/**
	 * Calls to
	 * {@link #subtraction(DeterministicAutomaton, DeterministicAutomaton)} with
	 * this automaton and another one.
	 * 
	 * @param a
	 *            The other automaton.
	 * @return The subtraction.
	 */
	public DeterministicAutomaton subtraction(DeterministicAutomaton a)
	{
		return subtraction(this, a);
	}

	/**
	 * Checks The equality of two deterministic automatons up to isomorphism.
	 * Should be minimized first to check if they recognize the same language.
	 * 
	 * @param a1
	 *            The first automaton to be compared.
	 * @param a2
	 *            The second automaton to be compared.
	 * @return Are they equal?
	 */
	public static boolean equals(DeterministicAutomaton a1, DeterministicAutomaton a2)
	{
		class Isomorphism
		{
			private Map<AutomatonState, AutomatonState> forward = new HashMap<AutomatonState, AutomatonState>();
			private Map<AutomatonState, AutomatonState> backward = new HashMap<AutomatonState, AutomatonState>();

			public boolean add(AutomatonState s1, AutomatonState s2)
			{
				AutomatonState s2_ = forward.get(s1);
				AutomatonState s1_ = backward.get(s2);
				if (s1_ == null && s2_ == null)
				{
					forward.put(s1, s2);
					backward.put(s2, s1);
					return true;
				}
				else
					return s1 == s1_ && s2 == s2_;
			}

			public AutomatonState forward(AutomatonState s1)
			{
				return forward.get(s1);
			}

			public AutomatonState backward(AutomatonState s2)
			{
				return backward.get(s2);
			}
		}
		;

		Isomorphism isomorphism = new Isomorphism();

		isomorphism.add(a1.startState, a2.startState);
		Stack<AutomatonState> stack = new Stack<AutomatonState>();
		stack.push(a1.startState);
		Set<AutomatonState> visited = new HashSet<AutomatonState>();
		while (!stack.isEmpty())
		{
			AutomatonState s1 = stack.pop();
			if (!visited.contains(s1))
			{
				visited.add(s1);
				AutomatonState s2 = isomorphism.forward(s1);
				Map<Character, AutomatonState> map1 = a1.transitions.get(s1);
				Map<Character, AutomatonState> map2 = a2.transitions.get(s2);
				if (map1 == null)
					return map2 == null || map2.isEmpty();
				else if (map2 == null)
					return map1 == null || map1.isEmpty();
				if (!map1.keySet().equals(map2.keySet()))
					return false;
				for (Map.Entry<Character, AutomatonState> e : map1.entrySet())
				{
					char c = e.getKey();
					AutomatonState s1_ = e.getValue();
					AutomatonState s2_ = map2.get(c);
					if (!isomorphism.add(s1_, s2_))
						return false;
					stack.push(s1_);
				}
			}
		}
		for (AutomatonState s1 : a1.acceptStates)
		{
			AutomatonState s2 = isomorphism.forward(s1);
			if (!a2.acceptStates.contains(s2))
				return false;
		}
		for (AutomatonState s2 : a2.acceptStates)
		{
			AutomatonState s1 = isomorphism.backward(s2);
			if (!a1.acceptStates.contains(s1))
				return false;
		}
		return true;
	}

	public void trace(PrintStream out)
	{
		class StateNumbering
		{
			private final ArrayList<AutomatonState> aStates = new ArrayList<AutomatonState>();
			private final Map<AutomatonState, Integer> mStates = new HashMap<AutomatonState, Integer>();

			public int get(AutomatonState state)
			{
				Integer i = mStates.get(state);
				if (i == null)
				{
					i = aStates.size();
					aStates.add(state);
					mStates.put(state, i);
				}
				return i;
			}

			@SuppressWarnings("unused")
			public AutomatonState get(int i)
			{
				return aStates.get(i);
			}

		}
		;

		StateNumbering numbering = new StateNumbering();

		out.print("Start: ");
		out.print(numbering.get(startState) + ", ");
		out.println();

		Set<AutomatonState> visited = new HashSet<AutomatonState>();
		Set<AutomatonState> pending = new HashSet<AutomatonState>(transitions.keySet());
		Stack<AutomatonState> stack = new Stack<AutomatonState>();
		stack.push(startState);
		while (!pending.isEmpty())
		{
			if (stack.isEmpty())
				stack.push(pending.iterator().next());
			while (!stack.isEmpty())
			{
				AutomatonState state = stack.pop();
				if (!visited.contains(state))
				{
					visited.add(state);
					pending.remove(state);
					out.print(numbering.get(state) + ": ");
					Map<Character, AutomatonState> map = transitions.get(state);
					if (map != null)
					{
						for (Map.Entry<Character, AutomatonState> e2 : map.entrySet())
						{
							Character c = e2.getKey();
							out.print(c + "[");
							AutomatonState s2 = e2.getValue();
							out.print(numbering.get(s2) + ", ");
							stack.add(s2);
							out.print("]");
						}
						out.println();
					}
				}
			}
		}

		out.print("Accept: ");
		for (AutomatonState s : acceptStates)
			out.print(numbering.get(s) + ", ");
		out.println();

		out.println();
	}

}
