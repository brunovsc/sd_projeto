namespace java graphservice

struct Aresta {
	1: i32 vertice1
	2: i32 vertice2
	3: double peso
	4: bool direcionado
	5: string descricao
}

struct Vertice {
	1: i32 nome
	2: i32 cor
	3: double peso
	4: string descricao
}

struct Grafo {
	1: list<Aresta> arestas
	2: list<Vertice> vertices
}

exception KeyNotFound {
        1: i32 key
        2: string errorMessage
}

exception KeyAlreadyUsed {
        1: i32 key
        2: string errorMessage
}

exception ResourceInUse {
        1: i32 key
}

enum ServiceEnum {

    CREATE_VERTICE,
    DELETE_VERTICE,
    UPDATE_VERTICE,
    READ_VERTICE,

    CREATE_ARESTA,
    DELETE_ARESTA,
    UPDATE_ARESTA,
    READ_ARESTA,

    LIST_VERTICES_ARESTA,
    LIST_ARESTAS_VERTICE,
    LIST_NEIGHBORS,

    LIST_VERTICES,
    LIST_ARESTAS
}

service Graph {

	bool createVertice(1:i32 nome, 2:i32 cor, 3:double peso, 4:string descricao) throws (1:KeyAlreadyUsed kau, 2:ResourceInUse riu),
	bool deleteVertice(1:i32 key) throws (1:KeyNotFound knf, 2:ResourceInUse riu),
	bool updateVertice(1:i32 nome, 2:i32 cor, 3:double peso, 4:string descricao) throws (1:KeyNotFound knf, 2:ResourceInUse riu),
	Vertice readVertice(1:i32 key) throws (1:KeyNotFound knf, 2:ResourceInUse riu),

	bool createAresta(1:i32 vertice1, 2:i32 vertice2, 3:double peso, 4:bool direcionado, 5:string descricao) throws (1:KeyAlreadyUsed kau, 2:ResourceInUse riu),
	bool deleteAresta(1:i32 vertice1, 2:i32 vertice2) throws (1:KeyNotFound knf, 2:ResourceInUse riu),
	bool updateAresta(1:i32 vertice1, 2:i32 vertice2, 3:double peso, 4:bool direcionado, 5:string descricao) throws (1:KeyNotFound knf, 2:ResourceInUse riu),
	Aresta readAresta(1:i32 vertice1, 2:i32 vertice2) throws (1:KeyNotFound knf, 2:ResourceInUse riu),

	list<Vertice> listVerticesFromAresta(1:i32 vertice1, 2:i32 vertice2) throws (1:KeyNotFound knf, 2:ResourceInUse riu),
	list<Aresta> listArestasFromVertice(1:i32 nome) throws (1:KeyNotFound knf, 2:ResourceInUse riu),
	list<Vertice> listNeighbors(1:i32 nome) throws (1:KeyNotFound knf, 2:ResourceInUse riu),

        list<Vertice> listVertices(),
        list<Aresta> listArestas()
}


