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
package aletheia.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.statement.Context;
import aletheia.model.statement.Declaration;
import aletheia.model.statement.Statement;
import aletheia.model.term.CompositionTerm;
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.ProjectionTerm;
import aletheia.model.term.ProjectionTerm.ProjectionTypeException;
import aletheia.model.term.TTerm;
import aletheia.model.term.Term;
import aletheia.model.term.Term.ComposeTypeException;
import aletheia.model.term.Term.ReplaceTypeException;
import aletheia.model.term.VariableTerm;
import aletheia.parsergenerator.ParserLexerException;
import aletheia.parsergenerator.lexer.AutomatonSet;
import aletheia.parsergenerator.lexer.AutomatonSetLexer;
import aletheia.parsergenerator.lexer.Lexer;
import aletheia.parsergenerator.lexer.LexerLexer;
import aletheia.parsergenerator.lexer.LexerParser;
import aletheia.parsergenerator.parser.Grammar;
import aletheia.parsergenerator.parser.GrammarParser;
import aletheia.parsergenerator.parser.Parser;
import aletheia.parsergenerator.parser.TransitionTable;
import aletheia.parsergenerator.parser.TransitionTable.ConflictException;
import aletheia.parsergenerator.parser.TransitionTableLalr1;
import aletheia.parsergenerator.symbols.NonTerminalSymbol;
import aletheia.parsergenerator.symbols.TaggedNonTerminalSymbol;
import aletheia.parsergenerator.symbols.TaggedTerminalSymbol;
import aletheia.parsergenerator.symbols.TerminalSymbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.TaggedTerminalToken;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.BufferedList;

/**
 * Implementation of the {@link TermParser term parser} for the aletheia system.
 * This {@linkplain Parser parser} (and {@link Lexer lexer}) loads the actual
 * {@linkplain TransitionTable transition table} for the parser and the
 * automaton set for the lexer from the files "aletheia.ttb" and "aletheia.ast"
 * that must be in the same path than the class file of this class. Those files
 * are generated using the {@link #generate()} method.
 * 
 * 
 * @see aletheia.parsergenerator.parser
 * @see aletheia.parsergenerator.lexer
 * @see AletheiaTermParserGenerator
 */
public class AletheiaTermParser extends Parser
{
	private static final long serialVersionUID = -4016748422579759655L;

	private final static String grammarPath = "aletheia/parser/aletheia.gra";
	private final static String lexerPath = "aletheia/parser/aletheia.lex";
	private final static String transitionTablePath = "aletheia/parser/aletheia.ttb";
	private final static String automatonSetPath = "aletheia/parser/aletheia.ast";

	private final static AletheiaTermParser instance = new AletheiaTermParser();

	private final AutomatonSet automatonSet;
	private final Map<String, TaggedTerminalSymbol> taggedTerminalSymbols;
	private final Map<String, TaggedNonTerminalSymbol> taggedNonTerminalSymbols;

	private AletheiaTermParser()
	{
		super(loadTransitionTable());
		try
		{
			{
				InputStream is = ClassLoader.getSystemResourceAsStream(automatonSetPath);
				try
				{
					automatonSet = AutomatonSet.load(is);
				}
				finally
				{
					if (is != null)
						is.close();
				}
			}
		}
		catch (IOException e)
		{
			throw new Error(e);
		}
		catch (ClassNotFoundException e)
		{
			throw new Error(e);
		}
		finally
		{
		}
		taggedTerminalSymbols = new HashMap<String, TaggedTerminalSymbol>();
		for (TerminalSymbol s : getGrammar().terminalSymbols())
			if (s instanceof TaggedTerminalSymbol)
			{
				TaggedTerminalSymbol ts = (TaggedTerminalSymbol) s;
				taggedTerminalSymbols.put(ts.getTag(), ts);
			}
		taggedNonTerminalSymbols = new HashMap<String, TaggedNonTerminalSymbol>();
		for (NonTerminalSymbol s : getGrammar().nonTerminalSymbols())
			if (s instanceof TaggedNonTerminalSymbol)
			{
				TaggedNonTerminalSymbol ts = (TaggedNonTerminalSymbol) s;
				taggedNonTerminalSymbols.put(ts.getTag(), ts);
			}
	}

	public static Term parseTerm(Context context, Transaction transaction, String input) throws TermParserException
	{
		return instance.parse(context, transaction, input);
	}

	public static Term parseTerm(String input) throws TermParserException
	{
		return parseTerm(null, null, input);
	}

