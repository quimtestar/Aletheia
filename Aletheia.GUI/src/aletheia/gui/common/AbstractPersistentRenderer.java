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

import java.awt.CardLayout;
import java.awt.Color;
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
import java.util.Iterator;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Assumption;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.model.term.CompositionTerm;
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.ProjectionTerm;
import aletheia.model.term.TTerm;
import aletheia.model.term.Term;
import aletheia.model.term.VariableTerm;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.AdaptedMap;
import aletheia.utilities.collections.CombinedMap;

public abstract class AbstractPersistentRenderer extends AbstractRenderer
{
	private final static long serialVersionUID = 3606688660803252832L;

	private final static Color activeLabelColor = Color.red;

	private final PersistenceManager persistenceManager;

	private final boolean clickableVariableReferences;

	public AbstractPersistentRenderer(PersistenceManager persistenceManager, boolean clickableVariableReferences)
	{
		super();
		this.persistenceManager = persistenceManager;
		this.clickableVariableReferences = clickableVariableReferences;
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

		public VariableReferenceComponent(Map<? extends VariableTerm, Identifier> variableToIdentifier, Term.ParameterNumerator parameterNumerator,
				IdentifiableVariableTerm variable)
		{
			super();
			this.variable = variable;
			setText(variable.toString(variableToIdentifier, parameterNumerator));
			setForeground(labelDefaultColor());
			setFont(getActiveFont());
			if (clickableVariableReferences)
			{
				addMouseListener(new MyMouseListener());
				addHierarchyListener(new MyHierarchyListener());
			}
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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
			Term.ParameterNumerator parameterNumerator, IdentifiableVariableTerm variable)
	{
		VariableReferenceComponent c = new VariableReferenceComponent(variableToIdentifier, parameterNumerator, variable);
		addEditableComponent(c);
		add(c);
		return c;
	}

	protected void addStatementReference(Transaction transaction, Statement statement)
	{
		addVariableReferenceComponent(statement.parentVariableToIdentifier(transaction), null, statement.getVariable());
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

	protected void addTerm(Map<? extends VariableTerm, Identifier> variableToIdentifier, Term term)
	{
		addTerm(variableToIdentifier, term.parameterNumerator(), term);
	}

	protected void addTerm(Map<? extends VariableTerm, Identifier> variableToIdentifier, Term.ParameterNumerator parameterNumerator, Term term)
	{
		if (term instanceof ParameterVariableTerm)
			addParameterVariableTerm(variableToIdentifier, parameterNumerator, (ParameterVariableTerm) term);
		else if (term instanceof IdentifiableVariableTerm)
			addIdentifiableVariableTerm(variableToIdentifier, parameterNumerator, (IdentifiableVariableTerm) term);
		else if (term instanceof CompositionTerm)
			addCompositionTerm(variableToIdentifier, parameterNumerator, (CompositionTerm) term);
		else if (term instanceof FunctionTerm)
			addFunctionTerm(variableToIdentifier, parameterNumerator, (FunctionTerm) term);
		else if (term instanceof TTerm)
			addTTerm(variableToIdentifier, parameterNumerator, (TTerm) term);
		else if (term instanceof ProjectionTerm)
			addProjectionTerm(variableToIdentifier, parameterNumerator, (ProjectionTerm) term);
		else
			throw new Error();
	}

	protected void addTerm(Transaction transaction, Statement statement)
	{

		Map<? extends VariableTerm, Identifier> variableToIdentifier = statement.parentVariableToIdentifier(transaction);
		if (statement instanceof Context)
		{
			Map<ParameterVariableTerm, Identifier> localVariableToIdentifier = new HashMap<ParameterVariableTerm, Identifier>();
			{
				Term body = statement.getTerm();
				Iterator<Assumption> assumptionIterator = ((Context) statement).assumptions(transaction).iterator();
				while (body instanceof FunctionTerm)
				{
					FunctionTerm function = (FunctionTerm) body;
					Assumption assumption = null;
					if (assumptionIterator.hasNext())
						assumption = assumptionIterator.next();
					if (function.getBody().freeVariables().contains(function.getParameter()) && assumption != null && assumption.getIdentifier() != null)
						localVariableToIdentifier.put(function.getParameter(), assumption.getIdentifier());
					body = function.getBody();
				}
			}
			variableToIdentifier = new CombinedMap<VariableTerm, Identifier>(new AdaptedMap<VariableTerm, Identifier>(localVariableToIdentifier),
					new AdaptedMap<VariableTerm, Identifier>(variableToIdentifier));
		}
		addTerm(variableToIdentifier, statement.getTerm());
	}

	protected void addParameterVariableTerm(Map<? extends VariableTerm, Identifier> variableToIdentifier, Term.ParameterNumerator parameterNumerator,
			ParameterVariableTerm parameterVariableTerm)
	{
		addTextLabel(parameterVariableTerm.toString(variableToIdentifier, parameterNumerator));
	}

	protected void addIdentifiableVariableTerm(Map<? extends VariableTerm, Identifier> variableToIdentifier, Term.ParameterNumerator parameterNumerator,
			IdentifiableVariableTerm identifiableVariableTerm)
	{
		addVariableReferenceComponent(variableToIdentifier, parameterNumerator, identifiableVariableTerm);
	}

	protected void addCompositionTerm(Map<? extends VariableTerm, Identifier> variableToIdentifier, Term.ParameterNumerator parameterNumerator,
			CompositionTerm compositionTerm)
	{
		addTerm(variableToIdentifier, parameterNumerator, compositionTerm.getHead());
		addSpaceLabel();
		if (compositionTerm.getTail() instanceof CompositionTerm)
			addOpenParLabel();
		addTerm(variableToIdentifier, parameterNumerator, compositionTerm.getTail());
		if (compositionTerm.getTail() instanceof CompositionTerm)
			addCloseParLabel();
	}

	protected void addFunctionTerm(Map<? extends VariableTerm, Identifier> variableToIdentifier, Term.ParameterNumerator parameterNumerator,
			FunctionTerm functionTerm)
	{
		boolean mappedParameter = variableToIdentifier.containsKey(functionTerm.getParameter());
		addOpenFunLabel();
		if (!mappedParameter)
			parameterNumerator.numberParameter(functionTerm.getParameter());
		addParameterVariableTerm(variableToIdentifier, parameterNumerator, functionTerm.getParameter());
		if (!mappedParameter)
			parameterNumerator.unNumberParameter();
		addColonLabel();
		addTerm(variableToIdentifier, parameterNumerator, functionTerm.getParameter().getType());
		addSpaceLabel();
		addArrowLabel();
		addSpaceLabel();
		if (!mappedParameter)
			parameterNumerator.numberParameter(functionTerm.getParameter());
		addTerm(variableToIdentifier, parameterNumerator, functionTerm.getBody());
		if (!mappedParameter)
			parameterNumerator.unNumberParameter();
		addCloseFunLabel();
	}

	protected void addProjectionTerm(Map<? extends VariableTerm, Identifier> variableToIdentifier, Term.ParameterNumerator parameterNumerator,
			ProjectionTerm term)
	{
		addFunctionTerm(variableToIdentifier, parameterNumerator, term.getFunction());
		addProjectionTermLabel();
		addSpaceLabel();
	}

	protected void addTTerm(Map<? extends VariableTerm, Identifier> variableToIdentifier, Term.ParameterNumerator parameterNumerator, TTerm term)
	{
		addTTermLabel();
	}

}
