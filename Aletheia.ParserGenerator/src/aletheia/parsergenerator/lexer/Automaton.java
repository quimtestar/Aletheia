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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

/**
 * A nondeterministic finite automaton for regular language recognition.
 * {@link Automaton}s are immutable.
 */
public class Automaton implements Serializable
{
	private static final long serialVersionUID = 725975801074600032L;

	private final Set<AutomatonState> startStates;
	private final Set<AutomatonState> acceptStates;
	private final Map<AutomatonState, Map<Character, Set<AutomatonState>>> transitions;

	/**
	 * Creates a new automaton with the specified set of start states, accepting
	 * states and transitions. This method is private and doesn't make a copy of
	 * the parameter data structures, so the caller must be responsible of the
	 * further modifications done on them.
	 * 
	 * @param startStates
	 *            Set of start states.
	 * @param acceptStates
	 *            Set of accepting states.
	 * @param transitions
	 *            Transition map.
	 */
	private Automaton(Set<AutomatonState> startStates, Set<AutomatonState> acceptStates, Map<AutomatonState, Map<Character, Set<AutomatonState>>> transitions)
	{
		super();
		this.startStates = startStates;
		this.acceptStates = acceptStates;
		this.transitions = transitions;
	}

	/**
	 * A isomorphism between two sets of states.
	 */
	private static class Isomorphism
	{
		private final Map<AutomatonState, AutomatonState> map = new HashMap<AutomatonState, AutomatonState>();

		public AutomatonState get(AutomatonState s)
		{
			AutomatonState s_ = map.get(s);
			if (s_ == null)
			{
				s_ = new AutomatonState();
				map.put(s, s_);
			}
			return s_;
		}
	}

	/**
	 * Creates a new automaton from a {@linkplain DeterministicAutomaton
	 * deterministic automaton}. A deterministic automaton can be seen as an
	 * specialized type of automaton. This constructor makes the conversion so
	 * we can apply the methods developed for generalized nondeterministic
	 * automatons to a deterministic one.
	 * 
	 * @param deterministicAutomaton
	 *            The original deterministic automaton.
	 */
	public Automaton(DeterministicAutomaton deterministicAutomaton)
	{
		super();
		Isomorphism isomorphism = new Isomorphism();
		startStates = Collections.singleton(isomorphism.get(deterministicAutomaton.startState()));
		acceptStates = new HashSet<AutomatonState>();
		for (AutomatonState s : deterministicAutomaton.acceptStates())
			acceptStates.add(isomorphism.get(s));
		transitions = new HashMap<AutomatonState, Map<Character, Set<AutomatonState>>>();
		Stack<AutomatonState> stack = new Stack<AutomatonState>();
		stack.push(deterministicAutomaton.startState());
		while (!stack.isEmpty())
		{
			AutomatonState s = stack.pop();
			AutomatonState s_ = isomorphism.get(s);
			if (!transitions.containsKey(s_))
			{
				Map<Character, Set<AutomatonState>> map = new TreeMap<Character, Set<AutomatonState>>();
				transitions.put(s_, map);
				for (Map.Entry<Character, AutomatonState> e : deterministicAutomaton.next(s).entrySet())
				{
					map.put(e.getKey(), Collections.singleton(isomorphism.get(e.getValue())));
					stack.push(e.getValue());
				}
			}
		}

	}

	/**
	 * The set of start states of a automaton.
	 * 
	 * @return The start states.
	 */
	public Set<AutomatonState> startStates()
	{
		return Collections.unmodifiableSet(startStates);
	}

	/**
	 * The set of accepting states of a automaton.
	 * 
	 * @return The accepting states.
	 */
	public Set<AutomatonState> acceptStates()
	{
		return Collections.unmodifiableSet(acceptStates);
	}

	/**
	 * Checks if any member of a set of states is an accept state.
	 * 
	 * @param setStates
	 *            The set of states to be checked.
	 * @return Is it acceptable?
	 */
	public boolean acceptable(Set<AutomatonState> setStates)
	{
		for (AutomatonState s : setStates)
			if (acceptStates.contains(s))
				return true;
		return false;
	}

