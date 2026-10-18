package com.todoo.seguridad;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContrasenaHasherTest {
    private final ContrasenaHasher hasher = new ContrasenaHasher();

    @Test
    void verificaSoloLaContrasenaCorrecta() {
        String hash = hasher.hashear("Secreta123!");
        assertTrue(hash.startsWith("pbkdf2_sha256$600000$"));
        assertTrue(hasher.verificar("Secreta123!", hash));
        assertFalse(hasher.verificar("secreta123!", hash));
    }

    @Test
    void usaUnaSalDistintaEnCadaHash() {
        assertNotEquals(hasher.hashear("igual"), hasher.hashear("igual"));
    }

    @Test
    void rechazaHashesMalFormados() {
        assertFalse(hasher.verificar("x", null));
        assertFalse(hasher.verificar("x", "texto-plano"));
        assertFalse(hasher.verificar("x", "pbkdf2_sha256$abc$$"));
        assertFalse(hasher.verificar("x", "pbkdf2_sha256$1000$%%%$%%%"));
    }
}
