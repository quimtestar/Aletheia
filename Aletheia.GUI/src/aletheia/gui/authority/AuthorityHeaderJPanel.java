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
package aletheia.gui.authority;

import java.awt.Color;
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

import aletheia.gui.app.AletheiaJFrame;
import aletheia.gui.common.BoldTextLabelRenderer;
import aletheia.gui.common.DateLabelRenderer;
import aletheia.gui.common.PersonLabelRenderer;
import aletheia.gui.common.SignatureStatusLabelRenderer;
import aletheia.gui.font.FontManager;
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
			super(AuthorityHeaderJPanel.this.getPersistenceManager(), name, statementList);
		}

		@Override
		protected void mouseClickedOnStatement(Statement statement)
		{
			getAuthorityJPanel().getContextJTreeJPanel().getContextJTree().selectStatement(statement, true);
		}

	}

	private final MyStatementListJTable dependenciesListJTable;
	private final MyStatementListJTable solverListJTable;
	private final SuccessorsJTable successorListJTable;

	public AuthorityHeaderJPanel(AuthorityJPanel authorityJPanel, StatementAuthority statementAuthority)
	{
		super();
		this.authorityJPanel = authorityJPanel;
		this.statementAuthority = statementAuthority;
		GridBagLayout gridBagLayout = new GridBagLayout();
		setLayout(gridBagLayout);
		setBackground(Color.white);
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
				add(new BoldTextLabelRenderer("Author"), gbc);
			}
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 1;
				gbc.gridy = 0;
				gbc.insets = insets;
				gbc.anchor = GridBagConstraints.WEST;
				add(new PersonLabelRenderer(statementAuthority.getAuthor(transaction)), gbc);
			}
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 1;
				gbc.insets = insets;
				gbc.anchor = GridBagConstraints.WEST;
				add(new BoldTextLabelRenderer("Created"), gbc);
			}
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 1;
				gbc.gridy = 1;
				gbc.insets = insets;
				gbc.anchor = GridBagConstraints.WEST;
				add(new DateLabelRenderer(statementAuthority.getCreationDate()), gbc);
			}
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 2;
				gbc.insets = insets;
				gbc.anchor = GridBagConstraints.WEST;
				add(new BoldTextLabelRenderer("Status"), gbc);
			}
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 1;
				gbc.gridy = 2;
				gbc.insets = insets;
				gbc.anchor = GridBagConstraints.WEST;
				add(new SignatureStatusLabelRenderer(statementAuthority.signatureStatus()), gbc);
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
					add(depPane, gbc);
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
						add(solPane, gbc);
					}
				}
				else
					this.solverListJTable = null;
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
						label.setFont(FontManager.instance.defaultFont());
						add(label, gbc);
					}
					this.successorListJTable = new SuccessorsJTable(getPersistenceManager(), delegateTreeRootNode);
					{
						GridBagConstraints gbc = new GridBagConstraints();
						gbc.gridx = 5;
						gbc.gridy = 0;
						gbc.gridheight = 4;
						gbc.insets = insets;
						gbc.anchor = GridBagConstraints.WEST;
						gbc.fill = GridBagConstraints.BOTH;
						JScrollPane sucPane = new JScrollPane(this.successorListJTable);
						add(sucPane, gbc);
					}
				}
				else
					this.successorListJTable = null;
			}
			else
			{
				this.dependenciesListJTable = null;
				this.solverListJTable = null;
				this.successorListJTable = null;
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

	protected AletheiaJFrame getAletheiaJFrame()
	{
		return authorityJPanel.getContextJTreeJPanel().getAletheiaJPanel().getAletheiaJFrame();
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
		if (solverListJTable != null)
			solverListJTable.close();
		if (successorListJTable != null)
			successorListJTable.close();
	}

}
