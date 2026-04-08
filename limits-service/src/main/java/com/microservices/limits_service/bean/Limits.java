package com.microservices.limits_service.bean;

/**
 * ============================================================
 * CONCEPTO: DTO / Bean de respuesta (Data Transfer Object)
 * ============================================================
 *
 * Esta clase representa los datos que el microservicio expone
 * hacia afuera a traves de su API REST.
 *
 * PATRON: DTO (Data Transfer Object)
 * ------------------------------------
 * Un DTO es un objeto cuyo unico proposito es transportar datos
 * entre capas o entre servicios. No tiene logica de negocio.
 *
 * DIFERENCIA con la clase Configuration:
 *   - Configuration: mapea propiedades del archivo de config -> uso interno
 *   - Limits:        estructura de la respuesta HTTP -> uso externo (cliente)
 *
 * Cuando el controller hace: return new Limits(config.getMinimum(), config.getMaximum())
 * Spring automaticamente serializa este objeto a JSON:
 *   {
 *     "minimum": 2,
 *     "maximum": 990
 *   }
 *
 * NOTA: Spring usa Jackson para la serializacion JSON. Jackson necesita
 * getters publicos para serializar y un constructor vacio para deserializar
 * (cuando este objeto se recibe como request body).
 *
 * MEJORA POSIBLE: En proyectos modernos se usan Java Records en lugar
 * de clases con getters/setters manuales:
 *   public record Limits(int minimum, int maximum) {}
 */
public class Limits {

    // NOTA: hay un typo en el campo: "minimun" en lugar de "minimum"
    // El getter se llama getMinimum() (correcto) pero el campo interno
    // tiene el typo. Jackson serializa usando el nombre del getter,
    // por eso el JSON muestra "minimum" correctamente.
    private int minimun;
    private int maximum;

    public Limits(int min, int max){
        super();
        this.minimun = min;
        this.maximum = max;
    }

    public int getMinimum(){
        return this.minimun;
    }

    public void setMinimum(int min){
        this.minimun = min;
    }

    public int getMaximum(){
        return this.maximum;
    }

    public void setMaximum(int max){
        this.maximum = max;
    }
}
