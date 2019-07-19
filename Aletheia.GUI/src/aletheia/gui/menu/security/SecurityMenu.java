/*******************************************************************************
 * Copyright (c) 2014, 2019 Quim Testar.
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
package aletheia.gui.menu.security;

import java.awt.event.KeyEvent;

import javax.crypto.SecretKey;
import javax.swing.SwingUtilities;

import aletheia.gui.menu.AletheiaJMenu;
import aletheia.gui.menu.AletheiaJMenuBar;
import aletheia.gui.menu.AletheiaMenuItem;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.PersistenceSecretKeyManager;

public class SecurityMenu extends AletheiaJMenu
{
	private static final long serialVersionUID = 5598459073059823214L;

	private final EnterPassphraseAction enterPassphraseAction;
	private final ClearPassphraseAction clearPassphraseAction;
	private final ChangePassphraseAction changePassphraseAction;
	private final DeletePassphraseAction deletePassphraseAction;
	private final ResetPassphraseAction resetPassphraseAction;

	private class PersistenceSecretKeyManagerListener implements PersistenceSecretKeyManager.Listener
	{

		@Override
		public void secretSet()
		{
			asynchronousUpdateEnabledActions();
		}

		@Override
		public void secretUnset()
		{
			asynchronousUpdateEnabledActions();
		}

		@Override
		public void keySet()
		{
			asynchronousUpdateEnabledActions();
		}

		@Override
		public void keyUnset()
		{
			asynchronousUpdateEnabledActions();
		}
	}

	private final PersistenceSecretKeyManagerListener persistenceSecretKeyManagerListener;

	private PersistenceManager persistenceManager;

	public SecurityMenu(AletheiaJMenuBar aletheiaJMenuBar)
	{
		super(aletheiaJMenuBar, "Security", KeyEvent.VK_S);
		this.enterPassphraseAction = new EnterPassphraseAction(this);
		this.add(new AletheiaMenuItem(enterPassphraseAction));
		this.clearPassphraseAction = new ClearPassphraseAction(this);
		this.add(new AletheiaMenuItem(clearPassphraseAction));
		this.changePassphraseAction = new ChangePassphraseAction(this);
		this.add(new AletheiaMenuItem(changePassphraseAction));
		this.deletePassphraseAction = new DeletePassphraseAction(this);
		this.add(new AletheiaMenuItem(deletePassphraseAction));
		this.resetPassphraseAction = new ResetPassphraseAction(this);
		this.add(new AletheiaMenuItem(resetPassphraseAction));
		this.persistenceSecretKeyManagerListener = new PersistenceSecretKeyManagerListener();
		updateEnabledActions();
	}

	public EnterPassphraseAction getEnterPassphraseAction()
	{
		return enterPassphraseAction;
	}

	public ClearPassphraseAction getClearPassphraseAction()
	{
		return clearPassphraseAction;
	}

	public ChangePassphraseAction getChangePassphraseAction()
	{
		return changePassphraseAction;
	}

	public DeletePassphraseAction getDeletePassphraseAction()
	{
		return deletePassphraseAction;
	}

	public synchronized void updatePersistenceManager()
	{
		PersistenceManager newPersistenceManager = getAletheiaJFrame().getPersistenceManager();
		if (newPersistenceManager != persistenceManager)
		{
			if (persistenceManager != null)
				persistenceManager.getSecretKeyManager().removeListener(persistenceSecretKeyManagerListener);
			persistenceManager = newPersistenceManager;
			if (persistenceManager != null)
				persistenceManager.getSecretKeyManager().addListener(persistenceSecretKeyManagerListener);
			updateEnabledActions();
		}
	}

	private synchronized void updateEnabledActions()
	{
		if (persistenceManager != null)
		{
			if (persistenceManager.getSecretKeyManager().isSecretSet())
			{
				SecretKey secretKey = persistenceManager.getSecretKeyManager().getSecretKey();
				if (secretKey == null)
				{
					enterPassphraseAction.setEnabled(true);
					clearPassphraseAction.setEnabled(false);
					changePassphraseAction.setEnabled(false);
					deletePassphraseAction.setEnabled(false);
					resetPassphraseAction.setEnabled(true);
				}
				else
				{
					enterPassphraseAction.setEnabled(false);
					clearPassphraseAction.setEnabled(true);
					changePassphraseAction.setEnabled(true);
					deletePassphraseAction.setEnabled(true);
					resetPassphraseAction.setEnabled(false);
				}
			}
			else
			{
				enterPassphraseAction.setEnabled(false);
				clearPassphraseAction.setEnabled(false);
				changePassphraseAction.setEnabled(true);
				deletePassphraseAction.setEnabled(false);
				resetPassphraseAction.setEnabled(false);
			}
		}
		else
		{
			enterPassphraseAction.setEnabled(false);
			clearPassphraseAction.setEnabled(false);
			changePassphraseAction.setEnabled(false);
			deletePassphraseAction.setEnabled(false);
			resetPassphraseAction.setEnabled(false);
		}
	}

	private void asynchronousUpdateEnabledActions()
	{
		SwingUtilities.invokeLater(new Runnable()
		{

			@Override
			public void run()
			{
				updateEnabledActions();
			}
		});
	}

}
