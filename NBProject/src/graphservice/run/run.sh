#!/bin/bash

N=$1
echo $N

firstPort=9090
id=0

while [ $id -lt $N ]; do
	echo Running java GraphServer $N $id $firstPort
	java GraphServer $N $id $firstPort
	((id++))
done
