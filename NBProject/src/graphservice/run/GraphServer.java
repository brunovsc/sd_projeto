/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphservice.run;

import graphservice.handler.Graph;
import graphservice.handler.ServerHandler;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TTransportException;

/**
 *
 * @author sd-server
 */
public class GraphServer {
    
    private static Graph.Processor processor;
    private static ServerHandler handler;
    
    public static void main(String [] args){
    
        try {

            TServerTransport serverTransport = new TServerSocket(Integer.parseInt(args[1]));
            handler = new ServerHandler(args);
            processor = new Graph.Processor(handler);

            TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));

            System.out.println("Starting the simple server on port " + args[1]);
            new Thread(new Runnable() {
                @Override
                public void run() {
                   connectServers(args);
                }
            }).start();
            server.serve();
        } catch (NumberFormatException | TTransportException x){
            System.out.println(x);
        }
    }
    
    public static void connectServers(String []args){
        int N = Integer.parseInt(args[0]);
        int selfId = Integer.parseInt(args[1]);
        int firstPort = Integer.parseInt(args[2]);
        
        for(int i = 0; i < selfId; i++){
            if(i != selfId){
                try{
                    handler.connectToServerId(i, firstPort+i);
                }catch(Exception e){
                    try {
                        sleep(1000);
                    } catch (InterruptedException ex) {
                        System.out.println(ex);
                    }
                }
            }
        }   
    }
}