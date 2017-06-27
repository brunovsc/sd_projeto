package graphservice;

import java.util.ArrayList;
import org.apache.thrift.TException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.lang.Thread.sleep;
import java.util.LinkedList;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class ServerHandler implements Graph.Iface{
    
    private final Grafo grafo = new Grafo();
    private static final long sleepTime = 1000;
    private static final int maxRetries = 10;
    private static final long waitToProcess = 5000; // test of concurrency
    private static final boolean testConcurrency = false; // test of concurrency
    
    // Multiple servers
    private TTransport []transports;
    private TProtocol []protocols;
    private Graph.Client []clients;
    private static int ports[]; // array of ports for others servers
    private int selfPort; // number of the port of this server
    private static int N; // number of servers
    private int selfId;
    
    private List<Integer> blockedVertices = new ArrayList<>();

    public ServerHandler(String []args){
        
        grafo.vertices = new ArrayList<>();
        grafo.arestas = new ArrayList<>(); 
        
        N = Integer.parseInt(args[0]);
        selfId = Integer.parseInt(args[1]);
        int firstPort = Integer.parseInt(args[2]);
        selfPort = firstPort + selfId;
        ports = new int[N];
        for(int i = 0; i < N; i++){
            ports[i] = firstPort + i;
        } 
        
        transports = new TTransport[N];
        protocols = new TProtocol[N];
        clients = new Graph.Client[N];
    }    
    
    public void connectToServerId(int id, int port){
        try{
            transports[id] = new TSocket("localhost", port);
            transports[id].open();
            protocols[id] = new TBinaryProtocol(transports[id]);
            clients[id] = new Graph.Client(protocols[id]);
            System.out.println("Server " + selfPort + " connected to server " + port);
        }catch(TTransportException e){
            System.out.println(e);
        }
    }
    
    public int processRequest(int vertice){
        try{
            int server = MD5.md5(String.format("%d", vertice), String.format("%d", N));  
            return server;
        }catch(Exception e){
            e.printStackTrace();
        }
        return -1;        
    }
    
    public boolean isBlockedVertice(int nome){
        Integer vertice = nome;
        return blockedVertices.contains(vertice);
    }
    
    public void blockVertice(int nome){
        if(testConcurrency){
            System.out.println("!!! Blocked vertice " + nome);
        }
        blockedVertices.add(nome);
    }
    
    public void unblockVertice(int nome){
        if(testConcurrency){
            System.out.println("!!! Unblocked vertice " + nome);
        }
        blockedVertices.remove(new Integer(nome));
    }
    
    public void unblockAresta(int v1, int v2){
        unblockVertice(v1);
        unblockVertice(v2);
    }
    
    public void verifyResourceVertice(int vertice) throws ResourceInUse{
        int retries = 0;
        while(isBlockedVertice(vertice) && retries < maxRetries){
            if(testConcurrency){
                System.out.println("Resource " + vertice + " being used");
            }
            waitResource();
            retries++;
        }
        if(isBlockedVertice(vertice)){
            throw new ResourceInUse(vertice);
        }
        blockVertice(vertice);
        if(testConcurrency){ // test of concurrency
            try {            
                sleep(waitToProcess);
            } catch (InterruptedException ex) {
                Logger.getLogger(ServerHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
        
    public void verifyResourceAresta(int vertice1, int vertice2) throws ResourceInUse{
        int retries = 0;
        while(isBlockedVertice(vertice1) && retries < maxRetries){
            if(testConcurrency){
                System.out.println("Resource " + vertice1 + " being used");
            }
            waitResource();
            retries++;
        }
        if(isBlockedVertice(vertice1)){
            throw new ResourceInUse(vertice1);
        }
        blockVertice(vertice1);
        retries = 0;
        while(isBlockedVertice(vertice2) && retries < maxRetries){
            if(testConcurrency){
                System.out.println("Resource " + vertice2 + " being used");
            }
            waitResource();
            retries++;
        }
        if(isBlockedVertice(vertice2)){
            unblockVertice(vertice1);
            throw new ResourceInUse(vertice2);
        }
        blockVertice(vertice2);        
        if(testConcurrency){ // test of concurrency
            try {            
                sleep(waitToProcess);
            } catch (InterruptedException ex) {
                Logger.getLogger(ServerHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void waitResource(){
        try {
            sleep(sleepTime);
        } catch (InterruptedException ex) {
            Logger.getLogger(ServerHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void logForOperation(int operation){
        System.out.println("----- OPERATION on port " + selfPort + " - " + textForOperation(operation));
        
    }    
    
    public void logForwardedRequest(int server, int operation){
        System.out.println("----- FORWARDED request (" + textForOperation(operation) + ")  from server " + selfPort + " to server " + ports[server]);
    }
    
    public String textForOperation(int operation){
        switch(operation){
            case 0:
                return "";
            case 1:
                return "Create Vertice";
            case 2:
                return "Delete Vertice";
            case 3:
                return "Update Vertice";
            case 4:
                return "Read Vertice";
            case 5:
                return "Create Aresta";
            case 6:
                return "Delete Aresta";
            case 7:
                return "Update Aresta";
            case 8:
                return "Read Aresta";
            case 9:
                return "List Vertices Aresta";
            case 10:
                return "List Arestas Vertice";
            case 11:
                return "List Neighbors";
            case 12:
                return "List Vertices";
            case 13:
                return "List Arestas";
            case 14:
                return "List Self Vertices";
            case 15:
                return "List Self Arestas";
            case 16:
                return "Menor Caminho";
            case 17:
                return "Delete Arestas From Vertice";
            default:
                return "Invalid operation";
        }
    }
    
    public Vertice findVertice(Grafo grafo, int vertice){
        for(Vertice v: grafo.vertices){
            if(v.nome == vertice){
                return v;
            }
        }
        return null;
    }
    
    public Aresta findAresta(Grafo grafo, int vertice1, int vertice2){
        for(Aresta a : grafo.arestas){
            if((a.vertice1 == vertice1 && a.vertice2 == vertice2) || (a.vertice1 == vertice2 && a.vertice2 == vertice1)){
               return a;
            }
        }
        return null;
    }
    
    @Override
    public boolean createVertice(int nome, int cor, double peso, String descricao) throws KeyAlreadyUsed, ResourceInUse, TException {
        int server = processRequest(nome);
        if(server != selfId){
            logForwardedRequest(server, 1);
            try{
                boolean p = clients[server].createVertice(nome, cor, peso, descricao);
                return p;
            } catch(Exception e){
                System.out.println(e.getCause());
                throw e;
            }
        }
        verifyResourceVertice(nome);
        
        logForOperation(1);
        
        if(findVertice(grafo, nome) != null){ // Restriction: restrição de criação apenas se vértice já não existe
            unblockVertice(nome);
            throw new KeyAlreadyUsed(nome, "Vertice ja existente");            
        }
        
        grafo.addToVertices(new Vertice(nome, cor, peso, descricao));
        
        unblockVertice(nome);
        return true;
    }

    @Override
    public boolean deleteVertice(int key) throws KeyNotFound, ResourceInUse, TException {
        int server = processRequest(key);
        if(server != selfId){
            logForwardedRequest(server, 2);
            boolean p = clients[server].deleteVertice(key);
            return p;
        }
        verifyResourceVertice(key);
        
        logForOperation(2);
        
        Vertice vertice = findVertice(grafo, key);
        if(vertice == null){
            unblockVertice(key);
            throw new KeyNotFound(key, "Vertice nao encontrado");
        }
        
	ArrayList<Aresta> arestasToRemove = new ArrayList<Aresta>();
        for(Aresta a : grafo.arestas) {
            if(a.vertice1 == vertice.nome || a.vertice2 == vertice.nome){
                arestasToRemove.add(a);
            }
        }
	grafo.arestas.removeAll(arestasToRemove);

        for(int i = 0; i < N; i++) { // Arestas (v2, key) sao removidas nos demais servidores
            boolean p =  false;
            if(i != selfId){
                p = clients[i].deleteArestasFromVertice(key);
                if(p){
                    System.out.println("Deleted arestas at server " + ports[i]);
                }
            }
        }

        grafo.vertices.remove(vertice);
        unblockVertice(key);
        return true;
    }

    @Override
    public Vertice readVertice(int key) throws KeyNotFound, ResourceInUse, TException {
        int server = processRequest(key);
        if(server != selfId){
            logForwardedRequest(server, 4);
            Vertice p = clients[server].readVertice(key);
            return p;
        }
        verifyResourceVertice(key);
        
        logForOperation(4);
        Vertice vertice = findVertice(grafo, key);
        if(vertice == null){
            unblockVertice(key);
            throw new KeyNotFound(key, "Vertice nao encontrado");  
        }
        
        unblockVertice(key);
        return vertice;
    }

    @Override
    public boolean updateVertice(int nome, int cor, double peso, String descricao) throws KeyNotFound, ResourceInUse, TException {
        int server = processRequest(nome);
        if(server != selfId){
            logForwardedRequest(server, 3);
            boolean p = clients[server].updateVertice(nome, cor, peso, descricao);
            return p;
        }
        verifyResourceVertice(nome); 
        
        logForOperation(3);
        Vertice vertice = findVertice(grafo, nome);
        if(vertice == null){
            unblockVertice(nome);
            throw new KeyNotFound(nome, "Vertice nao encontrado");
        }
        // Restriction: restrição de não alteração do nome do vértice
        vertice.cor = cor;
        vertice.peso = peso;
        vertice.descricao = descricao;
        unblockVertice(nome);
        return true;
    }

    @Override
    public boolean createAresta(int vertice1, int vertice2, double peso, boolean direcionado, String descricao) throws KeyAlreadyUsed, ResourceInUse, KeyNotFound, BadParameter, TException {
    	if(vertice1 == vertice2){
    		throw new BadParameter("Arestas with format (k, k) are not allowed.");
    	}
        int server = processRequest(vertice1);
        if(server != selfId){
            logForwardedRequest(server, 5);
            boolean p = clients[server].createAresta(vertice1, vertice2, peso, direcionado, descricao);
            return p;
        }
        
        Vertice v2 = null;
        try{
            v2 = readVertice(vertice2);
        } catch(Exception e){
            throw e;
        }
        // Vertice 2 existe
        verifyResourceAresta(vertice1, vertice2);
        
        logForOperation(5);
        // Restriction: restrição de criar aresta se ambos os vértices já existirem no grafo
        if(findVertice(grafo, vertice1) == null){
            unblockAresta(vertice1, vertice2);
            throw new KeyNotFound(vertice1, "Vertice nao encontrado");
        }
        
        Aresta aresta = findAresta(grafo, vertice1, vertice2);
        
        if(aresta == null){
            grafo.arestas.add(new Aresta(vertice1, vertice2, peso, direcionado, descricao));
            unblockAresta(vertice1, vertice2);
            return true;
        }
        // Aresta já existe, tratar bidirecionalidade
        if(!aresta.direcionado){ // aresta já é bidirecional
            unblockAresta(vertice1, vertice2);
            throw new KeyAlreadyUsed(0, "Aresta ja existente");
        }
        if(aresta.vertice1 == vertice1 && aresta.vertice2 == vertice2){ // direcionada de v1 pra v2 ja exist
            unblockAresta(vertice1, vertice2);
            throw new KeyAlreadyUsed(0, "Aresta direcionada ja existente do vertice 1 para o vertice 2");            
        }
        else{ // direcionada de v2 pra v1
            unblockAresta(vertice1, vertice2);
            throw new KeyAlreadyUsed(0, "Aresta direcionada ja existente do vertice 2 para o vertice 1");     
        }
    }

    @Override
    public boolean deleteAresta(int vertice1, int vertice2) throws KeyNotFound, ResourceInUse, BadParameter, TException {
        if(vertice1 == vertice2){
    		throw new BadParameter("Arestas with format (k, k) are not allowed.");
    	}
        int server = processRequest(vertice1);
        if(server != selfId){
            logForwardedRequest(server, 6);
            boolean p = clients[server].deleteAresta(vertice1, vertice2);
            return p;
        }
        verifyResourceAresta(vertice1, vertice2);        
        
        logForOperation(6);
        Aresta aresta = findAresta(grafo, vertice1, vertice2);
        if(aresta == null){
            unblockAresta(vertice1, vertice2);
            throw new KeyNotFound(0, "Aresta nao encontrada");
        }
        grafo.arestas.remove(aresta);
        unblockAresta(vertice1, vertice2);
        return true;        
    }

    @Override
    public Aresta readAresta(int vertice1, int vertice2) throws KeyNotFound, ResourceInUse, BadParameter, TException {
    	if(vertice1 == vertice2){
    		throw new BadParameter("Arestas with format (k, k) are not allowed.");
    	}
        int server = processRequest(vertice1);
        if(server != selfId){
            logForwardedRequest(server, 8);
            Aresta p = clients[server].readAresta(vertice1, vertice2);
            return p;
        }
        verifyResourceAresta(vertice1, vertice2);
        
        logForOperation(8);
        Aresta aresta = findAresta(grafo, vertice1, vertice2);
        if(aresta == null){
            unblockAresta(vertice1, vertice2);
            throw new KeyNotFound(0, "Aresta nao encontrada");            
        }
        unblockAresta(vertice1, vertice2);
        return aresta;
    }

    @Override
    public boolean updateAresta(int vertice1, int vertice2, double peso, boolean direcionado, String descricao) throws KeyNotFound, ResourceInUse, BadParameter, TException {
    	if(vertice1 == vertice2){
    		throw new BadParameter("Arestas with format (k, k) are not allowed.");
    	}
        int server = processRequest(vertice1);
        if(server != selfId){
            logForwardedRequest(server, 7);
            boolean p = clients[server].updateAresta(vertice1, vertice2, peso, direcionado, descricao);
            return p;
        }
        verifyResourceAresta(vertice1, vertice2);
        
        logForOperation(7);
        Aresta aresta = findAresta(grafo, vertice1, vertice2);
        if(aresta == null){
            unblockAresta(vertice1, vertice2);
            throw new KeyNotFound(0, "Aresta nao encontrada");  
        }
        // Restriction: aresta não tem seus vertices alterados
        aresta.peso = peso;
        aresta.direcionado = direcionado;
        aresta.descricao = descricao;
		unblockAresta(vertice1, vertice2);
        return true;        
    }
    
    @Override
    public List<Vertice> listVerticesFromAresta(int vertice1, int vertice2) throws KeyNotFound, ResourceInUse, BadParameter, TException {
    	if(vertice1 == vertice2){
    		throw new BadParameter("Arestas with format (k, k) are not allowed.");
    	}
        verifyResourceAresta(vertice1, vertice2);
        logForOperation(9);

        Grafo fullGraph = getFullGraph();
        Aresta aresta = findAresta(fullGraph, vertice1, vertice2);
        if(aresta == null){
            unblockAresta(vertice1, vertice2);
            throw new KeyNotFound(0, "Aresta nao encontrada");
        }
        Vertice v1 = findVertice(fullGraph, vertice1);
        if(v1 == null){
            unblockAresta(vertice1, vertice2);
            throw new KeyNotFound(vertice1, "Vertice nao encontrado");            
        }
        Vertice v2 = findVertice(fullGraph, vertice2);
        if(v2 == null){
            unblockAresta(vertice1, vertice2);
            throw new KeyNotFound(vertice2, "Vertice nao encontrado");            
        }
        
        List<Vertice> vertices = new ArrayList<>();
        vertices.add(v1);
        vertices.add(v2);
        unblockAresta(vertice1, vertice2);
        return vertices; 
    }

    @Override
    public List<Aresta> listArestasFromVertice(int nome) throws KeyNotFound, ResourceInUse, TException {
        verifyResourceVertice(nome);
        logForOperation(10);
        Grafo fullGraph = getFullGraph();
        
        Vertice v = findVertice(fullGraph, nome);
        if(v == null){
            unblockVertice(nome);
            throw new KeyNotFound(nome, "Vertice nao encontrado");
        }
        List<Aresta> arestas = new ArrayList<>();
        for(Aresta aresta: fullGraph.arestas){
            if(aresta.vertice1 == nome || aresta.vertice2 == nome){
                arestas.add(aresta);
            }
        }
        unblockVertice(nome);
        return arestas;
    }

    @Override
    public List<Vertice> listNeighbors(int nome) throws KeyNotFound, ResourceInUse, TException {
        verifyResourceVertice(nome);
        logForOperation(11);
        Grafo fullGraph = getFullGraph();
        
        Vertice v = findVertice(fullGraph, nome);
        if(v == null){
            unblockVertice(nome);
            throw new KeyNotFound(nome, "Vertice nao encontrado");
        }
        
        List<Integer> neighbors = new ArrayList<>();
        for(Aresta aresta: fullGraph.arestas){
            if(aresta.vertice1 == nome){
                neighbors.add(aresta.vertice2);
            }            
            else{
                if(aresta.vertice2 == nome && !aresta.direcionado){
                    neighbors.add(aresta.vertice1);
                }
            }
        }
        List<Vertice> vertices = new ArrayList<>();
        for(Vertice vertice: fullGraph.vertices){
            if(neighbors.contains(vertice.nome)){
                vertices.add(vertice);
            }
        }
        unblockVertice(nome);
        return vertices;
    }

    @Override
    public List<Vertice> listVertices(){
        logForOperation(12);
        ArrayList<Vertice> allVertices = new ArrayList<>();
        for(int i = 0; i < N; i++){
            if(i != selfId){
                try{
                    System.out.println("Requesting vertices from " + ports[i]);
                    allVertices.addAll(clients[i].listSelfVertices());
                } catch(TException e){
                    System.out.println("Failed to request vertices from " + ports[i]);
                }
            }
            else{
            	allVertices.addAll(grafo.vertices);
            }
        }
        return allVertices;
    }
    
    @Override
    public List<Vertice> listSelfVertices(){
        logForOperation(14);
        if(grafo.isSetVertices()){
            return grafo.vertices;
        }
        return null;
    }
    
    @Override
    public List<Aresta> listArestas(){
        logForOperation(13);
        ArrayList<Aresta> allArestas = new ArrayList<>();
        for(int i = 0; i < N; i++){
            if(i != selfId){
                try{
                    System.out.println("Requesting arestas from " + ports[i]);
                    allArestas.addAll(clients[i].listSelfArestas());
                } catch(TException e){
                    System.out.println("Failed to request arestas from " + ports[i]);
                }
            }
            else{
       			allArestas.addAll(grafo.arestas);
            }
        }
        return allArestas;  
    }

    @Override
    public List<Aresta> listSelfArestas() throws TException {
        logForOperation(15);
        if(grafo.isSetArestas()){
            return grafo.arestas;
        }
        return null;
    }

    @Override
    public List<Vertice> menorCaminho(int vertice1, int vertice2) throws KeyNotFound, ResourceInUse, BadParameter, TException {
    	if(vertice1 == vertice2){
    		throw new BadParameter("Cannot calculate path from k to k.");
    	}
        logForOperation(16);
        Grafo fullGraph = getFullGraph();
        
        Vertice v = findVertice(fullGraph, vertice1);
		if(v == null){
			throw new KeyNotFound(vertice1, "Vertice nao encontrado");
		}
        Vertice destino = findVertice(fullGraph, vertice2);
		if(destino == null){
			throw new KeyNotFound(vertice2, "Vertice nao encontrado");
		}

        Dijkstra algoritmo = new Dijkstra(fullGraph);

        algoritmo.executa(v); 

        LinkedList<Vertice> caminho = algoritmo.getCaminho(destino);
        return caminho;
    }
    
    
    public static void showGrafo(List<Vertice> vertices, List<Aresta> arestas){
        System.out.println("\n      GRAFO: ");
        showVertices(vertices);
        System.out.println("");
        showArestas(arestas);
    }

    public static void showVertices(List<Vertice> vertices){
        System.out.print("      - Vertices: ");
        for(Vertice vertice: vertices){
            System.out.print(vertice.nome + ", ");
        }
    }

    public static void showArestas(List<Aresta> arestas){
        System.out.print("      - Arestas: ");
        String a;
        for(Aresta aresta: arestas){
            if(!aresta.direcionado){
                a = "[" + aresta.vertice1 + ", " + aresta.vertice2 + "], ";   
            }
            else{
                a = "(" + aresta.vertice1 + ", " + aresta.vertice2 + "), ";                     
            }
            System.out.print(a);
        }
    }
    
    public Grafo getFullGraph(){
        Grafo g = new Grafo();
        g.vertices = new ArrayList<Vertice>();
        g.arestas = new ArrayList<Aresta>();
        for(int i = 0; i < N; i++){
            if(i != selfId){                
                try{
                    g.vertices.addAll(clients[i].listSelfVertices());
                    g.arestas.addAll(clients[i].listSelfArestas());
                } catch(Exception e){}
            }
			else {	
				g.vertices.addAll(grafo.vertices);
				g.arestas.addAll(grafo.arestas);
			}
        }
        showGrafo(g.vertices, g.arestas);
        return g;
    }

    @Override
    public boolean deleteArestasFromVertice(int key) throws KeyNotFound, ResourceInUse, TException {
        verifyResourceVertice(key);
        logForOperation(17);
        System.out.print("Removing local arestas of vertice " + key);
		ArrayList<Aresta> arestasToRemove = new ArrayList<Aresta>();
        for(Aresta a: grafo.arestas){
            if(a.vertice2 == key){
                arestasToRemove.add(a);
            }
        }
		grafo.arestas.removeAll(arestasToRemove);
		unblockVertice(key);
        return true;
    }
}
