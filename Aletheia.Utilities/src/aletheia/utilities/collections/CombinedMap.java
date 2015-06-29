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

import java.io.Serializable;
import java.util.Map;

/**
 * A combined map of any two other maps. The entries in the front map will
 * shadow the entries in the back map with the same key.
 *
 * @param <K>
 *            The keys type.
 * @param <V>
 *            The values type.
 *
 * @author Quim Testar
 */
public class CombinedMap<K, V> extends AbstractCombinedMap<K, V>implements Serializable
{
	private static final long serialVersionUID = 6151843791136257958L;

	private final Map<K, V> back;

	public CombinedMap(Map<K, V> front, Map<K, V> back)
	{
		super(front);
		this.back = back;
	}

	@Override
	protected Map<K, V> getBack()
	{
		return back;
	}

}
