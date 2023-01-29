/*******************************************************************************
 * Copyright (c) 2014, 2023 Quim Testar.
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
package aletheia.persistence.berkeleydb.entities;

import java.util.UUID;

import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;

@Persistent(version = 0)
public class UUIDKey implements Comparable<UUIDKey>
{
	@KeyField(1)
	private final long mostSigBits;

	@KeyField(2)
	private final long leastSigBits;

	@SuppressWarnings("unused")
	private UUIDKey()
	{
		this(0, 0);
	}

	public UUIDKey(long mostSigBits, long leastSigBits)
	{
		super();
		this.mostSigBits = mostSigBits;
		this.leastSigBits = leastSigBits;
	}

	public UUIDKey(UUID uuid)
	{
		this(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
	}

	public long getMostSigBits()
	{
		return mostSigBits;
	}

	public long getLeastSigBits()
	{
		return leastSigBits;
	}

	public UUID uuid()
	{
		return new UUID(mostSigBits, leastSigBits);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (leastSigBits ^ (leastSigBits >>> 32));
		result = prime * result + (int) (mostSigBits ^ (mostSigBits >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if ((obj == null) || (getClass() != obj.getClass()))
			return false;
		UUIDKey other = (UUIDKey) obj;
		if (leastSigBits != other.leastSigBits)
			return false;
		if (mostSigBits != other.mostSigBits)
			return false;
		return true;
	}

	@Override
	public int compareTo(UUIDKey o)
	{
		return uuid().compareTo(o.uuid());
	}

	@Override
	public String toString()
	{
		return uuid().toString();
	}

}
