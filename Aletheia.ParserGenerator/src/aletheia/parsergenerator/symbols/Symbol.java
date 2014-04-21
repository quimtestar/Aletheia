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
package aletheia.parsergenerator.symbols;

import java.io.Serializable;

import aletheia.parsergenerator.parser.Grammar;
import aletheia.parsergenerator.parser.Production;

/**
 * Any of the lexical elements used in the {@linkplain Production production
 * rules} of a {@linkplain Grammar grammar}.
 * 
 */
public abstract class Symbol implements Serializable
{
	private static final long serialVersionUID = 2694609152891736901L;

}
