package aletheia.persistence.berkeleydb.upgrade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.sleepycat.je.LockMode;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.model.EntityModel;
import com.sleepycat.persist.raw.RawObject;
import com.sleepycat.persist.raw.RawStore;
import aletheia.model.statement.RootContext;
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.model.term.SimpleTerm;
import aletheia.model.term.Term;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBAletheiaEnvironment;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBContextAuthorityEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBStatementAuthorityEntity;
import aletheia.persistence.berkeleydb.entities.local.BerkeleyDBContextLocalEntity;
import aletheia.persistence.berkeleydb.entities.local.BerkeleyDBStatementLocalEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBAssumptionEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBContextEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBDeclarationEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBStatementEntity;
import aletheia.protocol.primitive.IntegerProtocol;
import aletheia.protocol.primitive.UUIDProtocol;
import aletheia.security.messagedigester.BufferedMessageDigester;
import aletheia.security.utilities.SecurityUtilities;

public class EntityStoreUpgrade_020 extends EntityStoreUpgrade
{

	public EntityStoreUpgrade_020()
	{
	}

	@Override
	public Collection<Integer> versions()
	{
		return Arrays.asList(20);
	}

	protected class UpgradeInstance extends EntityStoreUpgrade.UpgradeInstance
	{
		private final UUIDProtocol uuidProtocol = new UUIDProtocol(0);
		private final IntegerProtocol integerProtocol = new IntegerProtocol(0);

		private final Set<UUID> declarationUuids = new HashSet<UUID>();
		private final Map<UUID, Collection<BerkeleyDBDeclarationEntity>> pendingInitializations = new HashMap<UUID, Collection<BerkeleyDBDeclarationEntity>>();

		protected UpgradeInstance(BerkeleyDBAletheiaEnvironment environment, String storeName)
		{
			super(environment, storeName);
		}

		@Override
		protected List<String> entityClassNames(RawStore store)
		{
			List<String> names = super.entityClassNames(store);
			Collections.sort(names, new Comparator<String>()
			{

				final String statementEntityClassName = BerkeleyDBStatementEntity.class.getName();

				@Override
				public int compare(String o1, String o2)
				{
					int c;
					c = -Boolean.compare(o1.equals(statementEntityClassName), o2.equals(statementEntityClassName));
					if (c != 0)
						return c;
					return 0;
				}
			});
			return names;
		}

