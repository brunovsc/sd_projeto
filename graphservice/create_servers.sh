#!/bin/bash

N=$1
echo $N" servers"

firstPort=9093
id=0

while [ $id -lt $N ]; do
	echo Running java GraphServer $N $id $firstPort >> logs/log.txt
	filename="logServer"$id".txt"
	touch $filename
	java -cp ".:./jars/libthrift-0.10.0.jar:./jars/slf4j.jar:./jars/slf4j-simple-1.7.25.jar" graphservice.GraphServer $N $id $firstPort > logs/$filename &
	((id++))
done

clear
