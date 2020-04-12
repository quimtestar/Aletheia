module aletheia.utilities
{
	exports aletheia.utilities;
	exports aletheia.utilities.collections;
	exports aletheia.utilities.io;
	exports aletheia.utilities.aborter;
	exports aletheia.utilities.gui;

	requires aletheia.log4j;
	requires transitive java.desktop;
	requires java.management;
}