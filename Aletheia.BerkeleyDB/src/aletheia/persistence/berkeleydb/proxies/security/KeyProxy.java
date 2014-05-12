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
package aletheia.persistence.berkeleydb.proxies.security;

import java.security.Key;

import aletheia.persistence.berkeleydb.proxies.AletheiaPersistentProxy;
import aletheia.utilities.Capsule;

import com.sleepycat.persist.model.Persistent;

@Persistent(version = 0)
public abstract class KeyProxy<K extends Key> implements AletheiaPersistentProxy<Capsule<K>>
{
	protected String format;
	protected String algorithm;
	protected byte[] encoded;

	@Override
	public void initializeProxy(Capsule<K> capsule)
	{
		this.format = capsule.getObject().getFormat();
		this.algorithm = capsule.getObject().getAlgorithm();
		this.encoded = capsule.getObject().getEncoded();
	}

}
