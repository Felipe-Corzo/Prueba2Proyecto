package com.logitrack.config;

import com.logitrack.model.TipoOperacion;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    TipoOperacion operacion();

    String entidad();

    Class<?> entidadClase() default Void.class;
}