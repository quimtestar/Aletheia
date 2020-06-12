/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
 *
 * This file is part of the Aletheia Proof Assistant.
 *
 * The Aletheia Proof Assistant is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The Aletheia Proof Assistant is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with the Aletheia Proof Assistant.
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package aletheia.peertopeer.statement;

import java.util.Date;

import aletheia.security.model.SignatureData;

public class RootContextSignature implements Comparable<RootContextSignature>
{
	private final Date signatureDate;
	private final SignatureData signatureData;

	public RootContextSignature(Date signatureDate, SignatureData signatureData)
	{
		super();
		this.signatureDate = signatureDate;
		this.signatureData = signatureData;
	}

	public Date getSignatureDate()
	{
		return signatureDate;
	}

	public SignatureData getSignatureData()
	{
		return signatureData;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((signatureData == null) ? 0 : signatureData.hashCode());
		result = prime * result + ((signatureDate == null) ? 0 : signatureDate.hashCode());
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
		RootContextSignature other = (RootContextSignature) obj;
		if (signatureData == null)
		{
			if (other.signatureData != null)
				return false;
		}
		else if (!signatureData.equals(other.signatureData))
			return false;
		if (signatureDate == null)
		{
			if (other.signatureDate != null)
				return false;
		}
		else if (!signatureDate.equals(other.signatureDate))
			return false;
		return true;
	}

	@Override
	public int compareTo(RootContextSignature o)
	{
		return signatureDate.compareTo(o.signatureDate);
	}

}
