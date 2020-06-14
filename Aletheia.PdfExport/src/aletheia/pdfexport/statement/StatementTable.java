/*******************************************************************************
 * Copyright (c) 2014, 2018 Quim Testar.
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
package aletheia.pdfexport.statement;

import java.util.Map;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Assumption;
import aletheia.model.statement.Context;
import aletheia.model.statement.Declaration;
import aletheia.model.statement.Specialization;
import aletheia.model.statement.Statement;
import aletheia.model.statement.UnfoldingContext;
import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.pdfexport.BasePhrase;
import aletheia.pdfexport.SimpleChunk;
import aletheia.pdfexport.font.FontManager;
import aletheia.pdfexport.term.IdentifiableVariableTermAnchorPhrase;
import aletheia.pdfexport.term.TermPhrase;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;

public abstract class StatementTable extends StatementOrConsequentTable
{

	private final PersistenceManager persistenceManager;
	private final Transaction transaction;
	private final Statement statement;

	private class VariableCellPhrase extends BasePhrase
	{
		private static final long serialVersionUID = -2481798513844016809L;

		public VariableCellPhrase()
		{
			super();
			if (statement.isProved())
				addSimpleChunk(new SimpleChunk("\u2713", FontManager.instance.getFont(fontSize, BaseColor.GREEN)));
			else
				addSimpleChunk(new SimpleChunk("?", FontManager.instance.getFont(fontSize, BaseColor.RED)));
			addSimpleChunk(new SimpleChunk(" "));
			addBasePhrase(new IdentifiableVariableTermAnchorPhrase(statement.parentVariableToIdentifier(transaction), statement.getVariable()));

		}
	}

	private class TermCellPhrase extends BasePhrase
	{
		private static final long serialVersionUID = -5825016265348182374L;

		public TermCellPhrase()
		{
			super();
			TermPhrase termPhrase = TermPhrase.termPhrase(persistenceManager, transaction, variableToIdentifier(), statement.getTermParameterIdentification(),
					statement.getTerm());
			addSimpleChunk(new SimpleChunk(":"));
			addBasePhrase(termPhrase);
		}
	}

	private static class ArrowPhrase extends BasePhrase
	{
		private static final long serialVersionUID = 8468267516108275316L;

		public ArrowPhrase()
		{
			super();
			addSimpleChunk(new SimpleChunk("\u21b3"));
		}
	}

	private final static ArrowPhrase arrowPhrase = new ArrowPhrase();

	private class MyCellArrow extends MyCell
	{

		public MyCellArrow()
		{
			super(arrowPhrase, BORDER_ALL & ~BORDER_RIGHT);
			this.setHorizontalAlignment(ALIGN_RIGHT);
		}

	}

	public StatementTable(Document doc, int depth, Transaction transaction, Statement statement)
	{
		super(4);
		this.setSplitRows(false);
		this.persistenceManager = statement.getPersistenceManager();
		this.transaction = transaction;
		this.statement = statement;
		VariableCellPhrase vcp = new VariableCellPhrase();
		TermCellPhrase tcp = new TermCellPhrase();

		try
		{
			float pcw = depth * (arrowPhrase.getWidthPoint() + 4);
			float pw = doc.getPageSize().getWidth();
			float lm = doc.leftMargin();
			float rm = doc.rightMargin();
			float tw = pw - lm - rm;
			float vcpw = vcp.getWidthPoint() + 4;
			float stw = tw - pcw - vcpw;
			float termw = Math.max(tw * 0.3f, stw - tw * 0.3f);
			float stbw = stw - termw;

			this.setTotalWidth(new float[]
			{ pcw, vcpw, termw, stbw });
			this.setLockedWidth(true);
		}
		catch (DocumentException e)
		{
			throw new Error(e);
		}

		addCell(new MyCellArrow());
		addCell(new MyCell(vcp, MyCell.BORDER_TOP | MyCell.BORDER_BOTTOM));
		addCell(new MyCell(tcp, MyCell.BORDER_ALL & ~MyCell.BORDER_LEFT));
	}

	public Statement getStatement()
	{
		return statement;
	}

	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	public Transaction getTransaction()
	{
		return transaction;
	}

	@Override
	protected Map<IdentifiableVariableTerm, Identifier> variableToIdentifier()
	{
		return statement.parentVariableToIdentifier(transaction);
	}

	public static StatementTable statementTable(Document doc, int depth, Transaction transaction, Statement statement)
	{
		if (statement instanceof Assumption)
			return new AssumptionTable(doc, depth, transaction, (Assumption) statement);
		else if (statement instanceof Specialization)
			return new SpecializationTable(doc, depth, transaction, (Specialization) statement);
		else if (statement instanceof Context)
		{
			if (statement instanceof UnfoldingContext)
				return new UnfoldingContextTable(doc, depth, transaction, (UnfoldingContext) statement);
			else
				return new ContextTable(doc, depth, transaction, (Context) statement);
		}
		else if (statement instanceof Declaration)
			return new DeclarationTable(doc, depth, transaction, (Declaration) statement);
		else
			throw new Error();
	}

}