	private Term parse(Context context, Transaction transaction, String input) throws TermParserException
	{
		try
		{
			NonTerminalToken token = parseToken(new AutomatonSetLexer(automatonSet, new StringReader(input)));
			Map<Identifier, ParameterVariableTerm> localVariables = new HashMap<Identifier, ParameterVariableTerm>();
			return processTerm(context, transaction, localVariables, token, input);
		}
		catch (ParserLexerException e)
		{
			throw new TermParserException(e, input);
		}
	}

	private Term processTerm(Context context, Transaction transaction, Map<Identifier, ParameterVariableTerm> localVariables, NonTerminalToken token,
			String input) throws TermParserException
	{
		if (token.getProduction().getLeft().equals(taggedNonTerminalSymbols.get("T")))
		{
			if (token.getProduction().getRight().size() == 2)
			{
				Term term = processTerm(context, transaction, localVariables, (NonTerminalToken) token.getChildren().get(0), input);
				Term tail = processTerm(context, transaction, localVariables, (NonTerminalToken) token.getChildren().get(1), input);
				try
				{
					return term.compose(tail);
				}
				catch (ComposeTypeException e)
				{
					throw new TermParserException(e, token.getChildren().get(0).getStartLocation(), token.getChildren().get(0).getStopLocation(), input);
				}
			}
			else if (token.getProduction().getRight().size() == 1)
			{
				return processTerm(context, transaction, localVariables, (NonTerminalToken) token.getChildren().get(0), input);
			}
			else
				throw new Error();
		}
		else if (token.getProduction().getLeft().equals(taggedNonTerminalSymbols.get("A")))
		{
			if (token.getProduction().getRight().size() == 1)
			{
				if (token.getProduction().getRight().get(0).equals(taggedTerminalSymbols.get("ttype")))
					return TTerm.instance;
				else if (token.getProduction().getRight().get(0).equals(taggedNonTerminalSymbols.get("I")))
				{
					Identifier identifier = processIdentifier((NonTerminalToken) token.getChildren().get(0), input);
					VariableTerm variable = localVariables.get(identifier);
					if (variable == null && context != null && transaction != null)
					{
						Statement statement = context.identifierToStatement(transaction).get(identifier);
						if (statement != null)
							variable = statement.getVariable();
					}
					if (variable == null)
						throw new TermParserException("Identifier:" + "'" + identifier + "'" + " not defined", token.getChildren().get(0).getStartLocation(),
								token.getChildren().get(0).getStopLocation(), input);
					return variable;
				}
				else if (token.getProduction().getRight().get(0).equals(taggedNonTerminalSymbols.get("F")))
				{
					return processTerm(context, transaction, localVariables, (NonTerminalToken) token.getChildren().get(0), input);
				}
				else if (token.getProduction().getRight().get(0).equals(taggedNonTerminalSymbols.get("R")))
				{
					if (context == null || transaction == null)
						throw new TermParserException("No context to use the reference", token.getChildren().get(0).getStartLocation(), token.getChildren()
								.get(0).getStopLocation(), input);
					return processReference(context, transaction, (NonTerminalToken) token.getChildren().get(0), input);
				}
				else if (token.getProduction().getRight().get(0).equals(taggedTerminalSymbols.get("hexref")))
				{
					String hexRef = ((TaggedTerminalToken) token.getChildren().get(0)).getText();
					Statement statement = context.getStatementByHexRef(transaction, hexRef);
					if (statement == null)
						throw new TermParserException("Reference not found on context", token.getChildren().get(0).getStartLocation(), token.getChildren()
								.get(0).getStopLocation(), input);
					return statement.getVariable();
				}
				else
					throw new Error();
			}
			else if (token.getProduction().getRight().size() == 2)
			{
				if (token.getProduction().getRight().get(0).equals(taggedNonTerminalSymbols.get("A")))
				{
					Term term = processTerm(context, transaction, localVariables, (NonTerminalToken) token.getChildren().get(0), input);
					if (token.getProduction().getRight().get(1).equals(taggedTerminalSymbols.get("projection")))
					{
						if (term instanceof FunctionTerm)
						{
							try
							{
								return new ProjectionTerm((FunctionTerm) term);
							}
							catch (ProjectionTypeException e)
							{
								throw new TermParserException(e, token.getStartLocation(), token.getStopLocation(), input);
							}
						}
						else
							throw new TermParserException("Only can project a function term", token.getStartLocation(), token.getStopLocation(), input);
					}
					else if (token.getProduction().getRight().get(1).equals(taggedTerminalSymbols.get("percent")))
					{
						if (term instanceof FunctionTerm)
							return ((FunctionTerm) term).getParameter().getType();
						else
							throw new TermParserException("Only can take the paremeter type of a function term", token.getStartLocation(),
									token.getStopLocation(), input);
					}
					else if (token.getProduction().getRight().get(1).equals(taggedTerminalSymbols.get("sharp")))
					{
						Term type = term.getType();
						if (type == null)
							throw new TermParserException("Term '" + term + "' has no type", token.getStartLocation(), token.getStopLocation(), input);
						return term.getType();
					}
					else
						throw new Error();
				}
				else
					throw new Error();
			}
			else if (token.getProduction().getRight().size() == 3)
			{
				if (token.getProduction().getRight().get(0).equals(taggedNonTerminalSymbols.get("A")))
				{
					Term term = processTerm(context, transaction, localVariables, (NonTerminalToken) token.getChildren().get(0), input);
					if (token.getProduction().getRight().get(1).equals(taggedTerminalSymbols.get("bang")))
					{
						Identifier identifier = processIdentifier((NonTerminalToken) token.getChildren().get(2), input);
						Statement statement = context.identifierToStatement(transaction).get(identifier);
						if (statement instanceof Declaration)
						{
							Declaration declaration = (Declaration) statement;
							try
							{
								return term.replace(declaration.getVariable(), declaration.getValue());
							}
							catch (ReplaceTypeException e)
							{
								throw new TermParserException(e, token.getStartLocation(), token.getStartLocation(), input);
							}
						}
						else
							throw new TermParserException("Referenced statement: '" + identifier + "' after the bang must be a declaration", token
									.getChildren().get(2).getStartLocation(), token.getChildren().get(2).getStopLocation(), input);
					}
					else if (token.getProduction().getRight().get(1).equals(taggedTerminalSymbols.get("bar")))
					{
						Identifier identifier = processIdentifier((NonTerminalToken) token.getChildren().get(2), input);
						IdentifiableVariableTerm variable = context.identifierToVariable(transaction).get(identifier);
						if (variable == null)
							throw new TermParserException("Referenced variable: '" + identifier + "' not declared", token.getChildren().get(2)
									.getStartLocation(), token.getChildren().get(2).getStopLocation(), input);
						ParameterVariableTerm param = new ParameterVariableTerm(variable.getType());
						try
						{
							return new FunctionTerm(param, term.replace(variable, param));
						}
						catch (ReplaceTypeException e)
						{
							throw new TermParserException(e, token.getStartLocation(), token.getStartLocation(), input);
						}
					}
					else
						throw new Error();
				}
				else if (token.getProduction().getRight().get(0).equals(taggedTerminalSymbols.get("openpar")))
				{
					return processTerm(context, transaction, localVariables, (NonTerminalToken) token.getChildren().get(1), input);
				}
				else if (token.getProduction().getRight().get(0).equals(taggedTerminalSymbols.get("equals")))
				{
					Identifier identifier = processIdentifier((NonTerminalToken) token.getChildren().get(2), input);
					Statement statement = context.identifierToStatement(transaction).get(identifier);
					if (statement instanceof Declaration)
						return ((Declaration) statement).getValue();
					else
						throw new TermParserException("Referenced statement: '" + identifier + "' after the bang must be a declaration", token.getChildren()
								.get(2).getStartLocation(), token.getChildren().get(2).getStopLocation(), input);
				}
				else
					throw new Error();
			}
			else if (token.getProduction().getRight().size() == 4)
			{
				Term term = processTerm(context, transaction, localVariables, (NonTerminalToken) token.getChildren().get(0), input);
				if (term instanceof CompositionTerm)
				{
					List<Term> components;
					if (((NonTerminalToken) token.getChildren().get(3)).getChildren().size() > 0)
						components = new BufferedList<>(((CompositionTerm) term).aggregateComponents());
					else
						components = new BufferedList<>(((CompositionTerm) term).components());
					int n = Integer.parseInt(((TaggedTerminalToken) token.getChildren().get(2)).getText());
					if (n < 0 || n >= components.size())
						throw new TermParserException("Composition coordinate " + n + " out of bounds for term: " + "'" + term + "'", token.getChildren()
								.get(2).getStartLocation(), token.getChildren().get(2).getStopLocation(), input);
					return components.get(n);
				}
				else
					throw new TermParserException("Only can use composition coordinates in compositions", token.getStartLocation(), token.getStopLocation(),
							input);
			}
			else
				throw new Error();
		}
		else if (token.getProduction().getLeft().equals(taggedNonTerminalSymbols.get("F")))
		{
			String name = ((TaggedTerminalToken) token.getChildren().get(1)).getText();
			Identifier identifier;
			try
			{
				identifier = new Identifier(name);
			}
			catch (InvalidNameException e)
			{
				throw new TermParserException(e, token.getChildren().get(1).getStartLocation(), token.getChildren().get(1).getStopLocation(), input);
			}
			Term type = processTerm(context, transaction, localVariables, (NonTerminalToken) token.getChildren().get(3), input);
			ParameterVariableTerm parameter = new ParameterVariableTerm(type);
			ParameterVariableTerm oldpar = localVariables.put(identifier, parameter);
			try
			{
				Term body = processTerm(context, transaction, localVariables, (NonTerminalToken) token.getChildren().get(5), input);
				return new FunctionTerm(parameter, body);
			}
			finally
			{
				if (oldpar != null)
					localVariables.put(identifier, oldpar);
				else
					localVariables.remove(identifier);
			}
		}
		else
			throw new Error();
	}

