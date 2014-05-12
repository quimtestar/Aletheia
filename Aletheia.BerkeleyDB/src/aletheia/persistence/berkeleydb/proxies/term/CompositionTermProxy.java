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
package aletheia.persistence.berkeleydb.proxies.term;

import aletheia.model.term.CompositionTerm;
import aletheia.model.term.CompositionTerm.CompositionTypeException;
import aletheia.model.term.SimpleTerm;
import aletheia.model.term.Term;

import com.sleepycat.persist.model.Persistent;

@Persistent(proxyFor = CompositionTerm.class, version = 0)
public class CompositionTermProxy extends SimpleTermProxy<CompositionTerm>
{
	private SimpleTerm head;
	private Term tail;

	protected SimpleTerm getHead()
	{
		return head;
	}

	protected Term getTail()
	{
		return tail;
	}

	@Override
	public void initializeProxy(CompositionTerm compositionTerm)
	{
		super.initializeProxy(compositionTerm);
		head = compositionTerm.getHead();
		tail = compositionTerm.getTail();
	}

	@Override
	public CompositionTerm convertProxy()
	{
		try
		{
			return new CompositionTerm(head, tail);
		}
		catch (CompositionTypeException e)
		{
			throw new ProxyConversionException(e);
		}
	}

}
