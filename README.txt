================= README.txt ===========================

This file contains instructions to build and run the project.

--- Basic Flow:

1. Compile GraphServer.java:
	javac -cp .:./jars/libthrift-0.10.0.jar:./jars/slf4j.jar -d . *.java GraphServer.java

2. Compile GraphClient.java:
	javac -cp .:./jars/libthrift-0.10.0.jar:./jars/slf4j.jar -d . *.java GraphClient.java

3. Create N servers:
	./create_servers.sh N
	
4. Run GraphClient:
	java -cp ".:./jars/libthrift-0.10.0.jar:./jars/slf4j.jar:./jars/slf4j-simple-1.7.25.jar" graphservice.GraphClient port 

5. Kill all server processes:
	./kill_servers.sh

--- Auxiliary commands:

1. Add execution permition to a .sh file:
	chmod +x filename.sh

2. Run GraphServer:
	java -cp ".:./jars/libthrift-0.10.0.jar:./jars/slf4j.jar:./jars/slf4j-simple-1.7.25.jar" graphservice.GraphServer N id firstPort


