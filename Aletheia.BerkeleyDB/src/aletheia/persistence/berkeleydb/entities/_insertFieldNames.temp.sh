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