	/**
	 * Checks if this automaton accepts the empty string. I.e. if the set of
	 * starting states is acceptable.
	 * 
	 * @return Does it?
	 */
	public boolean acceptsEmpty()
	{
		return acceptable(startStates);
	}

	/**
	 * Gives the transitions of a given state
	 * 
	 * @param state
	 *            The state.
	 * @return A map from characters to sets of states.
	 */
	public Map<Character, Set<AutomatonState>> next(AutomatonState state)
	{
		Map<Character, Set<AutomatonState>> map = transitions.get(state);
		if (map != null)
			return Collections.unmodifiableMap(transitions.get(state));
		else
			return null;
	}

	/**
	 * Gives the transition from a set of a states and a character.
	 * 
	 * @param setStates
	 *            The original set of states
	 * @param c
	 *            The character.
	 * @return The resulting set of states.
	 */
	public Set<AutomatonState> next(Set<AutomatonState> setStates, char c)
	{
		Set<AutomatonState> next = new HashSet<AutomatonState>();
		for (AutomatonState s : setStates)
		{
			Map<Character, Set<AutomatonState>> map = transitions.get(s);
			if (map != null)
			{
				Set<AutomatonState> set = map.get(c);
				if (set != null)
					next.addAll(set);
			}
		}
		return Collections.unmodifiableSet(next);
	}

	/**
	 * Gives the transitions of a given set of states. A compact form of
	 * {@link #next(Set)}.
	 * 
	 * @param setStates
	 *            The original set of states.
	 * @return A map from characters to set of states.
	 */
	public Map<Character, Set<AutomatonState>> next(Set<AutomatonState> setStates)
	{
		Map<Character, Set<AutomatonState>> next = new TreeMap<Character, Set<AutomatonState>>();
		for (AutomatonState s : setStates)
		{
			Map<Character, Set<AutomatonState>> map = transitions.get(s);
			if (map != null)
			{
				for (Map.Entry<Character, Set<AutomatonState>> e : map.entrySet())
				{
					Set<AutomatonState> set = next.get(e.getKey());
					if (set == null)
					{
						set = new HashSet<AutomatonState>();
						next.put(e.getKey(), set);
					}
					set.addAll(e.getValue());
				}
			}
		}
		return Collections.unmodifiableMap(next);
	}

	/**
	 * Creates the empty automaton. The empty automaton is the trivial automaton
	 * that accepts the empty set (i.e. doesn't accept any input)-
	 * 
	 * @return The empty automaton.
	 */
	public static Automaton empty()
	{
		return new Automaton(Collections.<AutomatonState> emptySet(), Collections.<AutomatonState> emptySet(),
				Collections.<AutomatonState, Map<Character, Set<AutomatonState>>> emptyMap());
	}

	/**
	 * Creates the empty string automaton. The empty string automaton is the
	 * trivial automaton that accepts the singleton set of the empty string
	 * (i.e. accepts the empty string and rejects any other).
	 * 
	 * @return The empty string automaton.
	 */
	public static Automaton emptyString()
	{
		AutomatonState state = new AutomatonState();
		return new Automaton(Collections.singleton(state), Collections.singleton(state),
				Collections.<AutomatonState, Map<Character, Set<AutomatonState>>> emptyMap());
	}

	/**
	 * Creates a singleton automaton of a given character. A singleton automaton
	 * is a trivial automaton that accepts the singleton set of a singleton
	 * string (i.e. accepts the string consisting on that sole character and
	 * rejects any other).
	 * 
	 * @param c
	 *            The character.
	 * @return The singleton automaton of the character.
	 */
	public static Automaton singleton(char c)
	{
		AutomatonState s0 = new AutomatonState();
		AutomatonState s1 = new AutomatonState();
		return new Automaton(Collections.singleton(s0), Collections.singleton(s1), Collections.singletonMap(s0,
				Collections.singletonMap(c, Collections.singleton(s1))));
	}

