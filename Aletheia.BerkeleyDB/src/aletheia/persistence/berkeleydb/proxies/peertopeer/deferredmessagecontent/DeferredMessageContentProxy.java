/*******************************************************************************
 * Copyright (c) 2014, 2020 Quim Testar.
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
package aletheia.persistence.berkeleydb.proxies.peertopeer.deferredmessagecontent;

import com.sleepycat.persist.model.Persistent;

import aletheia.model.peertopeer.deferredmessagecontent.DeferredMessageContent;
import aletheia.persistence.berkeleydb.proxies.AletheiaPersistentProxy;

@Persistent(proxyFor = DeferredMessageContent.class)
public abstract class DeferredMessageContentProxy<C extends DeferredMessageContent> implements AletheiaPersistentProxy<C>
{

	@Override
	public void initializeProxy(C deferredMessageContent)
	{
	}

}
