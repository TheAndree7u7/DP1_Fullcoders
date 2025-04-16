package com.plg.repository;

import com.plg.entity.Almacen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlmacenRepository extends JpaRepository<Almacen, Long> {
    List<Almacen> findByActivo(boolean activo);
    List<Almacen> findByEsCentralAndActivo(boolean esCentral, boolean activo);
    //busca el almacen central activo
    Almacen findByEsCentralAndActivoTrue(boolean esCentral);//Esto devuleve uno solo, el primero que encuentra
    List<Almacen> findByPosXAndPosY(int posX, int posY);
 
 
}