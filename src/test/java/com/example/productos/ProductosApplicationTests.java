package com.example.productos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de integración que verifica que el contexto de Spring Boot
 * se carga correctamente.
 */
@SpringBootTest
@DisplayName("ProductosApplication - Pruebas de contexto")
class ProductosApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("El contexto de Spring Boot debe cargarse sin errores")
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }

    @Test
    @DisplayName("El contexto debe contener el bean ProductoController")
    void debeContenerProductoController() {
        assertThat(applicationContext.containsBean("productoController")).isTrue();
    }

    @Test
    @DisplayName("El contexto debe contener el bean ProductoService")
    void debeContenerProductoService() {
        assertThat(applicationContext.containsBean("productoService")).isTrue();
    }

    @Test
    @DisplayName("El contexto debe contener el bean ProductoRepository")
    void debeContenerProductoRepository() {
        assertThat(applicationContext.containsBean("productoRepository")).isTrue();
    }
}
