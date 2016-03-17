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
package aletheia.gui.common;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class PassphraseDialog extends JDialog
{
	private static final long serialVersionUID = 5591536684887229752L;
	private final JFrame frame;
	private final JPasswordField passwordField;
	private final JPasswordField confirmField;
	private final JButton okButton;
	private final JButton cancelButton;
	private char[] passphrase;

	public PassphraseDialog(JFrame frame)
	{
		this(frame, false);
	}

	public PassphraseDialog(JFrame frame, boolean confirm)
	{
		super(frame, "Enter passphrase", true);
		this.frame = frame;
		this.setLayout(new BorderLayout());
		DocumentListener documentListener = new DocumentListener()
		{

			@Override
			public void insertUpdate(DocumentEvent e)
			{
				update();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				update();
			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
				update();
			}

			private void update()
			{
				if (confirmField != null)
					okButton.setEnabled(Arrays.equals(passwordField.getPassword(), confirmField.getPassword()));
			}

		};

		JPanel formPanel = new JPanel(new GridBagLayout());
		Insets formFieldInsets = new Insets(5, 10, 5, 10);
		int gridy = 0;
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = gridy;
			gbc.insets = formFieldInsets;
			gbc.anchor = GridBagConstraints.WEST;
			formPanel.add(new JLabel("Passphrase"), gbc);
		}
		this.passwordField = new JPasswordField(20);
		this.passwordField.getDocument().addDocumentListener(documentListener);
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 1;
			gbc.gridy = gridy;
			gbc.insets = formFieldInsets;
			gbc.anchor = GridBagConstraints.WEST;
			formPanel.add(passwordField, gbc);
		}
		this.passwordField.setAction(new AbstractAction()
		{
			private static final long serialVersionUID = -799097050917370341L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (confirmField != null)
					confirmField.requestFocus();
				else
				{
					passphrase = passwordField.getPassword();
					dispose();
				}
			}
		});
		gridy++;
		if (confirm)
		{
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = gridy;
				gbc.insets = formFieldInsets;
				gbc.anchor = GridBagConstraints.WEST;
				formPanel.add(new JLabel("Confirm"), gbc);
			}
			this.confirmField = new JPasswordField(20);
			this.confirmField.getDocument().addDocumentListener(documentListener);
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 1;
				gbc.gridy = gridy;
				gbc.insets = formFieldInsets;
				gbc.anchor = GridBagConstraints.WEST;
				formPanel.add(confirmField, gbc);
			}
			this.confirmField.setAction(new AbstractAction()
			{
				private static final long serialVersionUID = -799097050917370341L;

				@Override
				public void actionPerformed(ActionEvent e)
				{
					if (Arrays.equals(passwordField.getPassword(), confirmField.getPassword()))
					{
						passphrase = passwordField.getPassword();
						dispose();
					}
					else
						passwordField.requestFocus();
				}
			});

			gridy++;
		}
		else
			confirmField = null;
		this.add(formPanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new GridBagLayout());
		Insets buttonInsets = new Insets(10, 10, 10, 10);
		this.okButton = new JButton(new OkAction());
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.insets = buttonInsets;
			buttonPanel.add(okButton, gbc);
		}
		this.cancelButton = new JButton(new CancelAction());
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 1;
			gbc.gridy = 0;
			gbc.insets = buttonInsets;
			buttonPanel.add(cancelButton, gbc);
		}
		this.add(buttonPanel, BorderLayout.SOUTH);

		this.setResizable(false);
		this.pack();
		this.setLocationRelativeTo(frame);
		this.setVisible(true);

	}

	protected JFrame getFrame()
	{
		return frame;
	}

	public char[] getPassphrase()
	{
		return passphrase;
	}

	private class OkAction extends AbstractAction
	{
		private static final long serialVersionUID = 7381151484374373488L;

		public OkAction()
		{
			super("Ok");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			passphrase = passwordField.getPassword();
			dispose();
		}

	}

	private class CancelAction extends AbstractAction
	{
		private static final long serialVersionUID = -5865730842276718348L;

		public CancelAction()
		{
			super("Cancel");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			dispose();
		}

	}

}
