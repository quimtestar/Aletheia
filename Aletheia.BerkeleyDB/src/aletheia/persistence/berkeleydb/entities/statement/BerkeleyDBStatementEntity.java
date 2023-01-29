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
package aletheia.persistence.berkeleydb.entities.statement;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.Namespace;
import aletheia.model.identifier.NodeNamespace;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.identifier.RootNamespace;
import aletheia.model.parameteridentification.ParameterIdentification;
import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.entities.statement.ContextEntity;
import aletheia.persistence.entities.statement.RootContextEntity;
import aletheia.persistence.entities.statement.StatementEntity;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionSet;

@Entity(version = 2)
public abstract class BerkeleyDBStatementEntity implements StatementEntity
{
	@PrimaryKey
	private UUIDKey uuidKey;

	public static final String uuidKeyContext_FieldName = "uuidKeyContext";
	@SecondaryKey(name = uuidKeyContext_FieldName, relatedEntity = BerkeleyDBStatementEntity.class, relate = Relationship.MANY_TO_ONE)
	private UUIDKey uuidKeyContext;

	@Persistent(version = 0)
	public static class UUIDKeyTermHash
	{
		@KeyField(1)
		private long mostSigBits;

		@KeyField(2)
		private long leastSigBits;

		@KeyField(3)
		private int termHash;

		public UUIDKeyTermHash()
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

		public int getTermHash()
		{
			return termHash;
		}

		public void setTermHash(int termHash)
		{
			this.termHash = termHash;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (leastSigBits ^ (leastSigBits >>> 32));
			result = prime * result + (int) (mostSigBits ^ (mostSigBits >>> 32));
			result = prime * result + termHash;
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if ((obj == null) || (getClass() != obj.getClass()))
				return false;
			UUIDKeyTermHash other = (UUIDKeyTermHash) obj;
			if ((leastSigBits != other.leastSigBits) || (mostSigBits != other.mostSigBits) || (termHash != other.termHash))
				return false;
			return true;
		}

	}

	public static final String uuidKeyTermHash_FieldName = "uuidKeyTermHash";
	@SecondaryKey(name = uuidKeyTermHash_FieldName, relate = Relationship.MANY_TO_ONE)
	private UUIDKeyTermHash uuidKeyTermHash;

	private IdentifiableVariableTerm variable;

	private ParameterIdentification termParameterIdentification;

	public static final String uuidKeyDependencies_FieldName = "uuidKeyDependencies";
	@SecondaryKey(name = uuidKeyDependencies_FieldName, relatedEntity = BerkeleyDBStatementEntity.class, relate = Relationship.MANY_TO_MANY)
	private Set<UUIDKey> uuidKeyDependencies;

	private boolean proved;

	private Identifier identifier;

	@Persistent(version = 0)
	public static class UUIDContextIdentifier implements Comparable<UUIDContextIdentifier>
	{
		@KeyField(1)
		private long mostSigBits;

		@KeyField(2)
		private long leastSigBits;

		@KeyField(3)
		private String strIdentifier;

		public UUIDContextIdentifier()
		{
			super();
		}

		public UUIDKey getUUIDKey()
		{
			if (mostSigBits == 0 && leastSigBits == 0)
				return null;
			else
				return new UUIDKey(mostSigBits, leastSigBits);
		}

