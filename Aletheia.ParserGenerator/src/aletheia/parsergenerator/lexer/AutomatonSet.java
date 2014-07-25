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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import aletheia.parsergenerator.symbols.TerminalSymbol;

/**
 * A batch of deterministic automatons associated to:
 * <ul>
 * <li>A {@link TerminalSymbol}.</li>
 * <li>An order.</li>
 * </ul>
 * This class is meant to be used as a combined recognizer.
 */
public class AutomatonSet implements Serializable
{
	private static final long serialVersionUID = -3367288834352882675L;

	private final Map<DeterministicAutomaton, TerminalSymbol> tagMap;
	private final Map<DeterministicAutomaton, Integer> orderMap;
	private int numAutomatons;

	/**
	 * Creates a new empty automaton set.
	 */
	public AutomatonSet()
	{
		tagMap = new HashMap<DeterministicAutomaton, TerminalSymbol>();
		orderMap = new HashMap<DeterministicAutomaton, Integer>();
		numAutomatons = 0;
	}

	/**
	 * Adds a deterministic automaton associated to a {@link TerminalSymbol}.
	 * The order of the automatons is determined with the order in which this
	 * method is called.
	 *
	 * @param automaton
	 *            The automaton.
	 * @param terminalSymbol
	 *            The terminal symbol. Might be null when the input recognized
	 *            by this automaton is to be ignored.
	 */
	public void addAutomatonTag(DeterministicAutomaton automaton, TerminalSymbol terminalSymbol)
	{
		tagMap.put(automaton, terminalSymbol);
		orderMap.put(automaton, numAutomatons);
		numAutomatons++;
	}

	/**
	 * The set of deterministic automatons of this automaton set.
	 *
	 * @return The set.
	 */
	public Set<DeterministicAutomaton> automatonSet()
	{
		return Collections.unmodifiableSet(tagMap.keySet());
	}

	/**
	 * Creates a comparator of deterministic automatons based on the order
	 * defined.
	 *
	 * @return The comparator.
	 */
	public Comparator<DeterministicAutomaton> automatonComparator()
	{
		return new Comparator<DeterministicAutomaton>()
				{

			@Override
			public int compare(DeterministicAutomaton a0, DeterministicAutomaton a1)
			{
				return orderMap.get(a0) - orderMap.get(a1);
			}

				};
	}

	/**
	 * The terminal symbol associated to an automaton.
	 *
	 * @param automaton
	 *            The automaton.
	 * @return The terminal symbol.
	 */
	public TerminalSymbol getTerminalSymbol(DeterministicAutomaton automaton)
	{
		return tagMap.get(automaton);
	}

	/**
	 * The collection of terminal symbols associated to any automaton in this
	 * set.
	 *
	 * @return The collection.
	 */
	public Collection<TerminalSymbol> terminalSymbols()
	{
		return tagMap.values();
	}

	/**
	 * Saves this automaton set to a file for further use.
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
	 * Loads a previously saved automaton set from a file.
	 *
	 * @param file
	 *            The file.
	 * @return The automaton set.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static AutomatonSet load(File file) throws IOException, ClassNotFoundException
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
	 * Loads a previously saved automaton from an input stream.
	 *
	 * @param inputStream
	 *            The input stream.
	 * @return The automaton set.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static AutomatonSet load(InputStream inputStream) throws IOException, ClassNotFoundException
	{
		ObjectInputStream ois = new ObjectInputStream(inputStream);
		return (AutomatonSet) ois.readObject();
	}

}
