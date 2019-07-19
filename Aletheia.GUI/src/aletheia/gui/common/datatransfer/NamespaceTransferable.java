/*******************************************************************************
 * Copyright (c) 2014, 2016 Quim Testar.
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
package aletheia.gui.common.datatransfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;

import aletheia.model.identifier.Namespace;

public class NamespaceTransferable extends AletheiaTransferable
{
	private final Namespace namespace;

	public NamespaceTransferable(Namespace namespace)
	{
		super(Arrays.<DataFlavor> asList(NamespaceDataFlavor.instance, DataFlavor.stringFlavor));
		this.namespace = namespace;
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
	{
		if (flavor.equals(NamespaceDataFlavor.instance))
			return namespace;
		else if (flavor.equals(DataFlavor.stringFlavor))
			return namespace.qualifiedName();
		else
			throw new UnsupportedFlavorException(flavor);
	}

}
