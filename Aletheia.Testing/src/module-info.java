module aletheia.test
{
	exports aletheia.test.parameteridentification;
	exports aletheia.test.proofterm;
	exports aletheia.test.replacement;
	exports aletheia.test.unsorted;
	exports aletheia.test.gui;
	exports aletheia.test.local;
	exports aletheia.test.authority;
	exports aletheia.test.useless;
	exports aletheia.test.parser;
	exports aletheia.test.renumber;
	exports aletheia.test;
	exports aletheia.test.protocol;

	requires aletheia.gui;
	requires aletheia.base;
	requires aletheia.log4j;
	requires aletheia.parsergenerator;
	requires aletheia.persistence.berkeleydb;
	requires aletheia.protocol;
	requires aletheia.utilities;
	requires java.desktop;
	requires java.prefs;
	requires org.apache.logging.log4j;
}