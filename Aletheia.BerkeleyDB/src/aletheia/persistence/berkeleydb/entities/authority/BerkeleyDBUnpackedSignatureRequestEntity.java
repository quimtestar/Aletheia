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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBStatementEntity;
import aletheia.persistence.entities.authority.UnpackedSignatureRequestEntity;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionSet;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Persistent(version = 3)
public class BerkeleyDBUnpackedSignatureRequestEntity extends BerkeleyDBSignatureRequestEntity implements UnpackedSignatureRequestEntity
{
	public final static String contextUuidKeyPath_FieldName = "contextUuidKeyPath";
	@SecondaryKey(name = contextUuidKeyPath_FieldName, relatedEntity = BerkeleyDBStatementEntity.class, relate = Relationship.MANY_TO_MANY)
	private final List<UUIDKey> contextUuidKeyPath;

	public final static String statementUuidKeys_FieldName = "statementUuidKeys";
	@SecondaryKey(name = statementUuidKeys_FieldName, relatedEntity = BerkeleyDBStatementAuthorityEntity.class, relate = Relationship.MANY_TO_MANY)
	private final Set<UUIDKey> statementUuidKeys;

	public BerkeleyDBUnpackedSignatureRequestEntity()
	{
		super();
		this.contextUuidKeyPath = new ArrayList<UUIDKey>();
		this.statementUuidKeys = new HashSet<UUIDKey>();
	}

	@Override
	protected List<UUIDKey> getContextUuidKeyPath()
	{
		return contextUuidKeyPath;
	}

	private Set<UUIDKey> getStatementUuidKeys()
	{
		return statementUuidKeys;
	}

	@Override
	public Set<UUID> getStatementUuids()
	{
		return new BijectionSet<UUIDKey, UUID>(new Bijection<UUIDKey, UUID>()
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
				}, getStatementUuidKeys());
	}

}
