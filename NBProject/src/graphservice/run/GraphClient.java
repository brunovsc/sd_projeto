/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphservice.run;

import graphservice.model.Aresta;
import graphservice.handler.Graph;
import graphservice.model.Vertice;
import static java.lang.System.exit;
import java.util.List;
import java.util.Scanner;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TBinaryProtocol;

/**
 *
 * @author sd-server
 */
public class GraphClient {
    
    private static TTransport transport;
    private static TProtocol protocol;
    private static Graph.Client client;
    
    
	public static void main(String [] args) {
		try {
			transport = new TSocket("localhost", 9090);
			transport.open();

			protocol = new TBinaryProtocol(transport);
			client = new Graph.Client(protocol);
                        
                        

		} catch (TException x){
                        
			x.printStackTrace();
		}
                if(client != null){
                    int op = -1;
                    while(op != 0){
                        op = showMenu();
                        try {
                            handleOperation(op);
                        } catch (TException ex) {
                            System.out.println(ex);
                        }
                    }
                    try {
                        showGrafo();
                    } catch (TException ex) {
                        System.out.println(ex);
                    }
                    System.out.println("\nEncerrando cliente ... ");
                    try {
                        transport.close();
                    } catch (Exception e){
                    }
                    exit(0);
                }
                else{
                    System.out.println("\nNao foi possivel estabelecer a conexao");
                    exit(0);
                }
	}
        
        public static void showGrafo() throws TException{
            System.out.println("\n\n      GRAFO: ");
            showVertices();
            System.out.println("");
            showArestas();
        }
        
        public static void showVertices() throws TException{
            List<Vertice> vertices = client.listVertices();
            System.out.print("      - Vertices: ");
            for(Vertice vertice: vertices){
                System.out.print(vertice.nome + ", ");
            }
        }
        