		@Override
		protected void putConvertedRawObject(com.sleepycat.je.Transaction tx, EntityModel aletheiaModel, Class<Object> entityClass,
				Class<Object> primaryKeyClass, PrimaryIndex<Object, Object> newPrimaryIndex, RawObject oldRawObject)
		{
			if (entityClass.equals(BerkeleyDBStatementEntity.class))
			{
				BerkeleyDBStatementEntity se = (BerkeleyDBStatementEntity) aletheiaModel.convertRawObject(oldRawObject);
				if (se instanceof BerkeleyDBContextEntity)
				{
					{
						BerkeleyDBContextEntity ce = (BerkeleyDBContextEntity) se;
						Collection<BerkeleyDBDeclarationEntity> pending = pendingInitializations.get(se.getUuid());
						if (pending != null)
							for (BerkeleyDBDeclarationEntity de : pending)
							{
								de.initializeContextData(ce);
								de.setConsequent(de.getConsequent());
								newPrimaryIndex.put(tx, de);
							}
					}
					if (se instanceof BerkeleyDBDeclarationEntity)
					{

						try
						{
							BerkeleyDBDeclarationEntity de = (BerkeleyDBDeclarationEntity) se;
							declarationUuids.add(de.getUuid());
							BerkeleyDBContextEntity ce = (BerkeleyDBContextEntity) newPrimaryIndex.get(tx, de.getUuidKeyContext(), LockMode.DEFAULT);
							if (ce != null)
								de.initializeContextData(ce);
							else
							{
								Collection<BerkeleyDBDeclarationEntity> pending = pendingInitializations.get(de.getContextUuid());
								if (pending == null)
								{
									pending = new ArrayList<BerkeleyDBDeclarationEntity>();
									pendingInitializations.put(de.getContextUuid(), pending);
								}
								pending.add(de);
							}
							Term term = de.getVariable().getType();
							int i = 0;
							while (term instanceof FunctionTerm)
							{
								FunctionTerm functionTerm = (FunctionTerm) term;
								BerkeleyDBAssumptionEntity ae = new BerkeleyDBAssumptionEntity();
								BufferedMessageDigester md = new BufferedMessageDigester("SHA-1");
								uuidProtocol.send(md.dataOutput(), de.getUuid());
								integerProtocol.send(md.dataOutput(), i);
								UUID uuid = SecurityUtilities.instance.messageDigestDataToUUID(md.digest());
								IdentifiableVariableTerm variable = new IdentifiableVariableTerm(functionTerm.getParameter().getType(), uuid);
								ae.setUuid(variable.getUuid());
								ae.setVariable(variable);
								ae.setProved(false);
								ae.setContextUuid(de.getUuid());
								Set<UUID> uuidDependencies = ae.getUuidDependencies();
								for (IdentifiableVariableTerm v : variable.getType().freeIdentifiableVariables())
									uuidDependencies.add(v.getUuid());
								ae.initializeContextData(de);
								ae.setOrder(i);
								newPrimaryIndex.put(tx, ae);
								term = functionTerm.getBody().replace(functionTerm.getParameter(), variable);
								i++;
							}
							de.setConsequent((SimpleTerm) term);
						}
						catch (Exception e)
						{
							throw new UpgradeException(e);
						}
					}
				}
				se.setProved(false);
				newPrimaryIndex.put(tx, se);
			}
			else if (entityClass.equals(BerkeleyDBStatementAuthorityEntity.class))
			{
				BerkeleyDBStatementAuthorityEntity sae = (BerkeleyDBStatementAuthorityEntity) aletheiaModel.convertRawObject(oldRawObject);
				if (declarationUuids.contains(sae.getStatementUuid()))
				{
					BerkeleyDBContextAuthorityEntity cae = new BerkeleyDBContextAuthorityEntity();
					cae.setStatementUuid(sae.getStatementUuid());
					cae.setContextUuid(sae.getContextUuid());
					cae.setAuthorUuid(sae.getAuthorUuid());
					cae.setCreationDate(sae.getCreationDate());
					cae.setValidSignature(sae.isValidSignature());
					cae.setSignedDependencies(sae.isSignedDependencies());
					cae.setSignedProof(sae.isSignedProof());
					sae = cae;
				}
				sae.setSignedProof(false);
				newPrimaryIndex.put(tx, sae);
			}
			else if (entityClass.equals(BerkeleyDBStatementLocalEntity.class))
			{
				BerkeleyDBStatementLocalEntity sle = (BerkeleyDBStatementLocalEntity) aletheiaModel.convertRawObject(oldRawObject);
				if (declarationUuids.contains(sle.getStatementUuid()))
				{
					BerkeleyDBContextLocalEntity cle = new BerkeleyDBContextLocalEntity();
					cle.setStatementUuid(sle.getStatementUuid());
					cle.setContextUuid(sle.getContextUuid());
					cle.setSubscribeProof(sle.isSubscribeProof());
					cle.setSubscribeStatements(false);
					sle = cle;
				}
				newPrimaryIndex.put(tx, sle);
			}
			else
				super.putConvertedRawObject(tx, aletheiaModel, entityClass, primaryKeyClass, newPrimaryIndex, oldRawObject);
		}

		@Override
		protected void postProcessing(BerkeleyDBPersistenceManager persistenceManager) throws UpgradeException
		{
			super.postProcessing(persistenceManager);
			Transaction transaction = persistenceManager.beginTransaction();
			try
			{
				for (RootContext rootContext : persistenceManager.rootContexts(transaction).values())
					rootContext.rebuildProved(transaction);
				transaction.commit();
			}
			catch (Exception e)
			{
				throw new UpgradeException(e);
			}
			finally
			{
				transaction.abort();
			}
		}

	}

	@Override
	protected UpgradeInstance instance(BerkeleyDBAletheiaEnvironment environment, String storeName)
	{
		return new UpgradeInstance(environment, storeName);
	}

}
