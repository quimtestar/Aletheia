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
package aletheia.gui.preferences;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import org.apache.logging.log4j.Logger;

import aletheia.gui.app.DesktopAletheiaJFrame;
import aletheia.log4j.LoggerManager;
import aletheia.persistence.gui.PersistencePreferencesJPanel;
import aletheia.utilities.AsynchronousInvoker;
import aletheia.utilities.MiscUtilities;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCollection;
import aletheia.utilities.collections.BufferedList;
import aletheia.utilities.collections.UnionCollection;

public class PreferencesDialog extends JDialog
{
	private static final long serialVersionUID = -7370115847771157684L;
	private static final Logger logger = LoggerManager.instance.logger();

	private final DesktopAletheiaJFrame aletheiaJFrame;
	private final GUIAletheiaPreferences preferences;

	private final JComboBox<PersistenceClass> persistenceClassComboBox;

	private final Map<PersistenceClass, PersistencePreferencesJPanel> persistencePreferencesJPanels;

	private static class PeerToPeerNodeGenderItem
	{
		private final PeerToPeerNodeGender gender;
		private final String label;

		private PeerToPeerNodeGenderItem(PeerToPeerNodeGender gender, String label)
		{
			this.gender = gender;
			this.label = label;
		}

		public String name()
		{
			return gender.name();
		}

		@Override
		public String toString()
		{
			return label;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((gender == null) ? 0 : gender.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PeerToPeerNodeGenderItem other = (PeerToPeerNodeGenderItem) obj;
			if (gender != other.gender)
				return false;
			return true;
		}

		//@formatter:off
		private static final PeerToPeerNodeGenderItem[] values=new PeerToPeerNodeGenderItem[]{
			new PeerToPeerNodeGenderItem(PeerToPeerNodeGender.DISABLED, "\u26aa Disabled"),
			new PeerToPeerNodeGenderItem(PeerToPeerNodeGender.FEMALE, "\u2640 Female"),
			new PeerToPeerNodeGenderItem(PeerToPeerNodeGender.MALE, "\u2642 Male"),
		};
		//@formatter:on

		public static final PeerToPeerNodeGenderItem[] values()
		{
			return values;
		}

		private static final EnumMap<PeerToPeerNodeGender, PeerToPeerNodeGenderItem> itemMap = new EnumMap<>(PeerToPeerNodeGender.class);

		static
		{
			for (PeerToPeerNodeGenderItem item : values)
				itemMap.put(item.gender, item);
		}

		public static final Map<PeerToPeerNodeGender, PeerToPeerNodeGenderItem> itemMap()
		{
			return Collections.unmodifiableMap(itemMap);
		}

	}

	private final JComboBox<PeerToPeerNodeGenderItem> p2pNodeGenderComboBox;

	private class AddressComboBoxItem implements Comparable<AddressComboBoxItem>
	{
		private final NetworkInterface networkInterface;
		private final InterfaceAddress interfaceAddress;

		public AddressComboBoxItem(NetworkInterface networkInterface, InterfaceAddress interfaceAddress)
		{
			this.networkInterface = networkInterface;
			this.interfaceAddress = interfaceAddress;
		}

		public InetAddress getAddress()
		{
			return interfaceAddress.getAddress();
		}

		@Override
		public String toString()
		{
			return "(" + networkInterface.getName() + ") " + interfaceAddress.getAddress().getHostAddress();
		}

		@Override
		public int compareTo(AddressComboBoxItem o)
		{
			int c;
			c = networkInterface.getDisplayName().compareTo(o.networkInterface.getDisplayName());
			if (c != 0)
				return c;
			byte[] a0 = interfaceAddress.getAddress().getAddress();
			byte[] a1 = o.interfaceAddress.getAddress().getAddress();
			c = Integer.compare(a0.length, a1.length);
			if (c != 0)
				return c;
			for (int i = 0; i < a0.length && i < a1.length; i++)
			{
				c = Byte.compare(a0[i], a1[i]);
				if (c != 0)
					return c;
			}
			return 0;
		}
	}

