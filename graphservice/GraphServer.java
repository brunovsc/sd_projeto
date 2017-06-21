package graphservice;

import static java.lang.Thread.sleep;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TTransportException;

public class GraphServer {
    
    private static Graph.Processor processor;
    private static ServerHandler handler;
    private static int N;
    private static int selfId;
    private static int firstPort;
    private static int selfPort;
    private static final int RETRIES = 5;
    private static final int WAIT_RETRY_CONNECTION = 1000;
    
    public static void main(String [] args){
            
        N = Integer.parseInt(args[0]);
        selfId = Integer.parseInt(args[1]);
        firstPort = Integer.parseInt(args[2]);
        selfPort = firstPort + selfId;
        
        System.out.println("===== Starting the server on port " + selfPort);
        
        try {
               
            TServerTransport serverTransport = new TServerSocket(selfPort);
            handler = new ServerHandler(args);
            processor = new Graph.Processor(handler);

            TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));

            new Thread(new Runnable() {
                @Override
                public void run() {
                   connectServers();
                }
            }).start();
            server.serve();
        } catch (NumberFormatException | TTransportException x){
            System.out.println(x);
        }
    }
    
    public static void connectServers(){
        System.out.println("\n##### Creating connections to other servers #####\n");
        boolean connected;
        int retried;
        for(int i = 0; i < N; i++){
            if(i != selfId){
                connected = false;
                retried = 0;
                while(!connected && retried < RETRIES){
                    try{
                        System.out.println("Trying to connect to port " + (firstPort+i));
                        handler.connectToServerId(i, firstPort+i);
                        connected = true;
                    }catch(Exception e){
                        try {
                            System.out.println("Couldn't connect to server " + (firstPort+i) + " ... retrying in " + (WAIT_RETRY_CONNECTION / 1000) + " seconds");
                            sleep(WAIT_RETRY_CONNECTION);
                            retried++;
                        } catch (InterruptedException ex) {
                            System.out.println(ex);
                        }
                    }
                    if(!connected){
                        System.out.println("Unable to connect server on port " + (firstPort+i) + ". Finishing server...");
                    }
                }
            }
        }   
        System.out.println("\n##### Finished creating connections to other servers #####\n");
    }
}
