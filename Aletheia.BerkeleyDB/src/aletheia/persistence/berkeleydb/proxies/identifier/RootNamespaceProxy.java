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
package aletheia.persistence.berkeleydb.proxies.identifier;

import aletheia.model.identifier.RootNamespace;

import com.sleepycat.persist.model.Persistent;

@Persistent(proxyFor = RootNamespace.class, version = 0)
public class RootNamespaceProxy extends NamespaceProxy<RootNamespace>
{

	@Override
	public void initializeProxy(RootNamespace rootNamespace)
	{
		super.initializeProxy(rootNamespace);
	}

	@Override
	public RootNamespace convertProxy()
	{
		return RootNamespace.instance;
	}

}
