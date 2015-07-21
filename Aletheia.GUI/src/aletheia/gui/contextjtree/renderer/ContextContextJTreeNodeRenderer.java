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
package aletheia.gui.contextjtree.renderer;

import aletheia.gui.contextjtree.ContextJTree;
import aletheia.model.statement.Context;
import aletheia.model.statement.Declaration;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.UnfoldingContext;

public abstract class ContextContextJTreeNodeRenderer<C extends Context> extends ProperStatementContextJTreeNodeRenderer<C>
{
	private static final long serialVersionUID = -722490351732400121L;

	protected ContextContextJTreeNodeRenderer(ContextJTree contextJTree, C context)
	{
		super(contextJTree, context);
		setActiveFont(getItalicFont());
		addSpaceLabel();
		addOpenBracket();
		addContextLabel();
		addCloseBracket();
	}

	public static ContextContextJTreeNodeRenderer<?> renderer(ContextJTree contextJTree, Context statement)
	{
		if (statement == null)
			return null;
		if (statement instanceof UnfoldingContext)
			return new UnfoldingContextContextJTreeNodeRenderer(contextJTree, (UnfoldingContext) statement);
		else if (statement instanceof RootContext)
			return new RootContextContextJTreeNodeRenderer(contextJTree, (RootContext) statement);
		else if (statement instanceof Declaration)
			return new DeclarationContextJTreeNodeRenderer(contextJTree, (Declaration) statement);
		else
			return new ProperContextContextJTreeNodeRenderer(contextJTree, statement);
	}

}
