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
package aletheia.peertopeer.base.message;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.enumerate.ExportableEnum;
import aletheia.protocol.enumerate.ExportableEnumProtocol;

public abstract class SymmetricSelectionMessage<C, E extends ExportableEnum<C, ?>> extends NonPersistentMessage
{
	private final E selection;

	public SymmetricSelectionMessage(E selection)
	{
		this.selection = selection;
	}

	public E getSelection()
	{
		return selection;
	}

	@ProtocolInfo(availableVersions = 0)
	public static abstract class SubProtocol<C, E extends ExportableEnum<C, ?>, M extends SymmetricSelectionMessage<C, E>>
			extends NonPersistentMessage.SubProtocol<M>
	{
		protected final ExportableEnumProtocol<C, E> selectionProtocol;

		public SubProtocol(int requiredVersion, MessageCode messageCode, ExportableEnumProtocol<C, E> selectionProtocol)
		{
			super(0, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
			this.selectionProtocol = selectionProtocol;
		}

		@Override
		public void send(DataOutput out, M m) throws IOException
		{
			selectionProtocol.send(out, m.getSelection());
		}

		protected abstract M makeMessage(List<Object> args);

		protected void recvArgs(List<Object> args, DataInput in) throws IOException, ProtocolException
		{
			args.add(selectionProtocol.recv(in));
		}

		@Override
		public M recv(DataInput in) throws IOException, ProtocolException
		{
			List<Object> args = new ArrayList<>();
			recvArgs(args, in);
			return makeMessage(args);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			selectionProtocol.skip(in);
		}

	}

}
