package com.todoo.seguridad;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.stereotype.Component;

/**
 * Genera y verifica hashes de contrasenas con PBKDF2-HMAC-SHA256 (incluido en el JDK).
 * Formato guardado en la base de datos: pbkdf2_sha256$iteraciones$salBase64$hashBase64
 */
@Component
public class ContrasenaHasher {
    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final String PREFIJO = "pbkdf2_sha256";
    private static final int ITERACIONES = 600_000;
    private static final int BYTES_SAL = 16;
    private static final int BITS_HASH = 256;
    private final SecureRandom random = new SecureRandom();

    public String hashear(String contrasena) {
        byte[] sal = new byte[BYTES_SAL];
        random.nextBytes(sal);
        byte[] hash = derivar(contrasena, sal, ITERACIONES);
        Base64.Encoder b64 = Base64.getEncoder();
        return PREFIJO + "$" + ITERACIONES + "$" + b64.encodeToString(sal) + "$" + b64.encodeToString(hash);
    }

    public boolean verificar(String contrasena, String guardado) {
        if (contrasena == null || guardado == null) {
            return false;
        }
        String[] partes = guardado.split("\\$");
        if (partes.length != 4 || !PREFIJO.equals(partes[0])) {
            return false;
        }
        try {
            int iteraciones = Integer.parseInt(partes[1]);
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            return MessageDigest.isEqual(esperado, derivar(contrasena, sal, iteraciones));
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static byte[] derivar(String contrasena, byte[] sal, int iteraciones) {
        PBEKeySpec spec = new PBEKeySpec(contrasena.toCharArray(), sal, iteraciones, BITS_HASH);
        try {
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("PBKDF2 no esta disponible en esta JVM.", exception);
        } finally {
            spec.clearPassword();
        }
    }

    /**
     * Genera un hash desde la terminal para insertarlo con HeidiSQL. Desde Backend/main:
     * mvnw compile  y luego  java -cp target/classes com.todoo.seguridad.ContrasenaHasher "MiContrasena"
     */
    public static void main(String[] args) {
        if (args.length != 1 || args[0].isBlank()) {
            System.err.println("Uso: java -cp target/classes com.todoo.seguridad.ContrasenaHasher \"contrasena\"");
            System.exit(1);
        }
        System.out.println(new ContrasenaHasher().hashear(args[0]));
    }
}
