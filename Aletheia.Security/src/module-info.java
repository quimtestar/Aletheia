module aletheia.security
{
	exports aletheia.model.security; //TODO rename
	exports aletheia.security.signerverifier;
	exports aletheia.protocol.security; //TODO rename
	exports aletheia.security.messagedigester;
	exports aletheia.security.utilities;

	requires aletheia.protocol;
	requires aletheia.utilities;
}