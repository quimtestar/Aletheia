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
package aletheia.parsergenerator.parser;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The transition table for a LALR(1) parser.
 */
public class TransitionTableLalr1 extends TransitionTable implements Serializable
{
	private static final long serialVersionUID = 496255639905973249L;

	public TransitionTableLalr1(Grammar grammar) throws ConflictException
	{
		super(grammar);
	}

	@Override
	protected ItemStateTranslator itemStateTranslator()
	{
		return new ItemStateTranslator()
		{
			private final Map<State, ParserItem> stateToItem = new HashMap<State, ParserItem>();
			private final Map<Set<ProductionState>, State> itemToState = new HashMap<Set<ProductionState>, State>();

			@Override
			public ParserItem stateToItem(State state)
			{
				return stateToItem.get(state);
			}

			@Override
			public State itemToState(ParserItem item)
			{
				State state = itemToState.get(item.getProductionStates());
				if (state == null)
				{
					state = new State();
					itemToState.put(item.getProductionStates(), state);
				}
				ParserItem item2 = stateToItem.get(state);
				if (item2 == null)
					stateToItem.put(state, item);
				else
					stateToItem.put(state, item2.fusion(item));
				return state;
			}

			@Override
			public boolean mappedItem(ParserItem item)
			{
				State state = itemToState.get(item.getProductionStates());
				if (state == null)
					return false;
				ParserItem other = stateToItem.get(state);
				return item.isFollowerSubsetOf(other);
			}

		};

	}

}
