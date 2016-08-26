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
import java.util.UUID;

import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.statement.Context;
import aletheia.model.statement.Declaration;
import aletheia.model.statement.Specialization;
import aletheia.model.statement.Statement;
import aletheia.model.term.CompositionTerm;
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.FunctionTerm.NullParameterTypeException;
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
import aletheia.parsergenerator.tokens.Location;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.TaggedTerminalToken;
import aletheia.parsergenerator.tokens.TerminalToken;
import aletheia.persistence.Transaction;
import aletheia.persistence.collections.statement.GenericRootContextsMap;
import aletheia.utilities.MiscUtilities;
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
		taggedTerminalSymbols = new HashMap<>();
		for (TerminalSymbol s : getGrammar().terminalSymbols())
			if (s instanceof TaggedTerminalSymbol)
			{
				TaggedTerminalSymbol ts = (TaggedTerminalSymbol) s;
				taggedTerminalSymbols.put(ts.getTag(), ts);
			}
		taggedNonTerminalSymbols = new HashMap<>();
		for (NonTerminalSymbol s : getGrammar().nonTerminalSymbols())
			if (s instanceof TaggedNonTerminalSymbol)
			{
				TaggedNonTerminalSymbol ts = (TaggedNonTerminalSymbol) s;
				taggedNonTerminalSymbols.put(ts.getTag(), ts);
			}
	}

	public static Term parseTerm(Context context, Transaction transaction, String input, Map<ParameterVariableTerm, Identifier> parameterIdentifiers)
			throws TermParserException
	{
		return instance.parse(context, transaction, input, parameterIdentifiers);
	}

	public static Term parseTerm(Context context, Transaction transaction, String input) throws TermParserException
	{
		return parseTerm(context, transaction, input, null);
	}

	public static Term parseTerm(String input, Map<ParameterVariableTerm, Identifier> parameterIdentifiers) throws TermParserException
	{
		return parseTerm(null, null, input, parameterIdentifiers);
	}

	public static Term parseTerm(String input) throws TermParserException
	{
		return parseTerm(input, null);
	}

	private static abstract class ParameterRef
	{

		@Override
		public int hashCode()
		{
			return 1;
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
			return true;
		}

	}

	private static class IdentifierParameterRef extends ParameterRef
	{
		private final Identifier identifier;

		private IdentifierParameterRef(Identifier identifier)
		{
			super();
			this.identifier = identifier;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			IdentifierParameterRef other = (IdentifierParameterRef) obj;
			if (identifier == null)
			{
				if (other.identifier != null)
					return false;
			}
			else if (!identifier.equals(other.identifier))
				return false;
			return true;
		}
	}

	private static class NumberedParameterRef extends ParameterRef
	{
		private final String atParam;

		private NumberedParameterRef(String atParam)
		{
			super();
			this.atParam = atParam;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((atParam == null) ? 0 : atParam.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			NumberedParameterRef other = (NumberedParameterRef) obj;
			if (atParam == null)
			{
				if (other.atParam != null)
					return false;
			}
			else if (!atParam.equals(other.atParam))
				return false;
			return true;
		}

	}

	private Term parse(Context context, Transaction transaction, String input, Map<ParameterVariableTerm, Identifier> parameterIdentifiers)
			throws TermParserException
	{
		try
		{
			NonTerminalToken token = parseToken(new AutomatonSetLexer(automatonSet, new StringReader(input)));
			Map<ParameterRef, ParameterVariableTerm> tempParameterTable = new HashMap<>();
			return processTerm(context, transaction, tempParameterTable, token, input, parameterIdentifiers);
		}
		catch (ParserLexerException e)
		{
			throw new TermParserException(e, input);
		}
	}

	private Term processTerm(Context context, Transaction transaction, Map<ParameterRef, ParameterVariableTerm> tempParameterTable, NonTerminalToken token,
			String input, Map<ParameterVariableTerm, Identifier> parameterIdentifiers) throws TermParserException
	{
		if (token.getProduction().getLeft().equals(taggedNonTerminalSymbols.get("T")))
		{
			if (token.getProduction().getRight().size() == 2)
			{
				Term term = processTerm(context, transaction, tempParameterTable, (NonTerminalToken) token.getChildren().get(0), input);
				Term tail = processTerm(context, transaction, tempParameterTable, (NonTerminalToken) token.getChildren().get(1), input);
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
				return processTerm(context, transaction, tempParameterTable, (NonTerminalToken) token.getChildren().get(0), input, parameterIdentifiers);
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
					VariableTerm variable = tempParameterTable.get(new IdentifierParameterRef(identifier));
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
				else if (token.getProduction().getRight().get(0).equals(taggedTerminalSymbols.get("atparam")))
				{
					String atParam = ((TaggedTerminalToken) token.getChildren().get(0)).getText();
					VariableTerm variable = tempParameterTable.get(new NumberedParameterRef(atParam));
					if (variable == null)
						throw new TermParserException("Parameter:" + "'" + atParam + "'" + " not defined", token.getChildren().get(0).getStartLocation(),
								token.getChildren().get(0).getStopLocation(), input);
					return variable;
				}
				else if (token.getProduction().getRight().get(0).equals(taggedNonTerminalSymbols.get("F")))
				{
					return processTerm(context, transaction, tempParameterTable, (NonTerminalToken) token.getChildren().get(0), input, parameterIdentifiers);
				}
				else if (token.getProduction().getRight().get(0).equals(taggedNonTerminalSymbols.get("U")))
				{
					return processTerm(context, transaction, tempParameterTable, (NonTerminalToken) token.getChildren().get(0), input, parameterIdentifiers);
				}
				else if (token.getProduction().getRight().get(0).equals(taggedNonTerminalSymbols.get("R")))
				{
					if (transaction == null)
						throw new TermParserException("Can't process references", token.getChildren().get(0).getStartLocation(),
								token.getChildren().get(0).getStopLocation(), input);
					return processReference(context, transaction, (NonTerminalToken) token.getChildren().get(0), input);
				}
				else if (token.getProduction().getRight().get(0).equals(taggedTerminalSymbols.get("hexref")))
				{
					String hexRef = ((TaggedTerminalToken) token.getChildren().get(0)).getText();
					Statement statement = context.getStatementByHexRef(transaction, hexRef);
					if (statement == null)
						throw new TermParserException("Reference not found on context", token.getChildren().get(0).getStartLocation(),
								token.getChildren().get(0).getStopLocation(), input);
					return statement.getVariable();
				}
				else
					throw new Error();
			}
			else if (token.getProduction().getRight().size() == 2)
			{
				if (token.getProduction().getRight().get(0).equals(taggedNonTerminalSymbols.get("A")))
				{
					Term term = processTerm(context, transaction, tempParameterTable, (NonTerminalToken) token.getChildren().get(0), input);
					if (token.getProduction().getRight().get(1).equals(taggedTerminalSymbols.get("asterisk")))
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
					else if (token.getProduction().getRight().get(1).equals(taggedTerminalSymbols.get("tilde")))
					{
						if (term instanceof ProjectionTerm)
							return ((ProjectionTerm) term).getFunction();
						else
							throw new TermParserException("Only can unproject a projected function term", token.getStartLocation(), token.getStopLocation(),
									input);
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
						return type;
					}
					else if (token.getProduction().getRight().get(1).equals(taggedTerminalSymbols.get("apostrophe")))
					{
						if (term instanceof FunctionTerm)
						{
							FunctionTerm functionTerm = (FunctionTerm) term;
							Term body = functionTerm.getBody();
							if (body.freeVariables().contains(functionTerm.getParameter()))
								throw new TermParserException("Function's body depends on function parameter", token.getStartLocation(),
										token.getStopLocation(), input);
							else
								return body;
						}
						else
							throw new TermParserException("Only can take the body of a function term", token.getStartLocation(), token.getStopLocation(),
									input);
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
					Term term = processTerm(context, transaction, tempParameterTable, (NonTerminalToken) token.getChildren().get(0), input);
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
								throw new TermParserException(e, token.getStartLocation(), token.getStopLocation(), input);
							}
						}
						else
							throw new TermParserException("Referenced statement: '" + identifier + "' after the bang must be a declaration",
									token.getChildren().get(2).getStartLocation(), token.getChildren().get(2).getStopLocation(), input);
					}
					else if (token.getProduction().getRight().get(1).equals(taggedTerminalSymbols.get("bar")))
					{
						Identifier identifier = processIdentifier((NonTerminalToken) token.getChildren().get(2), input);
						IdentifiableVariableTerm variable = context.identifierToVariable(transaction).get(identifier);
						if (variable == null)
							throw new TermParserException("Referenced variable: '" + identifier + "' not declared",
									token.getChildren().get(2).getStartLocation(), token.getChildren().get(2).getStopLocation(), input);
						ParameterVariableTerm param = new ParameterVariableTerm(variable.getType());
						try
						{
							return new FunctionTerm(param, term.replace(variable, param));
						}
						catch (ReplaceTypeException | NullParameterTypeException e)
						{
							throw new TermParserException(e, token.getStartLocation(), token.getStopLocation(), input);
						}
					}
					else
						throw new Error();
				}
				else if (token.getProduction().getRight().get(0).equals(taggedTerminalSymbols.get("openpar")))
				{
					return processTerm(context, transaction, tempParameterTable, (NonTerminalToken) token.getChildren().get(1), input);
				}
				else if (token.getProduction().getRight().get(0).equals(taggedTerminalSymbols.get("equals")))
				{
					Identifier identifier = processIdentifier((NonTerminalToken) token.getChildren().get(2), input);
					Statement statement = context.identifierToStatement(transaction).get(identifier);
					if (statement instanceof Declaration)
						return ((Declaration) statement).getValue();
					else
						throw new TermParserException("Referenced statement: '" + identifier + "' after the bang must be a declaration",
								token.getChildren().get(2).getStartLocation(), token.getChildren().get(2).getStopLocation(), input);
				}
				else
					throw new Error();
			}
			else if (token.getProduction().getRight().size() == 4)
			{
				Term term = processTerm(context, transaction, tempParameterTable, (NonTerminalToken) token.getChildren().get(0), input);
				if (term instanceof CompositionTerm)
				{
					List<Term> components;
					if (((NonTerminalToken) token.getChildren().get(3)).getChildren().size() > 0)
						components = new BufferedList<>(((CompositionTerm) term).aggregateComponents());
					else
						components = new BufferedList<>(((CompositionTerm) term).components());
					int n = Integer.parseInt(((TaggedTerminalToken) token.getChildren().get(2)).getText());
					if (n < 0 || n >= components.size())
						throw new TermParserException("Composition coordinate " + n + " out of bounds for term: " + "'" + term + "'",
								token.getChildren().get(2).getStartLocation(), token.getChildren().get(2).getStopLocation(), input);
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
			ParameterRef parameterRef = processParameterRef((NonTerminalToken) token.getChildren().get(1), input);
			Term type = processTerm(context, transaction, tempParameterTable, (NonTerminalToken) token.getChildren().get(3), input);
			ParameterVariableTerm parameter = new ParameterVariableTerm(type);
			if (parameterIdentifiers != null && parameterRef instanceof IdentifierParameterRef)
				parameterIdentifiers.put(parameter, ((IdentifierParameterRef) parameterRef).identifier);
			ParameterVariableTerm oldpar = tempParameterTable.put(parameterRef, parameter);
			try
			{
				Term body = processTerm(context, transaction, tempParameterTable, (NonTerminalToken) token.getChildren().get(5), input, parameterIdentifiers);
				try
				{
					return new FunctionTerm(parameter, body);
				}
				catch (NullParameterTypeException e)
				{
					throw new TermParserException(e, token.getStartLocation(), token.getStopLocation(), input);
				}
			}
			finally
			{
				if (oldpar != null)
					tempParameterTable.put(parameterRef, oldpar);
				else
					tempParameterTable.remove(parameterRef);
			}
		}
		else if (token.getProduction().getLeft().equals(taggedNonTerminalSymbols.get("U")))
		{
			ParameterRef parameterRef = processParameterRef((NonTerminalToken) token.getChildren().get(1), input);
			Term value = processTerm(context, transaction, tempParameterTable, (NonTerminalToken) token.getChildren().get(3), input);
			ParameterVariableTerm parameter = new ParameterVariableTerm(value.getType());
			if (parameterIdentifiers != null && parameterRef instanceof IdentifierParameterRef)
				parameterIdentifiers.put(parameter, ((IdentifierParameterRef) parameterRef).identifier);
			ParameterVariableTerm oldpar = tempParameterTable.put(parameterRef, parameter);
			try
			{
				Term body = processTerm(context, transaction, tempParameterTable, (NonTerminalToken) token.getChildren().get(5), input, parameterIdentifiers);
				try
				{
					return new FunctionTerm(parameter, body).compose(value);
				}
				catch (ComposeTypeException | NullParameterTypeException e)
				{
					throw new TermParserException(e, token.getStartLocation(), token.getStopLocation(), input);
				}
			}
			finally
			{
				if (oldpar != null)
					tempParameterTable.put(parameterRef, oldpar);
				else
					tempParameterTable.remove(parameterRef);
			}
		}
		else
			throw new Error();
	}

	private Term processTerm(Context context, Transaction transaction, Map<ParameterRef, ParameterVariableTerm> tempParameterTable, NonTerminalToken token,
			String input) throws TermParserException
	{
		return processTerm(context, transaction, tempParameterTable, token, input, null);
	}

	private ParameterRef processParameterRef(NonTerminalToken token, String input) throws TermParserException
	{
		if (token.getProduction().getRight().get(0).equals(taggedNonTerminalSymbols.get("I")))
			return new IdentifierParameterRef(processIdentifier((NonTerminalToken) token.getChildren().get(0), input));
		else if (token.getProduction().getRight().get(0).equals(taggedTerminalSymbols.get("atparam")))
			return new NumberedParameterRef(((TaggedTerminalToken) token.getChildren().get(0)).getText());
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

	enum ReferenceType
	{
		TYPE, INSTANCE, VALUE,
	};

	private ReferenceType processReferenceType(NonTerminalToken token)
	{
		if (token.getProduction().getRight().size() == 1)
			return ReferenceType.TYPE;
		else if (token.getProduction().getRight().size() == 2)
		{
			if (token.getProduction().getRight().get(1).equals(taggedTerminalSymbols.get("caret")))
				return ReferenceType.INSTANCE;
			else if (token.getProduction().getRight().get(1).equals(taggedTerminalSymbols.get("bang")))
				return ReferenceType.VALUE;
			else
				throw new Error();
		}
		else
			throw new Error();
	}

	private Term processReference(Context context, Transaction transaction, NonTerminalToken token, String input) throws TermParserException
	{
		ReferenceType referenceType = processReferenceType((NonTerminalToken) token.getChildren().get(0));
		if (token.getProduction().getRight().size() == 2)
			return processStatementReference_terminal_s(context, transaction, referenceType, (NonTerminalToken) token.getChildren().get(1), input);
		else if (token.getProduction().getRight().size() == 4)
		{
			Term term = processStatementReference_path_s(context, transaction, referenceType, (NonTerminalToken) token.getChildren().get(2), input);
			for (IdentifiableVariableTerm v : term.freeIdentifiableVariables())
			{
				if (context == null)
					throw new TermParserException("Referenced term contains free variables", token.getChildren().get(2).getStartLocation(),
							token.getChildren().get(2).getStopLocation(), input);
				else if (!context.statements(transaction).containsKey(v))
				{
					throw new TermParserException("Referenced term contains free variables not of this context", token.getChildren().get(2).getStartLocation(),
							token.getChildren().get(2).getStopLocation(), input);
				}
			}
			return term;
		}
		else
			throw new Error();
	}

	private Term dereferenceStatement(Statement statement, ReferenceType referenceType, Location startLocation, Location stopLocation, String input)
			throws TermParserException
	{
		switch (referenceType)
		{
		case TYPE:
			return statement.getTerm();
		case INSTANCE:
		{
			if (!(statement instanceof Specialization))
				throw new TermParserException("Cannot reference the instance of a non-specialization statement", startLocation, stopLocation, input);
			return ((Specialization) statement).getInstance();
		}
		case VALUE:
		{
			if (!(statement instanceof Declaration))
				throw new TermParserException("Cannot reference the value of a non-declaration statement", startLocation, stopLocation, input);
			return ((Declaration) statement).getValue();
		}
		default:
			throw new Error();
		}
	}

	private Term processStatementReference_terminal_s(Context context, Transaction transaction, ReferenceType referenceType, NonTerminalToken token,
			String input) throws TermParserException
	{
		if (token.getProduction().getRight().get(0).equals(taggedNonTerminalSymbols.get("S_t")))
			return processStatementReference_terminal(context, transaction, referenceType, (NonTerminalToken) token.getChildren().get(0), input);
		else if (token.getProduction().getRight().get(0).equals(taggedTerminalSymbols.get("uuid")))
		{
			UUID uuid = processUuid((TerminalToken) token.getChildren().get(0), input);
			Statement statement = transaction.getPersistenceManager().getStatement(transaction, uuid);
			if (statement == null)
				throw new TermParserException("Statement not found with UUID: " + uuid, token.getChildren().get(0).getStartLocation(),
						token.getChildren().get(0).getStopLocation(), input);
			return dereferenceStatement(statement, referenceType, token.getChildren().get(0).getStartLocation(), token.getChildren().get(0).getStopLocation(),
					input);
		}
		else
			throw new Error();
	}

	private Term processStatementReference_terminal(Context context, Transaction transaction, ReferenceType referenceType, NonTerminalToken token, String input)
			throws TermParserException
	{
		if (token.getProduction().getRight().get(0).equals(taggedNonTerminalSymbols.get("S")))
		{
			Statement statement = processStatementReference(context, transaction, (NonTerminalToken) token.getChildren().get(0), input);
			return dereferenceStatement(statement, referenceType, token.getChildren().get(0).getStartLocation(), token.getChildren().get(0).getStopLocation(),
					input);
		}
		else if (token.getProduction().getRight().get(0).equals(taggedTerminalSymbols.get("turnstile")))
		{
			if (context == null)
				throw new TermParserException("Cannot refer to the consequent without a context", token.getChildren().get(0).getStartLocation(),
						token.getChildren().get(0).getStopLocation(), input);
			if (referenceType != ReferenceType.TYPE)
				throw new TermParserException("Invalid reference type to the consequent", token.getChildren().get(0).getStartLocation(),
						token.getChildren().get(0).getStopLocation(), input);
			return context.getConsequent();
		}
		else
			throw new Error();
	}

	private Statement processStatementReference(Context context, Transaction transaction, NonTerminalToken token, String input) throws TermParserException
	{
		if (token.getProduction().getRight().get(0).equals(taggedNonTerminalSymbols.get("I")))
		{
			Identifier identifier = processIdentifier((NonTerminalToken) token.getChildren().get(0), input);
			if (context == null)
			{
				GenericRootContextsMap rcm = transaction.getPersistenceManager().identifierToRootContexts(transaction).get(identifier);
				if (rcm == null || rcm.size() < 1)
					throw new TermParserException("Identifier: " + "'" + identifier + "'" + " not defined at root level",
							token.getChildren().get(0).getStartLocation(), token.getChildren().get(0).getStopLocation(), input);
				if (rcm.size() > 1)
					throw new TermParserException("Multiple root contexts with identifier: " + "'" + identifier + "'",
							token.getChildren().get(0).getStartLocation(), token.getChildren().get(0).getStopLocation(), input);
				else
					return MiscUtilities.firstFromCloseableIterable(rcm.values());
			}
			else
			{
				Statement statement = context.identifierToStatement(transaction).get(identifier);
				if (statement == null)
					throw new TermParserException("Identifier: " + "'" + identifier + "'" + " not defined in context: \"" + context.label() + "\"",
							token.getChildren().get(0).getStartLocation(), token.getChildren().get(0).getStopLocation(), input);
				return statement;
			}
		}
		else if (token.getProduction().getRight().get(0).equals(taggedTerminalSymbols.get("hexref")))
		{
			String hexRef = ((TaggedTerminalToken) token.getChildren().get(0)).getText();
			if (context == null)
			{
				Statement statement = transaction.getPersistenceManager().getRootContextByHexRef(transaction, hexRef);
				if (statement == null)
					throw new TermParserException("Reference: + " + "'" + hexRef + "'" + " not found on root level",
							token.getChildren().get(0).getStartLocation(), token.getChildren().get(0).getStopLocation(), input);
				return statement;
			}
			else
			{
				Statement statement = context.getStatementByHexRef(transaction, hexRef);
				if (statement == null)
					throw new TermParserException("Reference: + " + "'" + hexRef + "'" + " not found on context: \"" + context.label() + "\"",
							token.getChildren().get(0).getStartLocation(), token.getChildren().get(0).getStopLocation(), input);
				return statement;
			}
		}
		else
			throw new Error();

	}

	private Term processStatementReference_path_s(Context context, Transaction transaction, ReferenceType referenceType, NonTerminalToken token, String input)
			throws TermParserException
	{
		if (token.getProduction().getRight().size() == 2)
			return processStatementReference_path(null, transaction, referenceType, (NonTerminalToken) token.getChildren().get(1), input);
		else if (token.getProduction().getRight().size() == 3)
		{
			UUID uuid = processUuid((TerminalToken) token.getChildren().get(0), input);
			Statement st = transaction.getPersistenceManager().getStatement(transaction, uuid);
			if (st == null)
				throw new TermParserException("Statement not found with UUID: " + uuid, token.getChildren().get(0).getStartLocation(),
						token.getChildren().get(0).getStopLocation(), input);
			if (!(st instanceof Context))
				throw new TermParserException("Statement: " + "\"" + st.label() + "\"" + " not a context", token.getChildren().get(0).getStartLocation(),
						token.getChildren().get(0).getStopLocation(), input);
			return processStatementReference_path((Context) st, transaction, referenceType, (NonTerminalToken) token.getChildren().get(2), input);
		}
		else if (token.getProduction().getRight().size() == 1)
		{
			return processStatementReference_path(context, transaction, referenceType, (NonTerminalToken) token.getChildren().get(0), input);
		}
		else
			throw new Error();
	}

	private Term processStatementReference_path(Context context, Transaction transaction, ReferenceType referenceType, NonTerminalToken token, String input)
			throws TermParserException
	{
		if (token.getProduction().getRight().size() == 3)
		{
			Statement st = processStatementReference(context, transaction, (NonTerminalToken) token.getChildren().get(0), input);
			if (!(st instanceof Context))
				throw new TermParserException("Statement: " + "\"" + st.label() + "\"" + " not a context", token.getChildren().get(0).getStartLocation(),
						token.getChildren().get(0).getStopLocation(), input);
			return processStatementReference_path((Context) st, transaction, referenceType, (NonTerminalToken) token.getChildren().get(2), input);
		}
		else if (token.getProduction().getRight().size() == 2)
			return processStatementReference_path(null, transaction, referenceType, (NonTerminalToken) token.getChildren().get(1), input);
		else if (token.getProduction().getRight().size() == 1)
			return processStatementReference_terminal(context, transaction, referenceType, (NonTerminalToken) token.getChildren().get(0), input);
		else
			throw new Error();
	}

	private UUID processUuid(TerminalToken token, String input) throws TermParserException
	{
		try
		{
			return UUID.fromString(((TaggedTerminalToken) token).getText());
		}
		catch (IllegalArgumentException e)
		{
			throw new TermParserException("Bad UUID string", token.getStartLocation(), token.getStopLocation(), input);
		}
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