	private Identifier processIdentifier(NonTerminalToken token, String input) throws TermParserException
	{
		if (token.getProduction().getRight().size() == 3)
		{
			Identifier namespace = processIdentifier((NonTerminalToken) token.getChildren().get(0), input);
			String name = ((TaggedTerminalToken) token.getChildren().get(2)).getText();
			try
			{
				return new Identifier(namespace, name);
			}
			catch (InvalidNameException e)
			{
				throw new TermParserException(e, token.getChildren().get(2).getStartLocation(), token.getChildren().get(2).getStopLocation(), input);
			}

		}
		else if (token.getProduction().getRight().size() == 1)
		{
			String name = ((TaggedTerminalToken) token.getChildren().get(0)).getText();
			try
			{
				return new Identifier(name);
			}
			catch (InvalidNameException e)
			{
				throw new TermParserException(e, token.getChildren().get(0).getStartLocation(), token.getChildren().get(0).getStopLocation(), input);
			}
		}
		else
			throw new Error();

	}

	private Term processReference(Context context, Transaction transaction, NonTerminalToken token, String input) throws TermParserException
	{
		NonTerminalToken refToken = (NonTerminalToken) token.getChildren().get(1);
		if (refToken.getProduction().getRight().get(0).equals(taggedNonTerminalSymbols.get("I")))
		{
			Identifier identifier = processIdentifier((NonTerminalToken) refToken.getChildren().get(0), input);
			Statement statement = context.identifierToStatement(transaction).get(identifier);
			if (statement == null)
				throw new TermParserException("Identifier: " + "'" + identifier + "'" + " not defined", token.getChildren().get(1).getStartLocation(), token
						.getChildren().get(1).getStopLocation(), input);
			return statement.getTerm();
		}
		else if (refToken.getProduction().getRight().get(0).equals(taggedTerminalSymbols.get("hexref")))
		{
			String hexRef = ((TaggedTerminalToken) refToken.getChildren().get(0)).getText();
			Statement statement = context.getStatementByHexRef(transaction, hexRef);
			if (statement == null)
				throw new TermParserException("Reference not found on context", token.getChildren().get(0).getStartLocation(), token.getChildren().get(0)
						.getStopLocation(), input);
			return statement.getTerm();
		}
		else if (refToken.getProduction().getRight().get(0).equals(taggedTerminalSymbols.get("turnstile")))
		{
			return context.getConsequent();
		}
		else
			throw new Error();
	}