        public static void showArestas() throws TException{
            List<Aresta> arestas = client.listArestas();
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
        
        public static int showMenu(){
            System.out.println("\n\n");
            System.out.println("================================");
            System.out.println("|         1. Vertice           |");
            System.out.println("|         2. Aresta            |");
            System.out.println("|         3. Consulta          |");
            System.out.println("|         4. Listar            |");
            System.out.println("|         0. Sair              |");
            System.out.println("================================");            
            System.out.print("Operacao -> ");
            Scanner in = new Scanner(System.in);
            int op1 = in.nextInt();
            int op2 = 0;
            if(op1 != 0){
                op2 = showSubMenu(op1);
            }
            return op1 * 10 + op2;
        }
        
        public static int showSubMenu(int op1){
            System.out.println("");
            System.out.println("================================");
            switch(op1){
                case 1:
                case 2:
                    System.out.println("|         1. Criar             |");
                    System.out.println("|         2. Remover           |");
                    System.out.println("|         3. Atualizar         |");
                    System.out.println("|         4. Consultar         |");
                    break;
                case 3:
                    System.out.println("|   1. Listar vertices aresta  |");
                    System.out.println("|   2. Listar arestas vertice  |");
                    System.out.println("|   3. Listar vizinhos         |");
                    break;
                case 4:
                    System.out.println("|      1. Listar vertices      |");
                    System.out.println("|      2. Listar arestas       |");
                    System.out.println("|      3. Exibir grafo         |");
                    break;
                default:
                    System.out.println("");
            }
            System.out.println("================================");            
            System.out.print("Operacao -> ");
            Scanner in = new Scanner(System.in);
            int op2 = in.nextInt();
            return op2;
        }
        
        public static void handleOperation(int op) throws TException{
            int nome, cor, vertice1, vertice2;
            double peso;
            boolean direcionado;
            String descricao;
            Scanner in = new Scanner(System.in);
            boolean result;
            Vertice vertice;
            Aresta aresta;
            
            switch (op){
                case 0:
                    break;
                // Vertice
                case 11:
                    System.out.print("Nome: ");
                    nome = in.nextInt();
                    System.out.print("Cor: ");
                    cor = in.nextInt();
                    System.out.print("Peso: ");
                    peso = in.nextDouble();
                    System.out.print("Descricao: ");
                    in.nextLine();
                    descricao = in.nextLine();
                    
                    if(client.createVertice(nome, cor, peso, descricao)){
                        System.out.println("Vertice criado com sucesso");
                    }
                    break;
                case 12:
                    System.out.print("Nome: ");
                    nome = in.nextInt();
                    
                    if(client.deleteVertice(nome)){
                        System.out.println("Vertice removido com sucesso");
                    }
                    break;
                case 13:
                    System.out.print("Nome: ");
                    nome = in.nextInt();
                    System.out.print("Cor: ");
                    cor = in.nextInt();
                    System.out.print("Peso: ");
                    peso = in.nextDouble();
                    System.out.print("Descricao: ");
                    in.nextLine();
                    descricao = in.nextLine();
                    
                    if(client.updateVertice(nome, cor, peso, descricao)){
                        System.out.println("Vertice atualizado com sucesso");                        
                    }
                    break;
                case 14:
                    System.out.print("Nome: ");
                    nome = in.nextInt();
                    
                    Vertice v = client.readVertice(nome);
                    if(v != null){
                        printVertice(v);
                    }
                    break;
                // Aresta
                case 21:
                    System.out.print("Vertice 1: ");
                    vertice1 = in.nextInt();
                    System.out.print("Vertice 2: ");
                    vertice2 = in.nextInt();
                    System.out.print("Peso: ");
                    peso = in.nextDouble();
                    System.out.println("Direcionado: ");
                    in.nextLine();
                    direcionado = in.nextBoolean();
                    System.out.print("Descricao: ");
                    in.nextLine();
                    descricao = in.nextLine();
                    
                    if(client.createAresta(vertice1, vertice2, peso, direcionado, descricao)){
                        System.out.println("Aresta criada com sucesso");                         
                    }
                    break;
                case 22:
                    System.out.print("Vertice 1: ");
                    vertice1 = in.nextInt();
                    System.out.print("Vertice 2: ");
                    vertice2 = in.nextInt();
                    
                    if(client.deleteAresta(vertice1, vertice2)){
                        System.out.println("Aresta removida com sucesso"); 
                    }
                    break;
                case 23:
                    System.out.print("Vertice 1: ");
                    vertice1 = in.nextInt();
                    System.out.print("Vertice 2: ");
                    vertice2 = in.nextInt();
                    System.out.print("Peso: ");
                    peso = in.nextDouble();
                    System.out.println("Direcionado: ");
                    direcionado = in.nextBoolean();
                    System.out.print("Descricao: ");
                    in.nextLine();
                    descricao = in.nextLine();
                    
                    if(client.updateAresta(vertice1, vertice2, peso, direcionado, descricao)){
                        System.out.println("Aresta atualizada com sucesso"); 
                    }
                    break;
                case 24:
                    System.out.print("Vertice 1: ");
                    vertice1 = in.nextInt();
                    System.out.print("Vertice 2: ");
                    vertice2 = in.nextInt();
                    
                    Aresta a = client.readAresta(vertice1, vertice2);
                    if(a != null){
                        printAresta(a);
                    }
                    break;
                // Consulta
                case 31:
                    System.out.print("Vertice 1: ");
                    vertice1 = in.nextInt();
                    System.out.print("Vertice 2: ");
                    vertice2 = in.nextInt();
                    
                    List<Vertice> vertices = client.listVerticesFromAresta(vertice1, vertice2);
                    if(vertices != null){
                        for(Vertice vert:vertices){
                            printVertice(vert);
                        }
                    }
                    break;
                case 32:
                    System.out.print("Nome: ");
                    nome = in.nextInt();
                    
                    List<Aresta> arestas = client.listArestasFromVertice(nome);
                    if(arestas != null){
                        for(Aresta ares:arestas){
                            printAresta(ares);
                        }
                    }
                    break;
                case 33:
                    System.out.print("Nome: ");
                    nome = in.nextInt();
                                        
                    List<Vertice> vizinhos = client.listNeighbors(nome);
                    if(vizinhos != null){
                        for(Vertice vert:vizinhos){
                            printVertice(vert);
                        }
                    }
                    break;
                case 41:
                    showVertices();
                    break;
                case 42:
                    showArestas();
                    break;
                case 43:
                    showGrafo();
                    break;
                default:
                    System.out.println("\nOperacao desconhecida");
            }
        }
        
        public static void printVertice(Vertice v){
            System.out.println("");
            System.out.println("Nome: " + v.nome);
            System.out.println("Cor: " + v.cor);
            System.out.println("Peso: " + v.peso);
            System.out.println("Descricao: " + v.descricao);
        }        
        
        public static void printAresta(Aresta a){
            System.out.println("");
            System.out.println("Vertice1: " + a.vertice1);
            System.out.println("Vertice2: " + a.vertice2);
            System.out.println("Peso: " + a.peso);
            System.out.println("Direcionado: " + a.direcionado);
            System.out.println("Descricao: " + a.descricao);
        }
}