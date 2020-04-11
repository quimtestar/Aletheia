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
import java.util.SortedSet;
import java.util.UUID;

import aletheia.model.identifier.Namespace;
import aletheia.persistence.entities.Entity;
import aletheia.security.model.SignatureData;

public interface DelegateAuthorizerEntity extends Entity
{
	public UUID getStatementUuid();

	public void setStatementUuid(UUID statementUuid);

	public Namespace getPrefix();

	public void setPrefix(Namespace prefix);

	public UUID getDelegateUuid();

	public void setDelegateUuid(UUID delegateUuid);

	public UUID getAuthorizerUuid();

	public void setAuthorizerUuid(UUID authorizerUuid);

	public SortedSet<UUID> getRevokedSignatureUuids();

	public Date getSignatureDate();

	public void setSignatureDate(Date signatureDate);

	public int getSignatureVersion();

	public void setSignatureVersion(int signatureVersion);

	public SignatureData getSignatureData();

	public void setSignatureData(SignatureData signatureData);

}
