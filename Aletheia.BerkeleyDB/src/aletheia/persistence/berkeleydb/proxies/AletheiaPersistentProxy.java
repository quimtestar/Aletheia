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
package aletheia.persistence.berkeleydb.proxies;

import aletheia.persistence.berkeleydb.exceptions.BerkeleyDBPersistenceException;

import com.sleepycat.persist.model.PersistentProxy;

public interface AletheiaPersistentProxy<T> extends PersistentProxy<T>
{
	public class ProxyConversionException extends BerkeleyDBPersistenceException
	{
		private static final long serialVersionUID = -1290911517588806409L;

		public ProxyConversionException()
		{
			super();
		}

		public ProxyConversionException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public ProxyConversionException(String message)
		{
			super(message);
		}

		public ProxyConversionException(Throwable cause)
		{
			super(cause);
		}
	}

	@Override
	T convertProxy() throws ProxyConversionException;
}
