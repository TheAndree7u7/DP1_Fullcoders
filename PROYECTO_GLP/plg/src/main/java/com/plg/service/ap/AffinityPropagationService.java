package com.plg.service.ap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.plg.dto.GrupoDTO;
import com.plg.dto.PedidoDTO;
import com.plg.entity.Almacen;
import com.plg.entity.Pedido;
import com.plg.repository.AlmacenRepository;
import com.plg.repository.PedidoRepository;
import com.plg.util.Constants;

@Service
public class AffinityPropagationService {

    private static final Logger logger = LoggerFactory.getLogger(AffinityPropagationService.class);

    @Autowired private AlmacenRepository almacenRepo;
    @Autowired private PedidoRepository pedidoRepo;

    /**
     * 2.1 Definir nodos = almacenes ∪ clientes de pedidos
     */
    private static class Nodo {
        double x, y;
        double startWindow, endWindow;
        Pedido pedido;      // null si es un almacén
        Almacen almacen;    // null si es un cliente
        Nodo(double x, double y, double s, double e, Pedido p, Almacen a) {
            this.x = x; this.y = y;
            this.startWindow = s; this.endWindow = e;
            this.pedido = p; this.almacen = a;
        }
    }

    public List<GrupoDTO> clusterizar(double alpha,
                                      double beta,
                                      double damping,
                                      int maxIter) {
        // Add ERROR level log to ensure it gets captured
        logger.error("TEST ERROR LOG - Iniciando la clusterización");
        logger.warn("TEST WARN LOG - Parámetros: alpha={}, beta={}, damping={}, maxIter={}", alpha, beta, damping, maxIter);
        logger.info("TEST INFO LOG - Iniciando la clusterización con alpha={}, beta={}, damping={}, maxIter={}", alpha, beta, damping, maxIter);
        logger.debug("TEST DEBUG LOG - Verificando niveles de log");
        
        // 2.1: carga datos
        logger.info("Cargando los almacenes y pedidos desde las bases de datos...");
        List<Almacen> almacenes = almacenRepo.findAll();
        List<Pedido> pedidos   = pedidoRepo.findAll();

        List<Nodo> nodos = new ArrayList<>();
        // almacenes: sin ventana de servicio (0–∞)
        for (Almacen a : almacenes) {
            nodos.add(new Nodo(a.getPosX(), a.getPosY(), 0, Double.MAX_VALUE, null, a));
        }
        // clientes: ventana = [registro, entregaRequerida]
        for (Pedido p : pedidos) {
            long start = p.getFechaRegistro()
                          .atZone(Constants.ZONE_LIMA)
                          .toEpochSecond();
            long end   = p.getFechaEntregaRequerida()
                          .atZone(Constants.ZONE_LIMA)
                          .toEpochSecond();

            nodos.add(new Nodo(
                p.getPosX(), 
                p.getPosY(), 
                start, 
                end, 
                p, 
                null
            ));
        }

        logger.info("Total de nodos (almacenes + pedidos): {}", nodos.size());

        int N = nodos.size();
        // 2.2: calcular distEsp, distTemp y matriz S
        double[][] distEsp  = new double[N][N];
        double[][] distTemp = new double[N][N];
        double[][] S        = new double[N][N];

        logger.info("Calculando las distancias espaciales y temporales entre los nodos...");
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                distEsp[i][j]  = Math.abs(nodos.get(i).x - nodos.get(j).x)
                               + Math.abs(nodos.get(i).y - nodos.get(j).y);
                double dtStart = Math.abs(nodos.get(i).startWindow - nodos.get(j).startWindow);
                double dtEnd   = Math.abs(nodos.get(i).endWindow   - nodos.get(j).endWindow);
                distTemp[i][j] = Math.max(dtStart, dtEnd);
                S[i][j] = -alpha * distEsp[i][j] - beta * distTemp[i][j];
            }
        }
        //Calcular la mediana de S_off-diagonal
        List<Double> simList = new ArrayList<>(N * (N - 1));
        for (int i = 0; i < N; i++) {
          for (int j = 0; j < N; j++) {
            if (i != j) simList.add(S[i][j]);
          }
        }
        Collections.sort(simList);
        double median = simList.get(simList.size() / 2);
        for (int i = 0; i < N; i++) {
          S[i][i] = median;    // ahora la diagonal = mediana de S_off-diagonal
        }
        logger.info("Preference (mediana) = {}", median);
        //Calcular la mediana de S_off-diagonal
        // 2.3: inicializar R y A a cero
        double[][] R = new double[N][N];
        double[][] A = new double[N][N];

        // 2.4–2.5: iterar hasta maxIter
        List<Integer> prevCenters = null;
        logger.info("Comenzando las iteraciones del algoritmo...");
        for (int m = 1; m <= maxIter; m++) {
            logger.info("Iteración {}...", m);

            double[][] Rnew = new double[N][N];
            double[][] Anew = new double[N][N];

            // 2.5.1 Responsabilidad
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    double max = Double.NEGATIVE_INFINITY;
                    for (int k = 0; k < N; k++) {
                        if (k == j) continue;
                        max = Math.max(max, A[i][k] + S[i][k]);
                    }
                    Rnew[i][j] = S[i][j] - max;
                }
            }

            // 2.5.2 Disponibilidad
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    if (i != j) {
                        double sum = 0;
                        for (int k = 0; k < N; k++) {
                            if (k == i || k == j) continue;
                            sum += Math.max(0, Rnew[k][j]);
                        }
                        Anew[i][j] = Math.min(0, Rnew[j][j] + sum);
                    }
                }
                // diag
                double sumDiag = 0;
                for (int k = 0; k < N; k++) {
                    if (k == i) continue;
                    sumDiag += Math.max(0, Rnew[k][i]);
                }
                Anew[i][i] = sumDiag;
            }

            // 2.5.3 Amortiguamiento
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    R[i][j] = damping * R[i][j] + (1 - damping) * Rnew[i][j];
                    A[i][j] = damping * A[i][j] + (1 - damping) * Anew[i][j];
                }
            }

            // 2.5.4 Convergencia de centros
            List<Integer> centers = new ArrayList<>();
            for (int j = 0; j < N; j++) {
                if (A[j][j] + R[j][j] > 0) centers.add(j);
            }
            if (prevCenters != null && centers.equals(prevCenters)) {
                logger.info("Convergencia alcanzada en la iteración {}", m);
                break;
            }
            prevCenters = centers;
        }

        // 2.6 Asignar cada cliente a su cluster
        Map<Integer, List<Pedido>> clusters = new HashMap<>();
        logger.info("Asignando los pedidos a sus clusters...");
        for (int i = 0; i < N; i++) {
            double best = Double.NEGATIVE_INFINITY;
            int exemplar = -1;
            for (int j = 0; j < N; j++) {
                double val = A[i][j] + R[i][j];
                if (val > best) { best = val; exemplar = j; }
            }
            Nodo ni = nodos.get(i);
            if (ni.pedido != null) {
                clusters.computeIfAbsent(exemplar, k -> new ArrayList<>())
                        .add(ni.pedido);
            }
        }

        // 2.7 Construir DTO de salida
        logger.info("Construyendo el DTO de salida...");
        List<GrupoDTO> resultado = new ArrayList<>();
        int gid = 1;
        for (var entry : clusters.entrySet()) {
            int centerIdx = entry.getKey();
            Nodo centro = nodos.get(centerIdx);
            List<PedidoDTO> pedidosDTO = entry.getValue().stream()
                .map(p -> PedidoDTO.builder()
                                  .id(p.getId())
                                  .codigo(p.getCodigo())
                                  .posX(p.getPosX())
                                  .posY(p.getPosY())
                                  .build())
                .collect(Collectors.toList());

            GrupoDTO grupo = GrupoDTO.builder()
                .idGrupo("G" + (gid++))
                .ejemplar(PedidoDTO.builder()
                    .posX(centro.x).posY(centro.y).build())
                .centroideX(centro.x)
                .centroideY(centro.y)
                .pedidos(pedidosDTO)
                .numeroPedidos(pedidosDTO.size())
                .build();

            resultado.add(grupo);
        }

        logger.error("Clusterización completada. Se han generado {} grupos.", resultado.size());
        return resultado;
    }
}
