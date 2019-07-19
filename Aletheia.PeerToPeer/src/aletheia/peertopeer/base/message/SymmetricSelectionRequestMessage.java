/*******************************************************************************
 * Copyright (c) 2014, 2015 Quim Testar.
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
package aletheia.peertopeer.base.message;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.enumerate.ExportableEnum;
import aletheia.protocol.enumerate.ExportableEnumProtocol;
import aletheia.protocol.primitive.BooleanProtocol;

public abstract class SymmetricSelectionRequestMessage<C, E extends ExportableEnum<C, ?>> extends SymmetricSelectionMessage<C, E>
{
	private final boolean resolver;

	public SymmetricSelectionRequestMessage(E selection, boolean resolver)
	{
		super(selection);
		this.resolver = resolver;
	}

	public boolean isResolver()
	{
		return resolver;
	}

	@ProtocolInfo(availableVersions = 0)
	public static abstract class SubProtocol<C, E extends ExportableEnum<C, ?>, M extends SymmetricSelectionRequestMessage<C, E>>
			extends SymmetricSelectionMessage.SubProtocol<C, E, M>
	{
		private final BooleanProtocol booleanProtocol;

		public SubProtocol(int requiredVersion, MessageCode messageCode, ExportableEnumProtocol<C, E> selectionProtocol)
		{
			super(0, messageCode, selectionProtocol);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
			this.booleanProtocol = new BooleanProtocol(0);
		}

		@Override
		public void send(DataOutput out, M m) throws IOException
		{
			super.send(out, m);
			booleanProtocol.send(out, m.isResolver());
		}

		@Override
		protected void recvArgs(List<Object> args, DataInput in) throws IOException, ProtocolException
		{
			super.recvArgs(args, in);
			args.add(booleanProtocol.recv(in));
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			super.skip(in);
			booleanProtocol.skip(in);
		}

	}

}
