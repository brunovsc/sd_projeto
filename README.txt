=========================== README.txt ===========================

By:
11311BCC009 - Bruno Sergio Cardoso Vieira
11321BCC003 - Karen Catigua Junqueira
11411BCC018 - Matheus Prado Prandini Faria

==================================================================
This file contains instructions to build and run the project.
	* scripts must be executed from scripts/ folder

--- Basic Flow:

1. Compile GraphServer.java:
	javac -cp .:./jars/libthrift-0.10.0.jar:./jars/slf4j.jar:./jars/slf4j-simple-1.7.25.jar:./gen-java -d . *.java GraphServer.java

2. Compile GraphClient.java:
	javac -cp .:./jars/libthrift-0.10.0.jar:./jars/slf4j.jar:./jars/slf4j-simple-1.7.25.jar:./gen-java -d . *.java GraphClient.java

3. Create N servers:
	./create_servers.sh N
	* N is the number of servers to initialize
	* run command from /scripts folder
	
4. Run GraphClient:
	java -cp .:./jars/libthrift-0.10.0.jar:./jars/slf4j.jar:./jars/slf4j-simple-1.7.25.jar graphservice.GraphClient port 
	* port is the port in which the client should connect to (see ports opened in the logs/log.txt file)

5. Kill all server processes:
	./kill_servers.sh
	* run command from /scripts folder

--- Auxiliary commands:

1. Re-compile thrift files:
	thrift --gen java graph_service.thrift
	* there is an error at the class Graph. Add comment on entire inner class AsyncClient
	* recompile GraphServer and GraphClient afterwards

2. Add execution permition to a .sh file:
	chmod +x filename.sh

3. Run GraphServer:
	java -cp .:./jars/libthrift-0.10.0.jar:./jars/slf4j.jar:./jars/slf4j-simple-1.7.25.jar graphservice.GraphServer N id firstPort


