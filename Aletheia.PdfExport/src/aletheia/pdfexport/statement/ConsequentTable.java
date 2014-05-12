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
package aletheia.pdfexport.statement;

import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.pdfexport.BasePhrase;
import aletheia.pdfexport.SimpleChunk;
import aletheia.pdfexport.font.FontManager;
import aletheia.pdfexport.term.TermPhrase;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;

public class ConsequentTable extends StatementOrConsequentTable
{

	private final PersistenceManager persistenceManager;
	private final Transaction transaction;
	private final Context context;

	private class TermCellPhrase extends BasePhrase
	{
		private static final long serialVersionUID = -5825016265348182374L;

		public TermCellPhrase()
		{
			super();
			TermPhrase termPhrase = TermPhrase.termPhrase(persistenceManager, transaction, getVariableToIdentifier(), context.getConsequent());
			addSimpleChunk(new SimpleChunk("\u22a2 ", FontManager.instance.getFont(fontSize, BaseColor.ORANGE)));
			addBasePhrase(termPhrase);
		}
	}

	private class SolversCellPhrase extends BasePhrase
	{
		private static final long serialVersionUID = -5825016265348182374L;

		public SolversCellPhrase()
		{
			super();
			addSimpleChunk(new SimpleChunk("Solvers: "));
			boolean first = true;
			for (Statement st : context.solvers(getTransaction()))
			{
				if (!first)
					addSimpleChunk(new SimpleChunk(", "));
				first = false;
				addBasePhrase(TermPhrase.termPhrase(persistenceManager, transaction, getVariableToIdentifier(), st.getVariable()));
			}
		}
	}

	private static class ArrowPhrase extends BasePhrase
	{
		private static final long serialVersionUID = 8468267516108275316L;

		public ArrowPhrase()
		{
			super();
			addSimpleChunk(new SimpleChunk(" "));
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

	public ConsequentTable(Document doc, int depth, Transaction transaction, Context context)
	{
		super(3, context.variableToIdentifier(transaction));
		this.setSplitRows(false);
		this.persistenceManager = context.getPersistenceManager();
		this.transaction = transaction;
		this.context = context;
		TermCellPhrase tcp = new TermCellPhrase();
		SolversCellPhrase scp = new SolversCellPhrase();

		try
		{
			float pcw = depth * (arrowPhrase.getWidthPoint() + 4);
			float pw = doc.getPageSize().getWidth();
			float lm = doc.leftMargin();
			float rm = doc.rightMargin();
			float tw = pw - lm - rm;
			float stw = tw - pcw;
			float termw = Math.max(tw * 0.3f, stw - tw * 0.3f);
			float stbw = stw - termw;

			this.setTotalWidth(new float[]
			{ pcw, termw, stbw });
			this.setLockedWidth(true);
		}
		catch (DocumentException e)
		{
			throw new Error(e);
		}

		addCell(new MyCellArrow());
		addCell(new MyCell(tcp, MyCell.BORDER_ALL & ~MyCell.BORDER_LEFT));
		addCell(new MyCell(scp));
	}

	public Context getContext()
	{
		return context;
	}

	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	public Transaction getTransaction()
	{
		return transaction;
	}

}