	public static AutomatonSet createAutomatonSet() throws ParserLexerException, IOException
	{
		Reader reader = new InputStreamReader(ClassLoader.getSystemResourceAsStream(lexerPath));
		try
		{
			LexerLexer lexLex = new LexerLexer(reader);
			LexerParser lexParser = new LexerParser();
			AutomatonSet automatonSet = lexParser.parse(lexLex);
			return automatonSet;
		}
		finally
		{
			reader.close();
		}
	}

	public static TransitionTable createTransitionTable() throws ConflictException, ParserLexerException, IOException
	{
		Reader reader = new InputStreamReader(ClassLoader.getSystemResourceAsStream(grammarPath));
		try
		{
			GrammarParser gp = new GrammarParser();
			Grammar g = gp.parse(reader);
			TransitionTable table = new TransitionTableLalr1(g);
			return table;
		}
		finally
		{
			reader.close();
		}
	}

	private static TransitionTable loadTransitionTable()
	{
		InputStream is = ClassLoader.getSystemResourceAsStream(transitionTablePath);
		try
		{
			return TransitionTable.load(is);
		}
		catch (ClassNotFoundException e)
		{
			throw new Error(e);
		}
		catch (IOException e)
		{
			throw new Error(e);
		}
		finally
		{
			try
			{
				if (is != null)
					is.close();
			}
			catch (IOException e)
			{
				throw new Error(e);
			}
		}
	}

	protected static void generate() throws ParserLexerException, IOException, ConflictException
	{
		AutomatonSet automatonSet = createAutomatonSet();
		automatonSet.save(new File("src/" + automatonSetPath));

		TransitionTable table = createTransitionTable();
		table.save(new File("src/" + transitionTablePath));

	}

}