	private class InterfaceAddressComboBoxModel extends AbstractListModel<AddressComboBoxItem> implements ComboBoxModel<AddressComboBoxItem>
	{
		private static final long serialVersionUID = 3749213544443833888L;
		private final List<AddressComboBoxItem> itemList;

		private AddressComboBoxItem selected;

		public InterfaceAddressComboBoxModel()
		{
			List<AddressComboBoxItem> itemList_;
			try
			{
				itemList_ = new BufferedList<>(
						new UnionCollection<>(new BijectionCollection<>(new Bijection<NetworkInterface, Collection<AddressComboBoxItem>>()
						{

							@Override
							public Collection<AddressComboBoxItem> forward(final NetworkInterface networkInterface)
							{
								return new BijectionCollection<>(new Bijection<InterfaceAddress, AddressComboBoxItem>()
								{

									@Override
									public AddressComboBoxItem forward(InterfaceAddress interfaceAddress)
									{
										return new AddressComboBoxItem(networkInterface, interfaceAddress);
									}

									@Override
									public InterfaceAddress backward(AddressComboBoxItem output)
									{
										throw new UnsupportedOperationException();
									}
								}, networkInterface.getInterfaceAddresses());
							}

							@Override
							public NetworkInterface backward(Collection<AddressComboBoxItem> output)
							{
								throw new UnsupportedOperationException();
							}
						}, Collections.list(NetworkInterface.getNetworkInterfaces()))));
				Collections.sort(itemList_);
			}
			catch (Exception e)
			{
				itemList_ = Collections.emptyList();
			}
			this.itemList = itemList_;

		}

		@Override
		public int getSize()
		{
			return itemList.size();
		}

		@Override
		public AddressComboBoxItem getElementAt(int index)
		{
			return itemList.get(index);
		}

		protected void fireContentsChanged()
		{
			fireContentsChanged(this, 0, itemList.size());
		}

		@Override
		public void setSelectedItem(Object anItem)
		{
			this.selected = (AddressComboBoxItem) anItem;
			fireContentsChanged();
		}

		@Override
		public AddressComboBoxItem getSelectedItem()
		{
			return selected;
		}

		public int indexOf(InetAddress inetAddress)
		{
			if (inetAddress == null)
				return -1;
			int i = 0;
			for (AddressComboBoxItem item : itemList)
			{
				if (item.getAddress().equals(inetAddress))
					return i;
				i++;
			}
			return -1;
		}

	}

	private final InterfaceAddressComboBoxModel interfaceAddressComboBoxModel;
	private final JComboBox<AddressComboBoxItem> p2pExternalAddressComboBox;
	private final JSpinner p2pExternalPortSpinner;

	private final JTextField p2pSurrogateAddressTextField;
	private final JSpinner p2pSurrogatePortSpinner;

	private final JSpinner fontSizeSpinner;
	private final JSlider compactationThresholdSlider;

	private final JButton okButton;
	private final JButton cancelButton;

	private static void plainifyFont(Component comp)
	{
		comp.setFont(comp.getFont().deriveFont(Font.PLAIN));
	}

