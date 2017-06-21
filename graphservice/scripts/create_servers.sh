#!/bin/bash

cd .. # returning to project's root directory
rm logs/* # cleaning log files

N=$1
echo "Initializing "$N" servers" >> logs/log.txt

firstPort=9090
id=0

while [ $id -lt $N ]; do
	echo Running java GraphServer $N $id $firstPort >> logs/log.txt
	filename="logServer"$id".txt"
	filePath=logs/$filename
	touch $filePath
	java -cp ".:./jars/libthrift-0.10.0.jar:./jars/slf4j.jar:./jars/slf4j-simple-1.7.25.jar" graphservice.GraphServer $N $id $firstPort > $filePath &
	((id++))
done
