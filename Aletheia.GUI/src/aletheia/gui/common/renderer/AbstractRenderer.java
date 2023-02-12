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
package aletheia.gui.common.renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.LayoutManager;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import aletheia.gui.fonts.FontManager;
import aletheia.gui.lookandfeel.AletheiaLookAndFeel;
import aletheia.gui.lookandfeel.AletheiaTheme;
import aletheia.model.authority.Person;
import aletheia.model.authority.StatementAuthority;
import aletheia.utilities.MiscUtilities;

public abstract class AbstractRenderer extends JPanel
{
	private static final long serialVersionUID = -9049293577574541041L;

	private static final Color darkGreen = Color.green.darker().darker();
	private static final Color darkOrange = Color.orange.darker().darker();
	private static final Color unprovenLabelColor = darkOrange;
	private static final Color turnstileColor = Color.orange;
	private static final Color privatePersonColor = darkGreen;
	private static final Border emptyBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
	private static final Color focusBorderColor = Color.blue;

	protected static AletheiaTheme theme()
	{
		return AletheiaLookAndFeel.theme();
	}

	protected static Color getColor(AletheiaTheme.Key key)
	{
		return theme().get(key);
	}

	protected static Color getDefaultNormalBackgroundColor()
	{
		return theme().getNormalBackground();
	}

	protected static Color getDefaultSelectedBackgroundColor()
	{
		return theme().getSelectedBackground();
	}

	protected static Color getDefaultColor()
	{
		return theme().getDefault();
	}

	protected static Color getProvenLabelColor()
	{
		return theme().getProvenLabel();
	}

	protected static Color getUnprovenLabelColor()
	{
		return unprovenLabelColor;
	}

	protected static Color getPrivatePersonColor()
	{
		return privatePersonColor;
	}

	protected static Color getGroupSorterColor()
	{
		return theme().getGroupSorter();
	}

	protected static Color getTurnstileColor()
	{
		return turnstileColor;
	}

	protected static Color getActiveContextColor()
	{
		return theme().getActiveContext();
	}

	protected interface EditableComponent
	{
		public void cancelEditing();

		public void stopEditing();
	}

	private final FontManager fontManager;

	private final Set<EditableComponent> editableComponents;

	private final boolean withBorder;

	private final Stack<List<Component>> stack;

	private boolean hasFocus;

	private Font activeFont;

	private Color normalBackgroundColor = getDefaultNormalBackgroundColor();
	private Color selectedBackgroundColor = getDefaultSelectedBackgroundColor();

	public AbstractRenderer(FontManager fontManager, boolean withBorder)
	{
		super();
		this.fontManager = fontManager;
		this.editableComponents = new HashSet<>();
		this.withBorder = withBorder;
		this.stack = new Stack<>();

		this.activeFont = getDefaultFont();
		FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
		layout.setHgap(0);
		layout.setVgap(0);
		setLayout(layout);
		setBackground(getNormalBackgroundColor());
		if (withBorder)
			setBorder(emptyBorder);
	}

	public AbstractRenderer(FontManager fontManager)
	{
		this(fontManager, false);
	}

	protected FontManager getFontManager()
	{
		return fontManager;
	}

	protected void pushComponentList()
	{
		stack.push(new ArrayList<Component>());
	}

	protected List<Component> popComponentList()
	{
		return stack.pop();
	}

	@Override
	public Component add(Component comp)
	{
		if (stack.isEmpty())
			return super.add(comp);
		else
		{
			stack.peek().add(comp);
			return comp;
		}
	}

	protected void add(Collection<Component> list)
	{
		for (Component comp : list)
			add(comp);
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
		return fontManager.defaultFont();
	}

	protected Font getBoldFont()
	{
		return fontManager.boldFont();
	}

	protected Font getItalicFont()
	{
		return fontManager.italicFont();
	}

	protected Font getActiveFont()
	{
		return activeFont;
	}

	protected void setActiveFont(Font font)
	{
		activeFont = font;
	}

