/*******************************************************************************
 * Copyright (c) 2016, 2019 Quim Testar.
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
package aletheia.gui.app;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.HeadlessException;
import java.awt.Window;

import javax.swing.JFrame;

import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.utilities.gui.MyJSplitPane;

public abstract class AletheiaJFrame extends JFrame
{
	private static final long serialVersionUID = -5082905593301164532L;

	private static class MyFocusTraversalPolicy extends FocusTraversalPolicy
	{
		private final FocusTraversalPolicy originalPolicy;

		public MyFocusTraversalPolicy(FocusTraversalPolicy originalPolicy)
		{
			this.originalPolicy = originalPolicy;
		}

		private interface ComponentGetter
		{
			public Component get(Container container, Component component);
		}

		private Component findNotCollapsed(Container container, Component component, ComponentGetter getter)
		{
			Component c = component;
			while (true)
			{
				boolean collapsed = false;
				Component p = c;
				while (p != null && p != container)
				{
					Component p_ = p;
					p = p.getParent();
					if (p instanceof MyJSplitPane)
					{
						MyJSplitPane s = (MyJSplitPane) p;
						if (p_ == s.getLeftComponent() && s.isCollapsedLeft())
						{
							collapsed = true;
							break;
						}
						if (p_ == s.getRightComponent() && s.isCollapsedRight())
						{
							collapsed = true;
							break;
						}
					}
				}
				if (!collapsed)
					break;
				c = getter.get(container, c);
				if (c == component)
					break;
			}
			return c;
		}

		private Component nextNotCollapsed(Container container, Component component)
		{
			return findNotCollapsed(container, component, originalPolicy::getComponentAfter);
		}

		private Component prevNotCollapsed(Container container, Component component)
		{
			return findNotCollapsed(container, component, originalPolicy::getComponentBefore);
		}

		@Override
		public Component getComponentAfter(Container aContainer, Component aComponent)
		{
			return nextNotCollapsed(aContainer, originalPolicy.getComponentAfter(aContainer, aComponent));
		}

		@Override
		public Component getComponentBefore(Container aContainer, Component aComponent)
		{
			return prevNotCollapsed(aContainer, originalPolicy.getComponentBefore(aContainer, aComponent));
		}

		@Override
		public Component getFirstComponent(Container aContainer)
		{
			return nextNotCollapsed(aContainer, originalPolicy.getFirstComponent(aContainer));
		}

		@Override
		public Component getLastComponent(Container aContainer)
		{
			return prevNotCollapsed(aContainer, originalPolicy.getLastComponent(aContainer));
		}

		@Override
		public Component getDefaultComponent(Container aContainer)
		{
			return nextNotCollapsed(aContainer, originalPolicy.getDefaultComponent(aContainer));
		}

		@Override
		public Component getInitialComponent(Window window)
		{
			return nextNotCollapsed(window, originalPolicy.getInitialComponent(window));
		}

	}

	public AletheiaJFrame() throws HeadlessException
	{
		super();
		setFocusTraversalPolicy(new MyFocusTraversalPolicy(getFocusTraversalPolicy()));
	}

	public abstract void setExtraTitle(String extraTitle);

	public abstract void exit();

	public abstract void selectStatement(Statement statement);

	public abstract void setActiveContext(Context context);

	public void resetedGui()
	{

	}

}
