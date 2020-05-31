module aletheia.security
{
	requires aletheia.protocol;
	requires aletheia.utilities;

	exports aletheia.security.model;
	exports aletheia.security.signerverifier;
	exports aletheia.security.protocol;
	exports aletheia.security.messagedigester;
	exports aletheia.security.utilities;
}