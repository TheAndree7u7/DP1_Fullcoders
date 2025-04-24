package com.plg.config;

import com.plg.entity.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class DataLoader {


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
}