#*******************************************************************************
# Copyright (c) 2019 Quim Testar.
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
#*******************************************************************************
IFS="
"
for f in $(grep -l "@SecondaryKey" $(find . -name '*.java'))
do
	a=0
	for l in $(grep -n "@SecondaryKey" $f)
	do	
		n=$(expr $(echo $l | cut -d":" -f1) + $a)
		n_1=$(expr $n + 1)
		l=$(cat $f | sed -n "${n_1}p")
		ff=$(echo $l | sed "s/private//" | sed "s/final//" |  sed "s/^[[:blank:]]*//" | sed "s/[[:blank:]]\+/ /g" | sed "s/;.*$//" | cut -d" " -f2)
		echo $ff
		sed -i "${n} i\
public static final String ${ff}_FieldName=\"${ff}\";" $f
		a=$(expr $a + 1)
	done
done
