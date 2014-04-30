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
package aletheia.utilities.collections;

/**
 * A bijection between two types. A bijection is a function giving an exact
 * pairing of the instances of the two types. Implementations must provide the
 * functions that define that pairing in both directions as methods. One
 * function must be the inverse of the other; in other words
 * 
 * <pre>
 * for all x is an I, backward(forward x)=x 
 * and
 * for all y is an O, forward(backward y)=y
 * </pre>
 * 
 * @param <I>
 *            The input type.
 * @param <O>
 *            The output type.
 * 
 * @author Quim Testar
 */
public interface Bijection<I, O>
{
	/**
	 * The forward function of the bijection. Must be the inverse function of
	 * {@link #backward(Object)}
	 * 
	 * @param input
	 *            The input of the bijection.
	 * @return The computed output.
	 */
	O forward(I input);

	/**
	 * The backward function of the bijection. Must be the inverse function of
	 * {@link #forward(Object)}
	 * 
	 * @param output
	 *            The output of the bijection
	 * @return The computed input.
	 */
	I backward(O output);

};
