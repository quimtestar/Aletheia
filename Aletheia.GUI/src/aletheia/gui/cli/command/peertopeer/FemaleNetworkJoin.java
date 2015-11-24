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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;

import aletheia.gui.cli.CliJPanel;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.peertopeer.FemalePeerToPeerNode;
import aletheia.peertopeer.PeerToPeerNode;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "fnj", groupPath = "/p2p", factory = FemaleNetworkJoin.Factory.class)
public class FemaleNetworkJoin extends PeerToPeerCommand
{
	private final InetSocketAddress address;

	public FemaleNetworkJoin(CommandSource from, InetSocketAddress address)
	{
		super(from);
		this.address = address;
	}

	@Override
	public void run() throws Exception
	{
		PeerToPeerNode peerToPeerNode = getPeerToPeerNode();
		if (!(peerToPeerNode instanceof FemalePeerToPeerNode))
			throw new Exception("Not a female P2P node");
		FemalePeerToPeerNode femalePeerToPeerNode = (FemalePeerToPeerNode) peerToPeerNode;
		boolean joined = femalePeerToPeerNode.networkJoin(address);
		if (joined)
			getOut().println("Joined to network");
		else
			getOut().println("Not joined to network");
	}

	public static class Factory extends AbstractVoidCommandFactory<FemaleNetworkJoin>
	{

		@Override
		protected int minParameters()
		{
			return 2;
		}

		@Override
		public FemaleNetworkJoin parse(CliJPanel cliJPanel, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			try
			{
				InetAddress inetAddress = InetAddress.getByName(split.get(0));
				int port = Integer.parseInt(split.get(1));
				InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, port);
				return new FemaleNetworkJoin(cliJPanel, inetSocketAddress);
			}
			catch (UnknownHostException | NumberFormatException e)
			{
				throw new CommandParseException(e);
			}
		}

		@Override
		protected String paramSpec()
		{
			return "<host> <port>";
		}

		@Override
		public String shortHelp()
		{
			return "Join the female P2P node the network by the given hook.";
		}

	}

}
