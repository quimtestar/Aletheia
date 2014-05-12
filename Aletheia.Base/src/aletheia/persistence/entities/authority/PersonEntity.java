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
import java.util.UUID;

import aletheia.model.security.SignatureData;
import aletheia.persistence.entities.Entity;

public interface PersonEntity extends Entity
{
	public UUID getUuid();

	public void setUuid(UUID uuid);

	public String getNick();

	public void setNick(String nick);

	public String getName();

	public void setName(String name);

	public String getEmail();

	public void setEmail(String email);

	public Date getSignatureDate();

	public void setSignatureDate(Date signatureDate);

	public int getSignatureVersion();

	public void setSignatureVersion(int signatureVersion);

	public SignatureData getSignatureData();

	public void setSignatureData(SignatureData signatureData);

	public Date getOrphanSince();

	public void setOrphanSince(Date date);

}
