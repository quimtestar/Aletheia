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
package aletheia.persistence.berkeleydb.mutations;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBStatementAuthoritySignatureEntity;
import aletheia.security.model.SignatureData;

import com.sleepycat.persist.evolve.Conversion;
import com.sleepycat.persist.evolve.Converter;
import com.sleepycat.persist.evolve.Deleter;
import com.sleepycat.persist.evolve.EntityConverter;
import com.sleepycat.persist.model.EntityModel;
import com.sleepycat.persist.raw.RawObject;

public class MutationSet121117 extends MutationSet
{
	private static final long serialVersionUID = 2812236922985028565L;

	private static class StatementAuthoritySignatureConversion implements Conversion
	{
		private static final long serialVersionUID = -8550389131464978011L;

		private static final Class<BerkeleyDBStatementAuthoritySignatureEntity> entityClass = BerkeleyDBStatementAuthoritySignatureEntity.class;
		private static final String className = entityClass.getName();
		private static final int version = 2;
		private static final Set<String> deletedKeys = new HashSet<>(Arrays.asList("contextPrefixAuthorizers"));

		public StatementAuthoritySignatureConversion()
		{
		}

		@Override
		public void initialize(EntityModel model)
		{
		}

		@Override
		public BerkeleyDBStatementAuthoritySignatureEntity convert(Object fromValue)
		{
			BerkeleyDBStatementAuthoritySignatureEntity entity = new BerkeleyDBStatementAuthoritySignatureEntity();
			RawObject rawEntity = (RawObject) fromValue;
			RawObject rawStatementUuidKey = (RawObject) rawEntity.getValues().get("statementUuidKey");
			entity.setStatementUuid(rawUuidKeyToUuid(rawStatementUuidKey));
			RawObject rawAuthorizerUuidKey = (RawObject) rawEntity.getValues().get("authorizerUuidKey");
			entity.setAuthorizerUuid(rawUuidKeyToUuid(rawAuthorizerUuidKey));
			Date signatureDate = (Date) rawEntity.getValues().get("signatureDate");
			entity.setSignatureDate(signatureDate);
			RawObject rawSignatureData = (RawObject) rawEntity.getValues().get("signatureData");
			entity.setSignatureData(rawSignatureData(rawSignatureData));
			boolean valid = (boolean) rawEntity.getValues().get("valid");
			entity.setValid(valid);
			return entity;
		}

		private UUID rawUuidKeyToUuid(RawObject rawUuidKey)
		{
			long mostSigBits = (long) rawUuidKey.getValues().get("mostSigBits");
			long leastSigBits = (long) rawUuidKey.getValues().get("leastSigBits");
			return new UUID(mostSigBits, leastSigBits);
		}

		private SignatureData rawSignatureData(RawObject rawSignatureData)
		{
			String algorithm = (String) rawSignatureData.getValues().get("algorithm");
			RawObject rawEncoded = (RawObject) rawSignatureData.getValues().get("encoded");
			byte[] encoded = new byte[rawEncoded.getElements().length];
			for (int i = 0; i < rawEncoded.getElements().length; i++)
				encoded[i] = (byte) rawEncoded.getElements()[i];
			return new SignatureData(algorithm, encoded);
		}

		@Override
		public boolean equals(Object obj)
		{
			if (!(obj instanceof StatementAuthoritySignatureConversion))
				return false;
			return true;
		}

		public Converter makeConverter()
		{
			return new EntityConverter(className, version, this, deletedKeys);
		}

	}

	private final static String contextPrefixAuthorizerKeyDataClassName = "aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBStatementAuthoritySignatureEntity$ContextPrefixAuthorizerKeyData";

	public MutationSet121117(BerkeleyDBAletheiaMutations mutations)
	{
		super(mutations);
		StatementAuthoritySignatureConversion statementAuthoritySignatureConversion = new StatementAuthoritySignatureConversion();
		mutations.addConverter(statementAuthoritySignatureConversion.makeConverter());
		mutations.addDeleter(new Deleter(contextPrefixAuthorizerKeyDataClassName, 0));
	}

}
