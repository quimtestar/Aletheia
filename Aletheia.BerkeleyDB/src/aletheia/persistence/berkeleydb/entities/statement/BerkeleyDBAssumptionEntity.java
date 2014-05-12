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
package aletheia.persistence.berkeleydb.entities.statement;

import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.entities.statement.AssumptionEntity;

import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Persistent(version = 0)
public class BerkeleyDBAssumptionEntity extends BerkeleyDBStatementEntity implements AssumptionEntity
{
	@Persistent(version = 0)
	public static class UUIDKeyOrder
	{
		@KeyField(1)
		private long mostSigBits;

		@KeyField(2)
		private long leastSigBits;

		@KeyField(3)
		private int order;

		public UUIDKeyOrder()
		{
			super();
		}

		public UUIDKey getUUIDKey()
		{
			return new UUIDKey(mostSigBits, leastSigBits);
		}

		public void setUUIDKey(UUIDKey uuidKey)
		{
			this.mostSigBits = uuidKey.getMostSigBits();
			this.leastSigBits = uuidKey.getLeastSigBits();
		}

		public int getOrder()
		{
			return order;
		}

		public void setOrder(int order)
		{
			this.order = order;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (leastSigBits ^ (leastSigBits >>> 32));
			result = prime * result + (int) (mostSigBits ^ (mostSigBits >>> 32));
			result = prime * result + order;
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			UUIDKeyOrder other = (UUIDKeyOrder) obj;
			if (leastSigBits != other.leastSigBits)
				return false;
			if (mostSigBits != other.mostSigBits)
				return false;
			if (order != other.order)
				return false;
			return true;
		}

	}

	public static final String uuidKeyOrder_FieldName = "uuidKeyOrder";
	@SecondaryKey(name = uuidKeyOrder_FieldName, relate = Relationship.ONE_TO_ONE)
	private UUIDKeyOrder uuidKeyOrder;

	public BerkeleyDBAssumptionEntity()
	{
		super();
		this.uuidKeyOrder = new UUIDKeyOrder();
	}

	@Override
	public void setUuidKeyContext(UUIDKey uuidKeyContext)
	{
		super.setUuidKeyContext(uuidKeyContext);
		uuidKeyOrder.setUUIDKey(uuidKeyContext);
	}

	@Override
	public int getOrder()
	{
		return uuidKeyOrder.getOrder();
	}

	@Override
	public void setOrder(int order)
	{
		uuidKeyOrder.setOrder(order);
		getLocalSortKey().setAssumptionOrder(order);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((uuidKeyOrder == null) ? 0 : uuidKeyOrder.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		BerkeleyDBAssumptionEntity other = (BerkeleyDBAssumptionEntity) obj;
		if (uuidKeyOrder == null)
		{
			if (other.uuidKeyOrder != null)
				return false;
		}
		else if (!uuidKeyOrder.equals(other.uuidKeyOrder))
			return false;
		return true;
	}

}
