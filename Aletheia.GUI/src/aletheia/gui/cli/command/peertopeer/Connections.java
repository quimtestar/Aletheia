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

import java.net.InetSocketAddress;
import java.util.List;

import aletheia.gui.cli.CliJPanel;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.peertopeer.PeerToPeerConnection;
import aletheia.peertopeer.PeerToPeerConnection.Gender;
import aletheia.peertopeer.base.SubRootPhaseType;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "connections", groupPath = "/p2p", factory = Connections.Factory.class)
public class Connections extends PeerToPeerCommand
{
	protected Connections(CliJPanel from)
	{
		super(from);
	}

	@Override
	public void run() throws Exception
	{
		for (PeerToPeerConnection connection : getPeerToPeerNode().connections())
		{
			InetSocketAddress remoteAddress = (InetSocketAddress) connection.getSocketChannel().getRemoteAddress();
			Gender gender = connection.getGender();
			SubRootPhaseType subRootPhaseType = connection.getRootPhase().getSubRootPhaseType();
			getOut().println(remoteAddress + "\t" + gender + "\t" + (subRootPhaseType != null ? subRootPhaseType : "[no type]"));
		}
	}

	public static class Factory extends AbstractVoidCommandFactory<Connections>
	{

		@Override
		protected int minParameters()
		{
			return 0;
		}

		@Override
		public Connections parse(CliJPanel cliJPanel, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			return new Connections(cliJPanel);
		}

		@Override
		protected String paramSpec()
		{
			return "";
		}

		@Override
		public String shortHelp()
		{
			return "List of P2P connections.";
		}

	}

}
