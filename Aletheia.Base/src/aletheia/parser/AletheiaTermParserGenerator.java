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

import java.io.FileNotFoundException;
import java.io.IOException;

import aletheia.parsergenerator.ParserLexerException;
import aletheia.parsergenerator.parser.TransitionTable.Conflict;
import aletheia.parsergenerator.parser.TransitionTable.ConflictException;

/**
 * Executable class used to pre-generate the files needed by the parser and the
 * lexer.
 */
public class AletheiaTermParserGenerator
{

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
			AletheiaTermParser.generate();
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
