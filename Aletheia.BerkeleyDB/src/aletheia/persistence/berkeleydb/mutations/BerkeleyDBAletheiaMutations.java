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
package aletheia.persistence.berkeleydb.mutations;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;

import com.sleepycat.persist.evolve.Mutations;

public class BerkeleyDBAletheiaMutations extends Mutations
{
	private static final long serialVersionUID = 7528815599542804717L;

	private static final Logger logger = LoggerManager.instance.logger();

	//@formatter:off
	private final static List<Class<? extends MutationSet>> mutationSetClasses=new ArrayList<Class<? extends MutationSet>>(Arrays.asList(
			MutationSet121117.class
			));
	//@formatter:on

	private final List<MutationSet> mutationSets;

	public BerkeleyDBAletheiaMutations()
	{
		super();
		mutationSets = new ArrayList<>();
		for (Class<? extends MutationSet> mutationClass : mutationSetClasses)
		{
			try
			{
				Constructor<? extends MutationSet> constructor = mutationClass.getConstructor(BerkeleyDBAletheiaMutations.class);
				mutationSets.add(constructor.newInstance(this));
			}
			catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException
					| InvocationTargetException e)
			{
				logger.error("MutationSet instantiation error", e);
			}
		}
	}

	public List<MutationSet> getMutationSets()
	{
		return Collections.unmodifiableList(mutationSets);
	}

}
