#-------------------------------------------------------------------------------
# Copyright (c) 2016 Quim Testar.
# 
# This file is part of the Aletheia Proof Assistant.
# 
# The Aletheia Proof Assistant is free software: you can redistribute it and/or
# modify it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the License,
# or (at your option) any later version.
# 
# The Aletheia Proof Assistant is distributed in the hope that it will be
# useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero
# General Public License for more details.
# 
# You should have received a copy of the GNU Affero General Public License
# along with the Aletheia Proof Assistant. If not, see
# <http://www.gnu.org/licenses/>.
#-------------------------------------------------------------------------------

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

