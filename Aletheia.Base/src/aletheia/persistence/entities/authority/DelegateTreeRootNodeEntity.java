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
package aletheia.persistence.entities.authority;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import aletheia.model.identifier.RootNamespace;
import aletheia.model.security.SignatureData;

public interface DelegateTreeRootNodeEntity extends DelegateTreeNodeEntity
{
	@Override
	public RootNamespace getPrefix();

	public void setPrefix(RootNamespace prefix);

	public interface SuccessorEntryEntity
	{
		public UUID getSuccessorUuid();

		public void setSuccessorUuid(UUID successorUuid);

		public Date getSignatureDate();

		public void setSignatureDate(Date signatureDate);

		public int getSignatureVersion();

		public void setSignatureVersion(int signatureVersion);

		public SignatureData getSignatureData();

		public void setSignatureData(SignatureData signatureData);
	}

	public SuccessorEntryEntity instantiateSuccessorEntryEntity();

	public List<SuccessorEntryEntity> getSuccessorEntryEntities();

	public Set<UUID> successorUuids();

	public int getSuccessorIndex();

	public void setSuccessorIndex(int successorIndex);

	public Date getSignatureDate();

	public void setSignatureDate(Date signatureDate);

	public int getSignatureVersion();

	public void setSignatureVersion(int signatureVersion);

	public SignatureData getSignatureData();

	public void setSignatureData(SignatureData signatureData);

}
