package com.logitrack.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.logitrack.model.TipoOperacion;
import com.logitrack.service.AuditoriaService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditoriaAspect {

    private final AuditoriaService auditoriaService;
    private final ObjectMapper objectMapper;

    @PersistenceContext
    private EntityManager entityManager;

    public AuditoriaAspect(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
    }

    @Around("@annotation(auditable)")
    public Object auditar(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {

        String usuario = obtenerUsuarioAutenticado();
        TipoOperacion operacion = auditable.operacion();
        String entidad = auditable.entidad();

        Long entidadId = obtenerIdDesdeArgumentos(joinPoint.getArgs());

        String valoresAnteriores = null;
        String valoresNuevos = null;

        if ((operacion == TipoOperacion.UPDATE || operacion == TipoOperacion.DELETE)
                && entidadId != null
                && auditable.entidadClase() != Void.class) {

            Object entidadAntes = entityManager.find(auditable.entidadClase(), entidadId);

            if (entidadAntes != null) {
                valoresAnteriores = convertirAJson(entidadAntes);
            }
        }

        Object resultado = joinPoint.proceed();

        if (operacion == TipoOperacion.INSERT) {
            entidadId = obtenerIdDesdeObjeto(resultado);
            valoresNuevos = convertirAJson(resultado);
        }

        if (operacion == TipoOperacion.UPDATE) {
            valoresNuevos = convertirAJson(resultado);
        }

        if (operacion == TipoOperacion.DELETE) {
            valoresNuevos = null;
        }

        auditoriaService.registrarAuditoria(
                operacion,
                usuario,
                entidad,
                entidadId,
                valoresAnteriores,
                valoresNuevos
        );

        return resultado;
    }

    private String obtenerUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            return "ANONIMO";
        }

        return authentication.getName();
    }

    private Long obtenerIdDesdeArgumentos(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }

        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
        }

        return null;
    }

    private Long obtenerIdDesdeObjeto(Object objeto) {
        if (objeto == null) {
            return null;
        }

        try {
            Object id = objeto.getClass().getMethod("getId").invoke(objeto);

            if (id instanceof Long) {
                return (Long) id;
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String convertirAJson(Object objeto) {
        if (objeto == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(objeto);
        } catch (Exception e) {
            return "No se pudo convertir a JSON: " + e.getMessage();
        }
    }
}