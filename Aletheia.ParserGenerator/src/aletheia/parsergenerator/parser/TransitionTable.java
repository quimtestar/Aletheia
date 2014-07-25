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
package aletheia.parsergenerator.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.symbols.TerminalSymbol;

/**
 * A transition table is the full set of transitions and reductions of all the
 * accessible grammar states of a table. Everything is computed at creation
 * time. The subclasses of this abstract class must implement the
 * {@link #itemStateTranslator()} to define what kind of parser (namely LR(1) or
 * LALR(1)) will this be.
 */
public abstract class TransitionTable implements Serializable
{
	private static final long serialVersionUID = 8597731671418749514L;

	/**
	 * The abstract states used in this transition table.
	 */
	public class State implements Serializable
	{
		private static final long serialVersionUID = -3132083909164175331L;

		protected State()
		{
			super();
		}
	}

	private final Grammar grammar;
	private final State startState;
	private final Set<State> stateSet;
	private final Map<State, Map<Symbol, State>> transitions;
	private final State acceptState;
	private final Map<State, Map<TerminalSymbol, Production>> reductions;

	public abstract class Conflict
	{
		private final ParserItem parserItem;
		private final TerminalSymbol symbol;

		protected Conflict(ParserItem parserItem, TerminalSymbol symbol)
		{
			super();
			this.parserItem = parserItem;
			this.symbol = symbol;
		}

		public ParserItem getParserItem()
		{
			return parserItem;
		}

		public TerminalSymbol getSymbol()
		{
			return symbol;
		}

		public void trace(PrintStream out)
		{
			out.println("Conflict " + type() + ": " + getSymbol());
			ParserItem item = getParserItem();
			for (ProductionState ps : item.getProductionStates())
				out.println("\t" + ps + ":" + item.getFollowers(ps));
		}

		public abstract String type();

	}

	public abstract class ReduceConflict extends Conflict
	{
		private final Production reduceProduction;

		protected ReduceConflict(ParserItem parserItem, TerminalSymbol symbol, Production reduceProduction)
		{
			super(parserItem, symbol);
			this.reduceProduction = reduceProduction;
		}

		public Production getReduceProduction()
		{
			return reduceProduction;
		}

		@Override
		public void trace(PrintStream out)
		{
			super.trace(out);
			out.println("\tConflict on: " + getReduceProduction());
		}

	}

	public class ReduceReduceConflict extends ReduceConflict
	{
		private final Production otherReduceProduction;

		protected ReduceReduceConflict(ParserItem parserItem, TerminalSymbol symbol, Production reduceProduction, Production otherReduceProduction)
		{
			super(parserItem, symbol, reduceProduction);
			this.otherReduceProduction = otherReduceProduction;
		}

		public Production getOtherReduceProduction()
		{
			return otherReduceProduction;
		}

		@Override
		public void trace(PrintStream out)
		{
			super.trace(out);
			out.println("\tWith: " + getOtherReduceProduction());
		}

		@Override
		public String type()
		{
			return "Red/Red";
		}

	}

	public class ShiftReduceConflict extends ReduceConflict
	{
		protected ShiftReduceConflict(ParserItem parserItem, TerminalSymbol symbol, Production reduceProduction)
		{
			super(parserItem, symbol, reduceProduction);
		}

		@Override
		public String type()
		{
			return "Shift/Red";
		}

	}

	public class ConflictException extends Exception
	{
		private static final long serialVersionUID = -4035092498383158414L;

		private final Collection<Conflict> conflicts;

		private ConflictException(Collection<Conflict> conflicts)
		{
			super("There were conflicts in the transition table construction");
			this.conflicts = conflicts;
		}

		public Collection<Conflict> getConflicts()
		{
			return Collections.unmodifiableCollection(conflicts);
		}

	}

	/**
	 * This class is used to implement the bidirectional conversion from
	 * {@link ParserItem}s to {@link State}s. The creation of new states must be
	 * done inside the {@link ItemStateTranslator#itemToState(ParserItem)}, and
	 * the inverse {@link ItemStateTranslator#stateToItem(State)} must respond
	 * coherently to the former.
	 */
	protected abstract class ItemStateTranslator
	{
		/**
		 * Create a new {@link State} from a {@link ParserItem} or return a
		 * previously generated {@link State} if it was already created in a
		 * previous call.
		 *
		 * @param item
		 *            The {@link ParserItem}.
		 * @return The {@link State}.
		 */
		public abstract State itemToState(ParserItem item);

		/**
		 * Return a {@link ParserItem} corresponding from a previously generated
		 * {@link State}.
		 *
		 * @param state
		 *            The {@link State}.
		 * @return The {@link ParserItem}.
		 */
		public abstract ParserItem stateToItem(State state);

		/**
		 * This item is already mapped by this translator.
		 *
		 * @param item
		 *            The {@link ParserItem}.
		 * @return Is it?
		 */
		public abstract boolean mappedItem(ParserItem item);
	}

	/**
	 * Creates the instance of {@link ItemStateTranslator} to be used in this
	 * transition table generator.
	 *
	 * @return The item to state translator.
	 */
	protected abstract ItemStateTranslator itemStateTranslator();

