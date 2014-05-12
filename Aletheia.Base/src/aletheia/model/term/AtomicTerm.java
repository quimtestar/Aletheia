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
package aletheia.model.term;

import java.util.Deque;
import java.util.Set;

/**
 * An atomic term is a term which is neither a function or a composition. In
 * some sense, it cannot be 'divided' in smaller terms.
 */
public abstract class AtomicTerm extends SimpleTerm
{
	private static final long serialVersionUID = -4939621804232476823L;

	public AtomicTerm(Term type)
	{
		super(type);
	}

	/**
	 * An atomic term has length one.
	 */
	@Override
	public int length()
	{
		return 1;
	}

	@Override
	protected abstract Term replace(Deque<Replace> replaces, Set<VariableTerm> exclude) throws ReplaceTypeException;

	@Override
	public abstract Term unproject() throws UnprojectException;

}
