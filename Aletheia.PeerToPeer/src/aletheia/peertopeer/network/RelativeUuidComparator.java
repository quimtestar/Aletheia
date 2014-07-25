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
package aletheia.peertopeer.network;

import java.util.Comparator;
import java.util.UUID;

public class RelativeUuidComparator implements Comparator<UUID>
{
	private final UUID center;

	public RelativeUuidComparator(UUID center)
	{
		this.center = center;
	}

	@Override
	public int compare(UUID uuid1, UUID uuid2)
	{
		int c1 = center.compareTo(uuid1);
		int c2 = center.compareTo(uuid2);
		if (c1 == c2)
			return uuid1.compareTo(uuid2);
		else
			return Integer.compare(c1, c2);
	}

}
