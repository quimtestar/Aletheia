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
package aletheia.gui.common.renderer;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import aletheia.model.identifier.Identifier;
import aletheia.model.parameteridentification.CompositionParameterIdentification;
import aletheia.model.parameteridentification.FunctionParameterIdentification;
import aletheia.model.parameteridentification.ParameterIdentification;
import aletheia.model.statement.Statement;
import aletheia.model.term.CompositionTerm;
import aletheia.model.term.FoldingCastTypeTerm;
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.ProjectedCastTypeTerm;
import aletheia.model.term.ProjectionTerm;
import aletheia.model.term.TauTerm;
import aletheia.model.term.Term;
import aletheia.model.term.UnprojectedCastTypeTerm;
import aletheia.model.term.VariableTerm;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.CombinedMap;

public abstract class AbstractPersistentRenderer extends AbstractRenderer
{
	private final static long serialVersionUID = 3606688660803252832L;

	private final static Color activeLabelColor = Color.red;

	private final PersistenceManager persistenceManager;

	private final boolean clickableVariableReferences;

	public AbstractPersistentRenderer(boolean border, PersistenceManager persistenceManager, boolean clickableVariableReferences)
	{
		super(border);
		this.persistenceManager = persistenceManager;
		this.clickableVariableReferences = clickableVariableReferences;
	}

	public AbstractPersistentRenderer(PersistenceManager persistenceManager, boolean clickableVariableReferences)
	{
		this(false, persistenceManager, clickableVariableReferences);
	}

	protected PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	protected void mouseClickedOnVariableReference(VariableTerm variable, MouseEvent ev)
	{

	}

	protected abstract Transaction beginTransaction();

	private class VariableReferenceComponent extends JLabel implements EditableComponent
	{
		private static final long serialVersionUID = -666209096076198144L;

		private class MyMouseListener implements MouseListener
		{

			// Propagation: to be catched (for example) in the StatementListJTable (thus the drag & drop mechanism works as expected).
			private void propagateEvent(MouseEvent e)
			{
				MouseEvent e_ = new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), e.getX(), e.getY(), e.getClickCount(),
						e.isPopupTrigger(), e.getButton());
				Container parent = AbstractPersistentRenderer.this.getParent();
				e_.setSource(parent);
				Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(e_);
			}

			@Override
			public void mouseClicked(MouseEvent ev)
			{
				mouseClickedOnVariableReference(variable, ev);
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				setForeground(activeLabelColor);
				propagateEvent(e);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				setForeground(labelDefaultColor());
				propagateEvent(e);
			}