	public PreferencesDialog(DesktopAletheiaJFrame aletheiaJFrame)
	{
		super(aletheiaJFrame, "Aletheia Preferences", true);
		this.aletheiaJFrame = aletheiaJFrame;
		this.preferences = GUIAletheiaPreferences.instance;
		this.setLayout(new BorderLayout());
		JPanel formPanel = new JPanel(new GridBagLayout());
		Insets formFieldInsets = new Insets(5, 10, 5, 10);
		int gridy = 0;

		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = gridy;
			gbc.insets = formFieldInsets;
			gbc.anchor = GridBagConstraints.WEST;
			formPanel.add(new JLabel("Persistence Class"), gbc);
		}
		this.persistenceClassComboBox = new JComboBox<>(PersistenceClass.values());
		this.persistenceClassComboBox.setSelectedItem(preferences.getPersistenceClass());
		final JPanel persistenceManagerPanel = new JPanel();
		final CardLayout persistenceManagerPanelCardLayout = new CardLayout(0, 0);
		persistenceManagerPanel.setLayout(persistenceManagerPanelCardLayout);
		this.persistenceClassComboBox.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				persistenceManagerPanelCardLayout.show(persistenceManagerPanel,
						((PersistenceClass) persistenceClassComboBox.getSelectedItem()).persistenceGUIFactory.getName());
			}
		});
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 1;
			gbc.gridy = gridy;
			gbc.insets = formFieldInsets;
			gbc.anchor = GridBagConstraints.WEST;
			formPanel.add(persistenceClassComboBox, gbc);
		}
		gridy++;
		this.persistencePreferencesJPanels = new HashMap<>();
		for (PersistenceClass pc : PersistenceClass.values())
		{
			PersistencePreferencesJPanel persistencePreferencesJPanel = pc.persistenceGUIFactory.createPreferencesJPanel();
			persistencePreferencesJPanels.put(pc, persistencePreferencesJPanel);
			persistenceManagerPanel.add(persistencePreferencesJPanel);
			persistenceManagerPanelCardLayout.addLayoutComponent(persistencePreferencesJPanel, pc.persistenceGUIFactory.getName());
		}
		persistenceManagerPanelCardLayout.show(persistenceManagerPanel, persistenceClassComboBox.getSelectedItem().toString());
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridwidth = 2;
			gbc.gridy = gridy;
			gbc.insets = formFieldInsets;
			gbc.anchor = GridBagConstraints.WEST;
			formPanel.add(persistenceManagerPanel, gbc);
		}
		gridy++;

		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = gridy;
			gbc.insets = formFieldInsets;
			gbc.anchor = GridBagConstraints.WEST;
			formPanel.add(new JLabel("P2P node"), gbc);
		}
		this.p2pNodeGenderComboBox = new JComboBox<>(PeerToPeerNodeGenderItem.values());
		this.p2pNodeGenderComboBox.setSelectedItem(PeerToPeerNodeGenderItem.itemMap().get(preferences.peerToPeerNode().getP2pGender()));
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 1;
			gbc.gridy = gridy;
			gbc.insets = formFieldInsets;
			gbc.anchor = GridBagConstraints.WEST;
			formPanel.add(p2pNodeGenderComboBox, gbc);
		}
		gridy++;

		final JPanel p2pPanel = new JPanel();
		final CardLayout p2pCardLayout = new CardLayout(0, 0);
		p2pPanel.setLayout(p2pCardLayout);
		this.p2pNodeGenderComboBox.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				p2pCardLayout.show(p2pPanel, ((PeerToPeerNodeGenderItem) p2pNodeGenderComboBox.getSelectedItem()).name());
			}
		});
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridwidth = 2;
			gbc.gridy = gridy;
			gbc.insets = formFieldInsets;
			gbc.anchor = GridBagConstraints.WEST;
			formPanel.add(p2pPanel, gbc);
		}
		gridy++;

		p2pPanel.add(new JPanel(), PeerToPeerNodeGender.DISABLED.name());

		{
			JPanel p2pFemalePanel = new JPanel(new GridBagLayout());
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 0;
				gbc.insets = formFieldInsets;
				gbc.anchor = GridBagConstraints.WEST;
				p2pFemalePanel.add(new JLabel("External listen address"), gbc);
			}
			this.interfaceAddressComboBoxModel = new InterfaceAddressComboBoxModel();
			this.p2pExternalAddressComboBox = new JComboBox<>(interfaceAddressComboBoxModel);
			this.p2pExternalAddressComboBox
					.setSelectedIndex(interfaceAddressComboBoxModel.indexOf(preferences.peerToPeerNode().femalePeerToPeerNode().getP2pExternalAddress()));
			this.p2pExternalPortSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 65535, 1));
			this.p2pExternalPortSpinner.setEditor(new JSpinner.NumberEditor(p2pExternalPortSpinner, "0"));
			this.p2pExternalPortSpinner.setValue(preferences.peerToPeerNode().femalePeerToPeerNode().getP2pExternalPort());
			JPanel p2pExternalAddressPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
			p2pExternalAddressPanel.add(p2pExternalAddressComboBox);
			p2pExternalAddressPanel.add(p2pExternalPortSpinner);
			plainifyFont(p2pExternalPortSpinner);
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 1;
				gbc.gridy = 0;
				gbc.insets = formFieldInsets;
				gbc.anchor = GridBagConstraints.WEST;
				p2pFemalePanel.add(p2pExternalAddressPanel, gbc);
			}
			p2pPanel.add(p2pFemalePanel, PeerToPeerNodeGender.FEMALE.name());
		}

		{
			JPanel p2pMalePanel = new JPanel(new GridBagLayout());
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 0;
				gbc.insets = formFieldInsets;
				gbc.anchor = GridBagConstraints.WEST;
				p2pMalePanel.add(new JLabel("Surrogate address"), gbc);
			}
			this.p2pSurrogateAddressTextField = new JTextField(preferences.peerToPeerNode().malePeerToPeerNode().getP2pSurrogateAddress(), 25);
			this.p2pSurrogatePortSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 65535, 1));
			this.p2pSurrogatePortSpinner.setEditor(new JSpinner.NumberEditor(p2pSurrogatePortSpinner, "0"));
			this.p2pSurrogatePortSpinner.setValue(preferences.peerToPeerNode().malePeerToPeerNode().getP2pSurrogatePort());
			JPanel p2pSurrogateAddressPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
			p2pSurrogateAddressPanel.add(p2pSurrogateAddressTextField);
			p2pSurrogateAddressPanel.add(p2pSurrogatePortSpinner);
			plainifyFont(p2pSurrogatePortSpinner);
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 1;
				gbc.gridy = 0;
				gbc.insets = formFieldInsets;
				gbc.anchor = GridBagConstraints.WEST;
				p2pMalePanel.add(p2pSurrogateAddressPanel, gbc);
			}
			p2pPanel.add(p2pMalePanel, PeerToPeerNodeGender.MALE.name());
		}
		p2pCardLayout.show(p2pPanel, ((PeerToPeerNodeGenderItem) p2pNodeGenderComboBox.getSelectedItem()).name());

		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = gridy;
			gbc.insets = formFieldInsets;
			gbc.anchor = GridBagConstraints.WEST;
			formPanel.add(new JLabel("Font size"), gbc);
		}
		SpinnerNumberModel fontSizeModel = new SpinnerNumberModel(6, 6, 96, 1);
		this.fontSizeSpinner = new JSpinner(fontSizeModel);
		this.fontSizeSpinner.setEditor(new JSpinner.NumberEditor(fontSizeSpinner, "0"));
		this.fontSizeSpinner.setValue(preferences.appearance().getFontSize());
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 1;
			gbc.gridy = gridy;
			gbc.insets = formFieldInsets;
			gbc.anchor = GridBagConstraints.WEST;
			formPanel.add(fontSizeSpinner, gbc);
		}
		gridy++;

		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = gridy;
			gbc.insets = formFieldInsets;
			gbc.anchor = GridBagConstraints.WEST;
			formPanel.add(new JLabel("<html><body>Compactation<br>threshold<br>(advanced)</body></html>"), gbc);
		}
		this.compactationThresholdSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 512,
				Integer.max(0, Integer.min(512, preferences.appearance().getCompactationThreshold())));
		this.compactationThresholdSlider.setPaintTicks(true);
		this.compactationThresholdSlider.setMinorTickSpacing(16);
		this.compactationThresholdSlider.setMajorTickSpacing(128);
		this.compactationThresholdSlider.setPaintLabels(true);
		this.compactationThresholdSlider.setSnapToTicks(true);
		this.compactationThresholdSlider.setPaintTrack(true);
		this.compactationThresholdSlider.setToolTipText(Integer.toString(this.compactationThresholdSlider.getValue()));
		this.compactationThresholdSlider.addChangeListener(e -> {
			this.compactationThresholdSlider.setToolTipText(Integer.toString(this.compactationThresholdSlider.getValue()));
		});
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 1;
			gbc.gridy = gridy;
			gbc.insets = formFieldInsets;
			gbc.anchor = GridBagConstraints.WEST;
			formPanel.add(compactationThresholdSlider, gbc);
		}
		gridy++;

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
		this.setLocationRelativeTo(aletheiaJFrame);

		this.pack();
		this.setVisible(true);

	}

	@Override
	public void setEnabled(boolean enable)
	{
		super.setEnabled(enable);
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
			PreferencesDialog.this.setEnabled(false);
			AsynchronousInvoker.instance.invoke(() -> {
				try
				{
					preferences.setPersistenceClass((PersistenceClass) persistenceClassComboBox.getSelectedItem());
					for (PersistencePreferencesJPanel persistencePreferencesJPanel : persistencePreferencesJPanels.values())
						persistencePreferencesJPanel.okAction();
					PeerToPeerNodeGender peerToPeerNodeGender = ((PeerToPeerNodeGenderItem) p2pNodeGenderComboBox.getSelectedItem()).gender;
					preferences.peerToPeerNode().setP2pGender(peerToPeerNodeGender);
					AddressComboBoxItem addressComboBoxItem = (AddressComboBoxItem) p2pExternalAddressComboBox.getSelectedItem();
					preferences.peerToPeerNode().femalePeerToPeerNode()
							.setP2pExternalAddress(addressComboBoxItem != null ? addressComboBoxItem.getAddress() : null);
					if (peerToPeerNodeGender == PeerToPeerNodeGender.FEMALE && addressComboBoxItem == null)
						throw new Exception("Must select a P2P external address");
					preferences.peerToPeerNode().femalePeerToPeerNode().setP2pExternalPort((int) p2pExternalPortSpinner.getValue());
					String p2pSurrogateAddress = p2pSurrogateAddressTextField.getText().trim();
					preferences.peerToPeerNode().malePeerToPeerNode().setP2pSurrogateAddress(p2pSurrogateAddress);
					if (peerToPeerNodeGender == PeerToPeerNodeGender.MALE && p2pSurrogateAddress.isEmpty())
						throw new Exception("Must select a P2P surrogate address");
					preferences.peerToPeerNode().malePeerToPeerNode().setP2pSurrogatePort((int) p2pSurrogatePortSpinner.getValue());
					int fontSize = (Integer) fontSizeSpinner.getValue();
					preferences.appearance().setFontSize(fontSize);
					int oldFontSize = aletheiaJFrame.getFontManager().getFontSize();
					if (oldFontSize != fontSize)
						aletheiaJFrame.getFontManager().setFontSize(fontSize);
					preferences.appearance().setCompactationThreshold(compactationThresholdSlider.getValue());

					boolean ret = aletheiaJFrame.updateContentPane(false);
					if (ret)
						ret = aletheiaJFrame.updateServerStatus(false);
					if (ret)
					{
						if (oldFontSize != fontSize)
							aletheiaJFrame.updateFontSize();
					}
					if (ret)
						dispose();
				}
				catch (Exception ex)
				{
					logger.error(ex.getMessage(), ex);
					JOptionPane.showMessageDialog(PreferencesDialog.this, MiscUtilities.wrapText(ex.getMessage(), 80), "Error", JOptionPane.ERROR_MESSAGE);
				}
				finally
				{
					PreferencesDialog.this.setEnabled(true);
				}
			});
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
