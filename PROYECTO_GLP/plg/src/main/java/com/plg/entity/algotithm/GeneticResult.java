package com.plg.entity.algotithm;

import java.util.List;

import com.plg.entity.Pedido;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Clase que representa el resultado de un algoritmo genético.
 * Contiene el mejor cromosoma y su fitness asociado.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GeneticResult {
    private List<Pedido> bestChromosome;
    private double bestFitness1;   // e.g. consumo
    private double bestFitness2;   // e.g. % entregas en plazo
 
}