	/**
	 * Creates the transition table of a grammar.
	 *
	 * @param grammar
	 *            The grammar.
	 * @throws ConflictException
	 */
	public TransitionTable(Grammar grammar) throws ConflictException
	{
		super();
		this.grammar = grammar;
		ItemStateTranslator trans = itemStateTranslator();
		this.startState = trans.itemToState(ParserItem.initial(grammar));
		this.stateSet = new HashSet<State>();
		this.transitions = new HashMap<State, Map<Symbol, State>>();
		this.acceptState = trans.itemToState(trans.stateToItem(startState).next(grammar.getStartSymbol()));
		Stack<State> stack = new Stack<State>();
		stack.push(startState);
		while (!stack.isEmpty())
		{
			State state = stack.pop();
			stateSet.add(state);
			Map<Symbol, State> next = new HashMap<Symbol, State>();
			Collection<State> nextStates = new ArrayList<State>();
			for (Map.Entry<Symbol, ParserItem> e : trans.stateToItem(state).next().entrySet())
			{
				ParserItem item = e.getValue();
				boolean mapped = trans.mappedItem(item);
				State state_ = trans.itemToState(item);
				next.put(e.getKey(), state_);
				if (!stateSet.contains(state_) || !mapped)
					nextStates.add(state_);
			}
			if (state.equals(startState))
				next.put(grammar.getStartSymbol(), acceptState);
			transitions.put(state, Collections.unmodifiableMap(next));
			stack.addAll(nextStates);
		}
		this.reductions = new HashMap<State, Map<TerminalSymbol, Production>>();
		Collection<Conflict> conflicts = new ArrayList<Conflict>();
		for (State state : stateSet)
		{
			Map<TerminalSymbol, Production> map = new HashMap<TerminalSymbol, Production>();
			reductions.put(state, Collections.unmodifiableMap(map));
			for (Map.Entry<Production, Set<TerminalSymbol>> e : trans.stateToItem(state).endingProductions().entrySet())
			{
				for (TerminalSymbol s : e.getValue())
				{
					if (transitions.get(state).containsKey(s))
						conflicts.add(new ShiftReduceConflict(trans.stateToItem(state), s, e.getKey()));
					Production prod_ = map.put(s, e.getKey());
					if (prod_ != null)
						conflicts.add(new ReduceReduceConflict(trans.stateToItem(state), s, e.getKey(), prod_));
				}
			}
		}
		if (!conflicts.isEmpty())
			throw new ConflictException(conflicts);
	}

	/**
	 * The grammar associated to this transition table.
	 *
	 * @return The grammar.
	 */
	public Grammar getGrammar()
	{
		return grammar;
	}

	/**
	 * The starting state.
	 *
	 * @return The state.
	 */
	public State getStartState()
	{
		return startState;
	}

	/**
	 * The accepting state.
	 *
	 * @return The state.
	 */
	public State getAcceptState()
	{
		return acceptState;
	}

	/**
	 * The full set of states.
	 *
	 * @return The set of states.
	 */
	public Set<State> getStateSet()
	{
		return Collections.unmodifiableSet(stateSet);
	}

	/**
	 * The full map of transitions.
	 *
	 * @return The map of transitions
	 */
	public Map<State, Map<Symbol, State>> getTransitions()
	{
		return Collections.unmodifiableMap(transitions);
	}

	/**
	 * The full map of reductions.
	 *
	 * @return The map of reductions.
	 */
	public Map<State, Map<TerminalSymbol, Production>> getReductions()
	{
		return Collections.unmodifiableMap(reductions);
	}

	/**
	 * The set of terminals that may follow a state.
	 *
	 * @param state
	 *            The state.
	 * @return The set of terminals.
	 */
	public Set<TerminalSymbol> nextTerminals(State state)
	{
		Set<TerminalSymbol> set = new HashSet<TerminalSymbol>(reductions.get(state).keySet());
		for (Symbol s : transitions.get(state).keySet())
			if (s instanceof TerminalSymbol)
				set.add((TerminalSymbol) s);
		return set;
	}

	/**
	 * Saves this transition table to a file.
	 *
	 * @param file
	 *            The file.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void save(File file) throws FileNotFoundException, IOException
	{
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		try
		{
			oos.writeObject(this);
		}
		finally
		{
			oos.close();
		}
	}

	/**
	 * Loads a transition table from a file.
	 *
	 * @param file
	 *            The file.
	 * @return The transition table.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static TransitionTable load(File file) throws IOException, ClassNotFoundException
	{
		InputStream is = new FileInputStream(file);
		try
		{
			return load(is);
		}
		finally
		{
			is.close();
		}
	}

	/**
	 * Loads a transition table from an input stream.
	 *
	 * @param inputStream
	 *            The input stream.
	 * @return The transition table.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static TransitionTable load(InputStream inputStream) throws IOException, ClassNotFoundException
	{
		ObjectInputStream ois = new ObjectInputStream(inputStream);
		return (TransitionTable) ois.readObject();
	}

}
