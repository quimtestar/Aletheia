package aletheia.persistence.berkeleydb.upgrade;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;

import aletheia.model.authority.StatementAuthority;
import aletheia.model.authority.StatementAuthority.AuthorityWithNoParentException;
import aletheia.model.local.ContextLocal;
import aletheia.model.local.StatementLocal;
import aletheia.model.nomenclator.Nomenclator.NomenclatorException;
import aletheia.model.statement.Assumption;
import aletheia.model.statement.Context;
import aletheia.model.statement.Declaration;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Specialization;
import aletheia.model.statement.Statement;
import aletheia.model.statement.Statement.StatementException;
import aletheia.model.statement.UnfoldingContext;
import aletheia.model.term.AtomicTerm;
import aletheia.model.term.CompositionTerm;
import aletheia.model.term.CompositionTerm.CompositionTypeException;
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.FunctionTerm.NullParameterTypeException;
import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.ProjectionTerm;
import aletheia.model.term.ProjectionTerm.ProjectionTypeException;
import aletheia.model.term.SimpleTerm;
import aletheia.model.term.TTerm;
import aletheia.model.term.Term;
import aletheia.model.term.VariableTerm;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBAletheiaEnvironment;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.protocol.Protocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.primitive.StringProtocol;
import aletheia.protocol.primitive.UUIDProtocol;
import aletheia.utilities.collections.CombinedMap;

public class EntityStoreUpgrade_021 extends EntityStoreUpgrade
{

	@Override
	public Collection<Integer> versions()
	{
		return Arrays.asList(21);
	}

	protected class UpgradeInstance extends EntityStoreUpgrade.UpgradeInstance
	{

		protected UpgradeInstance(BerkeleyDBAletheiaEnvironment environment, String storeName)
		{
			super(environment, storeName);
		}

		@Override
		protected void upgrade() throws UpgradeException
		{
			justPostProcessingUpgrade();
		}

		private Term translateTerm(Map<UUID, UUID> uuidMap, Term term)
		{
			return translateTerm(new HashMap<ParameterVariableTerm, ParameterVariableTerm>(), uuidMap, term);
		}

		private Term translateTerm(Map<ParameterVariableTerm, ParameterVariableTerm> paramMap, Map<UUID, UUID> uuidMap, Term term)
		{
			try
			{
				if (term instanceof FunctionTerm)
				{
					FunctionTerm fTerm = (FunctionTerm) term;
					ParameterVariableTerm par = new ParameterVariableTerm(translateTerm(paramMap, uuidMap, fTerm.getParameter().getType()));
					paramMap.put(fTerm.getParameter(), par);
					Term body = translateTerm(paramMap, uuidMap, fTerm.getBody());
					paramMap.remove(fTerm.getParameter());
					return new FunctionTerm(par, body);
				}
				else if (term instanceof SimpleTerm)
				{
					if (term instanceof AtomicTerm)
					{
						if (term instanceof ProjectionTerm)
						{
							ProjectionTerm pTerm = (ProjectionTerm) term;
							return new ProjectionTerm((FunctionTerm) translateTerm(paramMap, uuidMap, pTerm.getFunction()));
						}
						else if (term instanceof TTerm)
						{
							return TTerm.instance;
						}
						else if (term instanceof VariableTerm)
						{
							if (term instanceof IdentifiableVariableTerm)
							{
								IdentifiableVariableTerm iTerm = (IdentifiableVariableTerm) term;
								return new IdentifiableVariableTerm(translateTerm(paramMap, uuidMap, iTerm.getType()), uuidMap.get(iTerm.getUuid()));
							}
							else if (term instanceof ParameterVariableTerm)
							{
								ParameterVariableTerm pTerm = (ParameterVariableTerm) term;
								return paramMap.get(pTerm);
							}
							else
								throw new Error();

						}
						else
							throw new Error();

					}
					else if (term instanceof CompositionTerm)
					{
						CompositionTerm cTerm = (CompositionTerm) term;
						SimpleTerm headD = (SimpleTerm) translateTerm(paramMap, uuidMap, cTerm.getHead());
						Term tailD = translateTerm(paramMap, uuidMap, cTerm.getTail());
						return new CompositionTerm(headD, tailD);
					}
					else
						throw new Error();
				}
				else
					throw new Error();
			}
			catch (NullParameterTypeException | ProjectionTypeException | CompositionTypeException e)
			{
				throw new RuntimeException(e);
			}
		}

		private UUID translateUuid(UUID uuid)
		{
			class UUIDGenerationProtocol extends Protocol<UUID>
			{
				final StringProtocol stringProtocol = new StringProtocol(0);
				final UUIDProtocol uuidProtocol = new UUIDProtocol(0);

				public UUIDGenerationProtocol()
				{
					super(0);
				}

				@Override
				public void send(DataOutput out, UUID uuid) throws IOException
				{
					stringProtocol.send(out, "EntityStoreUpgrade_021");
					uuidProtocol.send(out, uuid);
				}

				@Override
				public UUID recv(DataInput in) throws IOException, ProtocolException
				{
					throw new UnsupportedOperationException();
				}

				@Override
				public void skip(DataInput in) throws IOException, ProtocolException
				{
					throw new UnsupportedOperationException();
				}

			}

			return UUID.nameUUIDFromBytes(new UUIDGenerationProtocol().toByteArray(uuid));

		}

