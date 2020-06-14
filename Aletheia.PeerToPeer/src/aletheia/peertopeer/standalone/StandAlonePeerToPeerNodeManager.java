/*******************************************************************************
 * Copyright (c) 2014, 2016 Quim Testar.
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
package aletheia.peertopeer.standalone;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.peertopeer.FemalePeerToPeerNode;
import aletheia.peertopeer.FemalePeerToPeerNode.ExternalServerSocketManagerException;
import aletheia.peertopeer.MalePeerToPeerNode;
import aletheia.peertopeer.PeerToPeerNode;
import aletheia.peertopeer.PeerToPeerNode.ConnectException;
import aletheia.peertopeer.PeerToPeerNode.InternalServerSocketManagerException;
import aletheia.peertopeer.PeerToPeerNodeProperties;
import aletheia.peertopeer.standalone.StandAlonePeerToPeerNodeSubscriptions.ConfigurationException;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.utilities.CommandLineArguments;
import aletheia.version.VersionManager;

public class StandAlonePeerToPeerNodeManager
{
	private final static Logger logger = LoggerManager.instance.logger();
	private final static PeerToPeerNodeProperties properties = PeerToPeerNodeProperties.instance;

	private final PersistenceManager persistenceManager;
	private final PeerToPeerNode peerToPeerNode;
	@SuppressWarnings("unused")
	private final StandAlonePeerToPeerNodeSubscriptions standAlonePeerToPeerNodeSubscriptions;

	private StandAlonePeerToPeerNodeManager() throws ExternalServerSocketManagerException, ConnectException, IOException, InterruptedException,
			ConfigurationException, InternalServerSocketManagerException
	{
		this.persistenceManager = new BerkeleyDBPersistenceManager(makePersistenceManagerConfiguration());
		switch (getStandaloneGender())
		{
		case FEMALE:
		{
			this.peerToPeerNode = new FemalePeerToPeerNode(persistenceManager, getStandaloneFemaleExternalAddress());
			InetSocketAddress hookAddress = getStandaloneFemaleHookAddress();
			if (hookAddress != null)
				this.peerToPeerNode.networkJoin(hookAddress);
			break;
		}
		case MALE:
		{
			this.peerToPeerNode = new MalePeerToPeerNode(persistenceManager, getStandaloneMaleFemaleAddress());
			break;
		}
		default:
			throw new ConfigurationException();
		}
		InetSocketAddress internalAddress = getStandaloneInternalAddress();
		if (internalAddress != null)
			this.peerToPeerNode.setInternalBindSocketAddress(internalAddress);
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					peerToPeerNode.shutdown(true);
					persistenceManager.close();
				}
				catch (Exception e)
				{
					logger.error("Server shutdown error", e);
					e.printStackTrace();
					System.exit(1);
				}
			}
		});
		this.standAlonePeerToPeerNodeSubscriptions = new StandAlonePeerToPeerNodeSubscriptions(persistenceManager, peerToPeerNode);

	}

	private File getDbFile()
	{
		return properties.getStandaloneDbFile();
	}

	private boolean isAllowCreate()
	{
		return properties.isStandaloneAllowCreate();
	}

	private boolean isReadOnly()
	{
		return properties.isStandaloneReadOnly();
	}

	private boolean isAllowUpgrade()
	{
		return properties.isStandaloneAllowUpgrade();
	}

	private int getCachePercent()
	{
		return properties.getCachePercent();
	}

	private PeerToPeerNodeProperties.Gender getStandaloneGender()
	{
		return properties.getStandaloneGender();
	}

	private InetSocketAddress getStandaloneFemaleExternalAddress() throws UnknownHostException
	{
		return properties.getStandaloneFemaleExternalAddress();
	}

	private InetSocketAddress getStandaloneInternalAddress() throws UnknownHostException
	{
		return properties.getStandaloneInternalAddress();
	}

	private InetSocketAddress getStandaloneFemaleHookAddress() throws UnknownHostException
	{
		return properties.getStandaloneFemaleHookAddress();
	}

	private InetSocketAddress getStandaloneMaleFemaleAddress() throws UnknownHostException
	{
		return properties.getStandaloneMaleFemaleAddress();
	}

	private BerkeleyDBPersistenceManager.Configuration makePersistenceManagerConfiguration()
	{
		BerkeleyDBPersistenceManager.Configuration configuration = new BerkeleyDBPersistenceManager.Configuration();
		configuration.setDbFile(getDbFile());
		configuration.setAllowCreate(isAllowCreate());
		configuration.setReadOnly(isReadOnly());
		configuration.setAllowUpgrade(isAllowUpgrade());
		configuration.setCachePercent(getCachePercent());
		return configuration;
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws ConfigurationException
	 * @throws ConnectException
	 * @throws InterruptedException
	 */
	public static void main(String[] args)
	{
		try
		{
			CommandLineArguments cla = new CommandLineArguments(args);
			if (cla.getGlobalSwitches().containsKey("version"))
				System.out.println(VersionManager.getVersion());
			else
			{
				LoggerManager.instance.setUncaughtExceptionHandler();
				new StandAlonePeerToPeerNodeManager();
			}
		}
		catch (Exception e)
		{
			logger.fatal(e.getMessage(), e);
			e.printStackTrace();
			System.exit(1);
		}
	}

}
