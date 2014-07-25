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
package aletheia.persistence.collections.statement;

import java.util.Comparator;

import aletheia.model.statement.Statement;
import aletheia.persistence.collections.PersistenceManagerDataStructure;
import aletheia.utilities.collections.CloseableSortedSet;

public interface SortedStatements<S extends Statement> extends PersistenceManagerDataStructure, CloseableSortedSet<S>
{

	@Override
	Comparator<? super S> comparator();

	@Override
	S first();

	@Override
	S last();

	@Override
	SortedStatements<S> subSet(S fromElement, S toElement);

	@Override
	SortedStatements<S> headSet(S toElement);

	@Override
	SortedStatements<S> tailSet(S fromElement);

}