	/**
	 * Adds a transition to a transition map.
	 * 
	 * @param target
	 *            The transition map where the new transition is to be added.
	 * @param from
	 *            The source state of the transition.
	 * @param c
	 *            The character of the transition.
	 * @param to
	 *            The target state of the transition.
	 */
	private static void addTransition(Map<AutomatonState, Map<Character, Set<AutomatonState>>> target, AutomatonState from, char c, AutomatonState to)
	{
		Map<Character, Set<AutomatonState>> map = target.get(from);
		if (map == null)
		{
			map = new TreeMap<Character, Set<AutomatonState>>();
			target.put(from, map);
		}
		Set<AutomatonState> set = map.get(c);
		if (set == null)
		{
			set = new HashSet<AutomatonState>();
			map.put(c, set);
		}
		set.add(to);
	}

	/**
	 * Adds a map of transitions to another one, filtering it first with an
	 * {@link Isomorphism}
	 * 
	 * @param target
	 *            The transition map where the new transitions are to be added.
	 * @param isomorphism
	 *            The isomorphism to use as a filter.
	 * @param add
	 *            The transition map to filter and add to the target.
	 */
	private static void addTransitions(Map<AutomatonState, Map<Character, Set<AutomatonState>>> target, Isomorphism isomorphism,
			Map<AutomatonState, Map<Character, Set<AutomatonState>>> add)
	{
		for (Map.Entry<AutomatonState, Map<Character, Set<AutomatonState>>> e : add.entrySet())
		{
			AutomatonState s = e.getKey();
			AutomatonState s_ = isomorphism.get(s);
			for (Map.Entry<Character, Set<AutomatonState>> e2 : e.getValue().entrySet())
			{
				Character c = e2.getKey();
				for (AutomatonState s2 : e2.getValue())
				{
					AutomatonState s2_ = isomorphism.get(s2);
					addTransition(target, s_, c, s2_);
				}
			}
		}

	}

	/**
	 * Calls to {@link #union(Automaton, Automaton)} with this automaton and
	 * another one.
	 * 
	 * @param a
	 *            The automaton to unite to this.
	 * @return The resulting automaton.
	 */
	public Automaton union(Automaton a)
	{
		return union(this, a);
	}

	/**
	 * Create the union automaton of two previously existing automatons. The
	 * union automaton accepts the union set of the languages accepted by the
	 * two original functions. The union operation on automatons has the same
	 * properties as the union of sets.
	 * 
	 * @param a1
	 *            The first automaton to unite.
	 * @param a2
	 *            The second automaton to unite.
	 * @return The resulting automaton.
	 */
	public static Automaton union(Automaton a1, Automaton a2)
	{
		Isomorphism isomorphism = new Isomorphism();
		Set<AutomatonState> startStates = new HashSet<AutomatonState>();
		Set<AutomatonState> acceptStates = new HashSet<AutomatonState>();
		Map<AutomatonState, Map<Character, Set<AutomatonState>>> transitions = new HashMap<AutomatonState, Map<Character, Set<AutomatonState>>>();
		for (AutomatonState s : a1.startStates)
			startStates.add(isomorphism.get(s));
		for (AutomatonState s : a2.startStates)
			startStates.add(isomorphism.get(s));
		for (AutomatonState s : a1.acceptStates)
			acceptStates.add(isomorphism.get(s));
		for (AutomatonState s : a2.acceptStates)
			acceptStates.add(isomorphism.get(s));
		addTransitions(transitions, isomorphism, a1.transitions);
		addTransitions(transitions, isomorphism, a2.transitions);
		return new Automaton(startStates, acceptStates, transitions);
	}

	/**
	 * Computes the union of a set of automatons. The aggregate union of all the
	 * elements of the set.
	 * 
	 * @param set
	 *            The set of automatons.
	 * @return The union automaton.
	 */
	public static Automaton union(Set<Automaton> set)
	{
		Isomorphism isomorphism = new Isomorphism();
		Set<AutomatonState> startStates = new HashSet<AutomatonState>();
		Set<AutomatonState> acceptStates = new HashSet<AutomatonState>();
		Map<AutomatonState, Map<Character, Set<AutomatonState>>> transitions = new HashMap<AutomatonState, Map<Character, Set<AutomatonState>>>();
		for (Automaton a : set)
		{
			for (AutomatonState s : a.startStates)
				startStates.add(isomorphism.get(s));
			for (AutomatonState s : a.acceptStates)
				acceptStates.add(isomorphism.get(s));
			addTransitions(transitions, isomorphism, a.transitions);
		}
		return new Automaton(startStates, acceptStates, transitions);

	}

