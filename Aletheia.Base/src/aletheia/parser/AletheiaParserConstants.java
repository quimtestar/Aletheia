/*******************************************************************************
 * Copyright (c) 2017, 2020 Quim Testar
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

public final class AletheiaParserConstants
{
	public final static String lexerPath = "/aletheia/parser/aletheia.lex";
	public final static String automatonSetPath = "/aletheia/parser/aletheia.ast";

	public final static String termGrammarPath = "/aletheia/parser/term/term.gra";
	public final static String termTransitionTablePath = "/aletheia/parser/term/term.ttb";

	public final static String parameterIdentificationGrammarPath = "/aletheia/parser/parameteridentification/parameterIdentification.gra";
	public final static String parameterIdentificationTransitionTablePath = "/aletheia/parser/parameteridentification/parameterIdentification.ttb";

	private AletheiaParserConstants()
	{

	}

}
