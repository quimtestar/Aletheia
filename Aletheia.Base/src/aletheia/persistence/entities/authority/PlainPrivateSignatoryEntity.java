package aletheia.persistence.entities.authority;

import java.security.PrivateKey;

public interface PlainPrivateSignatoryEntity extends PrivateSignatoryEntity
{
	public PrivateKey getPrivateKey();

	public void setPrivateKey(PrivateKey privateKey);
}