		private void copyStatementExtras(Transaction transaction, Statement stO, Statement stD)
		{
			try
			{
				StatementAuthority stAuthO = stO.getAuthority(transaction);
				if (stAuthO != null)
					stD.createAuthorityOverwrite(transaction, stAuthO.getAuthor(transaction), stAuthO.getCreationDate());

			}
			catch (AuthorityWithNoParentException e)
			{
				throw new RuntimeException();
			}

			try
			{
				StatementLocal stLocO = stO.getLocal(transaction);
				if (stLocO != null)
				{
					StatementLocal stLocD = stD.getOrCreateLocal(transaction);
					stLocD.setSubscribeProof(transaction, stLocO.isSubscribeProof());
					if ((stLocO instanceof ContextLocal) && (stLocD instanceof ContextLocal))
					{
						ContextLocal ctxLocO = (ContextLocal) stLocO;
						ContextLocal ctxLocD = (ContextLocal) stLocD;
						ctxLocD.setSubscribeStatements(transaction, ctxLocO.isSubscribeStatements());
					}
				}

			}
			finally
			{

			}

		}

		@Override
		protected void postProcessing(BerkeleyDBPersistenceManager persistenceManager)
		{
			Transaction transaction = persistenceManager.beginTransaction();
			try
			{
				for (RootContext rootCtx : persistenceManager.rootContexts(transaction).values())
				{
					RootContext newRootCtx = RootContext.create(persistenceManager, transaction, translateUuid(rootCtx.getUuid()), rootCtx.getTerm());
					newRootCtx.identify(transaction, rootCtx.getIdentifier());
					copyStatementExtras(transaction, rootCtx, newRootCtx);
					class StackEntry
					{
						final Context ctxO;
						final Context ctxD;
						final Map<UUID, UUID> uuidMap;

						public StackEntry(Context ctxO, Context ctxD, Map<UUID, UUID> uuidMap)
						{
							super();
							this.ctxO = ctxO;
							this.ctxD = ctxD;
							this.uuidMap = uuidMap;
						}
					}
					;
					Stack<StackEntry> stack = new Stack<>();
					stack.push(new StackEntry(rootCtx, newRootCtx, Collections.singletonMap(rootCtx.getUuid(), newRootCtx.getUuid())));
					while (!stack.isEmpty())
					{
						StackEntry se = stack.pop();
						Map<UUID, UUID> frontUuidMap = new HashMap<>();
						Map<UUID, UUID> uuidMap = new CombinedMap<>(frontUuidMap, se.uuidMap);
						for (Statement stO : se.ctxO.localDependencySortedStatements(transaction))
						{
							UUID uuidD = translateUuid(stO.getUuid());
							Statement stD;
							if (stO instanceof Assumption)
							{
								Assumption asO = (Assumption) stO;
								stD = se.ctxD.assumptions(transaction).get(asO.getOrder());
							}
							else if (stO instanceof Specialization)
							{
								Specialization spcO = (Specialization) stO;
								Statement genD = persistenceManager.getStatement(transaction, uuidMap.get(spcO.getGeneralUuid()));
								Term instanceD = translateTerm(uuidMap, spcO.getInstance());
								stD = se.ctxD.specialize(transaction, uuidD, genD, instanceD);
							}
							else if (stO instanceof Context)
							{
								Context ctxO = (Context) stO;
								Term termD = translateTerm(uuidMap, ctxO.getTerm());
								if (stO instanceof UnfoldingContext)
								{
									UnfoldingContext unfO = (UnfoldingContext) stO;
									Declaration decD = persistenceManager.getDeclaration(transaction, uuidMap.get(unfO.getDeclarationUuid()));
									stD = se.ctxD.openUnfoldingSubContext(transaction, uuidD, termD, decD);
								}
								else if (stO instanceof Declaration)
								{
									Declaration decO = (Declaration) stO;
									Term valD = translateTerm(uuidMap, decO.getValue());
									stD = se.ctxD.declare(transaction, uuidD, valD);
								}
								else if (stO instanceof RootContext)
									throw new RuntimeException();
								else
								{
									stD = se.ctxD.openSubContext(transaction, uuidD, termD);
								}
								stack.push(new StackEntry(ctxO, (Context) stD, uuidMap));
							}
							else
								throw new Error();
							frontUuidMap.put(stO.getUuid(), stD.getUuid());
							if (stO.getIdentifier() != null)
								stD.identify(transaction, stO.getIdentifier());
							copyStatementExtras(transaction, stO, stD);
						}
					}
					rootCtx.delete(transaction);
				}
				transaction.commit();
			}
			catch (StatementException | NomenclatorException e)
			{
				throw new RuntimeException(e);
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
