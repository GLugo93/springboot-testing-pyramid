package com.example.demo.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.example.demo.model.dto.ClienteRequestDTO;
import com.example.demo.repository.ClienteRepository;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ClienteE2ETest {

    @LocalServerPort // ← Obtiene el puerto aleatorio
    private int port;

    @Autowired
    private ClienteRepository repository;

    @BeforeEach
    void setUp() {
        // Configurar REST Assured
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1/clientes";

        // Limpiar BD
        repository.deleteAll();
    }

    @Test
    @DisplayName("Flujo completo: CREAR → CONSULTAR → ACTUALIZAR → ELIMINAR")
    void flujoCompletoCRUD() {
        // ============================================
        // PASO 1: CREAR CLIENTE (POST)
        // ============================================
        ClienteRequestDTO createRequest = new ClienteRequestDTO(
                "Ana Martínez",
                "ana@test.com",
                "0966666666");

        Long clienteId = given() // ← Inicia la petición
                .contentType(ContentType.JSON)
                .body(createRequest)
                .when() // ← Ejecuta
                .post()
                .then() // ← Verifica
                .statusCode(201)
                .body("nombre", equalTo("Ana Martínez"))
                .body("email", equalTo("ana@test.com"))
                .body("telefono", equalTo("0966666666"))
                .body("activo", equalTo(true))
                .extract() // ← Extrae datos
                .jsonPath()
                .getLong("id");

        System.out.println("✅ Cliente creado con ID: " + clienteId);

        // ============================================
        // PASO 2: CONSULTAR CLIENTE CREADO (GET)
        // ============================================
        given()
                .pathParam("id", clienteId)
                .when()
                .get("/{id}")
                .then()
                .statusCode(200)
                .body("id", equalTo(clienteId.intValue()))
                .body("nombre", equalTo("Ana Martínez"));

        System.out.println("✅ Cliente consultado correctamente");

        // ============================================
        // PASO 3: ACTUALIZAR CLIENTE (PUT)
        // ============================================
        ClienteRequestDTO updateRequest = new ClienteRequestDTO(
                "Ana Martínez López",
                "ana.lopez@test.com",
                "0955555555");

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", clienteId)
                .body(updateRequest)
                .when()
                .put("/{id}")
                .then()
                .statusCode(200)
                .body("nombre", equalTo("Ana Martínez López"))
                .body("email", equalTo("ana.lopez@test.com"))
                .body("telefono", equalTo("0955555555"));

        System.out.println("✅ Cliente actualizado correctamente");

        // ============================================
        // PASO 4: ELIMINAR CLIENTE (DELETE)
        // ============================================
        given()
                .pathParam("id", clienteId)
                .when()
                .delete("/{id}")
                .then()
                .statusCode(204); // ← No Content

        System.out.println("✅ Cliente eliminado correctamente");

        // ============================================
        // PASO 5: VERIFICAR QUE YA NO EXISTE (GET)
        // ============================================
        given()
                .pathParam("id", clienteId)
                .when()
                .get("/{id}")
                .then()
                .statusCode(404); // ← Not Found

        System.out.println("✅ Verificado que cliente no existe");
        System.out.println("🎉 FLUJO CRUD COMPLETO EXITOSO");
    }
}