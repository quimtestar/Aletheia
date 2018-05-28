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
package aletheia.parser.term.parameterRef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.VariableTerm;
import aletheia.utilities.collections.AdaptedMap;
import aletheia.utilities.collections.CombinedMap;

public class TypedParameterRefList
{
	private final Map<ParameterRef, VariableTerm> backParameterTable;
	private final List<TypedParameterRef> list;
	private final Map<ParameterRef, ParameterVariableTerm> frontParameterTable;

	public TypedParameterRefList(Map<ParameterRef, VariableTerm> backParameterTable)
	{
		super();
		this.backParameterTable = backParameterTable;
		this.frontParameterTable = new HashMap<>();
		this.list = new ArrayList<>();
	}

	public List<TypedParameterRef> list()
	{
		return Collections.unmodifiableList(list);
	}

	public Map<ParameterRef, VariableTerm> parameterTable()
	{
		if (backParameterTable == null)
			return new AdaptedMap<>(frontParameterTable);
		else
			return new CombinedMap<>(new AdaptedMap<>(frontParameterTable), backParameterTable);
	}

	public void addTypedParameterRef(TypedParameterRef typedParameterRef)
	{
		list.add(typedParameterRef);
		frontParameterTable.put(typedParameterRef.getParameterRef(), typedParameterRef.getParameter());
	}

	@Override
	public String toString()
	{
		return list.toString();
	}

}
