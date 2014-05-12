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
package aletheia.persistence.berkeleydb.upgrade;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.TTerm;
import aletheia.model.term.Term;
import aletheia.persistence.berkeleydb.BerkeleyDBAletheiaEnvironment;

import com.sleepycat.je.Transaction;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.model.EntityModel;
import com.sleepycat.persist.raw.RawObject;
import com.sleepycat.persist.raw.RawType;

public class EntityStoreUpgrade_002 extends EntityStoreUpgrade_003
{

	@Override
	public Collection<Integer> versions()
	{
		return Arrays.asList(2);
	}

	protected class UpgradeInstance extends EntityStoreUpgrade_003.UpgradeInstance
	{

		protected UpgradeInstance(BerkeleyDBAletheiaEnvironment environment, String storeName)
		{
			super(environment, storeName);
		}

		@Override
		protected Object partialConvertRawObjectDefaultType(EntityModel model, RawObject rawObject, RawType rawType, Map<RawObject, Object> converted)
				throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
				IllegalArgumentException, InvocationTargetException
		{
			if (rawType.getClassName().equals("aletheia.persistence.berkeleydb.proxies.term.TypeProxy"))
				return TTerm.instance;
			else if (rawType.getClassName().equals("aletheia.persistence.berkeleydb.proxies.term.VariableTermProxy") && rawType.getVersion() == 0)
			{
				Term type = (Term) partialConvertRawObject(model, (RawObject) rawObject.getSuper().getValues().get("type"), converted);
				return new ParameterVariableTerm(type);
			}
			else if (rawType.getClassName().equals("aletheia.persistence.berkeleydb.proxies.term.UUIDVariableTermProxy"))
			{
				Term type = (Term) partialConvertRawObject(model, (RawObject) rawObject.getSuper().getValues().get("type"), converted);
				UUID uuid = (UUID) partialConvertRawObject(model, (RawObject) rawObject.getValues().get("uuid"), converted);
				return new IdentifiableVariableTerm(type, uuid);
			}

			else
				return super.partialConvertRawObjectDefaultType(model, rawObject, rawType, converted);
		}

		@Override
		protected void putConvertedRawObject(Transaction tx, EntityModel aletheiaModel, Class<Object> entityClass, Class<Object> primaryKeyClass,
				PrimaryIndex<Object, Object> newPrimaryIndex, RawObject oldRawObject)
		{
			try
			{
				Object newObject = partialConvertRawObjectIfException(aletheiaModel, oldRawObject);
				newPrimaryIndex.put(tx, newObject);
			}
			catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchFieldException | SecurityException | NoSuchMethodException
					| IllegalArgumentException | InvocationTargetException e)
			{
				throw new RuntimeException(e);
			}
		}

	}

	@Override
	protected UpgradeInstance instance(BerkeleyDBAletheiaEnvironment environment, String storeName)
	{
		return new UpgradeInstance(environment, storeName);
	}

}
