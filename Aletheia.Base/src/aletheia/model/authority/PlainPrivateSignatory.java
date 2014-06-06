package aletheia.model.authority;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.UUID;

import aletheia.persistence.PersistenceManager;
import aletheia.persistence.entities.authority.PlainPrivateSignatoryEntity;

public class PlainPrivateSignatory extends PrivateSignatory
{

	protected PlainPrivateSignatory(PersistenceManager persistenceManager, UUID uuid, String signatureAlgorithm, PublicKey publicKey, PrivateKey privateKey)
			throws KeysDontMatchException
	{
		super(persistenceManager, PlainPrivateSignatoryEntity.class, uuid, signatureAlgorithm, publicKey, privateKey);
		setPrivateKey(privateKey);
	}

	public PlainPrivateSignatory(PersistenceManager persistenceManager, PlainPrivateSignatoryEntity entity)
	{
		super(persistenceManager, entity);
	}

	@Override
	public PlainPrivateSignatoryEntity getEntity()
	{
		return (PlainPrivateSignatoryEntity) super.getEntity();
	}

	@Override
	protected void setPrivateKey(PrivateKey privateKey)
	{
		getEntity().setPrivateKey(privateKey);
	}

	@Override
	public PrivateKey getPrivateKey()
	{
		return getEntity().getPrivateKey();
	}

}