	protected Font styledActiveFont(int style)
	{
		return getActiveFont().deriveFont(style);
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

	private JLabel addTextLabel(String text, Font font, AletheiaTheme.Key colorKey)
	{
		JLabel label = new JLabel(text)
		{
			private static final long serialVersionUID = 2952752479616538516L;

			@Override
			public void updateUI()
			{
				super.updateUI();
				setFont(font);
				setForeground(getColor(colorKey));
			}
		};
		label.setFont(font);
		label.setForeground(getColor(colorKey));
		add(label);
		return label;
	}

	protected JLabel addTextLabel(String text)
	{
		return addTextLabel(text, getActiveFont(), AletheiaTheme.Key.default_);
	}

	protected JLabel addTextLabel(String text, Font font)
	{
		return addTextLabel(text, font, AletheiaTheme.Key.default_);
	}

	protected JLabel addTextLabel(String text, AletheiaTheme.Key colorKey)
	{
		return addTextLabel(text, getActiveFont(), colorKey);
	}

	protected JLabel addTauTermLabel()
	{
		return addTextLabel("\u03a4", styledActiveFont(Font.BOLD));
	}

	protected JLabel addOpenFunLabel()
	{
		return addTextLabel("<");
	}

	protected JLabel addColonLabel()
	{
		return addTextLabel(":");
	}

	protected JLabel addSemiColonLabel()
	{
		return addTextLabel(";");
	}

	protected JLabel addPipeLabel()
	{
		return addTextLabel("|");
	}

	protected JLabel addAlmostEqualLabel()
	{
		return addTextLabel("\u2248");
	}

	protected JLabel addArrowLabel()
	{
		return addTextLabel("\u2192");
	}

	protected JLabel addLeftArrowLabel()
	{
		return addTextLabel("\u2190");
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
		return addTextLabel("\u22a2", AletheiaTheme.Key.turnstile);
	}

	protected JLabel addSpaceLabel(int l)
	{
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < l; i++)
			b.append(' ');
		return addTextLabel(b.toString());
	}

	protected JLabel addSpaceLabel()
	{
		return addSpaceLabel(1);
	}

	protected JLabel addOpenSquareBracket()
	{
		return addOpenSquareBracket(AletheiaTheme.Key.default_);
	}

	protected JLabel addOpenSquareBracket(AletheiaTheme.Key colorKey)
	{
		return addTextLabel("[", colorKey);
	}

	protected JLabel addCloseSquareBracket()
	{
		return addCloseSquareBracket(AletheiaTheme.Key.default_);
	}

	protected JLabel addCloseSquareBracket(AletheiaTheme.Key colorKey)
	{
		return addTextLabel("]", colorKey);
	}

	protected JLabel addOpenCurlyBracket()
	{
		return addOpenCurlyBracket(AletheiaTheme.Key.default_);
	}

	protected JLabel addOpenCurlyBracket(AletheiaTheme.Key colorKey)
	{
		return addTextLabel("{", colorKey);
	}

	protected JLabel addCloseCurlyBracket()
	{
		return addCloseCurlyBracket(AletheiaTheme.Key.default_);
	}

	protected JLabel addCloseCurlyBracket(AletheiaTheme.Key colorKey)
	{
		return addTextLabel("}", colorKey);
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
		return addTextLabel("\u2713", AletheiaTheme.Key.tick);
	}

	protected JLabel addXMarkLabel()
	{
		return addTextLabel("\u2715", AletheiaTheme.Key.xMark);
	}

	protected JLabel addQuestionMarkLabel()
	{
		return addTextLabel("?", getBoldFont(), AletheiaTheme.Key.questionMark);
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
		return addTextLabel("\u25cb", AletheiaTheme.Key.notValidSignatureSymbol);
	}

	protected JLabel addValidSignatureSymbolLabel()
	{
		return addTextLabel("\u25d4", AletheiaTheme.Key.validSignatureSymbol);
	}

	protected JLabel addSignedDependenciesSymbolLabel()
	{
		return addTextLabel("\u25d1", AletheiaTheme.Key.signedDependenciesSymbol);
	}

	protected JLabel addSignedProofSymbolLabel()
	{
		return addTextLabel("\u25cf", AletheiaTheme.Key.signedProofSymbol);
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
		return addTextLabel("\u2690", AletheiaTheme.Key.subscribeSymbol);
	}

	protected JLabel addSubscribeProofSymbolLabel()
	{
		return addTextLabel("\u2691", AletheiaTheme.Key.subscribeSymbol);
	}

	protected JLabel addAsterismLabel()
	{
		return addAsterismLabel(AletheiaTheme.Key.default_);
	}

