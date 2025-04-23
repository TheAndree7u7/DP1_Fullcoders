package com.plg.simulator;

import com.plg.model.*;

import java.io.*;
import java.util.*;

public class FileParser {

    public static List<Pedido> leerPedidos(String path) {
        List<Pedido> pedidos = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] partes = linea.split(":");
                String tiempo = partes[0];
                String[] datos = partes[1].split(",");

                int dia = Integer.parseInt(tiempo.substring(0, 2));
                int hora = Integer.parseInt(tiempo.substring(3, 5));
                int minuto = Integer.parseInt(tiempo.substring(6, 8));
                int x = Integer.parseInt(datos[0]);
                int y = Integer.parseInt(datos[1]);
                String cliente = datos[2];
                int volumen = Integer.parseInt(datos[3].replace("m3", ""));
                int horasLimite = Integer.parseInt(datos[4].replace("h", ""));

                pedidos.add(new Pedido(cliente, new Coordenada(x, y), volumen, dia, hora, minuto, horasLimite));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pedidos;
    }

    public static List<Bloqueo> leerBloqueos(String path) {
        List<Bloqueo> bloqueos = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] partes = linea.split(":");
                String[] tiempo = partes[0].split("-");

                int inicioMin = convertirATotalMinutos(tiempo[0]);
                int finMin = convertirATotalMinutos(tiempo[1]);

                String[] puntos = partes[1].split(",");
                List<Coordenada> nodos = new ArrayList<>();
                for (int i = 0; i < puntos.length; i += 2) {
                    int x = Integer.parseInt(puntos[i]);
                    int y = Integer.parseInt(puntos[i + 1]);
                    nodos.add(new Coordenada(x, y));
                }

                bloqueos.add(new Bloqueo(inicioMin, finMin, nodos));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bloqueos;
    }

    private static int convertirATotalMinutos(String marca) {
        int dia = Integer.parseInt(marca.substring(0, 2));
        int hora = Integer.parseInt(marca.substring(3, 5));
        int minuto = Integer.parseInt(marca.substring(6, 8));
        return ((dia - 1) * 24 * 60) + (hora * 60) + minuto;
    }
}
