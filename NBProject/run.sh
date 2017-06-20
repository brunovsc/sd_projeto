#!/bin/bash

N=$1
echo $N" servers"

firstPort=9093
id=0

while [ $id -lt $N ]; do
	touch log.txt
	echo Running java GraphServer $N $id $firstPort >> log.txt
	filename="logServer"$id".txt"
	touch $filename
	java -jar "NBProject.jar" $N $id $firstPort > $filename &
	((id++))
done

clear
