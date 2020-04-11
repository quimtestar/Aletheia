module aletheia.pdfexport
{
	exports aletheia.pdfexport.statement;
	exports aletheia.pdfexport;
	exports aletheia.pdfexport.document;
	exports aletheia.pdfexport.font;
	exports aletheia.pdfexport.term;

	requires transitive aletheia.base;
	requires aletheia.utilities;
	requires transitive itextpdf;
}