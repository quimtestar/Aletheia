/*******************************************************************************
 * Copyright (c) 2018, 2020 Quim Testar
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
package aletheia.gui.app;

import org.apache.logging.log4j.Logger;

import aletheia.gui.app.SimpleAletheiaJFrame.Panel;
import aletheia.log4j.LoggerManager;
import aletheia.utilities.CommandLineArguments;
import aletheia.utilities.CommandLineArguments.Option;
import aletheia.utilities.CommandLineArguments.Switch;

public class SimpleAletheiaGUI extends AletheiaGUI
{
	private final static Logger logger = LoggerManager.instance.logger();

	private final Panel panel;
	private final boolean framed;

	public SimpleAletheiaGUI(CommandLineArguments cla)
	{
		super(cla.getGlobalSwitches());
		Panel panel_ = Panel.TABBED;
		Switch sw = cla.getGlobalSwitches().get("panel");
		if (sw instanceof Option)
			try
			{
				panel_ = Enum.valueOf(Panel.class, ((Option) sw).getValue());
			}
			catch (NullPointerException e)
			{
			}
			catch (IllegalArgumentException e)
			{
				logger.warn("Bad panel option in command line. Using '" + panel_.name() + "'.");
			}
		this.panel = panel_;
		this.framed = cla.getGlobalSwitches().containsKey("framed");
	}

	public Panel getPanel()
	{
		return panel;
	}

	public boolean isFramed()
	{
		return framed;
	}

	@Override
	protected void run()
	{
		super.run();
		try
		{
			SimpleAletheiaJFrame aletheiaJFrame = new SimpleAletheiaJFrame(this);
			Runtime.getRuntime().addShutdownHook(new Thread("ShutdownHook")
			{

				@Override
				public void run()
				{
					aletheiaJFrame.exit();
				}

			});
			aletheiaJFrame.pack();
			aletheiaJFrame.setVisible(true);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	public static void main(String[] args)
	{
		try
		{
			new SimpleAletheiaGUI(new CommandLineArguments(args)).run();
		}
		catch (Exception e)
		{
			logger.fatal("Startup error", e);
			System.exit(1);
		}
	}

}
