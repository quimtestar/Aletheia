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
package aletheia.peertopeer.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class RemoteRouterSet implements RouterSet
{
	public static class RemoteRouter implements Router
	{
		private final int distance;
		private final Set<UUID> spindle;

		public RemoteRouter(int distance, Set<UUID> spindle)
		{
			this.distance = distance;
			this.spindle = spindle;
		}

		@Override
		public int getDistance()
		{
			return distance;
		}

		@Override
		public Set<UUID> getSpindle()
		{
			return Collections.unmodifiableSet(spindle);
		}

		public boolean spindleIntersects(Collection<UUID> clearing)
		{
			for (UUID c : clearing)
				if (spindle.contains(c))
					return true;
			return false;
		}
	}

	private final ArrayList<RemoteRouter> routers;

	public RemoteRouterSet()
	{
		this.routers = new ArrayList<RemoteRouter>();
	}

	public void addRouter(RemoteRouter remoteRouter)
	{
		routers.add(remoteRouter);
	}

	@Override
	public List<Router> getRouters()
	{
		return Collections.<Router> unmodifiableList(routers);
	}

	@Override
	public RemoteRouter getRouter(int i)
	{
		if (i < 0)
			throw new IllegalArgumentException();
		if (i >= routers.size())
			return null;
		return routers.get(i);
	}

}