			@Override
			public void mousePressed(MouseEvent e)
			{
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
			}

		}

		private class MyHierarchyListener implements HierarchyListener
		{

			@Override
			public void hierarchyChanged(HierarchyEvent e)
			{
				if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0 && isDisplayable())
					setForeground(labelDefaultColor());
			}
		}

		private final VariableTerm variable;

		public VariableReferenceComponent(Map<? extends VariableTerm, Identifier> variableToIdentifier, IdentifiableVariableTerm variable)
		{
			super();
			this.variable = variable;
			setText(variable.toString(variableToIdentifier));
			setForeground(labelDefaultColor());
			setFont(getActiveFont());
			if (clickableVariableReferences)
			{
				addMouseListener(new MyMouseListener());
				addHierarchyListener(new MyHierarchyListener());
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}
		}

		private Color labelDefaultColor()
		{
			Transaction transaction = beginTransaction();
			try
			{
				Statement statement = getPersistenceManager().statements(transaction).get(variable);
				if (statement != null && statement.isProved())
					return getProvenLabelColor();
				else
					return getUnprovenLabelColor();
			}
			finally
			{
				transaction.abort();
			}
		}

		@Override
		public void cancelEditing()
		{
			setForeground(labelDefaultColor());
		}

		@Override
		public void stopEditing()
		{
			setForeground(labelDefaultColor());
		}
	}

	protected VariableReferenceComponent addVariableReferenceComponent(Map<? extends VariableTerm, Identifier> variableToIdentifier,
			IdentifiableVariableTerm variable)
	{
		VariableReferenceComponent c = new VariableReferenceComponent(variableToIdentifier, variable);
		addEditableComponent(c);
		add(c);
		return c;
	}

	protected void addStatementReference(Transaction transaction, Statement statement)
	{
		addVariableReferenceComponent(statement.parentVariableToIdentifier(transaction), statement.getVariable());
	}

	protected abstract class EditableTextLabelComponent extends JPanel implements EditableComponent
	{
		private static final long serialVersionUID = 4545382106986717075L;
		private static final String constLabel = "Label";
		private static final String constTextField = "TextField";

		private final JLabel label;
		private final JTextField textField;

		private class MyListener implements FocusListener, KeyListener
		{

			@Override
			public void focusGained(FocusEvent ev)
			{
				textField.selectAll();
			}

			@Override
			public void focusLost(FocusEvent ev)
			{
				stopEditing();
			}

			@Override
			public void keyPressed(KeyEvent ev)
			{
				EditableTextLabelComponent.this.keyPressed(ev);
			}

			@Override
			public void keyReleased(KeyEvent ev)
			{
			}

			@Override
			public void keyTyped(KeyEvent ev)
			{
			}

		}

		public EditableTextLabelComponent(Color color)
		{
			super();
			this.label = new JLabel();
			this.label.setFont(getActiveFont());
			this.label.setForeground(color);
			this.textField = new JTextField();
			this.textField.setFont(getActiveFont());
			this.textField.setForeground(color);
			MyListener listener = new MyListener();
			this.textField.addFocusListener(listener);
			this.textField.addKeyListener(listener);
			this.textField.setColumns(20);
			CardLayout layout = new CardLayout();
			this.setLayout(layout);
			this.add(this.label, constLabel);
			this.add(this.textField, constTextField);
			layout.show(this, constLabel);
			this.setOpaque(false);
		}

		protected String getLabelText()
		{
			return label.getText();
		}

		protected void setLabelText(String labelText)
		{
			label.setText(labelText);
		}

		protected String getFieldText()
		{
			return textField.getText();
		}

		protected void setFieldText(String fieldText)
		{
			textField.setText(fieldText);
		}

		public void setColor(Color color)
		{
			label.setForeground(color);
			textField.setForeground(color);
		}

		@Override
		public CardLayout getLayout()
		{
			return (CardLayout) super.getLayout();
		}

		@Override
		public void cancelEditing()
		{
			getLayout().show(this, constLabel);
		}

		public void edit()
		{
			getLayout().show(this, constTextField);
			textField.requestFocus();
		}

		@Override
		public void stopEditing()
		{
			getLayout().show(this, constLabel);
		}

		public abstract void keyPressed(KeyEvent ev);

	}

	protected void addTerm(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, Term term)
	{
		addTerm(variableToIdentifier, null, term);
	}

	protected void addTerm(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, ParameterIdentification parameterIdentification, Term term)
	{
		addTerm(variableToIdentifier, term.parameterNumerator(), parameterIdentification, null, term);
	}

	protected void addTerm(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, Term.ParameterNumerator parameterNumerator,
			ParameterIdentification parameterIdentification, Map<ParameterVariableTerm, Identifier> parameterToIdentifier, Term term)
	{
		if (term instanceof ParameterVariableTerm)
			addParameterVariableTerm(parameterNumerator, parameterToIdentifier, (ParameterVariableTerm) term);
		else if (term instanceof IdentifiableVariableTerm)
			addIdentifiableVariableTerm(variableToIdentifier, (IdentifiableVariableTerm) term);
		else if (term instanceof CompositionTerm)
			addCompositionTerm(variableToIdentifier, parameterNumerator, parameterIdentification, parameterToIdentifier, (CompositionTerm) term);
		else if (term instanceof FunctionTerm)
			addFunctionTerm(variableToIdentifier, parameterNumerator, parameterIdentification, parameterToIdentifier, (FunctionTerm) term);
		else if (term instanceof TauTerm)
			addTauTerm();
		else if (term instanceof ProjectionTerm)
			addProjectionTerm(variableToIdentifier, parameterNumerator, parameterIdentification, parameterToIdentifier, (ProjectionTerm) term);
		else if (term instanceof ProjectedCastTypeTerm)
			addProjectedCastTypeTerm(variableToIdentifier, parameterNumerator, parameterIdentification, parameterToIdentifier, (ProjectedCastTypeTerm) term);
		else if (term instanceof UnprojectedCastTypeTerm)
			addUnprojectedCastTypeTerm(variableToIdentifier, parameterNumerator, parameterIdentification, parameterToIdentifier,
					(UnprojectedCastTypeTerm) term);
		else if (term instanceof FoldingCastTypeTerm)
			addFoldingCastTypeTerm(variableToIdentifier, parameterNumerator, parameterIdentification, parameterToIdentifier, (FoldingCastTypeTerm) term);
		else
			throw new Error();
	}

	protected void addTerm(Transaction transaction, Statement statement, ParameterIdentification parameterIdentification, Term term)
	{
		addTerm(statement.parentVariableToIdentifier(transaction), parameterIdentification, term);
	}

	protected void addTerm(Transaction transaction, Statement statement, Term term)
	{
		addTerm(statement.parentVariableToIdentifier(transaction), statement.makeTermParameterIdentification(transaction), term);
	}

	protected void addTerm(Transaction transaction, Statement statement)
	{
		addTerm(transaction, statement, statement.getTerm());
	}

	protected void addParameterVariableTerm(Term.ParameterNumerator parameterNumerator, Map<ParameterVariableTerm, Identifier> parameterToIdentifier,
			ParameterVariableTerm parameterVariableTerm)
	{
		addTextLabel(parameterVariableTerm.toString(parameterToIdentifier, parameterNumerator));
	}

	protected void addIdentifiableVariableTerm(Map<? extends VariableTerm, Identifier> variableToIdentifier, IdentifiableVariableTerm identifiableVariableTerm)
	{
		addVariableReferenceComponent(variableToIdentifier, identifiableVariableTerm);
	}

	protected void addCompositionTerm(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, Term.ParameterNumerator parameterNumerator,
			ParameterIdentification parameterIdentification, Map<ParameterVariableTerm, Identifier> parameterToIdentifier, CompositionTerm compositionTerm)
	{
		ParameterIdentification headParameterIdentification = null;
		ParameterIdentification tailParameterIdentification = null;
		if (parameterIdentification instanceof CompositionParameterIdentification)
		{
			headParameterIdentification = ((CompositionParameterIdentification) parameterIdentification).getHead();
			tailParameterIdentification = ((CompositionParameterIdentification) parameterIdentification).getTail();
		}
		addTerm(variableToIdentifier, parameterNumerator, headParameterIdentification, parameterToIdentifier, compositionTerm.getHead());
		addSpaceLabel();
		if (compositionTerm.getTail() instanceof CompositionTerm)
			addOpenParLabel();
		addTerm(variableToIdentifier, parameterNumerator, tailParameterIdentification, parameterToIdentifier, compositionTerm.getTail());
		if (compositionTerm.getTail() instanceof CompositionTerm)
			addCloseParLabel();
	}

	protected void addFunctionTerm(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, Term.ParameterNumerator parameterNumerator,
			ParameterIdentification parameterIdentification, Map<ParameterVariableTerm, Identifier> parameterToIdentifier, FunctionTerm functionTerm)
	{
		Map<ParameterVariableTerm, Identifier> localParameterToIdentifier = new HashMap<>();
		Map<ParameterVariableTerm, Identifier> totalParameterToIdentifier = parameterToIdentifier == null ? localParameterToIdentifier
				: new CombinedMap<>(localParameterToIdentifier, parameterToIdentifier);
		addOpenFunLabel();
		Term term = functionTerm;
		boolean first = true;
		int numberedParameters = 0;
		while (term instanceof FunctionTerm)
		{
			ParameterVariableTerm parameter = ((FunctionTerm) term).getParameter();
			Term body = ((FunctionTerm) term).getBody();
			Identifier parameterParameterIdentification = null;
			ParameterIdentification domainParameterIdentification = null;
			ParameterIdentification bodyParameterIdentification = null;
			if (parameterIdentification instanceof FunctionParameterIdentification)
			{
				parameterParameterIdentification = ((FunctionParameterIdentification) parameterIdentification).getParameter();
				domainParameterIdentification = ((FunctionParameterIdentification) parameterIdentification).getDomain();
				bodyParameterIdentification = ((FunctionParameterIdentification) parameterIdentification).getBody();
			}
			if (!first)
			{
				addCommaLabel();
				addSpaceLabel();
			}
			pushComponentList();
			addTerm(variableToIdentifier, parameterNumerator, domainParameterIdentification, totalParameterToIdentifier, parameter.getType());
			List<Component> parameterTypeComponentList = popComponentList();
			if (body.isFreeVariable(parameter))
			{
				if (parameterParameterIdentification == null)
				{
					parameterNumerator.numberParameter(parameter);
					numberedParameters++;
				}
				else
					localParameterToIdentifier.put(parameter, parameterParameterIdentification);
				addParameterVariableTerm(parameterNumerator, localParameterToIdentifier, parameter);
				addColonLabel();
			}
			add(parameterTypeComponentList);
			first = false;
			term = body;
			parameterIdentification = bodyParameterIdentification;
		}
		addSpaceLabel();
		addArrowLabel();
		addSpaceLabel();
		addTerm(variableToIdentifier, parameterNumerator, parameterIdentification, totalParameterToIdentifier, term);
		parameterNumerator.unNumberParameters(numberedParameters);
		addCloseFunLabel();
	}

	protected void addProjectionTerm(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, Term.ParameterNumerator parameterNumerator,
			ParameterIdentification parameterIdentification, Map<ParameterVariableTerm, Identifier> parameterToIdentifier, ProjectionTerm projectionTerm)
	{
		Term term = projectionTerm;
		Stack<ParameterVariableTerm> stack = new Stack<>();
		while (term instanceof ProjectionTerm)
		{
			FunctionTerm function = ((ProjectionTerm) term).getFunction();
			stack.push(function.getParameter());
			term = function.getBody();
		}
		int nProjections = stack.size();
		while (!stack.isEmpty())
		{
			term = new FunctionTerm(stack.pop(), term);
		}
		addFunctionTerm(variableToIdentifier, parameterNumerator, parameterIdentification, parameterToIdentifier, (FunctionTerm) term);
		if (nProjections <= 3)
			for (int i = 0; i < nProjections; i++)
				addProjectionTermLabel();
		else
		{
			addProjectionTermLabel();
			addTextLabel(Integer.toString(nProjections));
		}
	}

	protected void addTauTerm()
	{
		addTauTermLabel();
	}

	protected void addProjectedCastTypeTerm(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, Term.ParameterNumerator parameterNumerator,
			ParameterIdentification parameterIdentification, Map<ParameterVariableTerm, Identifier> parameterToIdentifier, ProjectedCastTypeTerm term)
	{
		addOpenSquareBracket();
		addTerm(variableToIdentifier, parameterNumerator, parameterIdentification, parameterToIdentifier, term.getTerm());
		addCloseSquareBracket();
	}

	protected void addUnprojectedCastTypeTerm(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, Term.ParameterNumerator parameterNumerator,
			ParameterIdentification parameterIdentification, Map<ParameterVariableTerm, Identifier> parameterToIdentifier, UnprojectedCastTypeTerm term)
	{
		addOpenCurlyBracket();
		addTerm(variableToIdentifier, parameterNumerator, parameterIdentification, parameterToIdentifier, term.getTerm());
		addCloseCurlyBracket();
	}

	protected void addFoldingCastTypeTerm(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, Term.ParameterNumerator parameterNumerator,
			ParameterIdentification parameterIdentification, Map<ParameterVariableTerm, Identifier> parameterToIdentifier, FoldingCastTypeTerm term)
	{
		addOpenParLabel();
		addTerm(variableToIdentifier, parameterNumerator, parameterIdentification, parameterToIdentifier, term.getTerm());
		addColonLabel();
		addTerm(variableToIdentifier, parameterNumerator, null, parameterToIdentifier, term.getType());
		addSpaceLabel();
		addPipeLabel();
		addSpaceLabel();
		addTerm(variableToIdentifier, parameterNumerator, null, parameterToIdentifier, term.getValue());
		addSpaceLabel();
		addLeftArrowLabel();
		addSpaceLabel();
		addIdentifiableVariableTerm(variableToIdentifier, term.getVariable());
		addCloseParLabel();
	}

}
