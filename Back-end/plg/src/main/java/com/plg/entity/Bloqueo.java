package com.plg.entity;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.plg.utils.ExcepcionesPerzonalizadas.InvalidDataFormatException;
import com.plg.utils.Herramientas;

@Data
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class Bloqueo {

    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private List<Nodo> nodosBloqueados;
    private Boolean activo;
    

    public Bloqueo(String line) throws InvalidDataFormatException {
        nodosBloqueados = new ArrayList<>();
        String[] partes = line.split(":");
        if (partes.length != 2) {
            throw new InvalidDataFormatException("Formato de línea de bloqueo incorrecto. Se esperaban 2 partes separadas por ':'. Línea: " + line);
       
        }
        String[] partesFecha = partes[0].split("-");
        if (partesFecha.length != 2) {
            throw new InvalidDataFormatException("Formato de rango de fechas de bloqueo incorrecto. Se esperaban 2 fechas separadas por '-'. Valor: " + partes[0] + ". Línea: " + line);
        }

        LocalDateTime fecha1 = Herramientas.readFecha(partesFecha[0]);
        LocalDateTime fecha2 = Herramientas.readFecha(partesFecha[1]);

        if (fecha1.isAfter(fecha2)) {
            throw new InvalidDataFormatException("La fecha de inicio del bloqueo no puede ser posterior a la fecha de fin. Inicio: " + fecha1 + ", Fin: " + fecha2 + ". Línea: " + line);
        }

        List<Coordenada> coordenadas = new ArrayList<>();
        String[] coordenadasBloqueo = partes[1].split(",");
        if (coordenadasBloqueo.length > 0 && coordenadasBloqueo.length % 2 == 0) {
            for (int i = 0; i < coordenadasBloqueo.length; i += 2) {
                int x = Integer.parseInt(coordenadasBloqueo[i]);
                int y = Integer.parseInt(coordenadasBloqueo[i + 1]);
                Coordenada coordenada = new Coordenada(y, x);
                coordenadas.add(coordenada);
            }
        } else {
            throw new InvalidDataFormatException("Error en el formato de coordenadas de bloqueo: " + partes[1] + ". Línea: " + line);
        }

        for (int i = 0; i < coordenadas.size() - 1; i++) {
            Coordenada start = coordenadas.get(i);
            Coordenada end = coordenadas.get(i + 1);
            if (start.getColumna() == end.getColumna()) {
                int startRow = Math.min(start.getFila(), end.getFila());
                int endRow = Math.max(start.getFila(), end.getFila());
                for (int j = startRow; j <= endRow; j++) {
                    Nodo nodo = Mapa.getInstance().getNodo(j, start.getColumna());
                    nodosBloqueados.add(nodo);
                }
            } else if (start.getFila() == end.getFila()) {
                int startCol = Math.min(start.getColumna(), end.getColumna());
                int endCol = Math.max(start.getColumna(), end.getColumna());
                for (int j = startCol; j <= endCol; j++) {
                    Nodo nodo = Mapa.getInstance().getNodo(start.getFila(), j);
                    nodosBloqueados.add(nodo);
                }
            } else {
                System.out.println("Error en el formato de coordenadas de bloqueo: " + partes[1]);
                return;
            }
        }

        this.fechaInicio = fecha1;
        this.fechaFin = fecha2;
        this.activo = false;
    }


    public void activarBloqueo() {
        this.activo = true;
        for (Nodo nodo : nodosBloqueados) {
            nodo.setBloqueado(true);
        }
    }

    public void desactivarBloqueo() {
        this.activo = false;
        for (Nodo nodo : nodosBloqueados) {
            nodo.setBloqueado(false);
        }
    }

}
