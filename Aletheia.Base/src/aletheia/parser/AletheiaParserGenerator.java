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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import aletheia.parsergenerator.ParserLexerException;
import aletheia.parsergenerator.lexer.AutomatonSet;
import aletheia.parsergenerator.lexer.LexerLexer;
import aletheia.parsergenerator.lexer.LexerParser;
import aletheia.parsergenerator.parser.Grammar;
import aletheia.parsergenerator.parser.GrammarParser;
import aletheia.parsergenerator.parser.TransitionTable;
import aletheia.parsergenerator.parser.TransitionTableLalr1;
import aletheia.parsergenerator.parser.TransitionTable.Conflict;
import aletheia.parsergenerator.parser.TransitionTable.ConflictException;

/**
 * Executable class used to pre-generate the files needed by the parser and the
 * lexer.
 */
public class AletheiaParserGenerator
{

	private static AutomatonSet createAutomatonSet() throws ParserLexerException, IOException
	{
		Reader reader = new InputStreamReader(ClassLoader.getSystemResourceAsStream(AletheiaParserConstants.lexerPath));
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

	private static TransitionTable createTermTransitionTable() throws ConflictException, ParserLexerException, IOException
	{
		Reader reader = new InputStreamReader(ClassLoader.getSystemResourceAsStream(AletheiaParserConstants.termGrammarPath));
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

	private static TransitionTable createParameterIdentificationTransitionTable() throws ConflictException, ParserLexerException, IOException
	{
		Reader reader = new InputStreamReader(ClassLoader.getSystemResourceAsStream(AletheiaParserConstants.parameterIdentificationGrammarPath));
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

	private static void generate() throws ParserLexerException, IOException, ConflictException
	{
		AutomatonSet automatonSet = createAutomatonSet();
		File automatonSetFile = new File("src/" + AletheiaParserConstants.automatonSetPath);
		automatonSet.save(automatonSetFile);
		System.out.println("Aletheia lexer automaton saved to " + automatonSetFile);

		TransitionTable termTransitionTable = createTermTransitionTable();
		File termTransitionTableFile = new File("src/" + AletheiaParserConstants.termTransitionTablePath);
		termTransitionTable.save(termTransitionTableFile);
		System.out.println("Aletheia term parser transition table saved to " + termTransitionTableFile);

		TransitionTable parameterIdentificationTransitionTable = createParameterIdentificationTransitionTable();
		File parameterIdentificationTransitionTableFile = new File("src/" + AletheiaParserConstants.parameterIdentificationTransitionTablePath);
		parameterIdentificationTransitionTable.save(parameterIdentificationTransitionTableFile);
		System.out.println("Aletheia term parser transition table saved to " + parameterIdentificationTransitionTableFile);

	}

	/**
	 * The main method.
	 *
	 * @param args
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws ConflictException
	 * @throws ParserLexerException
	 */
	public static void main(String[] args) throws ParserLexerException, IOException, ConflictException
	{
		try
		{
			AletheiaParserGenerator.generate();
		}
		catch (ConflictException e)
		{
			for (Conflict conflict : e.getConflicts())
			{
				conflict.trace(System.err);
				System.err.println();
			}
			throw e;
		}
	}

}