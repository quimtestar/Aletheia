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

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.LayoutManager;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import aletheia.gui.font.FontManager;
import aletheia.model.authority.Person;
import aletheia.model.authority.StatementAuthority;
import aletheia.utilities.MiscUtilities;

public abstract class AbstractRenderer extends JPanel
{
	private static final long serialVersionUID = -9049293577574541041L;
	@SuppressWarnings("unused")
	private static final Color darkRed = Color.red.darker().darker();
	private static final Color darkGreen = Color.green.darker().darker();
	@SuppressWarnings("unused")
	private static final Color darkBlue = Color.blue.darker().darker();
	private static final Color defaultColor = Color.black;
	private static final Color provenLabelColor = darkGreen;
	private static final Color unprovenLabelColor = Color.orange.darker().darker();
	private static final Color tickColor = Color.green;
	private static final Color questionMarkColor = Color.red;
	private static final Color xMarkColor = Color.red;
	private static final Color turnstileColor = Color.orange;
	private static final Color darkGray = new Color(0x606060);
	private static final Color notValidSignatureSymbolColor = darkGray;
	private static final Color validSignatureSymbolColor = darkGray;
	private static final Color signedDependenciesSymbolColor = darkGray;
	private static final Color signedProofSymbolColor = darkGray;
	private static final Color darkCyan = new Color(0x008080);
	private static final Color subscribeSymbolColor = darkCyan;
	private static final Color privatePersonColor = darkGreen;
	private static final Border normalBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
	private static final Border focusBorder = BorderFactory.createLineBorder(Color.blue, 1);
	private static final Color defaultNormalBackgroundColor = Color.white;
	private static final Color defaultSelectedBackgroundColor = Color.lightGray;

	protected interface EditableComponent
	{
		public void cancelEditing();

		public void stopEditing();
	}

	private final Set<EditableComponent> editableComponents;

	private Font activeFont;

	private Color normalBackgroundColor = defaultNormalBackgroundColor;
	private Color selectedBackgroundColor = defaultSelectedBackgroundColor;

	public AbstractRenderer()
	{
		super();
		this.editableComponents = new HashSet<EditableComponent>();
		this.activeFont = getDefaultFont();
		FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
		layout.setHgap(0);
		layout.setVgap(0);
		setLayout(layout);
		setBackground(defaultNormalBackgroundColor);
	}

	public Color getNormalBackgroundColor()
	{
		return normalBackgroundColor;
	}

	public void setNormalBackgroundColor(Color normalBackgroundColor)
	{
		this.normalBackgroundColor = normalBackgroundColor;
	}

	public Color getSelectedBackgroundColor()
	{
		return selectedBackgroundColor;
	}

	public void setSelectedBackgroundColor(Color selectedBackgroundColor)
	{
		this.selectedBackgroundColor = selectedBackgroundColor;
	}

	protected void addEditableComponent(EditableComponent editableComponent)
	{
		editableComponents.add(editableComponent);
	}

	protected Font getDefaultFont()
	{
		return FontManager.instance.defaultFont();
	}

	protected Font getBoldFont()
	{
		return FontManager.instance.boldFont();
	}

	protected Font getItalicFont()
	{
		return FontManager.instance.italicFont();
	}

	protected Font getActiveFont()
	{
		return activeFont;
	}

	protected void setActiveFont(Font font)
	{
		activeFont = font;
	}

	@Override
	public FlowLayout getLayout()
	{
		return (FlowLayout) super.getLayout();
	}

	@Override
	public void setLayout(LayoutManager mgr)
	{
		if (!(mgr instanceof FlowLayout))
			throw new IllegalArgumentException();
		super.setLayout(mgr);
	}

	private JLabel addTextLabel(String text, Font font, Color color)
	{
		JLabel label = new JLabel(text);
		label.setFont(font);
		label.setForeground(color);
		add(label);
		return label;
	}

	protected JLabel addTextLabel(String text)
	{
		return addTextLabel(text, getActiveFont(), defaultColor);
	}

	protected JLabel addTextLabel(String text, Font font)
	{
		return addTextLabel(text, font, defaultColor);
	}

	protected JLabel addTextLabel(String text, Color color)
	{
		return addTextLabel(text, getActiveFont(), color);
	}

	protected JLabel addTTermLabel()
	{
		return addTextLabel("\u03a4");
	}

	protected JLabel addOpenFunLabel()
	{
		return addTextLabel("<");
	}

	protected JLabel addColonLabel()
	{
		return addTextLabel(":");
	}

	protected JLabel addArrowLabel()
	{
		return addTextLabel("\u2192");
	}

	protected JLabel addCloseFunLabel()
	{
		return addTextLabel(">");
	}

	protected JLabel addOpenParLabel()
	{
		return addTextLabel("(");
	}

	protected JLabel addCloseParLabel()
	{
		return addTextLabel(")");
	}

	protected JLabel addTurnstileLabel()
	{
		return addTextLabel("\u22a2", turnstileColor);
	}

	protected JLabel addSpaceLabel()
	{
		return addTextLabel(" ");
	}

	protected JLabel addOpenBracket()
	{
		return addOpenBracket(defaultColor);
	}

	protected JLabel addOpenBracket(Color color)
	{
		return addTextLabel("[", color);
	}

	protected JLabel addCloseBracket()
	{
		return addCloseBracket(defaultColor);
	}

	protected JLabel addCloseBracket(Color color)
	{
		return addTextLabel("]", color);
	}

	protected JLabel addReverseArrowLabel()
	{
		return addTextLabel("\u2190");
	}

	protected JLabel addAssumptionLabel()
	{
		return addTextLabel("Assumption");
	}

