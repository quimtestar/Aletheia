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
package aletheia.utilities.gui;

import java.awt.Component;
import java.awt.Graphics;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JSplitPane;
import javax.swing.plaf.SplitPaneUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

/**
 * An extension of the JSplitPane with the capability of setting de divider
 * position even if the container is not valid. The desired position is saved to
 * use when we get validated.
 *
 * @see #setDividerLocationOrExpandWhenValid(double)
 *
 * @author Quim Testar
 */
public class MyJSplitPane extends JSplitPane
{
	private static final long serialVersionUID = -5114895971103984430L;

	public MyJSplitPane()
	{
	}

	public MyJSplitPane(int newOrientation)
	{
		super(newOrientation);
	}

	public MyJSplitPane(int newOrientation, boolean newContinuousLayout)
	{
		super(newOrientation, newContinuousLayout);
	}

	public MyJSplitPane(int newOrientation, Component newLeftComponent, Component newRightComponent)
	{
		super(newOrientation, newLeftComponent, newRightComponent);
	}

	public MyJSplitPane(int newOrientation, boolean newContinuousLayout, Component newLeftComponent, Component newRightComponent)
	{
		super(newOrientation, newContinuousLayout, newLeftComponent, newRightComponent);
	}

	private boolean setProportionalLocationWhenValid = false;
	private double proportionalLocationWhenValid;

	private synchronized void updateProportionalLocationWhenValid()
	{
		if (setProportionalLocationWhenValid && isValid() && getParent() != null && getParent().isValid() && getParent().isVisible())
		{
			// For unknown reasons, without this delay it doesn't work properly sometimes
			Timer timer = new Timer();
			timer.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					setDividerLocationOrExpand(proportionalLocationWhenValid);
					setLastDividerLocation(getDividerLocation());
				}
			}, 10);
			setProportionalLocationWhenValid = false;
		}
	}

	private BasicSplitPaneDivider getBasicSplitPaneDivider()
	{
		SplitPaneUI ui = getUI();
		if (ui instanceof BasicSplitPaneUI)
			return ((BasicSplitPaneUI) ui).getDivider();
		else
			return null;

	}

	private void expand(int nComp)
	{
		BasicSplitPaneDivider divider = getBasicSplitPaneDivider();
		if (divider != null && (nComp < divider.getComponentCount()))
		{
			Component c = divider.getComponent(nComp);
			if (c instanceof JButton)
				((JButton) c).doClick();
		}
	}

	private void expandLeft()
	{
		expand(0);
	}

	private void expandRight()
	{
		expand(1);
	}

	/**
	 * If proportionalLocation is in the open interval (0,1),
	 * {@link #setDividerLocation(double)}, if not {@link #expandLeft()} or
	 * {@link #expandRight()}.
	 */
	public void setDividerLocationOrExpand(double proportionalLocation)
	{
		if (proportionalLocation <= 0)
			expandLeft();
		else if (proportionalLocation >= 1)
			expandRight();
		else
			setDividerLocation(proportionalLocation);
	}

	@Override
	public void validate()
	{
		synchronized (getTreeLock())
		{
			super.validate();
			updateProportionalLocationWhenValid();
		}
	}

	@Override
	protected void validateTree()
	{
		super.validateTree();
		updateProportionalLocationWhenValid();
	}

	@Override
	public void paint(Graphics g)
	{
		updateProportionalLocationWhenValid();
		super.paint(g);
	}

	/**
	 * If the parent container is valid we call to the
	 * {@link #setDividerLocationOrExpand(double)} inmediately. If not, we save
	 * the value which will be in a subsequent call to {@link #validate()},
	 * {@link #validateTree()} or {@link #paint(Graphics)}.
	 */
	public synchronized void setDividerLocationOrExpandWhenValid(double proportionalLocation)
	{
		if (isValid() && getParent() != null && getParent().isValid())
			setDividerLocationOrExpand(proportionalLocation);
		else
		{
			setProportionalLocationWhenValid = true;
			proportionalLocationWhenValid = proportionalLocation;
		}
	}

}
