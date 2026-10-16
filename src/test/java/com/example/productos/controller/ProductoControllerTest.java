package com.example.productos.controller;

import com.example.productos.model.Producto;
import com.example.productos.service.ProductoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de la capa web para {@link ProductoController}.
 * Usa @WebMvcTest para cargar solo la capa MVC con MockMvc.
 */
@WebMvcTest(ProductoController.class)
@DisplayName("ProductoController - Pruebas de la capa web")
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductoService productoService;

    @Autowired
    private ObjectMapper objectMapper;

    private Producto productoEjemplo;

    private static final String BASE_URL = "/api/v1/productos";

    @BeforeEach
    void setUp() {
        productoEjemplo = new Producto(
                "Laptop Pro",
                "Laptop de prueba",
                new BigDecimal("1299.99"),
                10,
                "Electronica"
        );
        productoEjemplo.setId(1L);
        productoEjemplo.setActivo(true);
    }

    // ==================== GET tests ====================

    @Nested
    @DisplayName("GET /api/v1/productos")
    class GetAllTests {

        @Test
        @DisplayName("Debe retornar 200 OK con lista de productos")
        void debeRetornarListaProductos() throws Exception {
            when(productoService.obtenerTodos()).thenReturn(List.of(productoEjemplo));

            mockMvc.perform(get(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].nombre", is("Laptop Pro")))
                    .andExpect(jsonPath("$[0].precio", is(1299.99)));
        }

        @Test
        @DisplayName("Debe retornar solo activos cuando soloActivos=true")
        void debeRetornarSoloActivos() throws Exception {
            when(productoService.obtenerActivos()).thenReturn(List.of(productoEjemplo));

            mockMvc.perform(get(BASE_URL)
                            .param("soloActivos", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(productoService).obtenerActivos();
            verify(productoService, never()).obtenerTodos();
        }

        @Test
        @DisplayName("Debe retornar lista vacia cuando no hay productos")
        void debeRetornarListaVacia() throws Exception {
            when(productoService.obtenerTodos()).thenReturn(List.of());

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/productos/{id}")
    class GetByIdTests {

        @Test
        @DisplayName("Debe retornar 200 OK cuando el producto existe")
        void debeRetornar200CuandoExiste() throws Exception {
            when(productoService.obtenerPorId(1L)).thenReturn(Optional.of(productoEjemplo));

            mockMvc.perform(get(BASE_URL + "/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.nombre", is("Laptop Pro")))
                    .andExpect(jsonPath("$.categoria", is("Electronica")));
        }

        @Test
        @DisplayName("Debe retornar 404 cuando el producto no existe")
        void debeRetornar404CuandoNoExiste() throws Exception {
            when(productoService.obtenerPorId(99L)).thenReturn(Optional.empty());

            mockMvc.perform(get(BASE_URL + "/99"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/productos")
    class PostTests {

        @Test
        @DisplayName("Debe retornar 201 Created al crear un producto valido")
        void debeRetornar201AlCrear() throws Exception {
            when(productoService.crear(any(Producto.class))).thenReturn(productoEjemplo);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(productoEjemplo)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.nombre", is("Laptop Pro")));
        }

        @Test
        @DisplayName("Debe retornar 409 Conflict si el nombre ya existe")
        void debeRetornar409SiNombreDuplicado() throws Exception {
            when(productoService.crear(any(Producto.class)))
                    .thenThrow(new IllegalArgumentException("Ya existe un producto con el nombre: Laptop Pro"));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(productoEjemplo)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error", containsString("Laptop Pro")));
        }

        @Test
        @DisplayName("Debe retornar 400 Bad Request si el nombre esta vacio")
        void debeRetornar400SiNombreVacio() throws Exception {
            Producto invalido = new Producto("", "desc", new BigDecimal("10"), 1, "Cat");

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalido)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Debe retornar 400 Bad Request si el precio es nulo")
        void debeRetornar400SiPrecioNulo() throws Exception {
            Producto invalido = new Producto("Producto Test", "desc", null, 1, "Cat");

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalido)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/productos/{id}")
    class PutTests {

        @Test
        @DisplayName("Debe retornar 200 OK al actualizar producto existente")
        void debeRetornar200AlActualizar() throws Exception {
            when(productoService.actualizar(anyLong(), any(Producto.class))).thenReturn(productoEjemplo);

            mockMvc.perform(put(BASE_URL + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(productoEjemplo)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nombre", is("Laptop Pro")));
        }

        @Test
        @DisplayName("Debe retornar 404 al actualizar producto que no existe")
        void debeRetornar404SiNoExiste() throws Exception {
            when(productoService.actualizar(anyLong(), any(Producto.class)))
                    .thenThrow(new NoSuchElementException("Producto no encontrado con id: 99"));

            mockMvc.perform(put(BASE_URL + "/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(productoEjemplo)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/productos/{id}/stock")
    class PatchStockTests {

        @Test
        @DisplayName("Debe retornar 200 OK al actualizar el stock")
        void debeActualizarStock() throws Exception {
            productoEjemplo.setStock(50);
            when(productoService.actualizarStock(1L, 50)).thenReturn(productoEjemplo);

            mockMvc.perform(patch(BASE_URL + "/1/stock")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"stock\": 50}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.stock", is(50)));
        }

        @Test
        @DisplayName("Debe retornar 400 si el campo stock no esta presente")
        void debeRetornar400SiStockAusente() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/1/stock")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/productos/{id}")
    class DeleteTests {

        @Test
        @DisplayName("Debe retornar 204 No Content al eliminar logicamente")
        void debeRetornar204AlEliminar() throws Exception {
            doNothing().when(productoService).eliminar(1L);

            mockMvc.perform(delete(BASE_URL + "/1"))
                    .andExpect(status().isNoContent());

            verify(productoService).eliminar(1L);
        }

        @Test
        @DisplayName("Debe retornar 404 al intentar eliminar producto que no existe")
        void debeRetornar404SiNoExiste() throws Exception {
            doThrow(new NoSuchElementException("No encontrado")).when(productoService).eliminar(99L);

            mockMvc.perform(delete(BASE_URL + "/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Debe retornar 204 al eliminar fisicamente")
        void debeRetornar204AlEliminarFisicamente() throws Exception {
            doNothing().when(productoService).eliminarFisicamente(1L);

            mockMvc.perform(delete(BASE_URL + "/1/fisico"))
                    .andExpect(status().isNoContent());
        }
    }
}
