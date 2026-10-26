package com.sivamani.docai.security;

/** The authenticated user stored in the security context (built from the JWT). */
public record AuthUser(Long id, String email) {}
