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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Collection;

public abstract class AletheiaTransferable implements Transferable
{
	private final Collection<DataFlavor> dataFlavors;

	public AletheiaTransferable(Collection<DataFlavor> dataFlavors)
	{
		this.dataFlavors = dataFlavors;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors()
	{
		return dataFlavors.toArray(new DataFlavor[0]);
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		return dataFlavors.contains(flavor);
	}

	@Override
	public abstract Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException;

}
