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
package aletheia.gui.catalogjtree;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import aletheia.model.catalog.SubCatalog;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

public class SubCatalogJTreeNodeRenderer extends CatalogJTreeNodeRenderer
{
	private static final long serialVersionUID = -7352462474574721093L;

	private class MyMouseListener implements MouseListener
	{

		@Override
		public void mouseClicked(MouseEvent arg0)
		{
			if (statement != null)
			{
				getCatalogJTree().getAletheiaJPanel().getContextJTree().selectStatement(statement, true);
			}
		}

		@Override
		public void mouseEntered(MouseEvent arg0)
		{
		}

		@Override
		public void mouseExited(MouseEvent arg0)
		{
		}

		@Override
		public void mousePressed(MouseEvent arg0)
		{
		}

		@Override
		public void mouseReleased(MouseEvent arg0)
		{
		}

	}

	private final Statement statement;

	public SubCatalogJTreeNodeRenderer(CatalogJTree catalogJTree, SubCatalogTreeNode node)
	{
		super(catalogJTree, node);
		SubCatalog catalog = node.getCatalog();
		Transaction transaction = catalogJTree.getModel().beginTransaction();
		try
		{
			statement = catalog.statement(transaction);
			if (statement == null)
			{
				this.addSpaceLabel();
				this.addSpaceLabel();
				this.addTextLabel(String.format("%-10s", catalog.name()));
			}
			else
			{
				if (statement.isProved())
					this.addTickLabel();
				else
					this.addQuestionMarkLabel();
				this.addSpaceLabel();
				Color labelColor = statement.isProved() ? getProvenLabelColor() : getUnprovenLabelColor();
				this.addTextLabel(String.format("%-10s", catalog.name()), labelColor);
				this.addColonLabel();
				this.addSpaceLabel();
				this.addTerm(transaction, statement);
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}
			setToolTipText(catalog.prefix().qualifiedName());

			addMouseListener(new MyMouseListener());
		}
		finally
		{
			transaction.abort();
		}
	}

}
