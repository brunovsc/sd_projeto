/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphservice.run;

import graphservice.handler.Graph;
import graphservice.handler.ServerHandler;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;

import java.util.HashMap;
/**
 *
 * @author sd-server
 */
public class GraphServer {
    public static void main(String [] args){
	
		try {

			TServerTransport serverTransport = new TServerSocket(9090);
			ServerHandler handler = new ServerHandler();
			Graph.Processor processor = new Graph.Processor(handler);

                        handler.createVertice(1, 1, 1.0, "1");
                        handler.createVertice(2, 2, 2.0, "2");
                        handler.createVertice(3, 3, 3.0, "3");
                        
                        handler.createAresta(1, 2, 1.2, false, "1.2");
                        handler.createAresta(2, 3, 2.3, true, "2.3 d");
                        handler.createAresta(3, 1, 3.1, false, "3.1");
                        
			TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));
			//TServer server = new TSimpleServer(new Args(serverTransport).processor(processor));

			System.out.println("Starting the simple server...");
                        
			server.serve();
		} catch (Exception x){
			x.printStackTrace();
		}
	}
}