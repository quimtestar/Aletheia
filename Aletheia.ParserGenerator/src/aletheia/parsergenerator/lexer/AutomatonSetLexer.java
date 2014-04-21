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

import java.io.Reader;

import aletheia.parsergenerator.symbols.TaggedTerminalSymbol;
import aletheia.parsergenerator.tokens.EndToken;
import aletheia.parsergenerator.tokens.Location;
import aletheia.parsergenerator.tokens.TaggedTerminalToken;
import aletheia.parsergenerator.tokens.TerminalToken;

/**
 * A lexer based on an {@link AutomatonSet} recognizer.
 */
public class AutomatonSetLexer extends AbstractLexer
{

	private final AutomatonSet automatonSet;

	/**
	 * Creates the lexer given the automaton set and the input reader.
	 * 
	 * @param automatonSet
	 *            The automaton set.
	 * @param reader
	 *            The reader.
	 * @throws LexerException
	 */
	public AutomatonSetLexer(AutomatonSet automatonSet, Reader reader) throws LexerException
	{
		super(reader);
		this.automatonSet = automatonSet;
	}

	public class NoMatchException extends LexerException
	{
		private static final long serialVersionUID = -7612231050734841106L;

		private final String text;

		public NoMatchException(Location startLocation, Location stopLocation, String text)
		{
			super(startLocation, stopLocation);
			this.text = text;
		}

		@Override
		public String getMessage()
		{
			return super.getMessage() + ": No match for input: " + text;
		}

	}

	@Override
	public TerminalToken readToken() throws LexerException
	{
		Location location = getLocation();
		try
		{
			return super.readToken();
		}
		catch (UnrecognizedInputException e)
		{
			do
			{
				if (isAtEnd())
					return new EndToken(location);
				AutomatonSetState state = new AutomatonSetState(automatonSet);
				while (!isAtEnd() && !state.atEnd())
				{
					state.next(getNext());
					if (!state.atEnd())
						eat();
				}
				if (isAtEnd())
					state.choose();
				if (!state.isIgnoreInput())
				{
					if (state.getChosen() == null)
					{
						String txt = state.getText();
						if (!isAtEnd())
							txt = txt + getNext();
						eat();
						throw new NoMatchException(location, getLocation(), txt);
					}
					return new TaggedTerminalToken((TaggedTerminalSymbol) state.getChosen(), location, getLocation(), state.getText());
				}
				else
					location = getLocation();
			} while (true);
		}
	}

}
