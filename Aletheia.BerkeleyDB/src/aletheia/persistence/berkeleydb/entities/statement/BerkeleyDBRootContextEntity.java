/*******************************************************************************
 * Copyright (c) 2014, 2020 Quim Testar.
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

import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.Namespace;
import aletheia.model.identifier.NodeNamespace;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.identifier.RootNamespace;
import aletheia.persistence.entities.statement.RootContextEntity;

@Persistent(version = 2)
public class BerkeleyDBRootContextEntity extends BerkeleyDBContextEntity implements RootContextEntity
{
	public static final String mark_FieldName = "mark";
	@SecondaryKey(name = mark_FieldName, relate = Relationship.MANY_TO_ONE)
	private boolean mark;

	@Persistent(version = 0)
	public static class IdentifierKey implements Comparable<IdentifierKey>
	{
		@KeyField(1)
		private String strIdentifier;

		public IdentifierKey()
		{
			super();
		}

		public IdentifierKey(Identifier identifier)
		{
			super();
			setIdentifier(identifier);
		}

		public Identifier getIdentifier()
		{
			try
			{
				return Identifier.parse(strIdentifier);
			}
			catch (InvalidNameException e)
			{
				throw new Error(e);
			}
		}

		public void setIdentifier(Identifier identifier)
		{
			this.strIdentifier = identifier.qualifiedName();
		}

		public static IdentifierKey minValue(NodeNamespace namespace)
		{
			IdentifierKey min = new IdentifierKey();
			min.setIdentifier(namespace.asIdentifier());
			return min;
		}

		public static IdentifierKey minValue()
		{
			return nextValue();
		}

		public static IdentifierKey nextValue(Namespace namespace)
		{
			IdentifierKey next = new IdentifierKey();
			next.setIdentifier(namespace.initiator());
			return next;
		}

		public static IdentifierKey nextValue()
		{
			return nextValue(RootNamespace.instance);
		}

		public static IdentifierKey maxValue(Namespace namespace)
		{
			IdentifierKey max = new IdentifierKey();
			max.setIdentifier(namespace.terminator());
			return max;
		}

		public static IdentifierKey maxValue()
		{
			return maxValue(RootNamespace.instance);
		}

		@Override
		public int compareTo(IdentifierKey o)
		{
			int c;
			c = getIdentifier().compareTo(o.getIdentifier());
			if (c != 0)
				return c;
			return 0;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((strIdentifier == null) ? 0 : strIdentifier.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if ((obj == null) || (getClass() != obj.getClass()))
				return false;
			IdentifierKey other = (IdentifierKey) obj;
			if (strIdentifier == null)
			{
				if (other.strIdentifier != null)
					return false;
			}
			else if (!strIdentifier.equals(other.strIdentifier))
				return false;
			return true;
		}

	}

	public static final String identifierKey_FieldName = "identifierKey";
	@SecondaryKey(name = identifierKey_FieldName, relate = Relationship.MANY_TO_ONE)
	private IdentifierKey identifierKey;

	public BerkeleyDBRootContextEntity()
	{
		super();
		mark = true;
	}

	@Override
	public void setIdentifier(Identifier identifier)
	{
		super.setIdentifier(identifier);
		if (identifier == null)
			this.identifierKey = null;
		else
			this.identifierKey = new IdentifierKey(identifier);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((identifierKey == null) ? 0 : identifierKey.hashCode());
		result = prime * result + (mark ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj) || (getClass() != obj.getClass()))
			return false;
		BerkeleyDBRootContextEntity other = (BerkeleyDBRootContextEntity) obj;
		if (identifierKey == null)
		{
			if (other.identifierKey != null)
				return false;
		}
		else if (!identifierKey.equals(other.identifierKey))
			return false;
		if (mark != other.mark)
			return false;
		return true;
	}

}
