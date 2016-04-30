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
package aletheia.gui.app;

import java.awt.BorderLayout;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import aletheia.gui.font.FontManager;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

public class VoidAletheiaContentPane extends AbstractAletheiaContentPane
{
	private static final long serialVersionUID = -7688744246450953102L;

	private JLabel labelInfo = new JLabel("Set a persistence folder on the preferences dialog", SwingConstants.CENTER);

	public VoidAletheiaContentPane()
	{
		super();
		this.setLayout(new BorderLayout());
		this.add(labelInfo, BorderLayout.CENTER);
		updateFontSize();
	}

	@Override
	public void close() throws Exception
	{
	}

	@Override
	public void updateFontSize()
	{
		labelInfo.setFont(FontManager.instance.defaultFont());
	}

	@Override
	public void lock(Collection<Transaction> owners)
	{
	}

	@Override
	public void exception(String message, Exception exception)
	{
	}

	@Override
	public void selectStatement(Statement statement)
	{
	}

	@Override
	public void setActiveContext(Context context)
	{
	}

}
