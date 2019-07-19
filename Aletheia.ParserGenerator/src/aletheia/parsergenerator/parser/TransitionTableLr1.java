/*******************************************************************************
 * Copyright (c) 2014, 2016 Quim Testar.
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

/**
 * The transition table for a LR(1) parser.
 */
public class TransitionTableLr1 extends TransitionTable implements Serializable
{
	private static final long serialVersionUID = 7325213043365072498L;

	public TransitionTableLr1(Grammar grammar) throws ConflictException
	{
		super(grammar);
	}

	@Override
	protected ItemStateTranslator itemStateTranslator()
	{
		return new ItemStateTranslator()
		{
			private final Map<State, ParserItem> stateToItem = new HashMap<>();
			private final Map<ParserItem, State> itemToState = new HashMap<>();

			@Override
			public ParserItem stateToItem(State state)
			{
				return stateToItem.get(state);
			}

			@Override
			public State itemToState(ParserItem item)
			{
				State state = itemToState.get(item);
				if (state == null)
				{
					state = new State();
					itemToState.put(item, state);
					stateToItem.put(state, item);
				}
				return state;
			}

			@Override
			public boolean mappedItem(ParserItem item)
			{
				return itemToState.containsKey(item);
			}

		};
	}

}
