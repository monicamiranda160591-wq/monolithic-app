package com.example.productos.service;

import com.example.productos.model.Producto;
import com.example.productos.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link ProductoService}.
 * Usa Mockito para simular el repositorio sin acceso a base de datos.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductoService - Pruebas Unitarias")
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto productoEjemplo;

    @BeforeEach
    void setUp() {
        productoEjemplo = new Producto(
                "Laptop Test",
                "Descripcion de prueba",
                new BigDecimal("999.99"),
                10,
                "Electronica"
        );
        productoEjemplo.setId(1L);
        productoEjemplo.setActivo(true);
    }

    // ==================== Tests de lectura ====================

    @Nested
    @DisplayName("obtenerTodos()")
    class ObtenerTodosTests {

        @Test
        @DisplayName("Debe retornar todos los productos")
        void debeRetornarTodosLosProductos() {
            List<Producto> productos = Arrays.asList(productoEjemplo,
                    new Producto("Teclado", "Desc", new BigDecimal("50"), 5, "Perifericos"));
            when(productoRepository.findAll()).thenReturn(productos);

            List<Producto> resultado = productoService.obtenerTodos();

            assertThat(resultado).hasSize(2);
            verify(productoRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Debe retornar lista vacia si no hay productos")
        void debeRetornarListaVacia() {
            when(productoRepository.findAll()).thenReturn(List.of());

            List<Producto> resultado = productoService.obtenerTodos();

            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("obtenerPorId()")
    class ObtenerPorIdTests {

        @Test
        @DisplayName("Debe retornar el producto cuando el ID existe")
        void debeRetornarProductoCuandoExiste() {
            when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEjemplo));

            Optional<Producto> resultado = productoService.obtenerPorId(1L);

            assertThat(resultado).isPresent();
            assertThat(resultado.get().getNombre()).isEqualTo("Laptop Test");
        }

        @Test
        @DisplayName("Debe retornar Optional vacio cuando el ID no existe")
        void debeRetornarOptionalVacioSiNoExiste() {
            when(productoRepository.findById(99L)).thenReturn(Optional.empty());

            Optional<Producto> resultado = productoService.obtenerPorId(99L);

            assertThat(resultado).isEmpty();
        }
    }

    // ==================== Tests de escritura ====================

    @Nested
    @DisplayName("crear()")
    class CrearTests {

        @Test
        @DisplayName("Debe crear un producto cuando el nombre es unico")
        void debeCrearProductoCuandoNombreEsUnico() {
            when(productoRepository.existsByNombreIgnoreCase("Laptop Test")).thenReturn(false);
            when(productoRepository.save(any(Producto.class))).thenReturn(productoEjemplo);

            Producto creado = productoService.crear(productoEjemplo);

            assertThat(creado).isNotNull();
            assertThat(creado.getNombre()).isEqualTo("Laptop Test");
            verify(productoRepository).save(productoEjemplo);
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException si el nombre ya existe")
        void debeLanzarExcepcionSiNombreDuplicado() {
            when(productoRepository.existsByNombreIgnoreCase("Laptop Test")).thenReturn(true);

            assertThatThrownBy(() -> productoService.crear(productoEjemplo))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Laptop Test");

            verify(productoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("actualizar()")
    class ActualizarTests {

        @Test
        @DisplayName("Debe actualizar el producto cuando existe")
        void debeActualizarProductoCuandoExiste() {
            Producto updated = new Producto("Laptop Test Pro", "Nueva desc",
                    new BigDecimal("1199.99"), 20, "Electronica");
            updated.setActivo(true);

            when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEjemplo));
            when(productoRepository.existsByNombreIgnoreCase("Laptop Test Pro")).thenReturn(false);
            when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

            Producto resultado = productoService.actualizar(1L, updated);

            assertThat(resultado.getNombre()).isEqualTo("Laptop Test Pro");
            assertThat(resultado.getPrecio()).isEqualByComparingTo("1199.99");
        }

        @Test
        @DisplayName("Debe lanzar NoSuchElementException si el producto no existe")
        void debeLanzarExcepcionSiProductoNoExiste() {
            when(productoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productoService.actualizar(99L, productoEjemplo))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("actualizarStock()")
    class ActualizarStockTests {

        @Test
        @DisplayName("Debe actualizar el stock correctamente")
        void debeActualizarStock() {
            when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEjemplo));
            when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

            Producto resultado = productoService.actualizarStock(1L, 50);

            assertThat(resultado.getStock()).isEqualTo(50);
        }

        @Test
        @DisplayName("Debe lanzar excepcion si stock es negativo")
        void debeLanzarExcepcionSiStockNegativo() {
            assertThatThrownBy(() -> productoService.actualizarStock(1L, -1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("negativo");
        }
    }

    @Nested
    @DisplayName("eliminar()")
    class EliminarTests {

        @Test
        @DisplayName("Debe desactivar el producto (eliminacion logica)")
        void debeDesactivarProducto() {
            when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEjemplo));
            when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

            productoService.eliminar(1L);

            assertThat(productoEjemplo.isActivo()).isFalse();
            verify(productoRepository).save(productoEjemplo);
        }

        @Test
        @DisplayName("Debe lanzar NoSuchElementException si el producto no existe")
        void debeLanzarExcepcionSiNoExiste() {
            when(productoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productoService.eliminar(99L))
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("obtenerPorRangoPrecio()")
    class RangoPrecioTests {

        @Test
        @DisplayName("Debe retornar productos en el rango indicado")
        void debeRetornarProductosEnRango() {
            BigDecimal min = new BigDecimal("100");
            BigDecimal max = new BigDecimal("2000");
            when(productoRepository.findByPrecioBetween(min, max))
                    .thenReturn(List.of(productoEjemplo));

            List<Producto> resultado = productoService.obtenerPorRangoPrecio(min, max);

            assertThat(resultado).hasSize(1);
        }

        @Test
        @DisplayName("Debe lanzar excepcion si precioMin > precioMax")
        void debeLanzarExcepcionSiRangoInvalido() {
            assertThatThrownBy(() ->
                    productoService.obtenerPorRangoPrecio(
                            new BigDecimal("500"), new BigDecimal("100")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("minimo");
        }

        @Test
        @DisplayName("Debe lanzar excepcion si los precios son nulos")
        void debeLanzarExcepcionSiPreciosNulos() {
            assertThatThrownBy(() ->
                    productoService.obtenerPorRangoPrecio(null, new BigDecimal("100")))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("obtenerConStockBajo()")
    class StockBajoTests {

        @Test
        @DisplayName("Debe retornar productos con stock bajo")
        void debeRetornarProductosConStockBajo() {
            when(productoRepository.findProductosConStockBajo(5))
                    .thenReturn(List.of(productoEjemplo));

            List<Producto> resultado = productoService.obtenerConStockBajo(5);

            assertThat(resultado).isNotEmpty();
        }

        @Test
        @DisplayName("Debe lanzar excepcion si el umbral es negativo")
        void debeLanzarExcepcionSiUmbralNegativo() {
            assertThatThrownBy(() -> productoService.obtenerConStockBajo(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positivo");
        }
    }
}
