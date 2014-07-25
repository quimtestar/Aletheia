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
package aletheia.gui.cli.command.peertopeer;

import aletheia.gui.cli.CliJPanel;
import aletheia.gui.cli.command.Command;
import aletheia.peertopeer.PeerToPeerNode;

public abstract class PeerToPeerCommand extends Command
{

	protected PeerToPeerCommand(CliJPanel from)
	{
		super(from);
	}

	public abstract class PeerToPeerCommandException extends CommandException
	{
		private static final long serialVersionUID = -3677262339848337525L;

		public PeerToPeerCommandException()
		{
			super();
		}

		public PeerToPeerCommandException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public PeerToPeerCommandException(String message)
		{
			super(message);
		}

		public PeerToPeerCommandException(Throwable cause)
		{
			super(cause);
		}
	}

	public class PeerToPeerNotStartedException extends PeerToPeerCommandException
	{
		private static final long serialVersionUID = -5509028093118788832L;

		public PeerToPeerNotStartedException()
		{
			super("P2P node not started");
		}
	}

	protected PeerToPeerNode getPeerToPeerNode() throws PeerToPeerNotStartedException
	{
		PeerToPeerNode peerToPeerNode = getFrom().getAletheiaJPanel().getAletheiaJFrame().getPeerToPeerNode();
		if (peerToPeerNode == null)
			throw new PeerToPeerNotStartedException();
		return peerToPeerNode;
	}

}
