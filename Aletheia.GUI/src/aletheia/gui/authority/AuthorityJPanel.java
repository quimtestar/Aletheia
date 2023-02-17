/*******************************************************************************
 * Copyright (c) 2014, 2020 Quim Testar.
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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.Objects;
import java.util.stream.Stream;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import aletheia.gui.app.MainAletheiaJFrame;
import aletheia.gui.common.FocusBorderManager;
import aletheia.gui.common.PersistentJTreeLayerUI;
import aletheia.gui.contextjtree.ContextJTree;
import aletheia.gui.contextjtree.ContextJTreeJPanel;
import aletheia.gui.contextjtree.sorter.GroupSorter;
import aletheia.gui.delegatejtree.DelegateTreeJTree;
import aletheia.gui.fonts.FontManager;
import aletheia.gui.lookandfeel.AletheiaLookAndFeel;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.authority.StatementAuthoritySignature;
import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.exceptions.PersistenceLockTimeoutException;
import aletheia.utilities.gui.MyJSplitPane;

public class AuthorityJPanel extends JPanel
{
	private static final long serialVersionUID = 7732434741412425374L;

	private final static int transactionTimeOut = 100;

	private final static String emptyComponentName = "empty";
	private final static String contentComponentName = "content";

	private final ContextJTreeJPanel contextJTreeJPanel;

	private class Listener implements ContextJTree.SelectionListener, Statement.StateListener, StatementAuthority.StateListener
	{

		private class SetStatementAuthorityTransactionHook implements Transaction.Hook
		{

			private final Statement statement;
			private final StatementAuthority statementAuthority;

			private SetStatementAuthorityTransactionHook(Statement statement, StatementAuthority statementAuthority)
			{
				this.statement = statement;
				this.statementAuthority = statementAuthority;
			}

			@Override
			public void run(Transaction closedTransaction)
			{
				setStatementAuthority(statement, statementAuthority);
			}

		}

		@Override
		public void statementSelected(Statement statement, boolean expanded)
		{
			setStatement(statement);
		}

		@Override
		public void consequentSelected(Context context)
		{
			clear();
		}

		@Override
		public void groupSorterSelected(GroupSorter<? extends Statement> groupSorter, boolean expanded)
		{
			if (!expanded)
				setGroupSorter(groupSorter);
			else
				clear();
		}

		@Override
		public void statementAuthorityCreated(Transaction transaction, Statement statement, StatementAuthority statementAuthority)
		{
			transaction.runWhenCommit(new SetStatementAuthorityTransactionHook(statement, statementAuthority));
		}

		@Override
		public void statementAuthorityDeleted(Transaction transaction, Statement statement, StatementAuthority statementAuthority)
		{
			transaction.runWhenCommit(new SetStatementAuthorityTransactionHook(statement, null));
		}

		@Override
		public void validSignatureStateChanged(Transaction transaction, StatementAuthority statementAuthority, boolean validSignature)
		{
			transaction.runWhenCommit(new SetStatementAuthorityTransactionHook(statement, statementAuthority));
		}

		@Override
		public void signedDependenciesStateChanged(Transaction transaction, StatementAuthority statementAuthority, boolean signedDependencies)
		{
			transaction.runWhenCommit(new SetStatementAuthorityTransactionHook(statement, statementAuthority));
		}

		@Override
		public void signedProofStateChanged(Transaction transaction, StatementAuthority statementAuthority, boolean signedProof)
		{
			transaction.runWhenCommit(new SetStatementAuthorityTransactionHook(statement, statementAuthority));
		}

		@Override
		public void signatureAdded(Transaction transaction, StatementAuthority statementAuthority, StatementAuthoritySignature statementAuthoritySignature)
		{
			transaction.runWhenCommit(new SetStatementAuthorityTransactionHook(statement, statementAuthority));
		}

		@Override
		public void signatureDeleted(Transaction transaction, StatementAuthority statementAuthority, StatementAuthoritySignature statementAuthoritySignature)
		{
			transaction.runWhenCommit(new SetStatementAuthorityTransactionHook(statement, statementAuthority));
		}

		@Override
		public void delegateTreeChanged(Transaction transaction, StatementAuthority statementAuthority)
		{
			transaction.runWhenCommit(new SetStatementAuthorityTransactionHook(statement, statementAuthority));
		}

		@Override
		public void successorEntriesChanged(Transaction transaction, StatementAuthority statementAuthority)
		{
			delegateTreeChanged(transaction, statementAuthority);
		}

	}

	private final Listener listener;

	private final JPanel emptyPanel;
	private final JPanel contentPanel;
	private final JScrollPane headerJScrollPane;
	private final JScrollPane authoritySignatureTableJScrollPane;
	private final JScrollPane delegateTreeJScrollPane;
	private final MyJSplitPane jSplitPane1;
	private final MyJSplitPane jSplitPane0;

	private Statement statement;
	private StatementAuthority statementAuthority;
	private AuthorityHeaderJPanel authorityHeaderJPanel;
	private PersistentJTreeLayerUI<AuthorityHeaderJPanel> authorityHeaderJPanelLayerUI;
	private AbstractAuthoritySignatureJTable authoritySignatureJTable;
	private PersistentJTreeLayerUI<AbstractAuthoritySignatureJTable> authoritySignatureJTableLayerUI;
	private FocusBorderManager authoritySignatureTableFocusBorderManager;
	private DelegateTreeJTree delegateTreeJTree;
	private PersistentJTreeLayerUI<DelegateTreeJTree> delegateTreeJTreeLayerUI;
	private FocusBorderManager delegateTreeFocusBorderManager;
	private String showing;

	public AuthorityJPanel(ContextJTreeJPanel contextJTreeJPanel)
	{
		super(new CardLayout());
		this.emptyPanel = new JPanel();
		this.emptyPanel.setBackground(AletheiaLookAndFeel.theme().getWindowBackground());
		add(this.emptyPanel, emptyComponentName);
		this.contentPanel = new JPanel(new BorderLayout());
		this.contentPanel.setBackground(AletheiaLookAndFeel.theme().getWindowBackground());
		add(this.contentPanel, contentComponentName);
		this.contextJTreeJPanel = contextJTreeJPanel;
		this.listener = new Listener();
		this.headerJScrollPane = new JScrollPane();
		this.headerJScrollPane.getViewport().setBackground(AletheiaLookAndFeel.theme().getWindowBackground());
		this.authoritySignatureTableJScrollPane = new JScrollPane();
		this.authoritySignatureTableJScrollPane.getViewport().setBackground(AletheiaLookAndFeel.theme().getWindowBackground());
		this.delegateTreeJScrollPane = new JScrollPane();
		this.jSplitPane1 = new MyJSplitPane(JSplitPane.HORIZONTAL_SPLIT, authoritySignatureTableJScrollPane, delegateTreeJScrollPane);
		this.jSplitPane1.setDividerLocationOrCollapseWhenValid(1d);
		this.jSplitPane1.setOneTouchExpandable(true);
		this.jSplitPane0 = new MyJSplitPane(JSplitPane.VERTICAL_SPLIT, headerJScrollPane, this.jSplitPane1);
		this.jSplitPane0.setResizeWeight(1);
		this.jSplitPane0.setDividerLocationOrCollapseWhenValid(1d);
		this.jSplitPane0.setOneTouchExpandable(true);
		this.contentPanel.add(this.jSplitPane0, BorderLayout.CENTER);
		this.statement = null;
		this.authorityHeaderJPanel = null;
		this.authoritySignatureJTable = null;
		this.authoritySignatureTableFocusBorderManager = null;
		this.delegateTreeJTree = null;
		this.delegateTreeFocusBorderManager = null;

		updatedContextJTree(contextJTreeJPanel.getContextJTree());
	}

	public ContextJTreeJPanel getContextJTreeJPanel()
	{
		return contextJTreeJPanel;
	}

	public MainAletheiaJFrame getAletheiaJFrame()
	{
		return contextJTreeJPanel.getAletheiaJFrame();
	}

	public FontManager getFontManager()
	{
		return getAletheiaJFrame().getFontManager();
	}

	@Override
	public CardLayout getLayout()
	{
		return (CardLayout) super.getLayout();
	}

	public PersistenceManager getPersistenceManager()
	{
		return contextJTreeJPanel.getPersistenceManager();
	}

	private Transaction beginTransaction()
	{
		return getPersistenceManager().beginTransaction(transactionTimeOut);
	}

	private void setStatement(Statement statement)
	{
		Transaction transaction = beginTransaction();
		try
		{
			setStatement(transaction, statement);
		}
		catch (PersistenceLockTimeoutException e)
		{
		}
		finally
		{
			transaction.abort();
		}
	}

	private void setStatement(Transaction transaction, Statement statement)
	{
		StatementAuthority stAuth = statement.getAuthority(transaction);
		setStatementAuthority(statement, stAuth);
	}

	private void setGroupSorter(GroupSorter<? extends Statement> sorter)
	{
		Transaction transaction = beginTransaction();
		try
		{
			setGroupSorter(transaction, sorter);
		}
		catch (PersistenceLockTimeoutException e)
		{
		}
		finally
		{
			transaction.abort();
		}
	}

	private void setGroupSorter(Transaction transaction, GroupSorter<? extends Statement> sorter)
	{
		Statement statement = sorter.getStatement(transaction);
		if (statement != null)
			setStatement(transaction, statement);
		else
			clear();
	}

	public void clear()
	{
		setStatementAuthority(null, null);
	}

	private synchronized void removeAuthorityHeaderJPanel()
	{
		if (authorityHeaderJPanel != null)
		{
			authorityHeaderJPanel.close();
			headerJScrollPane.setViewportView(null);
			authorityHeaderJPanel = null;
		}
		if (authorityHeaderJPanelLayerUI != null)
		{
			authorityHeaderJPanelLayerUI.close();
			authorityHeaderJPanelLayerUI = null;
		}
	}

	private synchronized void removeAuthoritySignatureJTable()
	{
		if (authoritySignatureJTable != null)
		{
			authoritySignatureTableJScrollPane.setViewportView(null);
			authoritySignatureJTable = null;
		}
		if (authoritySignatureTableFocusBorderManager != null)
		{
			authoritySignatureTableFocusBorderManager.close();
			authoritySignatureTableFocusBorderManager = null;
		}
		if (authoritySignatureJTableLayerUI != null)
		{
			authoritySignatureJTableLayerUI.close();
			authoritySignatureJTableLayerUI = null;
		}
	}

	private synchronized void removeDelegateTreeJTree()
	{
		if (delegateTreeJTree != null)
		{
			delegateTreeJTree.close();
			delegateTreeJScrollPane.setViewportView(null);
			delegateTreeJTree = null;
		}
		if (delegateTreeFocusBorderManager != null)
		{
			delegateTreeFocusBorderManager.close();
			delegateTreeFocusBorderManager = null;
		}
		if (delegateTreeJTreeLayerUI != null)
		{
			delegateTreeJTreeLayerUI.close();
			delegateTreeJTreeLayerUI = null;
		}
	}

	@SuppressWarnings("unused")
	private synchronized String getShowing()
	{
		return showing;
	}

	private synchronized void show(String componentName)
	{
		showing = componentName;
		getLayout().show(this, componentName);
	}

	private void showEmpty()
	{
		show(emptyComponentName);
	}

	private void showContent()
	{
		show(contentComponentName);
	}

	private synchronized void setStatementAuthority(Statement statement, StatementAuthority statementAuthority)
	{
		if (this.statement != null)
			this.statement.removeStateListener(listener);
		this.statement = statement;
		if (statement != null)
			statement.addStateListener(listener);

		if (this.statementAuthority != null)
			this.statementAuthority.removeStateListener(listener);
		this.statementAuthority = statementAuthority;
		if (statementAuthority != null)
			statementAuthority.addStateListener(listener);

		removeAuthorityHeaderJPanel();
		removeAuthoritySignatureJTable();
		removeDelegateTreeJTree();
		if (statementAuthority != null)
		{
			authorityHeaderJPanel = new AuthorityHeaderJPanel(this, statementAuthority);
			authorityHeaderJPanelLayerUI = new PersistentJTreeLayerUI<>(getAletheiaJFrame(), authorityHeaderJPanel);
			headerJScrollPane.setViewportView(authorityHeaderJPanelLayerUI.getJLayer());
			if (statement instanceof RootContext)
				authoritySignatureJTable = new RootContextAuthoritySignatureJTable(this, (RootContext) statement, statementAuthority);
			else
				authoritySignatureJTable = new AuthoritySignatureJTable(this, statement, statementAuthority);
			authoritySignatureJTableLayerUI = new PersistentJTreeLayerUI<>(getAletheiaJFrame(), authoritySignatureJTable);
			authoritySignatureTableJScrollPane.setViewportView(authoritySignatureJTableLayerUI.getJLayer());
			authoritySignatureTableFocusBorderManager = new FocusBorderManager(authoritySignatureTableJScrollPane, authoritySignatureJTable);
			delegateTreeJTree = new DelegateTreeJTree(this, statementAuthority);
			delegateTreeJTreeLayerUI = new PersistentJTreeLayerUI<>(getAletheiaJFrame(), delegateTreeJTree);
			delegateTreeJScrollPane.setViewportView(delegateTreeJTreeLayerUI.getJLayer());
			delegateTreeFocusBorderManager = new FocusBorderManager(delegateTreeJScrollPane, delegateTreeJTree);
			showContent();
		}
		else
			showEmpty();
		//updateUI();
	}

	public synchronized void updateFontSize()
	{
		setStatementAuthority(statement, statementAuthority);
	}

	public synchronized void close()
	{

		if (delegateTreeJTree != null)
			delegateTreeJTree.close();
		if (delegateTreeFocusBorderManager != null)
			delegateTreeFocusBorderManager.close();
	}

	public void updatedContextJTree(ContextJTree contextJTree)
	{
		contextJTree.addSelectionListener(listener);
	}

	@Override
	public void updateUI()
	{
		Stream.of(emptyPanel, contentPanel, headerJScrollPane, authoritySignatureTableJScrollPane).filter(Objects::nonNull)
				.forEach(c -> c.setBackground(AletheiaLookAndFeel.theme().getWindowBackground()));
		setStatementAuthority(statement, statementAuthority);
		super.updateUI();
	}

}
