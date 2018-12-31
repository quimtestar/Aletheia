/*******************************************************************************
 * Copyright (c) 2018 Quim Testar
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

import aletheia.log4j.LoggerManager;

public class SimpleAletheiaGUI extends AletheiaGUI
{
	private final static Logger logger = LoggerManager.instance.logger();

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
		new SimpleAletheiaGUI().run();
	}

}