	/**
	 * Calls to {@link #concatenate(Automaton, Automaton)} with this automaton
	 * and another one.
	 * 
	 * @param a
	 *            The automaton to concatenate to this.
	 * @return The concatenated automaton.
	 */
	public Automaton concatenate(Automaton a)
	{
		return concatenate(this, a);
	}

	/**
	 * Creates the concatenation of two automatons. The concatenated automaton
	 * accepts the set of strings that are the result of the concatenation of
	 * two string accepted by the two parameter automatons, respectively.
	 * 
	 * @param a1
	 *            The first automaton to concatenate.
	 * @param a2
	 *            The second automaton to concatenate.
	 * @return The concatenated automaton.
	 */
	public static Automaton concatenate(Automaton a1, Automaton a2)
	{
		Isomorphism isomorphism = new Isomorphism();
		Set<AutomatonState> startStates = new HashSet<AutomatonState>();
		Set<AutomatonState> acceptStates = new HashSet<AutomatonState>();
		Map<AutomatonState, Map<Character, Set<AutomatonState>>> transitions = new HashMap<AutomatonState, Map<Character, Set<AutomatonState>>>();
		for (AutomatonState s : a1.startStates)
			startStates.add(isomorphism.get(s));
		if (a1.acceptsEmpty())
		{
			for (AutomatonState s : a2.startStates)
				startStates.add(isomorphism.get(s));
		}
		if (a2.acceptsEmpty())
		{
			for (AutomatonState s : a1.acceptStates)
				acceptStates.add(isomorphism.get(s));
		}
		for (AutomatonState s : a2.acceptStates)
			acceptStates.add(isomorphism.get(s));
		addTransitions(transitions, isomorphism, a1.transitions);
		addTransitions(transitions, isomorphism, a2.transitions);
		for (AutomatonState s : a1.acceptStates)
		{
			AutomatonState s_ = isomorphism.get(s);
			for (AutomatonState s2 : a2.startStates)
			{
				Map<Character, Set<AutomatonState>> map = a2.transitions.get(s2);
				if (map != null)
				{
					for (Map.Entry<Character, Set<AutomatonState>> e : map.entrySet())
					{
						Character c = e.getKey();
						for (AutomatonState s3 : e.getValue())
						{
							AutomatonState s3_ = isomorphism.get(s3);
							addTransition(transitions, s_, c, s3_);
						}
					}
				}
			}
		}
		return new Automaton(startStates, acceptStates, transitions);
	}

	/**
	 * Calls to {@link #kleene(Automaton)} with this automaton.
	 * 
	 * @return The Kleene star automaton.
	 */
	public Automaton kleene()
	{
		return kleene(this);
	}

	/**
	 * Creates the Kleene star of an automaton. The Kleene star automaton
	 * accepts any string that is an iterated concatenation of any sequence of
	 * strings accepted by the original automaton.
	 * 
	 * @param a
	 *            The source automaton.
	 * @return The Kleene star automaton.
	 */
	public static Automaton kleene(Automaton a)
	{
		Isomorphism isomorphism = new Isomorphism();
		Set<AutomatonState> startStates = new HashSet<AutomatonState>();
		Set<AutomatonState> acceptStates = new HashSet<AutomatonState>();
		Map<AutomatonState, Map<Character, Set<AutomatonState>>> transitions = new HashMap<AutomatonState, Map<Character, Set<AutomatonState>>>();
		for (AutomatonState s : a.startStates)
		{
			startStates.add(isomorphism.get(s));
			acceptStates.add(isomorphism.get(s));
		}
		for (AutomatonState s : a.acceptStates)
			acceptStates.add(isomorphism.get(s));
		addTransitions(transitions, isomorphism, a.transitions);
		for (AutomatonState s : a.acceptStates)
		{
			AutomatonState s_ = isomorphism.get(s);
			for (AutomatonState s2 : a.startStates)
			{
				Map<Character, Set<AutomatonState>> map = a.transitions.get(s2);
				if (map != null)
				{
					for (Map.Entry<Character, Set<AutomatonState>> e : map.entrySet())
					{
						Character c = e.getKey();
						for (AutomatonState s3 : e.getValue())
						{
							AutomatonState s3_ = isomorphism.get(s3);
							addTransition(transitions, s_, c, s3_);
						}
					}
				}
			}
		}
		return new Automaton(startStates, acceptStates, transitions);
	}

