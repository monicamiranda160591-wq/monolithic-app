package com.example.productos.repository;

import com.example.productos.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad {@link Producto}.
 * Proporciona operaciones CRUD estándar y consultas personalizadas.
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /**
     * Busca productos por categoría (búsqueda exacta).
     */
    List<Producto> findByCategoria(String categoria);

    /**
     * Busca productos activos o inactivos.
     */
    List<Producto> findByActivo(boolean activo);

    /**
     * Busca productos cuyo nombre contenga el texto indicado (case-insensitive).
     */
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Busca productos en un rango de precio.
     */
    List<Producto> findByPrecioBetween(BigDecimal precioMin, BigDecimal precioMax);

    /**
     * Verifica si existe un producto con el mismo nombre (case-insensitive).
     */
    boolean existsByNombreIgnoreCase(String nombre);

    /**
     * Busca productos con stock por debajo de un umbral.
     */
    @Query("SELECT p FROM Producto p WHERE p.stock <= :umbral AND p.activo = true")
    List<Producto> findProductosConStockBajo(@Param("umbral") Integer umbral);

    /**
     * Busca productos activos por categoría.
     */
    @Query("SELECT p FROM Producto p WHERE p.categoria = :categoria AND p.activo = true")
    List<Producto> findActivosByCategoria(@Param("categoria") String categoria);

    /**
     * Busca un producto activo por ID.
     */
    Optional<Producto> findByIdAndActivoTrue(Long id);
}
