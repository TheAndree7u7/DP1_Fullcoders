import type { Camion } from "./Camion";
import type { Coordenada } from "./Coordenada";
import type { Nodo } from "./Nodo";
import type { Pedido } from "./Pedido";

export interface Gen{
    camion: Camion;
    nodos: Nodo[];
    destino: Coordenada;
    pedidos: Pedido[];
}