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
package aletheia.gui.contextjtree.renderer;

import aletheia.gui.contextjtree.ContextJTree;
import aletheia.model.statement.RootContext;

public class RootContextContextJTreeNodeRenderer extends ContextContextJTreeNodeRenderer<RootContext>
{
	private static final long serialVersionUID = 2931816812478769992L;

	protected RootContextContextJTreeNodeRenderer(ContextJTree contextJTree, RootContext rootContext)
	{
		super(contextJTree, rootContext);
		setActiveFont(getItalicFont());
		addSpaceLabel();
		addOpenSquareBracket();
		addRootLabel();
		addCloseSquareBracket();
	}

}
