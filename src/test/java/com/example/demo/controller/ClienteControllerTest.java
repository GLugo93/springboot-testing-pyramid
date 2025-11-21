package com.example.demo.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.example.demo.model.dto.ClienteRequestDTO;
import com.example.demo.repository.ClienteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo.model.entity.Cliente;

@SpringBootTest // ← Levanta contexto completo de Spring
@AutoConfigureMockMvc // ← Configura MockMvc automáticamente
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc; // ← Para simular peticiones HTTP

    @Autowired
    private ObjectMapper objectMapper; // ← Para convertir objetos a JSON

    @Autowired
    private ClienteRepository repository; // ← Para limpiar BD
    @Autowired
    private ClienteController controller; // ← Para limpiar BD

    @BeforeEach
    void setUp() {
        // Limpiar base de datos antes de cada test
        repository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/clientes - Debe crear cliente")
    void debeCrearCliente() throws Exception {
        // ============================================
        // ARRANGE
        // ============================================
        ClienteRequestDTO request = new ClienteRequestDTO(
                "María García",
                "maria@test.com",
                "0988888888");

        // ============================================
        // ACT & ASSERT
        // ============================================
        mockMvc.perform(
                post("/api/v1/clientes") // ← Método y URL
                        .contentType(MediaType.APPLICATION_JSON) // ← Enviar JSON
                        .content(objectMapper.writeValueAsString(request)) // ← Body
        )
                // Imprimir request/response en consola (útil para debug)
                .andDo(print())

                // Verificar status HTTP 201 CREATED
                .andExpect(status().isCreated())

                // Verificar que retorna JSON
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                // Verificar campos del JSON de respuesta
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre").value("María García"))
                .andExpect(jsonPath("$.email").value("maria@test.com"))
                .andExpect(jsonPath("$.telefono").value("0988888888"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/clientes/{id} - Debe obtener cliente")
    void debeObtenerClientePorId() throws Exception {
        // ============================================
        // ARRANGE - Guardar cliente en BD primero
        // ============================================
        Cliente cliente = new Cliente();
        cliente.setNombre("Pedro López");
        cliente.setEmail("pedro@test.com");
        cliente.setTelefono("0977777777");
        cliente.setActivo(true);

        Cliente guardado = repository.save(cliente);

        // ============================================
        // ACT & ASSERT
        // ============================================
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/clientes/{id}", guardado.getId()) // ← Path variable
        )
                .andDo(print())

                // Verificar status 200 OK
                .andExpect(status().isOk())

                // Verificar JSON
                .andExpect(jsonPath("$.id").value(guardado.getId()))
                .andExpect(jsonPath("$.nombre").value("Pedro López"))
                .andExpect(jsonPath("$.email").value("pedro@test.com"))
                .andExpect(jsonPath("$.telefono").value("0977777777"))
                .andExpect(jsonPath("$.activo").value(true));
    }

}