package com.plg.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.plg.dto.GrupoDTO;
import com.plg.entity.Pedido;
import com.plg.repository.PedidoRepository;
import com.plg.service.ap.AffinityPropagationService;
@Service
public class ApsagService {

    @Autowired
    private AffinityPropagationService agrupamientoAPService;

    @Autowired
    private GeneticService geneticService;

    @Autowired
    private PedidoRepository pedidoRepo;

    /**
     * Ejecuta el flujo completo: AP → inicialización genética.
     *
     * @param alpha coef espacial
     * @param beta  coef temporal
     * @param damping factor de amortiguamiento
     * @param maxIter max iter AP
     * @param nInd tamaño población genética
     */
    public ApsagResult run(double alpha, double beta, double damping,
                           int maxIter, int nInd) {
        // 1) Clusterizar
        List<GrupoDTO> grupos = agrupamientoAPService.clusterizar(alpha, beta, damping, maxIter);

        // 2) Convertir DTOs a List<List<Pedido>>
        List<List<Pedido>> apClusters = grupos.stream()
            .map(g -> g.getPedidos().stream()
                       .map(dto -> pedidoRepo.findById(dto.getId()).orElseThrow())
                       .collect(Collectors.toList()))
            .collect(Collectors.toList());

        // 3) Inicializar población genética
        List<List<Pedido>> poblacion = geneticService.inicializarPoblacion(apClusters, nInd);

        // Devolver un resultado “ligero” con clusters + tamaño de población
        return new ApsagResult(grupos, poblacion.size());
    }

    /** Simple DTO de resultado para el controlador */
    public static class ApsagResult {
        private final List<GrupoDTO> clusters;
        private final int poblacionSize;
        public ApsagResult(List<GrupoDTO> clusters, int poblacionSize) {
            this.clusters = clusters;
            this.poblacionSize = poblacionSize;
        }
        public List<GrupoDTO> getClusters() { return clusters; }
        public int getPoblacionSize() { return poblacionSize; }
    }
}
