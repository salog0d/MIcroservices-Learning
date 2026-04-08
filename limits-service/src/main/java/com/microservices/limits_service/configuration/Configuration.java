package com.microservices.limits_service.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ============================================================
 * CONCEPTO: @ConfigurationProperties - Binding de configuracion
 * ============================================================
 *
 * Esta clase es el PUENTE entre el archivo de configuracion (ya sea
 * local o del Config Server) y el codigo Java.
 *
 * COMO FUNCIONA:
 *   1. Spring lee las propiedades con el prefijo "limits-service"
 *   2. Las mapea automaticamente a los campos de esta clase
 *   3. Convierte los tipos automaticamente (String -> int, etc.)
 *
 * Mapeo en el archivo de propiedades:
 *   limits-service.minimum=2   -->  this.minimum = 2
 *   limits-service.maximum=990 -->  this.maximum = 990
 *
 * ALTERNATIVA: Se puede usar @Value("${limits-service.minimum}")
 * campo por campo, pero @ConfigurationProperties es mas limpio
 * cuando hay multiples propiedades relacionadas (es el enfoque recomendado).
 *
 * REQUISITO: La clase debe tener getters Y setters porque Spring
 * usa reflexion para inyectar los valores.
 *
 * VALIDACION: Puedes agregar @Validated + anotaciones como
 * @Min, @Max, @NotNull para validar los valores al arrancar:
 *   @Validated
 *   public class Configuration {
 *       @Min(1) private int minimum;
 *       @Max(9999) private int maximum;
 *   }
 */

// @Component: registra esta clase como un bean de Spring (en el contexto IoC)
@Component

// @ConfigurationProperties: le dice a Spring que inyecte propiedades
// cuyo nombre empiece con "limits-service" en los campos de esta clase
@ConfigurationProperties("limits-service")
public class Configuration {
    private int minimum;
    private int maximum;

    public int getMinimum(){
        return this.minimum;
    }

    public void setMinimum(int min){
        this.minimum = min;
    }

    public int getMaximum(){
        return this.maximum;
    }

    public void setMaximum(int max){
        this.maximum = max;
    }
}
