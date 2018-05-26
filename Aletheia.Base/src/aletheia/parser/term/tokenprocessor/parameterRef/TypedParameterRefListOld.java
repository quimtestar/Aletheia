/*******************************************************************************
 * Copyright (c) 2017 Quim Testar
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
package aletheia.parser.term.tokenprocessor.parameterRef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aletheia.model.identifier.Identifier;
import aletheia.model.term.ParameterVariableTerm;

@Deprecated
public class TypedParameterRefListOld
{
	private final List<TypedParameterRef> list;
	private final Map<ParameterRef, ParameterVariableTerm> oldParameterTable;

	public TypedParameterRefListOld()
	{
		super();
		this.list = new ArrayList<>();
		this.oldParameterTable = new HashMap<>();
	}

	public List<TypedParameterRef> getList()
	{
		return list;
	}

	public Map<ParameterRef, ParameterVariableTerm> getOldParameterTable()
	{
		return oldParameterTable;
	}

	public void addTypedParameterRef(TypedParameterRef typedParameterRef, Map<ParameterRef, ParameterVariableTerm> tempParameterTable,
			Map<ParameterVariableTerm, Identifier> parameterIdentifiers)
	{
		getList().add(typedParameterRef);
		ParameterRef parameterRef = typedParameterRef.getParameterRef();
		if (parameterRef != null)
		{
			ParameterVariableTerm parameter = typedParameterRef.getParameter();
			if (parameterIdentifiers != null && parameterRef instanceof IdentifierParameterRef)
				parameterIdentifiers.put(parameter, ((IdentifierParameterRef) parameterRef).getIdentifier());
			ParameterVariableTerm oldpar = tempParameterTable.put(parameterRef, parameter);
			if (!getOldParameterTable().containsKey(parameterRef))
				getOldParameterTable().put(parameterRef, oldpar);
		}
	}

}
