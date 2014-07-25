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
package aletheia.utilities.collections;

/**
 * A {@link CombinedMap} that is also a {@link CloseableMap}.
 *
 * @author Quim Testar
 */
public class CombinedCloseableMap<K, V> extends AbstractCombinedCloseableMap<K, V>
{
	private static final long serialVersionUID = 558821798610737716L;

	protected CloseableMap<K, V> back;

	public CombinedCloseableMap(CloseableMap<K, V> front, CloseableMap<K, V> back)
	{
		super(front);
		this.back = back;
	}

	@Override
	protected CloseableMap<K, V> getBack()
	{
		return back;
	}

}
