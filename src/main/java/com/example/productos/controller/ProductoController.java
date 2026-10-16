package com.example.productos.controller;

import com.example.productos.model.Producto;
import com.example.productos.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Controlador REST para las operaciones CRUD de {@link Producto}.
 * Base path: /api/v1/productos
 */
@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // ==================== GET ====================

    /**
     * Obtiene todos los productos.
     * GET /api/v1/productos
     * GET /api/v1/productos?soloActivos=true
     */
    @GetMapping
    public ResponseEntity<List<Producto>> obtenerTodos(
            @RequestParam(name = "soloActivos", defaultValue = "false") boolean soloActivos) {
        List<Producto> productos = soloActivos
                ? productoService.obtenerActivos()
                : productoService.obtenerTodos();
        return ResponseEntity.ok(productos);
    }

    /**
     * Obtiene un producto por ID.
     * GET /api/v1/productos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id) {
        return productoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Busca productos por nombre (parcial, case-insensitive).
     * GET /api/v1/productos/buscar?nombre=laptop
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<Producto>> buscarPorNombre(
            @RequestParam(name = "nombre") String nombre) {
        List<Producto> resultados = productoService.buscarPorNombre(nombre);
        return ResponseEntity.ok(resultados);
    }

    /**
     * Obtiene productos por categoría.
     * GET /api/v1/productos/categoria/{categoria}
     */
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Producto>> obtenerPorCategoria(
            @PathVariable String categoria) {
        return ResponseEntity.ok(productoService.obtenerPorCategoria(categoria));
    }

    /**
     * Obtiene productos en un rango de precio.
     * GET /api/v1/productos/precio?min=10.00&max=100.00
     */
    @GetMapping("/precio")
    public ResponseEntity<?> obtenerPorRangoPrecio(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {
        try {
            List<Producto> productos = productoService.obtenerPorRangoPrecio(min, max);
            return ResponseEntity.ok(productos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Obtiene productos con stock por debajo del umbral.
     * GET /api/v1/productos/stock-bajo?umbral=5
     */
    @GetMapping("/stock-bajo")
    public ResponseEntity<?> obtenerConStockBajo(
            @RequestParam(defaultValue = "5") Integer umbral) {
        try {
            List<Producto> productos = productoService.obtenerConStockBajo(umbral);
            return ResponseEntity.ok(productos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== POST ====================

    /**
     * Crea un nuevo producto.
     * POST /api/v1/productos
     */
    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody Producto producto) {
        try {
            Producto creado = productoService.crear(producto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== PUT ====================

    /**
     * Actualiza completamente un producto existente.
     * PUT /api/v1/productos/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Producto producto) {
        try {
            Producto actualizado = productoService.actualizar(id, producto);
            return ResponseEntity.ok(actualizado);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Actualiza el stock de un producto.
     * PATCH /api/v1/productos/{id}/stock
     */
    @PatchMapping("/{id}/stock")
    public ResponseEntity<?> actualizarStock(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {
        Integer cantidad = body.get("stock");
        if (cantidad == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El campo 'stock' es obligatorio"));
        }
        try {
            Producto actualizado = productoService.actualizarStock(id, cantidad);
            return ResponseEntity.ok(actualizado);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== DELETE ====================

    /**
     * Elimina lógicamente un producto (lo desactiva).
     * DELETE /api/v1/productos/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            productoService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Elimina físicamente un producto de la base de datos.
     * DELETE /api/v1/productos/{id}/fisico
     */
    @DeleteMapping("/{id}/fisico")
    public ResponseEntity<?> eliminarFisicamente(@PathVariable Long id) {
        try {
            productoService.eliminarFisicamente(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
