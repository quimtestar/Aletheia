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
 * An inverse bijection from another one. Simply a swap of the
 * {@link #forward(Object)} and {@link #backward(Object)} methods.
 * 
 * @param <I>
 *            The input type of the inverted bijection (so, the output type of
 *            the original bijection).
 * @param <O>
 *            The output type of the inverted bijection (so, the input type of
 *            the original bijection).
 */
public class InverseBijection<I, O> implements Bijection<I, O>
{
	private final Bijection<O, I> bijection;

	public InverseBijection(Bijection<O, I> bijection)
	{
		this.bijection = bijection;
	}

	@Override
	public O forward(I input)
	{
		return bijection.backward(input);
	}

	@Override
	public I backward(O output)
	{
		return bijection.forward(output);
	}

}
