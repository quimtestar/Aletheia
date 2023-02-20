/*******************************************************************************
 * Copyright (c) 2014, 2023 Quim Testar.
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
package aletheia.gui.authority;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;

import aletheia.gui.app.MainAletheiaJFrame;
import aletheia.gui.common.FocusBorderManager;
import aletheia.gui.common.renderer.BoldTextLabelRenderer;
import aletheia.gui.common.renderer.DateLabelRenderer;
import aletheia.gui.common.renderer.PersonLabelRenderer;
import aletheia.gui.common.renderer.SignatureStatusLabelRenderer;
import aletheia.gui.fonts.FontManager;
import aletheia.gui.lookandfeel.AletheiaLookAndFeel;
import aletheia.model.authority.DelegateTreeRootNode;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.BufferedList;

public class AuthorityHeaderJPanel extends JPanel implements Scrollable
{
	private static final long serialVersionUID = 4752307133011138660L;
	private static final int transactionTimeOut = 100;

	private final AuthorityJPanel authorityJPanel;
	private final StatementAuthority statementAuthority;

	private class MyStatementListJTable extends StatementListJTable
	{

		private static final long serialVersionUID = 7259849370385594954L;

		public MyStatementListJTable(String name, List<Statement> statementList)
		{
			super(AuthorityHeaderJPanel.this, name, statementList);
		}

		@Override
		protected void mouseClickedOnStatement(Statement statement)
		{
			getAuthorityJPanel().getContextJTreeJPanel().getContextJTree().selectStatement(statement, true);
		}

	}

	private final MyStatementListJTable dependenciesListJTable;
	private final FocusBorderManager dependenciesListFocusBorderManager;
	private final MyStatementListJTable solverListJTable;
	private final FocusBorderManager solverListFocusBorderManager;
	private final SuccessorsJTable successorListJTable;
	private final FocusBorderManager successorListFocusBorderManager;

	public AuthorityHeaderJPanel(AuthorityJPanel authorityJPanel, StatementAuthority statementAuthority)
	{
		super();
		this.authorityJPanel = authorityJPanel;
		this.statementAuthority = statementAuthority;
		GridBagLayout gridBagLayout = new GridBagLayout();
		setLayout(gridBagLayout);
		setBackground(AletheiaLookAndFeel.theme().getWindowBackground());
		Transaction transaction = getPersistenceManager().beginTransaction(transactionTimeOut);
		try
		{
			Insets insets = new Insets(0, 0, 0, 10);
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 0;
				gbc.insets = insets;
				gbc.anchor = GridBagConstraints.WEST;
				add(new BoldTextLabelRenderer(getFontManager(), "Author"), gbc);
			}
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 1;
				gbc.gridy = 0;
				gbc.insets = insets;
				gbc.anchor = GridBagConstraints.WEST;
				add(new PersonLabelRenderer(getFontManager(), statementAuthority.getAuthor(transaction)), gbc);
			}
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 1;
				gbc.insets = insets;
				gbc.anchor = GridBagConstraints.WEST;
				add(new BoldTextLabelRenderer(getFontManager(), "Created"), gbc);
			}
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 1;
				gbc.gridy = 1;
				gbc.insets = insets;
				gbc.anchor = GridBagConstraints.WEST;
				add(new DateLabelRenderer(getFontManager(), statementAuthority.getCreationDate()), gbc);
			}
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 2;
				gbc.insets = insets;
				gbc.anchor = GridBagConstraints.WEST;
				add(new BoldTextLabelRenderer(getFontManager(), "Status"), gbc);
			}
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 1;
				gbc.gridy = 2;
				gbc.insets = insets;
				gbc.anchor = GridBagConstraints.WEST;
				add(new SignatureStatusLabelRenderer(getFontManager(), statementAuthority.signatureStatus()), gbc);
			}
			Statement statement = statementAuthority.getStatement(transaction);
			if (statement != null)
			{
				this.dependenciesListJTable = new MyStatementListJTable("Dependencies", new BufferedList<>(statement.dependencies(transaction)));
				{
					GridBagConstraints gbc = new GridBagConstraints();
					gbc.gridx = 2;
					gbc.gridy = 0;
					gbc.gridheight = 4;
					gbc.insets = insets;
					gbc.anchor = GridBagConstraints.WEST;
					gbc.fill = GridBagConstraints.BOTH;
					JScrollPane depPane = new JScrollPane(this.dependenciesListJTable);
					depPane.getViewport().setBackground(AletheiaLookAndFeel.theme().getTableBackground());
					add(depPane, gbc);
					this.dependenciesListFocusBorderManager = new FocusBorderManager(depPane, dependenciesListJTable);
				}
				if (statement instanceof Context)
				{
					Context context = (Context) statement;
					this.solverListJTable = new MyStatementListJTable("Solvers", new BufferedList<>(context.solvers(transaction)));
					{
						GridBagConstraints gbc = new GridBagConstraints();
						gbc.gridx = 3;
						gbc.gridy = 0;
						gbc.gridheight = 4;
						gbc.insets = insets;
						gbc.anchor = GridBagConstraints.WEST;
						gbc.fill = GridBagConstraints.BOTH;
						JScrollPane solPane = new JScrollPane(this.solverListJTable);
						solPane.getViewport().setBackground(AletheiaLookAndFeel.theme().getTableBackground());
						add(solPane, gbc);
						this.solverListFocusBorderManager = new FocusBorderManager(solPane, solverListJTable);
					}
				}
				else
				{
					this.solverListJTable = null;
					this.solverListFocusBorderManager = null;
				}
				DelegateTreeRootNode delegateTreeRootNode = statementAuthority.getDelegateTreeRootNode(transaction);
				if (delegateTreeRootNode != null && !delegateTreeRootNode.successorEntries().isEmpty())
				{

					{
						GridBagConstraints gbc = new GridBagConstraints();
						gbc.gridx = 4;
						gbc.gridy = 0;
						gbc.gridheight = 4;
						gbc.insets = insets;
						gbc.anchor = GridBagConstraints.WEST;
						gbc.fill = GridBagConstraints.BOTH;
						JLabel label = new JLabel("             ");
						label.setFont(authorityJPanel.getFontManager().defaultFont());
						add(label, gbc);
					}
					this.successorListJTable = new SuccessorsJTable(this, delegateTreeRootNode);
					{
						GridBagConstraints gbc = new GridBagConstraints();
						gbc.gridx = 5;
						gbc.gridy = 0;
						gbc.gridheight = 4;
						gbc.insets = insets;
						gbc.anchor = GridBagConstraints.WEST;
						gbc.fill = GridBagConstraints.BOTH;
						JScrollPane sucPane = new JScrollPane(this.successorListJTable);
						sucPane.getViewport().setBackground(AletheiaLookAndFeel.theme().getTableBackground());
						add(sucPane, gbc);
						this.successorListFocusBorderManager = new FocusBorderManager(sucPane, successorListJTable);
					}
				}
				else
				{
					this.successorListJTable = null;
					this.successorListFocusBorderManager = null;
				}
			}
			else
			{
				this.dependenciesListJTable = null;
				this.dependenciesListFocusBorderManager = null;
				this.solverListJTable = null;
				this.solverListFocusBorderManager = null;
				this.successorListJTable = null;
				this.successorListFocusBorderManager = null;
			}

		}
		finally
		{
			transaction.abort();
		}

	}

	protected AuthorityJPanel getAuthorityJPanel()
	{
		return authorityJPanel;
	}

	protected StatementAuthority getStatementAuthority()
	{
		return statementAuthority;
	}

	protected PersistenceManager getPersistenceManager()
	{
		return authorityJPanel.getPersistenceManager();
	}

	protected MainAletheiaJFrame getAletheiaJFrame()
	{
		return authorityJPanel.getContextJTreeJPanel().getAletheiaJPanel().getAletheiaJFrame();
	}

	protected FontManager getFontManager()
	{
		return getAletheiaJFrame().getFontManager();
	}

	@Override
	public Dimension getPreferredScrollableViewportSize()
	{
		return getPreferredSize();
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
	{
		return 1;
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
	{
		return 1;
	}

	@Override
	public boolean getScrollableTracksViewportWidth()
	{
		return false;
	}

	@Override
	public boolean getScrollableTracksViewportHeight()
	{
		return false;
	}

	public void close()
	{
		if (dependenciesListJTable != null)
			dependenciesListJTable.close();
		if (dependenciesListFocusBorderManager != null)
			dependenciesListFocusBorderManager.close();
		if (solverListJTable != null)
			solverListJTable.close();
		if (solverListFocusBorderManager != null)
			solverListFocusBorderManager.close();
		if (successorListJTable != null)
			successorListJTable.close();
		if (successorListFocusBorderManager != null)
			successorListFocusBorderManager.close();
	}

}
