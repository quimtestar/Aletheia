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

import java.util.ArrayList;
import java.util.List;

import aletheia.model.term.SimpleTerm;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.entities.statement.ContextEntity;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Persistent(version = 0)
public class BerkeleyDBContextEntity extends BerkeleyDBStatementEntity implements ContextEntity
{
	public static final String uuidKeyContext__FieldName = "uuidKeyContext_";
	@SecondaryKey(name = uuidKeyContext__FieldName, relatedEntity = BerkeleyDBStatementEntity.class, relate = Relationship.MANY_TO_ONE)
	private UUIDKey uuidKeyContext_;

	private SimpleTerm consequent;

	public static final String uuidKeyAncestorsConsequentHash_FieldName = "uuidKeyAncestorsConsequentHash";
	@SecondaryKey(name = uuidKeyAncestorsConsequentHash_FieldName, relate = Relationship.MANY_TO_MANY)
	private List<UUIDKeyTermHash> uuidKeyAncestorsConsequentHash;

	public BerkeleyDBContextEntity()
	{
		super();
	}

	private void initializeUuidKeyAncestorsConsequentHash(BerkeleyDBContextEntity parent)
	{
		uuidKeyAncestorsConsequentHash = new ArrayList<UUIDKeyTermHash>();
		if (parent != null)
		{
			for (UUIDKeyTermHash uuidKeyTermHashParent : parent.uuidKeyAncestorsConsequentHash)
			{
				UUIDKeyTermHash uuidKeyTermHash = new UUIDKeyTermHash();
				uuidKeyTermHash.setUUIDKey(uuidKeyTermHashParent.getUUIDKey());
				uuidKeyAncestorsConsequentHash.add(uuidKeyTermHash);
			}
		}
		UUIDKeyTermHash uuidKeyTermHash = new UUIDKeyTermHash();
		uuidKeyTermHash.setUUIDKey(getUuidKey());
		uuidKeyAncestorsConsequentHash.add(uuidKeyTermHash);
	}

	@Override
	public void initializeContextData(ContextEntity contextEntity)
	{
		initializeUuidKeyAncestorsConsequentHash((BerkeleyDBContextEntity) contextEntity);
	}

	@Override
	public void setUuidKeyContext(UUIDKey uuidKeyContext)
	{
		super.setUuidKeyContext(uuidKeyContext);
		this.uuidKeyContext_ = uuidKeyContext;
	}

	@Override
	public SimpleTerm getConsequent()
	{
		return consequent;
	}

	@Override
	public void setConsequent(SimpleTerm consequent)
	{
		this.consequent = consequent;
		if ((consequent != null) && (uuidKeyAncestorsConsequentHash != null))
		{
			int hash = consequent.hashCode();
			for (UUIDKeyTermHash uuidKeyTermHash : uuidKeyAncestorsConsequentHash)
				uuidKeyTermHash.setTermHash(hash);
		}
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((consequent == null) ? 0 : consequent.hashCode());
		result = prime * result + ((uuidKeyAncestorsConsequentHash == null) ? 0 : uuidKeyAncestorsConsequentHash.hashCode());
		result = prime * result + ((uuidKeyContext_ == null) ? 0 : uuidKeyContext_.hashCode());
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
		BerkeleyDBContextEntity other = (BerkeleyDBContextEntity) obj;
		if (consequent == null)
		{
			if (other.consequent != null)
				return false;
		}
		else if (!consequent.equals(other.consequent))
			return false;
		if (uuidKeyAncestorsConsequentHash == null)
		{
			if (other.uuidKeyAncestorsConsequentHash != null)
				return false;
		}
		else if (!uuidKeyAncestorsConsequentHash.equals(other.uuidKeyAncestorsConsequentHash))
			return false;
		if (uuidKeyContext_ == null)
		{
			if (other.uuidKeyContext_ != null)
				return false;
		}
		else if (!uuidKeyContext_.equals(other.uuidKeyContext_))
			return false;
		return true;
	}

}
