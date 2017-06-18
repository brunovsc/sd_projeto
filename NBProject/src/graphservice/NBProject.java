/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphservice;
import graphservice.handler.ServerHandler;
import graphservice.run.GraphServer;
import java.util.Scanner;
import java.util.ArrayList;
/**
 *
 * @author sd-server
 */
public class NBProject {

    /**
     * @param args the command line arguments
     */
    private static String[] ports;
    
    public static void main(String[] args) {
        // TODO code application logic here
        int N;
        System.out.print("Number of servers: ");
        Scanner s = new Scanner(System.in);
        N = s.nextInt();
        ports = new String[N];
        
        int firstPort = 9090;
        for(int i = 0; i < N; i++){
            ports[i] = String.format("%d", firstPort + i);
        }       
        
        ArrayList<ServerHandler> handlers = new ArrayList<>();
        for(int i = 0; i < N; i++){
            String[] arguments = new String[N+2];
            arguments[0] = String.format("%d", N); // total of ports (other servers)
            arguments[1] = ports[i];
            for(int j = 0; j < N; j++){
                arguments[j+2] = ports[j];
            }
            
            handlers.add(GraphServer.initServer(arguments));
        }
        
        for(ServerHandler h: handlers){
            h.connectServers();
        }
    }
}
