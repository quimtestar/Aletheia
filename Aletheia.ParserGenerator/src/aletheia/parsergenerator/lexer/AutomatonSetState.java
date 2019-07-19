/*******************************************************************************
 * Copyright (c) 2014, 2018 Quim Testar.
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

import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import aletheia.parsergenerator.symbols.TerminalSymbol;

/**
 * The state of an automaton set recognizer. It is essentially the combination
 * of {@link AutomatonState}s of the {@link DeterministicAutomaton}s that
 * compose the automaton set.
 */
public class AutomatonSetState
{
	private final AutomatonSet automatonSet;
	private final SortedMap<DeterministicAutomaton, AutomatonState> state;
	private final StringBuffer textBuffer;
	private TerminalSymbol chosen;
	boolean processedSomething = false;
	boolean ignoreInput = false;

	/**
	 * Creates the initial automaton set state for a given automaton set.
	 *
	 * @param automatonSet
	 *            The automaton set.
	 */
	public AutomatonSetState(AutomatonSet automatonSet)
	{
		this.automatonSet = automatonSet;
		this.state = new TreeMap<>(automatonSet.automatonComparator());
		this.textBuffer = new StringBuffer();
		for (DeterministicAutomaton a : automatonSet.automatonSet())
			state.put(a, a.startState());
	}

	/**
	 * Forces the choosing of the automaton still non-discarded state that is
	 * first in the defined order.
	 *
	 * @see #getChosen()
	 */
	public void choose()
	{
		chosen = null;
		ignoreInput = false;
		for (Map.Entry<DeterministicAutomaton, AutomatonState> e : state.entrySet())
		{
			DeterministicAutomaton a = e.getKey();
			AutomatonState s = e.getValue();
			if (a.acceptable(s))
			{
				chosen = automatonSet.getTerminalSymbol(a);
				if (chosen == null)
					ignoreInput = true;
				break;
			}
		}
	}

	/**
	 * Alters this state by processing the given character.
	 *
	 * @param c
	 *            The character.
	 */
	public void next(char c)
	{
		if (processedSomething)
			choose();
		for (Iterator<Map.Entry<DeterministicAutomaton, AutomatonState>> i = state.entrySet().iterator(); i.hasNext();)
		{
			Map.Entry<DeterministicAutomaton, AutomatonState> e = i.next();
			DeterministicAutomaton a = e.getKey();
			AutomatonState s = e.getValue();
			s = a.next(s, c);
			if (s != null)
				e.setValue(s);
			else
				i.remove();
		}
		if (!state.isEmpty())
			textBuffer.append(c);
		processedSomething = true;
	}

	/**
	 * Checks if there is any non-discarded automaton in this state.
	 *
	 * @return Is this state at end?
	 */
	public boolean atEnd()
	{
		return state.isEmpty();
	}

	/**
	 * Returns the chosen terminal symbol of this state.
	 *
	 * @return The chosen symbol.
	 */
	public TerminalSymbol getChosen()
	{
		return chosen;
	}

	/**
	 * Checks true if the chosen automaton is associated with no terminal
	 * symbol.
	 *
	 * @return Is this state ignoring input?
	 */
	public boolean isIgnoreInput()
	{
		return ignoreInput;
	}

	public String getText()
	{
		return textBuffer.toString();
	}

}
