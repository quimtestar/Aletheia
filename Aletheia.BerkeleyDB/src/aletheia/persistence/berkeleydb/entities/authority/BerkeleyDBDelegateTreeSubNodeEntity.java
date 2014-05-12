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
package aletheia.persistence.berkeleydb.entities.authority;

import java.util.UUID;

import aletheia.model.identifier.NodeNamespace;
import aletheia.persistence.entities.authority.DelegateTreeSubNodeEntity;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Persistent(version = 0)
public class BerkeleyDBDelegateTreeSubNodeEntity extends BerkeleyDBDelegateTreeNodeEntity implements DelegateTreeSubNodeEntity
{
	public static final String parent_FieldName = "parent";
	@SecondaryKey(name = parent_FieldName, relatedEntity = BerkeleyDBDelegateTreeNodeEntity.class, relate = Relationship.MANY_TO_ONE)
	private final PrimaryKeyData parent;

	public BerkeleyDBDelegateTreeSubNodeEntity()
	{
		super();
		this.parent = new PrimaryKeyData();
	}

	@Override
	public void setStatementUuid(UUID contextUuid)
	{
		super.setStatementUuid(contextUuid);
		parent.setContextUuid(contextUuid);
	}

	@Override
	public void setPrefix(NodeNamespace prefix)
	{
		super.setPrefix(prefix);
		parent.setPrefix(prefix.getParent());
	}

	@Override
	public NodeNamespace getPrefix()
	{
		return (NodeNamespace) super.getPrefix();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
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
		BerkeleyDBDelegateTreeSubNodeEntity other = (BerkeleyDBDelegateTreeSubNodeEntity) obj;
		if (parent == null)
		{
			if (other.parent != null)
				return false;
		}
		else if (!parent.equals(other.parent))
			return false;
		return true;
	}

}
