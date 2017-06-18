/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphservice.handler;

import graphservice.exception.KeyNotFound;
import graphservice.exception.ResourceInUse;
import graphservice.exception.KeyAlreadyUsed;
import graphservice.model.*;
import java.util.ArrayList;
import org.apache.thrift.TException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.lang.Thread.sleep;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

/**
 *
 * @author sd-server
 */
public class ServerHandler implements Graph.Iface{
    
    private final Grafo grafo = new Grafo();
    private static final long sleepTime = 1000;
    private static final int maxRetries = 10;
    private static final long waitToProcess = 5000; // test of concurrency
    private static final boolean testConcurrency = false; // test of concurrency
    
    // Multiple servers
    private static TTransport []transports;
    private static TProtocol []protocols;
    private static Graph.Client []clients;
    private static int ports[]; // array of ports for others servers
    private int selfPort; // number of the port of this server
    private static int N; // number of servers
    private int selfId;
    
    private List<Integer> blockedVertices = new ArrayList<>();

    public ServerHandler(String []args){
        
        grafo.vertices = new ArrayList<>();
        grafo.arestas = new ArrayList<>();
        
        N = Integer.parseInt(args[0]);
        selfPort = Integer.parseInt(args[1]);
        ports = new int[N];
        for(int i = 0; i < N; i++){
            ports[i] = Integer.parseInt(args[i+2]);
            if(ports[i] == selfPort){
                selfId = i;
            }
        } 
    }    
    