	/**
	 * Creates an automaton that solely recognizes a given string.
	 * 
	 * @param s
	 *            The string.
	 * @return The automaton.
	 */
	public static Automaton string(CharSequence s)
	{
		Automaton a = emptyString();
		for (int i = 0; i < s.length(); i++)
			a = concatenate(a, singleton(s.charAt(i)));
		return a;
	}

	/**
	 * Creates an automaton that recognizes a set of singleton strings defined
	 * by a given collection of characters.
	 * 
	 * @param col
	 *            The collection of characters to recognize.
	 * @return The automaton.
	 */
	public static Automaton charset(Collection<Character> col)
	{
		Automaton a = empty();
		for (char c : col)
			a = union(a, singleton(c));
		return a;
	}

	/**
	 * Computes the deterministic version of an automaton.
	 * 
	 * @return The deterministic automaton
	 * 
	 * @see DeterministicAutomaton#DeterministicAutomaton(Automaton)
	 */
	public DeterministicAutomaton determinize()
	{
		return new DeterministicAutomaton(this);
	}

	/**
	 * Computes the intersection automaton of two previously existing automaton.
	 * Calls to the intersection operation on the deterministic version of the
	 * automatons.
	 * 
	 * @param a
	 *            The first automaton to intersect.
	 * @param b
	 *            The second automaton to intersect.
	 * @return The resulting automaton.
	 * 
	 * @see DeterministicAutomaton#intersection(DeterministicAutomaton,
	 *      DeterministicAutomaton)
	 */
	public static Automaton intersection(Automaton a, Automaton b)
	{
		return DeterministicAutomaton.intersection(a.determinize(), b.determinize()).undeterminize();
	}

	/**
	 * Calls to {@link #intersection(Automaton, Automaton)} with this automaton
	 * and another one.
	 * 
	 * @param a
	 *            The automaton to intersect to this.
	 * @return The resulting automaton.
	 */
	public Automaton intersection(Automaton a)
	{
		return intersection(this, a);
	}

	/**
	 * Computes the subtraction automaton of two previously existing automaton.
	 * Calls to the subtraction operation on the deterministic version of the
	 * automatons.
	 * 
	 * @param a
	 *            The first automaton to subtract.
	 * @param b
	 *            The second automaton to subtract.
	 * @return The resulting automaton.
	 * 
	 * @see DeterministicAutomaton#subtraction(DeterministicAutomaton,
	 *      DeterministicAutomaton)
	 */
	public static Automaton subtraction(Automaton a, Automaton b)
	{
		return DeterministicAutomaton.subtraction(a.determinize(), b.determinize()).undeterminize();
	}

	/**
	 * Calls to {@link #subtraction(Automaton, Automaton)} with this automaton
	 * and another one.
	 * 
	 * @param a
	 *            The automaton to subtract to this.
	 * @return The resulting automaton.
	 */
	public Automaton subtraction(Automaton a)
	{
		return subtraction(this, a);
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
		for (AutomatonState s : startStates)
			out.print(numbering.get(s) + ", ");
		out.println();

		Set<AutomatonState> visited = new HashSet<AutomatonState>();
		Set<AutomatonState> pending = new HashSet<AutomatonState>(transitions.keySet());
		Stack<AutomatonState> stack = new Stack<AutomatonState>();
		stack.addAll(startStates);
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
					Map<Character, Set<AutomatonState>> map = transitions.get(state);
					if (map != null)
					{
						for (Map.Entry<Character, Set<AutomatonState>> e2 : map.entrySet())
						{
							Character c = e2.getKey();
							out.print(c + "[");
							for (AutomatonState s2 : e2.getValue())
							{
								out.print(numbering.get(s2) + ", ");
								stack.add(s2);
							}
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
