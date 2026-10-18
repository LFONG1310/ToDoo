package com.todoo.autenticacion;

/** Datos publicos de un usuario. Nunca incluye el hash de la contrasena. */
public record Usuario(long id, String nombre, String correo) {
}
