module aletheia.utilities
{
	requires aletheia.log4j;
	requires transitive java.desktop;
	requires java.management;

	exports aletheia.utilities;
	exports aletheia.utilities.collections;
	exports aletheia.utilities.io;
	exports aletheia.utilities.aborter;
	exports aletheia.utilities.gui;
}