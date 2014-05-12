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
package aletheia.model.term;

import java.util.Map;
import java.util.UUID;

import aletheia.model.identifier.Identifier;

/**
 * An specialization of a {@link VariableTerm} which is identified by a unique
 * {@link UUID}.
 * 
 * <p>
 * This kind of variables cannot be used as a function parameter, that is, only
 * work as unbounded variable in a term.
 * <p>
 * The textual representation is the same of the {@link VariableTerm}, but can
 * also be represented by an identifier.
 * </p>
 * 
 * 
 */
public class IdentifiableVariableTerm extends VariableTerm
{
	private static final long serialVersionUID = 3200092082689254350L;

	private final static int hashPrime = 2961463;
	private final UUID uuid;

	public IdentifiableVariableTerm(Term type)
	{
		super(type);
		this.uuid = UUID.randomUUID();
	}

	public IdentifiableVariableTerm(Term type, UUID uuid)
	{
		super(type);
		this.uuid = uuid;
	}

	public UUID getUuid()
	{
		return uuid;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof IdentifiableVariableTerm))
			return false;
		IdentifiableVariableTerm variableTerm = (IdentifiableVariableTerm) obj;
		if (!uuid.equals(variableTerm.uuid))
			return false;
		return true;

	}

	@Override
	public int hashCode(int hasher)
	{
		int ret = 0;
		ret = ret * hashPrime + uuid.hashCode();
		return ret;
	}

	@Override
	public String hexRef()
	{
		return "$" + String.format("%08x", hashCode());
	}

	@Override
	public String toString(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, ParameterNumerator parameterNumerator)
	{
		Identifier identifier = variableToIdentifier.get(this);
		if (identifier != null)
			return identifier.toString();
		else
			return hexRef();
	}

}
