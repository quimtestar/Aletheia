'<'																: openfun; 			# Open a function term.
':'																: colon;			# Separates the parameter name from its type in a function term.
'\->'															: arrow;			# Separates the parameter from the body in a function term.
'>'																: closefun;			# Close a function term.
'\*'															: projection; 		# Applied to a function creates a projection term.
'\('															: openpar;			# Open parentheses.
'\)'															: closepar;			# Close patentheses.
'\.'															: dot;				# Separate the identifier qualifying components.
'T'																: ttype;			# The primitive type symbol.
'&'																: ampersand;		# Begins a reference.
'\,'															: comma;			# For the component number operator.
'\|\-'															: turnstile;		# "|-" symbol to refer to the consequent of a context.
'%'																: percent;			# Parameter's type operator.
'\#'															: sharp;			# Type operator.
'!'																: bang;				# Unfold operator.
'='																: equals;			# Auto-unfold operator.
'\-'															: hyphen;			# For the component number operator.
'/'																: bar;				# Path separator.
'\?'															: question;			# Unused.
'\''															: apostrophe;		# Function body operator.
'[\0-\9]+'														: number;			# For the component number operator.
'[a-zA-Z\_][a-zA-Z\_\0-\9]*'									: id;				# Identifier unqualified components. 
'@[\0-\9]+'														: atparam;			# Alternative to identifiers just for function parameters.
'\$[\0-\9a-fA-F]+'												: hexref;			# Hexadecimal references.
'[\0-\9a-fA-F]{8}\-([\0-\9a-fA-F]{4}\-){3}[\0-\9a-fA-F]{12}'	: uuid;				# UUID references.
'_*'															: ;					# Skip whitespaces.

