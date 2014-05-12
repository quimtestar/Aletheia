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
package aletheia.model.peertopeer.deferredmessagecontent;

import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

import aletheia.protocol.Exportable;
import aletheia.protocol.ExportableProtocol;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.peertopeer.deferredmessagecontent.DeferredMessageContentCode;

public abstract class DeferredMessageContent implements Exportable, Serializable
{
	private static final long serialVersionUID = -1921263100296921627L;

	public DeferredMessageContent()
	{

	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return true;
	}

	@ProtocolInfo(availableVersions = 0)
	public static abstract class SubProtocol<C extends DeferredMessageContent> extends ExportableProtocol<C>
	{
		private final DeferredMessageContentCode code;

		public SubProtocol(int requiredVersion, DeferredMessageContentCode code)
		{
			super(0);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
			this.code = code;
		}

		protected DeferredMessageContentCode getCode()
		{
			return code;
		}

		@SuppressWarnings("unchecked")
		public void sendDeferredMessageContent(DataOutput out, DeferredMessageContent m) throws IOException
		{
			send(out, (C) m);
		}
	}
}
