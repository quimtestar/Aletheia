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
package aletheia.peertopeer.network.message;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.peertopeer.base.message.MessageCode;
import aletheia.peertopeer.base.message.MessageSubProtocolInfo;
import aletheia.peertopeer.base.message.NonPersistentMessage;
import aletheia.peertopeer.network.ResourceTreeNodeSet;
import aletheia.peertopeer.resource.Resource;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.collection.ListProtocol;
import aletheia.protocol.collection.MapProtocol;

@MessageSubProtocolInfo(subProtocolClass = ResourceTreeNodeMessage.SubProtocol.class)
public class ResourceTreeNodeMessage extends NonPersistentMessage
{
	@SuppressWarnings("unused")
	private final static Logger logger = LoggerManager.logger();

	private final Map<Resource, List<ResourceTreeNodeSet.Action>> actionMap;

	public ResourceTreeNodeMessage(Map<Resource, List<ResourceTreeNodeSet.Action>> actionMap)
	{
		this.actionMap = actionMap;
	}

	public Map<Resource, List<ResourceTreeNodeSet.Action>> getActionMap()
	{
		return actionMap;
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends NonPersistentMessage.SubProtocol<ResourceTreeNodeMessage>
	{
		private final MapProtocol<Resource, List<ResourceTreeNodeSet.Action>> actionMapProtocol;

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(0, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
			this.actionMapProtocol = new MapProtocol<Resource, List<ResourceTreeNodeSet.Action>>(0, new Resource.Protocol(0), new ListProtocol<>(0,
					new ResourceTreeNodeSet.Action.Protocol(0)));
		}

		@Override
		public void send(DataOutput out, ResourceTreeNodeMessage m) throws IOException
		{
			actionMapProtocol.send(out, m.getActionMap());
		}

		@Override
		public ResourceTreeNodeMessage recv(DataInput in) throws IOException, ProtocolException
		{
			Map<Resource, List<ResourceTreeNodeSet.Action>> actionMap = actionMapProtocol.recv(in);
			return new ResourceTreeNodeMessage(actionMap);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			actionMapProtocol.skip(in);
		}
	}

}