		public void setUUIDKey(UUIDKey uuidKey)
		{
			this.mostSigBits = uuidKey.getMostSigBits();
			this.leastSigBits = uuidKey.getLeastSigBits();
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

		public static UUIDContextIdentifier minValue(UUIDKey uuidKey, NodeNamespace namespace)
		{
			UUIDContextIdentifier min = new UUIDContextIdentifier();
			min.setUUIDKey(uuidKey);
			min.setIdentifier(namespace.asIdentifier());
			return min;
		}

		public static UUIDContextIdentifier minValue(UUIDKey uuidKey)
		{
			return nextValue(uuidKey);
		}

		public static UUIDContextIdentifier nextValue(UUIDKey uuidKey, Namespace namespace)
		{
			UUIDContextIdentifier next = new UUIDContextIdentifier();
			next.setUUIDKey(uuidKey);
			next.setIdentifier(namespace.initiator());
			return next;
		}

		public static UUIDContextIdentifier nextValue(UUIDKey uuidKey)
		{
			return nextValue(uuidKey, RootNamespace.instance);
		}

		public static UUIDContextIdentifier maxValue(UUIDKey uuidKey, Namespace namespace)
		{
			UUIDContextIdentifier max = new UUIDContextIdentifier();
			max.setUUIDKey(uuidKey);
			max.setIdentifier(namespace.terminator());
			return max;
		}

		public static UUIDContextIdentifier maxValue(UUIDKey uuidKey)
		{
			return maxValue(uuidKey, RootNamespace.instance);
		}

		@Override
		public int compareTo(UUIDContextIdentifier o)
		{
			int c;
			UUIDKey uuidKey = getUUIDKey();
			UUIDKey oUuidKey = o.getUUIDKey();
			c = Boolean.compare(uuidKey != null, oUuidKey != null);
			if (c != 0)
				return c;
			if (uuidKey != null)
			{
				c = uuidKey.compareTo(oUuidKey);
				if (c != 0)
					return c;
			}
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
			result = prime * result + (int) (leastSigBits ^ (leastSigBits >>> 32));
			result = prime * result + (int) (mostSigBits ^ (mostSigBits >>> 32));
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
			UUIDContextIdentifier other = (UUIDContextIdentifier) obj;
			if ((leastSigBits != other.leastSigBits) || (mostSigBits != other.mostSigBits))
				return false;
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

	public static final String uuidContextIdentifier_FieldName = "uuidContextIdentifier";
	@SecondaryKey(name = uuidContextIdentifier_FieldName, relate = Relationship.ONE_TO_ONE)
	private UUIDContextIdentifier uuidContextIdentifier;

	private int dependencyLevel;

	@Persistent(version = 1)
	public static class LocalSortKey implements Comparable<LocalSortKey>
	{

		private static final String globalMaxStrIdentifier = "~~";

		@KeyField(1)
		private long uuidContextmostSigBits;

		@KeyField(2)
		private long uuidContextleastSigBits;

		@KeyField(3)
		private int assumptionOrder;

		@KeyField(4)
		private String strIdentifier;

		public LocalSortKey()
		{
			super();
			setUuidKeyContext(null);
			setAssumptionOrder(Integer.MAX_VALUE);
			setIdentifier(null);
		}

		public UUIDKey getUuidKeyContext()
		{
			if (uuidContextmostSigBits == 0 && uuidContextleastSigBits == 0)
				return null;
			else
				return new UUIDKey(uuidContextmostSigBits, uuidContextleastSigBits);
		}

		public void setUuidKeyContext(UUIDKey uuidKey)
		{
			if (uuidKey == null)
			{
				this.uuidContextmostSigBits = 0;
				this.uuidContextleastSigBits = 0;
			}
			else
			{
				this.uuidContextmostSigBits = uuidKey.getMostSigBits();
				this.uuidContextleastSigBits = uuidKey.getLeastSigBits();
			}
		}

		public int getAssumptionOrder()
		{
			return assumptionOrder;
		}

		public void setAssumptionOrder(int assumptionOrder)
		{
			this.assumptionOrder = assumptionOrder;
		}

		private Identifier getIdentifier()
		{
			if (strIdentifier.isEmpty())
				return null;
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
			if (identifier == null)
				this.strIdentifier = "";
			else
				this.strIdentifier = identifier.qualifiedName();
		}

		private void setIdentifierGlobalMax()
		{
			this.strIdentifier = globalMaxStrIdentifier;
		}

		private boolean isIdentifierGlobalMax()
		{
			return globalMaxStrIdentifier.equals(strIdentifier);
		}

		@Override
		public int compareTo(LocalSortKey o)
		{
			int c;
			UUIDKey uuidKey = getUuidKeyContext();
			UUIDKey oUuidKey = o.getUuidKeyContext();
			c = Boolean.compare(uuidKey != null, oUuidKey != null);
			if (c != 0)
				return c;
			if (uuidKey != null)
			{
				c = uuidKey.compareTo(oUuidKey);
				if (c != 0)
					return c;
			}
			c = Integer.compare(getAssumptionOrder(), o.getAssumptionOrder());
			if (c != 0)
				return c;
			c = Boolean.compare(isIdentifierGlobalMax(), o.isIdentifierGlobalMax());
			if (c != 0)
				return c;
			Identifier id1 = getIdentifier();
			Identifier id2 = o.getIdentifier();
			c = Boolean.compare(id1 == null, id2 == null);
			if (c != 0)
				return c;
			if (id1 != null)
			{
				c = id1.compareTo(id2);
				if (c != 0)
					return c;
			}
			return 0;
		}

		protected static LocalSortKey minValue(UUIDKey contextUuidKey)
		{
			LocalSortKey dependencySortKey = new LocalSortKey();
			dependencySortKey.setUuidKeyContext(contextUuidKey);
			dependencySortKey.setAssumptionOrder(Integer.MIN_VALUE);
			return dependencySortKey;
		}

		public static LocalSortKey minValue(UUID contextUuid)
		{
			return minValue(new UUIDKey(contextUuid));
		}

		public static LocalSortKey minValue()
		{
			return minValue((UUIDKey) null);
		}

		protected static LocalSortKey maxValue(UUIDKey contextUuidKey)
		{
			LocalSortKey dependencySortKey = new LocalSortKey();
			dependencySortKey.setUuidKeyContext(contextUuidKey);
			dependencySortKey.setAssumptionOrder(Integer.MAX_VALUE);
			dependencySortKey.setIdentifierGlobalMax();
			return dependencySortKey;
		}

		public static LocalSortKey maxValue(UUID contextUuid)
		{
			return maxValue(new UUIDKey(contextUuid));
		}

		public static LocalSortKey maxValue()
		{
			return maxValue((UUIDKey) null);
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (uuidContextleastSigBits ^ (uuidContextleastSigBits >>> 32));
			result = prime * result + (int) (uuidContextmostSigBits ^ (uuidContextmostSigBits >>> 32));
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
			LocalSortKey other = (LocalSortKey) obj;
			if ((uuidContextleastSigBits != other.uuidContextleastSigBits) || (uuidContextmostSigBits != other.uuidContextmostSigBits))
				return false;
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

	public static final String localSortKey_FieldName = "localSortKey";
	@SecondaryKey(name = localSortKey_FieldName, relate = Relationship.MANY_TO_ONE)
	private final LocalSortKey localSortKey;

	public BerkeleyDBStatementEntity()
	{
		super();
		if (!(this instanceof RootContextEntity))
			this.uuidKeyTermHash = new UUIDKeyTermHash();
		else
			this.uuidKeyTermHash = null;
		this.uuidKeyDependencies = new HashSet<>();
		this.localSortKey = new LocalSortKey();
	}

	public UUIDKey getUuidKey()
	{
		return uuidKey;
	}

	private void setUuidKey(UUIDKey uuidKey)
	{
		this.uuidKey = uuidKey;
	}

	public UUIDKey getUuidKeyContext()
	{
		return uuidKeyContext;
	}

	public void setUuidKeyContext(UUIDKey uuidKeyContext)
	{
		this.uuidKeyContext = uuidKeyContext;
		if (uuidKeyTermHash != null)
			uuidKeyTermHash.setUUIDKey(uuidKeyContext);
		if (uuidContextIdentifier != null)
			uuidContextIdentifier.setUUIDKey(uuidKeyContext);
		localSortKey.setUuidKeyContext(uuidKeyContext);
	}

	@Override
	public IdentifiableVariableTerm getVariable()
	{
		return variable;
	}

	@Override
	public void setVariable(IdentifiableVariableTerm variable)
	{
		this.variable = variable;
		if (uuidKeyTermHash != null)
			uuidKeyTermHash.setTermHash(variable.getType().hashCode());
	}

	@Override
	public ParameterIdentification getTermParameterIdentification()
	{
		return termParameterIdentification;
	}

	@Override
	public void setTermParameterIdentification(ParameterIdentification termParameterIdentification)
	{
		this.termParameterIdentification = termParameterIdentification;
	}

	public Set<UUIDKey> getUuidKeyDependencies()
	{
		return uuidKeyDependencies;
	}

	public void setUuidKeyDependencies(Set<UUIDKey> uuidKeyDependencies)
	{
		this.uuidKeyDependencies = uuidKeyDependencies;
	}

	@Override
	public boolean isProved()
	{
		return proved;
	}

	@Override
	public void setProved(boolean proved)
	{
		this.proved = proved;
	}

	@Override
	public Identifier getIdentifier()
	{
		return identifier;
	}

	@Override
	public void setIdentifier(Identifier identifier)
	{
		this.identifier = identifier;
		if (uuidKeyContext != null)
		{
			if (identifier == null)
				this.uuidContextIdentifier = null;
			else
			{
				this.uuidContextIdentifier = new UUIDContextIdentifier();
				this.uuidContextIdentifier.setUUIDKey(uuidKeyContext);
				this.uuidContextIdentifier.setIdentifier(identifier);
			}
		}
		localSortKey.setIdentifier(identifier);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + dependencyLevel;
		result = prime * result + ((localSortKey == null) ? 0 : localSortKey.hashCode());
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		result = prime * result + (proved ? 1231 : 1237);
		result = prime * result + ((uuidContextIdentifier == null) ? 0 : uuidContextIdentifier.hashCode());
		result = prime * result + ((uuidKey == null) ? 0 : uuidKey.hashCode());
		result = prime * result + ((uuidKeyContext == null) ? 0 : uuidKeyContext.hashCode());
		result = prime * result + ((uuidKeyDependencies == null) ? 0 : uuidKeyDependencies.hashCode());
		result = prime * result + ((uuidKeyTermHash == null) ? 0 : uuidKeyTermHash.hashCode());
		result = prime * result + ((variable == null) ? 0 : variable.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if ((obj == null) || (getClass() != obj.getClass()))
			return false;
		BerkeleyDBStatementEntity other = (BerkeleyDBStatementEntity) obj;
		if (dependencyLevel != other.dependencyLevel)
			return false;
		if (localSortKey == null)
		{
			if (other.localSortKey != null)
				return false;
		}
		else if (!localSortKey.equals(other.localSortKey))
			return false;
		if (identifier == null)
		{
			if (other.identifier != null)
				return false;
		}
		else if (!identifier.equals(other.identifier))
			return false;
		if (proved != other.proved)
			return false;
		if (uuidContextIdentifier == null)
		{
			if (other.uuidContextIdentifier != null)
				return false;
		}
		else if (!uuidContextIdentifier.equals(other.uuidContextIdentifier))
			return false;
		if (uuidKey == null)
		{
			if (other.uuidKey != null)
				return false;
		}
		else if (!uuidKey.equals(other.uuidKey))
			return false;
		if (uuidKeyContext == null)
		{
			if (other.uuidKeyContext != null)
				return false;
		}
		else if (!uuidKeyContext.equals(other.uuidKeyContext))
			return false;
		if (uuidKeyDependencies == null)
		{
			if (other.uuidKeyDependencies != null)
				return false;
		}
		else if (!uuidKeyDependencies.equals(other.uuidKeyDependencies))
			return false;
		if (uuidKeyTermHash == null)
		{
			if (other.uuidKeyTermHash != null)
				return false;
		}
		else if (!uuidKeyTermHash.equals(other.uuidKeyTermHash))
			return false;
		if (variable == null)
		{
			if (other.variable != null)
				return false;
		}
		else if (!variable.equals(other.variable))
			return false;
		return true;
	}

	@Override
	public UUID getUuid()
	{
		return getUuidKey().uuid();
	}

	@Override
	public void setUuid(UUID uuid)
	{
		setUuidKey(new UUIDKey(uuid));
	}

	@Override
	public UUID getContextUuid()
	{
		if (uuidKeyContext == null)
			return null;
		return uuidKeyContext.uuid();
	}

	@Override
	public void setContextUuid(UUID contextUuid)
	{
		if (contextUuid == null)
			setUuidKeyContext(null);
		else
			setUuidKeyContext(new UUIDKey(contextUuid));
	}

	@Override
	public Set<UUID> getUuidDependencies()
	{
		return new BijectionSet<>(new Bijection<UUIDKey, UUID>()
		{

			@Override
			public UUID forward(UUIDKey input)
			{
				return input.uuid();
			}

			@Override
			public UUIDKey backward(UUID output)
			{
				return new UUIDKey(output);
			}
		}, uuidKeyDependencies);
	}

	public LocalSortKey getLocalSortKey()
	{
		return localSortKey;
	}

	@Override
	public void initializeContextData(ContextEntity contextEntity)
	{
	}

}