	protected JLabel addIntLabel(int n)
	{
		return addTextLabel(Integer.toString(n));
	}

	protected JLabel addContextLabel()
	{
		return addTextLabel("Context");
	}

	protected JLabel addUnfoldingLabel()
	{
		return addTextLabel("Unfolding");
	}

	protected JLabel addRootLabel()
	{
		return addTextLabel("Root");
	}

	protected JLabel addDeclarationLabel()
	{
		return addTextLabel("Declaration");
	}

	protected JLabel addSpecializationLabel()
	{
		return addTextLabel("Specialization");
	}

	protected JLabel addTickLabel()
	{
		return addTextLabel("\u2713", tickColor);
	}

	protected JLabel addXMarkLabel()
	{
		return addTextLabel("\u2715", xMarkColor);
	}

	protected JLabel addQuestionMarkLabel()
	{
		return addTextLabel("?", getBoldFont(), questionMarkColor);
	}

	protected JLabel addCommaLabel()
	{
		return addTextLabel(",");
	}

	protected JLabel addProjectionTermLabel()
	{
		return addTextLabel("*");
	}

	protected JLabel addNotValidSignatureSymbolLabel()
	{
		return addTextLabel("\u25cb", notValidSignatureSymbolColor);
	}

	protected JLabel addValidSignatureSymbolLabel()
	{
		return addTextLabel("\u25d4", validSignatureSymbolColor);
	}

	protected JLabel addSignedDependenciesSymbolLabel()
	{
		return addTextLabel("\u25d1", signedDependenciesSymbolColor);
	}

	protected JLabel addSignedProofSymbolLabel()
	{
		return addTextLabel("\u25cf", signedProofSymbolColor);
	}

	protected JLabel addSignatureStatusSymbolLabel(StatementAuthority.SignatureStatus status)
	{
		switch (status)
		{
		case NotValid:
			return addNotValidSignatureSymbolLabel();
		case Valid:
			return addValidSignatureSymbolLabel();
		case Dependencies:
			return addSignedDependenciesSymbolLabel();
		case Proof:
			return addSignedProofSymbolLabel();
		default:
			throw new Error();
		}
	}

	protected JLabel addSubscribeStatementsSymbolLabel()
	{
		return addTextLabel("\u2690", subscribeSymbolColor);
	}

	protected JLabel addSubscribeProofSymbolLabel()
	{
		return addTextLabel("\u2691", subscribeSymbolColor);
	}

	protected JLabel addAsterismLabel()
	{
		return addAsterismLabel(defaultColor);
	}

	protected JLabel addAsterismLabel(Color color)
	{
		return addTextLabel("\u2042", FontManager.instance.expandFont(), color);
	}

	protected JLabel addEnvelopeLabel()
	{
		return addEnvelopeLabel(defaultColor);
	}

	protected JLabel addEnvelopeLabel(Color color)
	{
		return addTextLabel("\u2709", color);
	}

	protected JLabel addOpenBoxLabel()
	{
		return addOpenBoxLabel(defaultColor);
	}

	protected JLabel addOpenBoxLabel(Color color)
	{
		return addTextLabel("\u25a1", color);
	}

	protected JLabel addClosedBoxLabel()
	{
		return addClosedBoxLabel(defaultColor);
	}

	protected JLabel addClosedBoxLabel(Color color)
	{
		return addTextLabel("\u25a3", color);
	}

	public void setSelected(boolean selected)
	{
		if (selected)
			setBackground(selectedBackgroundColor);
		else
			setBackground(normalBackgroundColor);
	}

	public void setHasFocus(boolean hasFocus)
	{
		if (hasFocus)
			setBorder(focusBorder);
		else
			setBorder(normalBorder);
	}

	public void cancelEditing()
	{
		for (EditableComponent c : editableComponents)
			c.cancelEditing();
	}

	public void stopEditing()
	{
		for (EditableComponent c : editableComponents)
			c.stopEditing();
	}

	protected static Color getDefaultColor()
	{
		return defaultColor;
	}

	protected static Color getProvenLabelColor()
	{
		return provenLabelColor;
	}

	protected static Color getUnprovenLabelColor()
	{
		return unprovenLabelColor;
	}

	protected static Color getPrivatePersonColor()
	{
		return privatePersonColor;
	}

	protected JLabel addUUIDLabel(UUID uuid)
	{
		return addUUIDLabel(uuid, defaultColor);
	}

	protected JLabel addUUIDLabel(UUID uuid, Color color)
	{
		return addTextLabel(uuid.toString(), color);
	}

	protected JLabel addPersonReference(Person person, Color color)
	{
		return addTextLabel(person.getNick(), color);
	}

	protected JLabel addPersonReference(Person person)
	{
		return addPersonReference(person, defaultColor);
	}

	protected JLabel addDateLabel(Date date)
	{
		return addDateLabel(date, defaultColor);
	}

	protected JLabel addDateLabel(Date date, Color color)
	{
		return addTextLabel(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date), color);
	}

	protected JLabel addTrueLabel()
	{
		return addTextLabel("\u2713", Color.green.darker().darker());
	}

	protected JLabel addFalseLabel()
	{
		return addTextLabel("\u2715", Color.red);
	}

	protected JLabel addBooleanLabel(boolean bool)
	{
		if (bool)
			return addTrueLabel();
		else
			return addFalseLabel();
	}

	protected JLabel addByteSize(int size, Color color)
	{
		return addTextLabel(MiscUtilities.byteSizeToString(size), color);
	}

	protected JLabel addByteSize(int size)
	{
		return addByteSize(size, defaultColor);
	}

}