	protected JLabel addAsterismLabel(AletheiaTheme.Key colorKey)
	{
		return addTextLabel("\u2042", fontManager.expandFont(), colorKey);
	}

	protected JLabel addEnvelopeLabel()
	{
		return addEnvelopeLabel(AletheiaTheme.Key.default_);
	}

	protected JLabel addEnvelopeLabel(AletheiaTheme.Key colorKey)
	{
		return addTextLabel("\u2709", colorKey);
	}

	protected JLabel addOpenBoxLabel()
	{
		return addOpenBoxLabel(AletheiaTheme.Key.default_);
	}

	protected JLabel addOpenBoxLabel(AletheiaTheme.Key colorKey)
	{
		return addTextLabel("\u25a1", colorKey);
	}

	protected JLabel addClosedBoxLabel()
	{
		return addClosedBoxLabel(AletheiaTheme.Key.default_);
	}

	protected JLabel addClosedBoxLabel(AletheiaTheme.Key colorKey)
	{
		return addTextLabel("\u25a3", colorKey);
	}

	protected JLabel addExpandedGroupSorterLabel(AletheiaTheme.Key colorKey)
	{
		return addTextLabel("\u21b3", colorKey);
	}

	protected JLabel addExpandedGroupSorterLabel()
	{
		return addExpandedGroupSorterLabel(AletheiaTheme.Key.groupSorter);
	}

	protected JLabel addCollapsedGroupSorterLabel(AletheiaTheme.Key colorKey)
	{
		return addTextLabel("\u2192", colorKey);
	}

	protected JLabel addCollapsedGroupSorterLabel()
	{
		return addCollapsedGroupSorterLabel(AletheiaTheme.Key.groupSorter);
	}

	public void setSelected(boolean selected)
	{
		if (selected)
			setBackground(selectedBackgroundColor);
		else
			setBackground(normalBackgroundColor);
	}

	protected void setBorderColor(Color color)
	{
		if (withBorder)
		{
			if (color == null)
				setBorder(emptyBorder);
			else
				setBorder(BorderFactory.createLineBorder(color, 1));
		}
	}

	@SuppressWarnings("unused")
	private Color getBorderColor()
	{
		if (withBorder)
		{
			Border border = getBorder();
			if (border instanceof LineBorder)
				return ((LineBorder) border).getLineColor();
			else
				return null;
		}
		else
			return null;
	}

	public void setHasFocus(boolean hasFocus)
	{
		this.hasFocus = hasFocus;
		updateBorderColor();
	}

	protected void updateBorderColor()
	{
		if (hasFocus)
			setBorderColor(focusBorderColor);
		else
			setBorderColor(null);
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

	protected JLabel addUUIDLabel(UUID uuid)
	{
		return addUUIDLabel(uuid, AletheiaTheme.Key.default_);
	}

	protected JLabel addUUIDLabel(UUID uuid, AletheiaTheme.Key colorKey)
	{
		return addTextLabel(uuid.toString(), colorKey);
	}

	protected JLabel addPersonReference(Person person, AletheiaTheme.Key colorKey)
	{
		return addTextLabel(person.getNick(), colorKey);
	}

	protected JLabel addPersonReference(Person person)
	{
		return addPersonReference(person, AletheiaTheme.Key.default_);
	}

	protected JLabel addDateLabel(Date date)
	{
		return addDateLabel(date, AletheiaTheme.Key.default_);
	}

	protected JLabel addDateLabel(Date date, AletheiaTheme.Key colorKey)
	{
		return addTextLabel(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date), colorKey);
	}

	protected JLabel addTrueLabel()
	{
		return addTextLabel("\u2713", AletheiaTheme.Key.true_);
	}

	protected JLabel addFalseLabel()
	{
		return addTextLabel("\u2715", AletheiaTheme.Key.false_);
	}

	protected JLabel addBooleanLabel(boolean bool)
	{
		if (bool)
			return addTrueLabel();
		else
			return addFalseLabel();
	}

	protected JLabel addByteSize(int size, AletheiaTheme.Key colorKey)
	{
		return addTextLabel(MiscUtilities.byteSizeToString(size), colorKey);
	}

	protected JLabel addByteSize(int size)
	{
		return addByteSize(size, AletheiaTheme.Key.default_);
	}

}
