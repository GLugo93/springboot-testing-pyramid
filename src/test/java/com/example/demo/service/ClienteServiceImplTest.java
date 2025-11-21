package com.example.demo.service;

import com.example.demo.exception.ClienteNotFoundException;
import com.example.demo.mapper.ClienteMapper;
import com.example.demo.model.dto.ClienteRequestDTO;
import com.example.demo.model.dto.ClienteResponseDTO;
import com.example.demo.model.entity.Cliente;
import com.example.demo.repository.ClienteRepository;
import com.example.demo.service.impl.ClienteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository repository;

    @Mock
    private ClienteMapper mapper;

    @InjectMocks
    private ClienteServiceImpl service;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Debe crear cliente cuando datos son válidos")
    void debeCrearClienteCuandoDatosValidos() {
        // ============================================
        // ARRANGE (Preparar) - Configurar el escenario
        // ============================================

        // 1. Crear datos de entrada
        ClienteRequestDTO request = new ClienteRequestDTO(
                "Juan Pérez",
                "juan@test.com",
                "0999999999"
        );
        // 2. Crear entidad que "retornará" el mapper
        Cliente clienteEntity = new Cliente();
        clienteEntity.setNombre("Juan Pérez");
        clienteEntity.setEmail("juan@test.com");
        clienteEntity.setTelefono("0999999999");

        // 3. Crear entidad guardada (con ID)
        Cliente clienteGuardado = new Cliente();
        clienteGuardado.setId(1L);
        clienteGuardado.setNombre("Juan Pérez");
        clienteGuardado.setEmail("juan@test.com");
        clienteGuardado.setTelefono("0999999999");
        clienteGuardado.setActivo(true);

        // 4. Crear DTO de respuesta esperada
        ClienteResponseDTO expectedResponse = new ClienteResponseDTO(
                1L,
                "Juan Pérez",
                "juan@test.com",
                "0999999999",
                LocalDateTime.now(),
                true
        );

        // 5. CONFIGURAR COMPORTAMIENTO DE LOS MOCKS
        // "Cuando llamen a mapper.toEntity() con request, retorna clienteEntity"
        when(mapper.toEntity(request)).thenReturn(clienteEntity);

        // "Cuando llamen a repository.save() con cualquier Cliente, retorna clienteGuardado"
        when(repository.save(any(Cliente.class))).thenReturn(clienteGuardado);

        // "Cuando llamen a mapper.toResponseDTO() con clienteGuardado, retorna expectedResponse"
        when(mapper.toResponseDTO(clienteGuardado)).thenReturn(expectedResponse);

        // ============================================
        // ACT (Ejecutar) - Llamar al método a testear
        // ============================================
        ClienteResponseDTO result = service.crear(request);

        // ============================================
        // ASSERT (Verificar) - Comprobar resultados
        // ============================================

        // Verificar que el resultado no es nulo
        assertNotNull(result);

        // Verificar campos del resultado
        assertEquals(1L, result.getId());
        assertEquals("Juan Pérez", result.getNombre());
        assertEquals("juan@test.com", result.getEmail());
        assertEquals("0999999999", result.getTelefono());
        assertTrue(result.getActivo());

        // Verificar que se llamaron los métodos correctos
        verify(mapper).toEntity(request);
        verify(repository).save(any(Cliente.class));
        verify(mapper).toResponseDTO(clienteGuardado);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando cliente no existe")
    void debeLanzarExcepcionCuandoClienteNoExiste() {
        // ARRANGE
        Long clienteId = 99L;

        // Configurar mock: repository NO encuentra el cliente
        when(repository.findById(clienteId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        // Verificar que se lanza la excepción esperada
        ClienteNotFoundException exception = assertThrows(
                ClienteNotFoundException.class,
                () -> service.obtenerPorId(clienteId)
        );

        // Verificar mensaje de la excepción
        assertEquals("Cliente no encontrado con id: 99", exception.getMessage());

        // Verificar que se llamó findById
        verify(repository).findById(clienteId);

        // Verificar que NO se llamó save (porque no existe)
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe eliminar cliente existente")
    void debeEliminarClienteExistente() {
        // ARRANGE
        Long clienteId = 1L;

        Cliente cliente = new Cliente();
        cliente.setId(clienteId);
        cliente.setNombre("Cliente a Eliminar");

        // Mock: repository encuentra el cliente
        when(repository.findById(clienteId)).thenReturn(Optional.of(cliente));

        // ACT
        service.eliminar(clienteId);

        // ASSERT
        // Verificar que se buscó el cliente
        verify(repository).findById(clienteId);

        // Verificar que se eliminó
        verify(repository).save(cliente);
    }

}