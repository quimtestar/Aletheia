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
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import aletheia.model.identifier.Identifier;
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

		public VariableReferenceComponent(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, Term.ParameterNumerator parameterNumerator,
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

	protected VariableReferenceComponent addVariableReferenceComponent(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier,
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

	protected void keyPressedOnEditableVariableName(Statement statement, KeyEvent ev, String text)
	{
	}

	protected class EditableVariableNameComponent extends JPanel implements EditableComponent
	{
		private static final long serialVersionUID = 4545382106986717075L;
		private static final String constLabel = "Label";
		private static final String constTextField = "TextField";

		private final Statement statement;
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
				keyPressedOnEditableVariableName(statement, ev, textField.getText());
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

		public EditableVariableNameComponent(Statement statement)
		{
			super();
			Transaction transaction = beginTransaction();
			try
			{
				this.statement = statement;
				Identifier id = statement.identifier(transaction);
				String idString;
				if (id != null)
					idString = id.toString();
				else
					idString = statement.getVariable().toString();
				this.label = new JLabel(idString);
				this.label.setFont(getActiveFont());
				this.label.setForeground(getDefaultColor());
				this.textField = new JTextField();
				if (id != null)
					this.textField.setText(id.toString());
				this.textField.setFont(getActiveFont());
				this.textField.setForeground(getDefaultColor());
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
			finally
			{
				transaction.abort();
			}
		}

		protected Statement getStatement()
		{
			return statement;
		}

		protected VariableTerm getVariable()
		{
			return statement.getVariable();
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

		public void resetTextField()
		{
			Transaction transaction = getPersistenceManager().beginTransaction();
			try
			{
				Identifier id = statement.identifier(transaction);
				if (id != null)
					textField.setText(id.toString());
				else
					textField.setText("");
			}
			finally
			{
				transaction.abort();
			}
		}

	}

	protected EditableVariableNameComponent addEditableVariableNameComponent(Statement statement)
	{
		EditableVariableNameComponent c = new EditableVariableNameComponent(statement);
		addEditableComponent(c);
		add(c);
		return c;
	}

	protected void addTerm(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, Term term)
	{
		addTerm(variableToIdentifier, term.parameterNumerator(), term);
	}

	protected void addTerm(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, Term.ParameterNumerator parameterNumerator, Term term)
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

	protected void addParameterVariableTerm(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, Term.ParameterNumerator parameterNumerator,
			ParameterVariableTerm parameterVariableTerm)
	{
		addTextLabel(parameterVariableTerm.toString(variableToIdentifier, parameterNumerator));
	}

	protected void addIdentifiableVariableTerm(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, Term.ParameterNumerator parameterNumerator,
			IdentifiableVariableTerm identifiableVariableTerm)
	{
		addVariableReferenceComponent(variableToIdentifier, parameterNumerator, identifiableVariableTerm);
	}

	protected void addCompositionTerm(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, Term.ParameterNumerator parameterNumerator,
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

	protected void addFunctionTerm(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, Term.ParameterNumerator parameterNumerator,
			FunctionTerm functionTerm)
	{
		addOpenFunLabel();
		parameterNumerator.numberParameter(functionTerm.getParameter());
		addParameterVariableTerm(variableToIdentifier, parameterNumerator, functionTerm.getParameter());
		parameterNumerator.unNumberParameter();
		addColonLabel();
		addTerm(variableToIdentifier, parameterNumerator, functionTerm.getParameter().getType());
		addSpaceLabel();
		addArrowLabel();
		addSpaceLabel();
		parameterNumerator.numberParameter(functionTerm.getParameter());
		addTerm(variableToIdentifier, parameterNumerator, functionTerm.getBody());
		parameterNumerator.unNumberParameter();
		addCloseFunLabel();
	}

	protected void addProjectionTerm(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, Term.ParameterNumerator parameterNumerator,
			ProjectionTerm term)
	{
		addFunctionTerm(variableToIdentifier, parameterNumerator, term.getFunction());
		addProjectionTermLabel();
		addSpaceLabel();
	}

	protected void addTTerm(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, Term.ParameterNumerator parameterNumerator, TTerm term)
	{
		addTTermLabel();
	}

}
