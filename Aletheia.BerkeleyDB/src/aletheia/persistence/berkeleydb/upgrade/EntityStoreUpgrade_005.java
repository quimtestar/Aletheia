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

import java.util.Arrays;
import java.util.Collection;

import aletheia.persistence.berkeleydb.BerkeleyDBAletheiaEntityStore;
import aletheia.persistence.berkeleydb.BerkeleyDBAletheiaEnvironment;

import com.sleepycat.je.Transaction;
import com.sleepycat.persist.raw.RawStore;

public class EntityStoreUpgrade_005 extends EntityStoreUpgrade_014
{

	public EntityStoreUpgrade_005()
	{
	}

	@Override
	public Collection<Integer> versions()
	{
		return Arrays.asList(5, 6, 7, 8, 9, 10, 11, 12, 13);
	}

	protected class UpgradeInstance extends EntityStoreUpgrade_014.UpgradeInstance
	{

		protected UpgradeInstance(BerkeleyDBAletheiaEnvironment environment, String storeName)
		{
			super(environment, storeName);
		}

		@Override
		protected void convertClass(RawStore oldStore, BerkeleyDBAletheiaEntityStore aletheiaStore, Transaction tx, String className) throws UpgradeException
		{
			if (className.endsWith("BerkeleyDBUnpackedSignatureRequestEntity") || className.endsWith("BerkeleyDBPackedSignatureRequestEntity")
					|| className.endsWith("BerkeleyDBSignatureRequestEntity") || className.endsWith("BerkeleyDBDeferredMessageEntity"))
				return;
			super.convertClass(oldStore, aletheiaStore, tx, className);
		}

	}

	@Override
	protected UpgradeInstance instance(BerkeleyDBAletheiaEnvironment environment, String storeName)
	{
		return new UpgradeInstance(environment, storeName);
	}

}
