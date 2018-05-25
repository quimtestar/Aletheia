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

import aletheia.parsergenerator.ParserLexerException;
import aletheia.parsergenerator.lexer.Lexer;
import aletheia.parsergenerator.parser.TransitionTable.State;
import aletheia.parsergenerator.semantic.ParseTreeToken;
import aletheia.parsergenerator.semantic.ParseTreeTokenReducer;
import aletheia.parsergenerator.semantic.TokenReducer;
import aletheia.parsergenerator.symbols.EndTerminalSymbol;
import aletheia.parsergenerator.tokens.Location;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.TerminalToken;
import aletheia.parsergenerator.tokens.Token;

/**
 * A class that parses. That is, use a {@link TransitionTable} for converting
 * the flow of {@link TerminalToken}s produced by a {@link Lexer} to a
 * {@link ParseTreeToken} object according to the {@link Grammar} rules. Once
 * generated, a parser might be saved to a file for further use skipping the
 * generation phase.
 *
 * This class is abstract; his subclasses will typically implement a method that
 * builds the useful parsed objects by converting the {@link ParseTreeToken}
 * structure returned by the {@link #parseToken(Lexer)} method.
 *
 */
public abstract class Parser implements Serializable
{
	private static final long serialVersionUID = 5012002493949683810L;

	public class ParserException extends ParserLexerException
	{
		private static final long serialVersionUID = 9168128419101166987L;

		public ParserException(Location startLocation, Location stopLocation, String message, Throwable cause)
		{
			super(startLocation, stopLocation, message, cause);
		}

		public ParserException(Location startLocation, Location stopLocation, String message)
		{
			super(startLocation, stopLocation, message);
		}

		public ParserException(Location startLocation, Location stopLocation, Throwable cause)
		{
			super(startLocation, stopLocation, cause);
		}

		public ParserException(Location startLocation, Location stopLocation)
		{
			super(startLocation, stopLocation);
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
			super(token.getStartLocation(), token.getStopLocation(),
					"Unexpected token " + token.toString() + " (expecting:" + transitionTable.nextTerminals(state) + ")");
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
	 * @param tokenReducer
	 *            The token reducer.
	 * @param globals
	 *            Global data needed for parsing.
	 * 
	 * @return The {@link Token} containing the parsed structure.
	 * @throws ParserLexerException
	 */
	protected <G, T extends NonTerminalToken> T parseToken(Lexer lexer, TokenReducer<G, T> tokenReducer, G globals) throws ParserLexerException
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
				T pop = (T) outputStack.pop();
				return pop;
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
				inputStack.push(tokenReducer.reduce(globals, antecedents, prod, reducees));
				outputStack.setSize(outputStack.size() - prod.getRight().size());
				stateStack.setSize(stateStack.size() - prod.getRight().size());
			}
		}
		throw new RuntimeException();
	}

	protected <T extends NonTerminalToken> T parseToken(Lexer lexer, TokenReducer<Void, T> tokenReducer) throws ParserLexerException
	{
		return parseToken(lexer, tokenReducer, null);
	}

	protected ParseTreeToken parseToken(Lexer lexer) throws ParserLexerException
	{
		return parseToken(lexer, new ParseTreeTokenReducer());
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
