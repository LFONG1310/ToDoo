package com.todoo.autenticacion;

import com.todoo.seguridad.ContrasenaHasher;
import java.util.Locale;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AutenticacionService {
    private static final int MAX_CORREO = 255;
    private static final int MAX_CONTRASENA = 200;
    private final UsuarioRepository repository;
    private final ContrasenaHasher hasher;
    // Se verifica contra este hash cuando el correo no existe, para que la respuesta tarde lo mismo
    // y no revele que correos estan registrados.
    private final String hashFicticio;

    public AutenticacionService(UsuarioRepository repository, ContrasenaHasher hasher) {
        this.repository = repository;
        this.hasher = hasher;
        this.hashFicticio = hasher.hashear("contrasena-ficticia");
    }

    @Transactional(readOnly = true)
    public Usuario iniciarSesion(String correo, String contrasena) {
        if (correo == null || contrasena == null || correo.isBlank() || contrasena.isEmpty()
                || correo.length() > MAX_CORREO || contrasena.length() > MAX_CONTRASENA) {
            throw credencialesInvalidas();
        }
        Optional<UsuarioRepository.Credencial> credencial =
                repository.buscarCredencial(correo.strip().toLowerCase(Locale.ROOT));
        boolean valida = hasher.verificar(contrasena,
                credencial.map(UsuarioRepository.Credencial::contrasenaHash).orElse(hashFicticio));
        if (credencial.isEmpty() || !valida || !credencial.get().activo()) {
            throw credencialesInvalidas();
        }
        return credencial.get().usuario();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> usuarioActivo(long id) {
        return repository.buscarActivo(id);
    }

    private static ResponseStatusException credencialesInvalidas() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contrasena incorrectos.");
    }
}
