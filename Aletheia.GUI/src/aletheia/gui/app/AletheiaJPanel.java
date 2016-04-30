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
import java.awt.Cursor;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import aletheia.gui.cli.CliController;
import aletheia.gui.cli.CliJPanel;
import aletheia.gui.cli.ProofFinderExecutor;
import aletheia.gui.common.PersistentJTreeLayerUI;
import aletheia.gui.contextjtree.ContextJTree;
import aletheia.gui.contextjtree.ContextJTreeJPanel;
import aletheia.gui.signaturerequestjtree.SignatureRequestJTree;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.prooffinder.ProofFinder;
import aletheia.utilities.gui.MyJSplitPane;

public class AletheiaJPanel extends AbstractAletheiaContentPane
{
	private static final long serialVersionUID = -1729417409537499892L;

	private final MainAletheiaJFrame aletheiaJFrame;
	private final AletheiaJFrame ownerFrame;
	private final PersistenceManager persistenceManager;
	private final ContextJTreeJPanel contextJTreeJPanel;
	private final PersistentJTreeLayerUI<ContextJTreeJPanel> contextJTreeLayerUI;
	private final SignatureRequestJTree signatureRequestJTree;
	private final PersistentJTreeLayerUI<SignatureRequestJTree> signatureRequestJTreeLayerUI;
	private final MyJSplitPane splitPane0;
	private final CliController cliController;
	private final CliJPanel cliJPanel;
	private final MyJSplitPane splitPane;
	private final ProofFinder proofFinder;

	private boolean closed;

	public AletheiaJPanel(MainAletheiaJFrame aletheiaJFrame, AletheiaJFrame ownerFrame, PersistenceManager persistenceManager) throws InterruptedException
	{
		super();
		this.aletheiaJFrame = aletheiaJFrame;
		this.ownerFrame = ownerFrame;
		this.setOpaque(true);
		this.setLayout(new BorderLayout());
		this.persistenceManager = persistenceManager;
		this.contextJTreeJPanel = new ContextJTreeJPanel(this);
		this.contextJTreeLayerUI = new PersistentJTreeLayerUI<ContextJTreeJPanel>(aletheiaJFrame, contextJTreeJPanel);
		this.signatureRequestJTree = new SignatureRequestJTree(this);
		this.signatureRequestJTreeLayerUI = new PersistentJTreeLayerUI<SignatureRequestJTree>(aletheiaJFrame, signatureRequestJTree);
		this.splitPane0 = new MyJSplitPane(JSplitPane.HORIZONTAL_SPLIT, contextJTreeLayerUI.getJLayer(),
				new JScrollPane(signatureRequestJTreeLayerUI.getJLayer()));
		this.splitPane0.setResizeWeight(1);
		this.splitPane0.setDividerLocationOrExpandWhenValid(1);
		this.splitPane0.setOneTouchExpandable(true);
		this.cliController = new CliController();
		this.cliJPanel = new CliJPanel(this, cliController);
		this.splitPane = new MyJSplitPane(JSplitPane.VERTICAL_SPLIT, splitPane0, cliJPanel);
		this.splitPane.setResizeWeight(1);
		this.splitPane.setDividerLocationOrExpandWhenValid(0.8);
		this.splitPane.setOneTouchExpandable(true);
		this.add(splitPane, BorderLayout.CENTER);
		this.cliController.start();
		this.proofFinder = new ProofFinder(persistenceManager);
		this.proofFinder.addListener(new ProofFinderExecutor(persistenceManager, cliJPanel));

		this.closed = false;
	}

	public MainAletheiaJFrame getAletheiaJFrame()
	{
		return aletheiaJFrame;
	}

	public AletheiaJFrame getOwnerFrame()
	{
		return ownerFrame;
	}

	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	public ContextJTreeJPanel getContextJTreeJPanel()
	{
		return contextJTreeJPanel;
	}

	public SignatureRequestJTree getSignatureRequestJTree()
	{
		return signatureRequestJTree;
	}

	public CliController getCliController()
	{
		return cliController;
	}

	public CliJPanel getCliJPanel()
	{
		return cliJPanel;
	}

	public ProofFinder getProofFinder()
	{
		return proofFinder;
	}

	@Override
	public void lock(Collection<Transaction> owners)
	{
		contextJTreeLayerUI.lock(owners);
		signatureRequestJTreeLayerUI.lock(owners);
		cliJPanel.lock(owners);
	}

	public void lock(Transaction owner)
	{
		lock(Collections.singleton(owner));
	}

	public void waitCursor(boolean wait)
	{
		if (wait)
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		else
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	@Override
	public synchronized void close() throws InterruptedException, IOException
	{
		if (!closed)
		{
			closed = true;
			cliJPanel.close();
			cliController.shutdown(cliJPanel);
			contextJTreeJPanel.close();
			signatureRequestJTree.close();
			proofFinder.shutdown();
		}
	}

	@Override
	public void updateFontSize()
	{
		cliJPanel.updateFontSize();
		contextJTreeJPanel.updateFontSize();
		signatureRequestJTree.updateFontSize();
	}

	public ContextJTree getContextJTree()
	{
		return getContextJTreeJPanel().getContextJTree();
	}

	@Override
	public void exception(String message, Exception exception)
	{
		try
		{
			cliJPanel.exception(message, exception);
		}
		catch (InterruptedException e)
		{
		}
	}

	@Override
	public void selectStatement(Statement statement)
	{
		contextJTreeJPanel.selectStatement(statement);
	}

	@Override
	public void setActiveContext(Context context)
	{
		cliJPanel.setActiveContext(context);
	}

}
