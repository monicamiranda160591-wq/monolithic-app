package com.example.productos.service;

import com.example.productos.model.Producto;
import com.example.productos.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Servicio con la lógica de negocio para la gestión de {@link Producto}.
 * Todas las operaciones de escritura son transaccionales.
 */
@Service
@Transactional(readOnly = true)
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    // ==================== Operaciones de Lectura ====================

    /**
     * Obtiene todos los productos registrados en el sistema.
     *
     * @return lista de todos los productos
     */
    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    /**
     * Obtiene solo los productos activos.
     *
     * @return lista de productos activos
     */
    public List<Producto> obtenerActivos() {
        return productoRepository.findByActivo(true);
    }

    /**
     * Busca un producto por su ID.
     *
     * @param id identificador del producto
     * @return Optional con el producto si existe
     */
    public Optional<Producto> obtenerPorId(Long id) {
        return productoRepository.findById(id);
    }

    /**
     * Busca productos por categoría.
     *
     * @param categoria nombre de la categoría
     * @return lista de productos en esa categoría
     */
    public List<Producto> obtenerPorCategoria(String categoria) {
        return productoRepository.findByCategoria(categoria);
    }

    /**
     * Busca productos cuyo nombre contenga el texto indicado.
     *
     * @param nombre texto a buscar
     * @return lista de productos que coinciden
     */
    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    /**
     * Busca productos en un rango de precio.
     *
     * @param precioMin precio mínimo
     * @param precioMax precio máximo
     * @return lista de productos en el rango
     */
    public List<Producto> obtenerPorRangoPrecio(BigDecimal precioMin, BigDecimal precioMax) {
        if (precioMin == null || precioMax == null) {
            throw new IllegalArgumentException("Los precios minimo y maximo son obligatorios");
        }
        if (precioMin.compareTo(precioMax) > 0) {
            throw new IllegalArgumentException("El precio minimo no puede ser mayor que el precio maximo");
        }
        return productoRepository.findByPrecioBetween(precioMin, precioMax);
    }

    /**
     * Obtiene productos con stock por debajo del umbral indicado.
     *
     * @param umbral cantidad mínima de stock
     * @return lista de productos con stock bajo
     */
    public List<Producto> obtenerConStockBajo(Integer umbral) {
        if (umbral == null || umbral < 0) {
            throw new IllegalArgumentException("El umbral de stock debe ser un valor positivo");
        }
        return productoRepository.findProductosConStockBajo(umbral);
    }

    // ==================== Operaciones de Escritura ====================

    /**
     * Crea un nuevo producto en el sistema.
     *
     * @param producto datos del nuevo producto
     * @return el producto creado con su ID asignado
     * @throws IllegalArgumentException si ya existe un producto con el mismo nombre
     */
    @Transactional
    public Producto crear(Producto producto) {
        if (productoRepository.existsByNombreIgnoreCase(producto.getNombre())) {
            throw new IllegalArgumentException(
                    "Ya existe un producto con el nombre: " + producto.getNombre());
        }
        return productoRepository.save(producto);
    }

    /**
     * Actualiza los datos de un producto existente.
     *
     * @param id      identificador del producto a actualizar
     * @param updated datos actualizados
     * @return el producto actualizado
     * @throws java.util.NoSuchElementException si el producto no existe
     */
    @Transactional
    public Producto actualizar(Long id, Producto updated) {
        Producto existente = productoRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException(
                        "Producto no encontrado con id: " + id));

        // Verificar nombre duplicado solo si cambió
        if (!existente.getNombre().equalsIgnoreCase(updated.getNombre())
                && productoRepository.existsByNombreIgnoreCase(updated.getNombre())) {
            throw new IllegalArgumentException(
                    "Ya existe un producto con el nombre: " + updated.getNombre());
        }

        existente.setNombre(updated.getNombre());
        existente.setDescripcion(updated.getDescripcion());
        existente.setPrecio(updated.getPrecio());
        existente.setStock(updated.getStock());
        existente.setCategoria(updated.getCategoria());
        existente.setActivo(updated.isActivo());

        return productoRepository.save(existente);
    }

    /**
     * Actualiza únicamente el stock de un producto.
     *
     * @param id       identificador del producto
     * @param cantidad nuevo valor del stock
     * @return el producto con stock actualizado
     */
    @Transactional
    public Producto actualizarStock(Long id, Integer cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException(
                        "Producto no encontrado con id: " + id));
        producto.setStock(cantidad);
        return productoRepository.save(producto);
    }

    /**
     * Elimina (desactiva) un producto de forma lógica.
     *
     * @param id identificador del producto
     * @throws java.util.NoSuchElementException si el producto no existe
     */
    @Transactional
    public void eliminar(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException(
                        "Producto no encontrado con id: " + id));
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    /**
     * Elimina permanentemente un producto de la base de datos.
     *
     * @param id identificador del producto
     * @throws java.util.NoSuchElementException si el producto no existe
     */
    @Transactional
    public void eliminarFisicamente(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new java.util.NoSuchElementException("Producto no encontrado con id: " + id);
        }
        productoRepository.deleteById(id);
    }
}
