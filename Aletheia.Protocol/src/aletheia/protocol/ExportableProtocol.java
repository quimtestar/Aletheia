/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
 * 
 * This file is part of the Aletheia Proof Assistant.
 * 
 * The Aletheia Proof Assistant is free software: you can redistribute it 
 * and/or modify it under the terms of the GNU Affero General Public License 
 * as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * The Aletheia Proof Assistant is distributed in the hope that it will be 
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with the Aletheia Proof Assistant. 
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package aletheia.protocol;

/**
 * Extension of {@link Protocol} for {@link Exportable} objects.
 * 
 * @param <E>
 *            The class of objects to export.
 */
@ProtocolInfo(availableVersions = 0)
public abstract class ExportableProtocol<E extends Exportable> extends Protocol<E>
{

	public ExportableProtocol(int requiredVersion)
	{
		super(0);
		checkVersionAvailability(ExportableProtocol.class, requiredVersion);
	}

}
