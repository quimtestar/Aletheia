/*******************************************************************************
 * Copyright (c) 2018 Quim Testar
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
package aletheia.persistence.berkeleydb.proxies.term;

import com.sleepycat.persist.model.Persistent;

import aletheia.model.term.ProjectedCastTypeTerm;
import aletheia.model.term.ProjectedCastTypeTerm.ProjectedCastTypeException;

@Persistent(proxyFor = ProjectedCastTypeTerm.class, version = 1)
public class ProjectedCastTypeTermProxy extends ProjectionCastTypeTermProxy<ProjectedCastTypeTerm>
{

	@Override
	public ProjectedCastTypeTerm convertProxy()
	{
		try
		{
			return new ProjectedCastTypeTerm(getTerm());
		}
		catch (ProjectedCastTypeException e)
		{
			throw new ProxyConversionException(e);
		}
	}

}
