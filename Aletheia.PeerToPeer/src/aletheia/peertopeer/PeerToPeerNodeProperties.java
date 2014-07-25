/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
 *
 * This file is part of the Aletheia Proof Assistant.
 *
 * The Aletheia Proof Assistant is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The Aletheia Proof Assistant is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with the Aletheia Proof Assistant.
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package aletheia.peertopeer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Properties;

public class PeerToPeerNodeProperties extends Properties
{
	private static final long serialVersionUID = -5327860583875855798L;
	private static final String propertiesFileName_Default = "aletheia.p2pnode.properties";
	private static final String propertiesFileName;
	static
	{
		String s = System.getProperty("aletheia.p2pnode.properties.file");
		propertiesFileName = s == null ? propertiesFileName_Default : s;
	}

	private static final String debug = "aletheia.p2pnode.debug";
	private static final String standalone_dbfile_name = "aletheia.p2pnode.standalone.dbfile_name";
	private static final String standalone_allow_create = "aletheia.p2pnode.standalone.allow_create";
	private static final String standalone_read_only = "aletheia.p2pnode.standalone.read_only";
	private static final String standalone_allow_upgrade = "aletheia.p2pnode.standalone.allow_upgrade";
	private static final String standalone_cache_percent = "aletheia.p2pnode.standalone.cache_percent";
	private static final String standalone_gender = "aletheia.p2pnode.standalone.gender";

	private static final String standalone_female_external_host = "aletheia.p2pnode.standalone.female.external.host";
	private static final String standalone_female_external_port = "aletheia.p2pnode.standalone.female.external.port";
	private static final String standalone_female_hook_host = "aletheia.p2pnode.standalone.female.hook.host";
	private static final String standalone_female_hook_port = "aletheia.p2pnode.standalone.female.hook.port";

	private static final String standalone_male_female_host = "aletheia.p2pnode.standalone.male.female.host";
	private static final String standalone_male_female_port = "aletheia.p2pnode.standalone.male.female.port";

	private static final String standalone_internal_host = "aletheia.p2pnode.standalone.internal.host";
	private static final String standalone_internal_port = "aletheia.p2pnode.standalone.internal.port";

	private static final String standalone_subscriptions = "aletheia.p2pnode.standalone.subscriptions";

	private static final Properties defaults;
	static
	{
		defaults = new Properties();
		defaults.setProperty(debug, Boolean.toString(false));
		defaults.setProperty(standalone_allow_create, Boolean.toString(false));
		defaults.setProperty(standalone_read_only, Boolean.toString(true));
		defaults.setProperty(standalone_allow_upgrade, Boolean.toString(false));
		defaults.setProperty(standalone_cache_percent, Integer.toString(0));
	}

	public static final PeerToPeerNodeProperties instance = new PeerToPeerNodeProperties();

	private PeerToPeerNodeProperties()
	{
		super(defaults);
		try
		{
			InputStream is = new FileInputStream(propertiesFileName);
			try
			{
				load(is);
			}
			finally
			{
				is.close();
			}
		}
		catch (IOException e)
		{
		}
	}

	public boolean isDebug()
	{
		return Boolean.parseBoolean(getProperty(debug));
	}

	public File getStandaloneDbFile()
	{
		return new File(getProperty(standalone_dbfile_name));
	}

	public boolean isStandaloneAllowCreate()
	{
		return Boolean.parseBoolean(getProperty(standalone_allow_create));
	}

	public boolean isStandaloneReadOnly()
	{
		return Boolean.parseBoolean(getProperty(standalone_read_only));
	}

	public boolean isStandaloneAllowUpgrade()
	{
		return Boolean.parseBoolean(getProperty(standalone_allow_upgrade));
	}

	public int getCachePercent()
	{
		try
		{
			return Integer.parseInt(getProperty(standalone_cache_percent));
		}
		catch (NumberFormatException e)
		{
			return 0;
		}
	}

	public enum Gender
	{
		FEMALE, MALE;
	}

	public Gender getStandaloneGender()
	{
		try
		{
			return Enum.valueOf(Gender.class, getProperty(standalone_gender).toUpperCase());
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
	}

	public InetAddress getStandaloneFemaleExternalHost() throws UnknownHostException
	{
		String host = getProperty(standalone_female_external_host);
		if (host == null)
			return null;
		return InetAddress.getByName(host);
	}

	public int getStandaloneFemaleExternalPort()
	{
		try
		{
			return Integer.parseInt(getProperty(standalone_female_external_port));
		}
		catch (NumberFormatException e)
		{
			return -1;
		}
	}

	public InetSocketAddress getStandaloneFemaleExternalAddress() throws UnknownHostException
	{
		return new InetSocketAddress(getStandaloneFemaleExternalHost(), getStandaloneFemaleExternalPort());
	}

	public InetAddress getStandaloneFemaleHookHost() throws UnknownHostException
	{
		String host = getProperty(standalone_female_hook_host);
		if (host == null)
			return null;
		return InetAddress.getByName(host);
	}

	public int getStandaloneFemaleHookPort()
	{
		try
		{
			return Integer.parseInt(getProperty(standalone_female_hook_port));
		}
		catch (NumberFormatException e)
		{
			return -1;
		}
	}

	public InetSocketAddress getStandaloneFemaleHookAddress() throws UnknownHostException
	{
		InetAddress hostAddress = getStandaloneFemaleHookHost();
		if (hostAddress == null)
			return null;
		return new InetSocketAddress(hostAddress, getStandaloneFemaleHookPort());
	}

	public InetAddress getStandaloneMaleFemaleHost() throws UnknownHostException
	{
		String host = getProperty(standalone_male_female_host);
		if (host == null)
			return null;
		return InetAddress.getByName(host);
	}

	public int getStandaloneMaleFemalePort()
	{
		try
		{
			return Integer.parseInt(getProperty(standalone_male_female_port));
		}
		catch (NumberFormatException e)
		{
			return -1;
		}
	}

	public InetSocketAddress getStandaloneMaleFemaleAddress() throws UnknownHostException
	{
		return new InetSocketAddress(getStandaloneMaleFemaleHost(), getStandaloneMaleFemalePort());
	}

	public InetAddress getStandaloneInternalHost() throws UnknownHostException
	{
		String host = getProperty(standalone_internal_host);
		if (host == null)
			return null;
		return InetAddress.getByName(host);
	}

	public int getStandaloneInternalPort()
	{
		try
		{
			return Integer.parseInt(getProperty(standalone_internal_port));
		}
		catch (NumberFormatException e)
		{
			return -1;
		}
	}

	public InetSocketAddress getStandaloneInternalAddress() throws UnknownHostException
	{
		InetAddress hostAddress = getStandaloneInternalHost();
		return new InetSocketAddress(hostAddress, getStandaloneInternalPort());
	}

	public String getStandaloneSubscriptions()
	{
		return getProperty(standalone_subscriptions);
	}

}
