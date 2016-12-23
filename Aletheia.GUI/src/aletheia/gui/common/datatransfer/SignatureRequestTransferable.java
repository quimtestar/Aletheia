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
package aletheia.gui.common.datatransfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;

import aletheia.model.authority.SignatureRequest;

public class SignatureRequestTransferable extends AletheiaTransferable
{
	private final SignatureRequest signatureRequest;

	public SignatureRequestTransferable(SignatureRequest signatureRequest)
	{
		super(Arrays.<DataFlavor> asList(SignatureRequestDataFlavor.instance, UUIDDataFlavor.instance, DataFlavor.stringFlavor));
		this.signatureRequest = signatureRequest;
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
	{
		if (flavor.equals(SignatureRequestDataFlavor.instance))
			return signatureRequest;
		else if (flavor.equals(UUIDDataFlavor.instance))
			return signatureRequest.getUuid();
		else if (flavor.equals(DataFlavor.stringFlavor))
			return signatureRequest.getUuid().toString();
		else
			throw new UnsupportedFlavorException(flavor);
	}

}
