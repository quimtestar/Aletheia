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
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import aletheia.parsergenerator.LocationInterval;
import aletheia.parsergenerator.ParserBaseException;
import aletheia.parsergenerator.lexer.Lexer;
import aletheia.parsergenerator.parser.TransitionTable.State;
import aletheia.parsergenerator.semantic.ParseTree;
import aletheia.parsergenerator.semantic.ParseTreeReducer;
import aletheia.parsergenerator.semantic.TokenPayloadReducer;
import aletheia.parsergenerator.symbols.EndTerminalSymbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.TerminalToken;
import aletheia.parsergenerator.tokens.Token;

/**
 * A class that parses. That is, use a {@link TransitionTable} for converting
 * the flow of {@link TerminalToken}s produced by a {@link Lexer} to a
 * {@link ParseTree} object according to the {@link Grammar} rules. Once
 * generated, a parser might be saved to a file for further use skipping the
 * generation phase.
 *
 * This class is abstract; his subclasses will typically implement a method that
 * builds the useful parsed objects by converting the {@link ParseTree}
 * structure returned by the {@link #parseToken(Lexer)} method.
 *
 */
public abstract class Parser implements Serializable
{
	private static final long serialVersionUID = 5012002493949683810L;

	public class ParserException extends ParserBaseException
	{
		private static final long serialVersionUID = 9168128419101166987L;

		public ParserException(LocationInterval locationInterval, String message, Throwable cause)
		{
			super(locationInterval, message, cause);
		}

		public ParserException(LocationInterval locationInterval, String message)
		{
			super(locationInterval, message);
		}

		public ParserException(LocationInterval locationInterval, Throwable cause)
		{
			super(locationInterval, cause);
		}

		public ParserException(LocationInterval locationInterval)
		{
			super(locationInterval);
		}

		@Override
		public String getGenericMessage()
		{
			return "Parser error";
		}

	}

	public class UnexpectedTokenException extends ParserException
	{
		private static final long serialVersionUID = -6443315634210818099L;

		public UnexpectedTokenException(Token<?> token, State state)
		{
			super(token.getLocationInterval(), "Unexpected token " + token.toString() + " (expecting:" + transitionTable.nextTerminals(state) + ")");
		}

	}

	private final TransitionTable transitionTable;

	/**
	 * Creates a new parser from a {@linkplain TransitionTable transition table}
	 * .
	 *
	 * @param transitionTable
	 *            The transition table.
	 */
	public Parser(TransitionTable transitionTable)
	{
		this.transitionTable = transitionTable;
	}

	/**
	 * Builds a new parser with the same transition table.
	 *
	 * @param parser
	 */
	public Parser(Parser parser)
	{
		this(parser.transitionTable);
	}

	/**
	 * The transition table of this parser.
	 *
	 * @return The transition table.
	 */
	public TransitionTable getTransitionTable()
	{
		return transitionTable;
	}

	/**
	 * Saves this parser to a file.
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
	 * Loads a parser from a file.
	 *
	 * @param file
	 *            The file.
	 * @return The parser.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Parser load(File file) throws IOException, ClassNotFoundException
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
	 * Loads a parser from an input stream.
	 *
	 * @param inputStream
	 *            The input stream.
	 * @return The parser.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Parser load(InputStream inputStream) throws IOException, ClassNotFoundException
	{
		ObjectInputStream ois = new ObjectInputStream(inputStream);
		return (Parser) ois.readObject();
	}

	/**
	 * Parse the flow of terminal tokens served by a lexer, building a output
	 * token using the given token reducer.
	 *
	 * @param lexer
	 *            The lexer.
	 * @param reducer
	 *            The token reducer.
	 * @param globals
	 *            Global data needed for parsing.
	 * 
	 * @return The {@link Token} containing the parsed structure.
	 * @throws ParserBaseException
	 */
	protected <G, P> P parseToken(Lexer lexer, TokenPayloadReducer<G, P> reducer, G globals) throws ParserBaseException
	{
		Stack<State> stateStack = new Stack<>();
		Stack<Token<?>> inputStack = new Stack<>();
		Stack<Token<?>> outputStack = new Stack<>();
		stateStack.push(transitionTable.getStartState());
		while (!stateStack.isEmpty())
		{
			State state = stateStack.peek();
			if (inputStack.isEmpty())
				inputStack.push(lexer.readToken());
			if (state.equals(transitionTable.getAcceptState()) && inputStack.peek().getSymbol().equals(EndTerminalSymbol.instance))
			{
				@SuppressWarnings("unchecked")
				NonTerminalToken<G, P> pop = (NonTerminalToken<G, P>) outputStack.pop();
				return pop.getPayload();
			}
			State shiftTo = transitionTable.getTransitions().get(state).get(inputStack.peek().getSymbol());
			if (shiftTo != null)
			{
				stateStack.push(shiftTo);
				outputStack.push(inputStack.pop());
			}
			else
			{
				Production prod = transitionTable.getReductions().get(state).get(inputStack.peek().getSymbol());
				if (prod == null)
					throw new UnexpectedTokenException(inputStack.peek(), state);
				List<Token<?>> antecedents = Collections.unmodifiableList(outputStack.subList(0, outputStack.size() - prod.getRight().size()));
				List<Token<?>> reducees = Collections.unmodifiableList(outputStack.subList(outputStack.size() - prod.getRight().size(), outputStack.size()));
				inputStack.push(new NonTerminalToken<>(globals, antecedents, prod, reducees, reducer));
				outputStack.setSize(outputStack.size() - prod.getRight().size());
				stateStack.setSize(stateStack.size() - prod.getRight().size());
			}
		}
		throw new RuntimeException();
	}

	protected <P> P parseToken(Lexer lexer, TokenPayloadReducer<Void, P> tokenReducer) throws ParserBaseException
	{
		return parseToken(lexer, tokenReducer, null);
	}

	protected ParseTree parseToken(Lexer lexer) throws ParserBaseException
	{
		return parseToken(lexer, new ParseTreeReducer());
	}

	/**
	 * The grammar of this parser.
	 *
	 * @return The grammar.
	 */
	public Grammar getGrammar()
	{
		return transitionTable.getGrammar();
	}

}
