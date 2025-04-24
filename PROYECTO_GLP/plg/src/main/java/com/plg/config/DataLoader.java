package com.plg.config;

import com.plg.entity.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class DataLoader {

    private String pathAverias = "data/averias/averias.v1.txt";

    public List<Camion> initializeCamiones() {
        List<Camion> camiones = new ArrayList<>();

        // Create different truck types
        String[] tipos = { "TA", "TB", "TC", "TD" };
        double[] capacidades = { 25.0, 20.0, 15.0, 10.0 };
        double[] taras = { 15.0, 12.0, 9.0, 7.0 };

        for (int i = 0; i < tipos.length; i++) {
            for (int j = 1; j <= 3; j++) {
                Camion camion = new Camion();
                camion.setCodigo(tipos[i] + "0" + j);
                camion.setTipo(tipos[i]);
                camion.setCapacidad(capacidades[i]);
                camion.setTara(taras[i]);
                camion.setPesoCarga(0);
                camion.setPesoCombinado(taras[i]);
                camion.setEstado(null); // Set to null for now
                camiones.add(camion);
            }
        }
        return camiones;
    }

    public List<Averia> initializeAverias(List<Camion> camiones) {
        List<Averia> averias = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream(pathAverias)))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] partes = line.split("_");
                String turno = partes[0];
                String codigoCamion = partes[1];
                String tipoIncidente = partes[2];
                TipoTurno tipoTurno = new TipoTurno(turno);
                TipoIncidente tipoIncidenteObj = new TipoIncidente(tipoIncidente);
                
                Camion camion = camiones.stream()
                        .filter(c -> c.getCodigo().equals(codigoCamion))
                        .findFirst()
                        .orElse(null);

                Averia averia = Averia.builder()
                        .camion(camion)
                        .turno(tipoTurno)
                        .tipoIncidente(tipoIncidenteObj)
                        .build();           
                averias.add(averia);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return averias;
    }
}