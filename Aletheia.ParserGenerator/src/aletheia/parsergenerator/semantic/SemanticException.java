/*******************************************************************************
 * Copyright (c) 2018 Quim Testar
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
package aletheia.parsergenerator.semantic;

import java.util.List;

import aletheia.parsergenerator.ParserBaseException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.Token;

public class SemanticException extends ParserBaseException
{
	private static final long serialVersionUID = -292833502687472137L;

	public SemanticException(List<Token<? extends Symbol>> antecedents, List<Token<? extends Symbol>> reducees, String message, Throwable cause)
	{
		super(Token.locationPairFromAntecedentsReducees(antecedents, reducees), message, cause);
	}

	public SemanticException(List<Token<? extends Symbol>> antecedents, List<Token<? extends Symbol>> reducees, String message)
	{
		super(Token.locationPairFromAntecedentsReducees(antecedents, reducees), message);
	}

	public SemanticException(List<Token<? extends Symbol>> antecedents, List<Token<? extends Symbol>> reducees, Throwable cause)
	{
		super(Token.locationPairFromAntecedentsReducees(antecedents, reducees), cause);
	}

	public SemanticException(List<Token<? extends Symbol>> antecedents, List<Token<? extends Symbol>> reducees)
	{
		super(Token.locationPairFromAntecedentsReducees(antecedents, reducees));
	}

	public SemanticException(List<Token<? extends Symbol>> reducees, String message, Throwable cause)
	{
		super(Token.locationPairFromAntecedentsReducees(null, reducees), message, cause);
	}

	public SemanticException(List<Token<? extends Symbol>> reducees, String message)
	{
		super(Token.locationPairFromAntecedentsReducees(null, reducees), message);
	}

	public SemanticException(List<Token<? extends Symbol>> reducees, Throwable cause)
	{
		super(Token.locationPairFromAntecedentsReducees(null, reducees), cause);
	}

	public SemanticException(List<Token<? extends Symbol>> reducees)
	{
		super(Token.locationPairFromAntecedentsReducees(null, reducees));
	}

	public SemanticException(Token<? extends Symbol> token, String message, Throwable cause)
	{
		super(token.getLocationInterval(), message, cause);
	}

	public SemanticException(Token<? extends Symbol> token, String message)
	{
		super(token.getLocationInterval(), message);
	}

	public SemanticException(Token<? extends Symbol> token, Throwable cause)
	{
		super(token.getLocationInterval(), cause);
	}

	public SemanticException(Token<? extends Symbol> token)
	{
		super(token.getLocationInterval());
	}

}