    public void connectServers(){
        
        transports = new TTransport[N];
        protocols = new TProtocol[N];
        clients = new Graph.Client[N];
        for(int i = 0; i < N; i++){
            if(ports[i] != selfPort){
                try{
                    transports[i] =  new TSocket("localhost", ports[i]);
                    transports[i].open();
                    protocols[i] = new TBinaryProtocol(transports[i]);
                    clients[i] = new Graph.Client(protocols[i]);
                    System.out.println("Server " + selfPort + " connected to server " + ports[i]);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
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
        System.out.print("----- OPERATION on port " + selfPort + " - ");
        switch(operation){
            case 0:
                break;
            case 1:
                System.out.println("Create Vertice");
                break;
            case 2:
                System.out.println("Delete Vertice");
                break;
            case 3:
                System.out.println("Update Vertice");
                break;
            case 4:
                System.out.println("Read Vertice");
                break;
            case 5:
                System.out.println("Create Aresta");
                break;
            case 6:
                System.out.println("Delete Aresta");
                break;
            case 7:
                System.out.println("Update Aresta");
                break;
            case 8:
                System.out.println("Read Aresta");
                break;
            case 9:
                System.out.println("List Vertices Aresta");
                break;
            case 10:
                System.out.println("List Arestas Vertice");
                break;
            case 11:
                System.out.println("List Neighbors");
                break;
            case 12:
                System.out.println("List Vertices");
                break;
            case 13:
                System.out.println("List Arestas");
                break;
            default:
                break;
        }
    }    
    
    public void logForwardedRequest(int server){
        System.out.println("Request forwarded from server " + selfPort + " to server " + ports[server]);
    }
    
    public Vertice findVertice(int vertice){
        for(Vertice v: grafo.vertices){
            if(v.nome == vertice){
                return v;
            }
        }
        return null;
    }
    
    public Aresta findAresta(int vertice1, int vertice2){
        for(Aresta a: grafo.arestas){
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
            logForwardedRequest(server);
            boolean p = clients[server].createVertice(nome, cor, peso, descricao);
            return p;
        }
        verifyResourceVertice(nome);
        
        logForOperation(1);
        
        if(findVertice(nome) != null){ // Restriction: restrição de criação apenas se vértice já não existe
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
            logForwardedRequest(server);
            boolean p = clients[server].deleteVertice(key);
            return p;
        }
        verifyResourceVertice(key);
        
        logForOperation(2);
        
        Vertice vertice = findVertice(key);
        if(vertice == null){
            unblockVertice(key);
            throw new KeyNotFound(key, "Vertice nao encontrado");
        }
        
        for(Aresta a: grafo.arestas){ // Restriction: remoção de vértice implica na remoção de suas arestas
            if(a.vertice1 == key || a.vertice2 == key){
                grafo.arestas.remove(a);
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
            logForwardedRequest(server);
            Vertice p = clients[server].readVertice(key);
            return p;
        }
        verifyResourceVertice(key);
        
        logForOperation(3);
        Vertice vertice = findVertice(key);
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
            logForwardedRequest(server);
            boolean p = clients[server].updateVertice(nome, cor, peso, descricao);
            return p;
        }
        verifyResourceVertice(nome); 
        
        logForOperation(4);
        Vertice vertice = findVertice(nome);
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
    public boolean createAresta(int vertice1, int vertice2, double peso, boolean direcionado, String descricao) throws KeyAlreadyUsed, ResourceInUse, TException {
        int server = processRequest(vertice1);
        if(server != selfId){
            logForwardedRequest(server);
            boolean p = clients[server].createAresta(vertice1, vertice2, peso, direcionado, descricao);
            return p;
        }
        verifyResourceAresta(vertice1, vertice2);
        
        logForOperation(5);
        // Restriction: restrição de criar aresta se ambos os vértices já existirem no grafo
        if(findVertice(vertice1) == null){
            unblockAresta(vertice1, vertice2);
            throw new KeyNotFound(vertice1, "Vertice nao encontrado");
        }
        if(findVertice(vertice2) == null){ 
            unblockAresta(vertice1, vertice2);
            throw new KeyNotFound(vertice2, "Vertice nao encontrado");
        }
        
        Aresta aresta = findAresta(vertice1, vertice2);
        
        if(aresta == null){
            grafo.arestas.add(new Aresta(vertice1, vertice2, peso, direcionado, descricao));
            unblockAresta(vertice1, vertice2);
            return true;
        }
        // Aresta já existe, tratar bidirecionalidade
        if(!aresta.direcionado){ // aresta já é bidirecional
        System.out.println("PRINT 2");
            unblockAresta(vertice1, vertice2);
            throw new KeyAlreadyUsed(0, "Aresta ja existente");
        }
        if(aresta.vertice1 == vertice1 && aresta.vertice2 == vertice2){ // direcionada de v1 pra v2 ja existe
        System.out.println("PRINT 3");
            unblockAresta(vertice1, vertice2);
            throw new KeyAlreadyUsed(0, "Aresta direcionada ja existente do vertice 1 para o vertice 2");            
        }
        else{ // direcionada de v2 pra v1
        System.out.println("PRINT 4");
            unblockAresta(vertice1, vertice2);
            throw new KeyAlreadyUsed(0, "Aresta direcionada ja existente do vertice 2 para o vertice 1");     
        }
    }

    @Override
    public boolean deleteAresta(int vertice1, int vertice2) throws KeyNotFound, ResourceInUse, TException {
        int server = processRequest(vertice1);
        if(server != selfId){
            logForwardedRequest(server);
            boolean p = clients[server].deleteAresta(vertice1, vertice2);
            return p;
        }
        verifyResourceAresta(vertice1, vertice2);        
        
        logForOperation(6);
        Aresta aresta = findAresta(vertice1, vertice2);
        if(aresta == null){
            unblockAresta(vertice1, vertice2);
            throw new KeyNotFound(0, "Aresta nao encontrada");
        }
        grafo.arestas.remove(aresta);
        unblockAresta(vertice1, vertice2);
        return true;        
    }

    @Override
    public Aresta readAresta(int vertice1, int vertice2) throws KeyNotFound, ResourceInUse, TException {
        int server = processRequest(vertice1);
        if(server != selfId){
            logForwardedRequest(server);
            Aresta p = clients[server].readAresta(vertice1, vertice2);
            return p;
        }
        verifyResourceAresta(vertice1, vertice2);
        
        logForOperation(7);
        Aresta aresta = findAresta(vertice1, vertice2);
        if(aresta == null){
            unblockAresta(vertice1, vertice2);
            throw new KeyNotFound(0, "Aresta nao encontrada");            
        }
        unblockAresta(vertice1, vertice2);
        return aresta;
    }

    @Override
    public boolean updateAresta(int vertice1, int vertice2, double peso, boolean direcionado, String descricao) throws KeyNotFound, ResourceInUse, TException {
        int server = processRequest(vertice1);
        if(server != selfId){
            logForwardedRequest(server);
            boolean p = clients[server].updateAresta(vertice1, vertice2, peso, direcionado, descricao);
            return p;
        }
        verifyResourceAresta(vertice1, vertice2);
        
        logForOperation(8);
        Aresta aresta = findAresta(vertice1, vertice2);
        if(aresta == null){
            unblockAresta(vertice1, vertice2);
            throw new KeyNotFound(0, "Aresta nao encontrada");  
        }
        // Restriction: aresta não tem seus vertices alterados
        aresta.peso = peso;
        aresta.direcionado = direcionado;
        aresta.descricao = descricao;
        return true;        
    }

    @Override
    public List<Vertice> listVerticesFromAresta(int vertice1, int vertice2) throws KeyNotFound, ResourceInUse, TException {
        verifyResourceAresta(vertice1, vertice2);
        
        logForOperation(9);
        Aresta aresta = findAresta(vertice1, vertice2);
        if(aresta == null){
            unblockAresta(vertice1, vertice2);
            throw new KeyNotFound(0, "Aresta nao encontrada");
        }
        Vertice v1 = findVertice(vertice1);
        if(v1 == null){
            unblockAresta(vertice1, vertice2);
            throw new KeyNotFound(vertice1, "Vertice nao encontrado");            
        }
        Vertice v2 = findVertice(vertice2);
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
        Vertice v = findVertice(nome);
        if(v == null){
            unblockVertice(nome);
            throw new KeyNotFound(nome, "Vertice nao encontrado");
        }
        List<Aresta> arestas = new ArrayList<>();
        for(Aresta aresta: grafo.arestas){
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
        Vertice v = findVertice(nome);
        if(v == null){
            unblockVertice(nome);
            throw new KeyNotFound(nome, "Vertice nao encontrado");
        }
        
        List<Integer> neighbors = new ArrayList<>();
        for(Aresta aresta: grafo.arestas){
            if(aresta.vertice1 == nome){
                neighbors.add(aresta.vertice2);
            }            
            else{
                if(aresta.vertice2 == nome){
                    neighbors.add(aresta.vertice1);
                }
            }
        }
        List<Vertice> vertices = new ArrayList<>();
        for(Vertice vertice: grafo.vertices){
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
        if(grafo.isSetVertices()){
            return grafo.vertices;
        }
        return null;
    }
    
    @Override
    public List<Aresta> listArestas(){
        logForOperation(13);
        if(grafo.isSetArestas()){
            return grafo.arestas;
        }
        return null;
    }
}
