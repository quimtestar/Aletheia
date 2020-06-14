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
package aletheia.persistence.berkeleydb.proxies.net;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.sleepycat.persist.model.Persistent;

import aletheia.persistence.berkeleydb.proxies.AletheiaPersistentProxy;

@Persistent(version = 1, proxyFor = InetAddress.class)
public abstract class InetAddressProxy<T extends InetAddress> implements AletheiaPersistentProxy<T>
{
	private String hostName;
	private byte[] address;

	public InetAddressProxy()
	{
	}

	@Override
	public void initializeProxy(InetAddress inetAddress)
	{
		this.hostName = inetAddress.getHostName();
		this.address = inetAddress.getAddress();
	}

	@SuppressWarnings("unchecked")
	@Override
	public T convertProxy() throws ProxyConversionException
	{
		try
		{
			return (T) InetAddress.getByAddress(hostName, address);
		}
		catch (UnknownHostException | ClassCastException e)
		{
			throw new ProxyConversionException(e);
		}
	}

	@Persistent(version = 1, proxyFor = Inet4Address.class)
	public static class Inet4AddressProxy extends InetAddressProxy<Inet4Address>
	{

	}

	@Persistent(version = 1, proxyFor = Inet6Address.class)
	public static class Inet6AddressProxy extends InetAddressProxy<Inet6Address>
	{

	}

}
