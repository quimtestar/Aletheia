/*******************************************************************************
 * Copyright (c) 2015 Quim Testar.
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
package aletheia.utilities;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import aletheia.utilities.MiscUtilities.NoConstructorException;

public class ObjectConstructor<C>
{
	private final Constructor<C> constructor;
	private final Object[] args;

	public ObjectConstructor(Class<C> clazz, Object... args) throws NoConstructorException
	{
		this.constructor = MiscUtilities.matchingConstructor(clazz, args);
		if (this.constructor == null)
			throw new NoConstructorException();
		this.constructor.setAccessible(true);
		this.args = args;
	}

	public C construct() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		return constructor.newInstance(args);
	}

}
