# Guia de Microservicios con Java y Spring Cloud

Una referencia practica basada en este proyecto para levantar, conectar y mantener infraestructura de microservicios.

---

## Tabla de contenidos

1. [Arquitectura general](#arquitectura-general)
2. [Servicios core (infraestructura)](#servicios-core-infraestructura)
   - [Naming Server (Eureka)](#naming-server-eureka)
   - [Config Server](#config-server)
   - [API Gateway](#api-gateway)
3. [Microservicios de negocio](#microservicios-de-negocio)
   - [Currency Exchange Service](#currency-exchange-service)
   - [Currency Conversion Service](#currency-conversion-service)
   - [Limits Service](#limits-service)
4. [Orden de arranque](#orden-de-arranque)
5. [Comunicacion entre servicios](#comunicacion-entre-servicios)
6. [Patrones de resiliencia](#patrones-de-resiliencia)
7. [URLs de referencia](#urls-de-referencia)

---

## Arquitectura general

```
                        ┌─────────────────┐
                        │   API Gateway   │  :8765
                        │  (entrada unica)│
                        └────────┬────────┘
                                 │ routea trafico
              ┌──────────────────┼──────────────────┐
              │                  │                  │
              ▼                  ▼                  ▼
  ┌──────────────────┐ ┌──────────────────┐  otros servicios...
  │ Currency Exchange│ │Currency Conversion│
  │    :8000         │ │    :8100          │
  └──────────┬───────┘ └────────┬─────────┘
             │                  │ Feign Client
             └──────────────────┘
                      │ todos se registran en
                      ▼
             ┌────────────────┐
             │  Naming Server │  :8761
             │    (Eureka)    │
             └────────────────┘
                      │ obtienen config de
                      ▼
             ┌────────────────┐
             │ Config Server  │  :8888
             │  (Git-backed)  │
             └────────────────┘
```

Hay dos capas bien diferenciadas:

- **Core / Infraestructura**: servicios que no tienen logica de negocio pero son esenciales para que todo funcione. Sin ellos, los demas no arrancan o se comportan mal.
- **Negocio**: servicios con logica de dominio que se registran en el core y se comunican entre si.

---

## Servicios core (infraestructura)

Estos tres servicios deben estar corriendo **antes** que cualquier microservicio de negocio.

### Naming Server (Eureka)

**Puerto:** `8761`  
**Carpeta:** `naming-server/`

Es el registro central de servicios. Cada microservicio se anuncia aqui al arrancar y consulta aqui para encontrar a otros. Permite que los servicios se comuniquen por nombre logico en lugar de IP/puerto hardcodeado.

#### Dependencia en `pom.xml`

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

#### Configuracion `application.properties`

```properties
spring.application.name=naming-server
server.port=8761

# El servidor de Eureka no se registra a si mismo
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

#### Clase principal

```java
@SpringBootApplication
@EnableEurekaServer  // <-- esto activa el servidor
public class NamingServerApplication { ... }
```

#### Como se conectan los demas servicios

Cada microservicio de negocio agrega esto a su `application.properties`:

```properties
eureka.client.service-url.defaultZone=http://localhost:8761/eureka
```

Y su dependencia:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

Con eso, al arrancar el servicio se registra automaticamente en Eureka con el nombre definido en `spring.application.name`.

**Dashboard:** http://localhost:8761

---

### Config Server

**Puerto:** `8888`  
**Carpeta:** `spring_cloud_config_server/`

Centraliza la configuracion de todos los microservicios. En lugar de tener valores hardcodeados en cada `application.properties`, los servicios los obtienen de aqui al arrancar. El backend de configuracion es un repositorio Git.

#### Dependencia en `pom.xml`

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-server</artifactId>
</dependency>
```

#### Configuracion `application.properties`

```properties
spring.application.name=spring_cloud_config_server
server.port=8888

# Repositorio Git con los archivos de configuracion
spring.cloud.config.server.git.uri=file:///home/salog0d/Downloads/microservices/git-local-config-repo
spring.cloud.config.server.git.default-label=master
spring.cloud.config.server.git.clone-on-start=true
```

#### Clase principal

```java
@SpringBootApplication
@EnableConfigServer  // <-- esto activa el servidor de configuracion
public class SpringCloudConfigServerApplication { ... }
```

#### Estructura del repositorio Git

El Config Server lee de un repo Git. Los archivos siguen la convencion `{nombre-servicio}.properties` con soporte para perfiles:

```
git-local-config-repo/
├── limits-service.properties         # config base
├── limits-service-dev.properties     # config para perfil dev
└── limits-service-qa.properties      # config para perfil qa
```

Ejemplo de `limits-service.properties`:

```properties
limits-service.minimum=4
limits-service.maximum=996
```

#### Como se conectan los demas servicios

Los microservicios agregan esto a su `application.properties`:

```properties
# Importacion obligatoria (falla si Config Server no esta disponible)
spring.config.import=configserver:http://localhost:8888

# O importacion opcional (sigue sin Config Server)
spring.config.import=optional:configserver:http://localhost:8888
```

Para activar un perfil especifico (y obtener la config de ese perfil):

```properties
spring.profiles.active=qa
```

**Verificar que el Config Server tiene la config de un servicio:**

```
GET http://localhost:8888/{nombre-servicio}/{perfil}/master
GET http://localhost:8888/limits-service/qa/master
```

---

### API Gateway

**Puerto:** `8765`  
**Carpeta:** `api-gateway/`

Es el punto de entrada unico para los clientes externos. Rutea el trafico hacia los microservicios usando Eureka para resolverlos, aplica filtros globales (como logging) y puede hacer load balancing entre instancias.

#### Dependencias en `pom.xml`

```xml
<!-- Gateway reactivo basado en WebFlux -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway-server-webflux</artifactId>
</dependency>

<!-- Necesario para load balancing via Eureka -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>

<!-- Para registrarse en Eureka y descubrir otros servicios -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

#### Configuracion `application.properties`

```properties
spring.application.name=api-gateway
server.port=8765
eureka.client.service-url.defaultZone=http://localhost:8761/eureka

# Exponer endpoints de actuator para monitoreo
management.endpoints.web.exposure.include=*
```

#### Definicion de rutas (`ApiGatewayConfiguration.java`)

Las rutas se definen en codigo con el `RouteLocatorBuilder`. El prefijo `lb://` indica que debe resolver el servicio via Eureka con load balancing:

```java
@Configuration
public class ApiGatewayConfiguration {

    @Bean
    public RouteLocator gatewayRouter(RouteLocatorBuilder builder) {
        return builder.routes()
            // Ruta de prueba
            .route(p -> p.path("/get")
                .filters(f -> f.addRequestHeader("MyHeader", "MyURI"))
                .uri("http://httpbin.org:80"))

            // Ruta a Currency Exchange (lb:// = load balanced via Eureka)
            .route(p -> p.path("/currency-exchange/**")
                .uri("lb://currency-exchange"))

            // Agregar mas servicios aqui
            .build();
    }
}
```

#### Filtro global de logging (`LoggingFilter.java`)

```java
@Component
public class LoggingFilter implements GlobalFilter {

    private Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        logger.info("Path of the request: {}", exchange.getRequest().getPath());
        return chain.filter(exchange);
    }
}
```

Todos los requests que pasan por el gateway quedan registrados automaticamente.

---

## Microservicios de negocio

### Currency Exchange Service

**Puerto:** `8000`  
**Carpeta:** `currency-exchange-service/`  
**Rol:** Provee la tasa de cambio entre dos monedas.

#### Configuracion

```properties
spring.application.name=currency-exchange
server.port=8000

# Base de datos H2 en memoria
spring.jpa.show-sql=true
spring.h2.console.enabled=true
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.defer-datasource-initialization=true

# Registro en Eureka
eureka.client.service-url.defaultZone=http://localhost:8761/eureka

# Config server (opcional: sigue sin el)
spring.config.import=optional:configserver:http://localhost:8888

# Resilience4j
resilience4j.retry.instances.sample-api.max-attempts=5
resilience4j.retry.instances.sample-api.wait-duration=2s
resilience4j.retry.instances.sample-api.enable-exponential-backoff=true
resilience4j.ratelimiter.instances.default.limit-for-period=2
resilience4j.ratelimiter.instances.default.limit-refresh-period=10s
resilience4j.bulkhead.instances.default.max-concurrent-calls=10
```

#### Endpoint principal

```
GET /currency-exchange/from/{from}/to/{to}
GET /currency-exchange/from/USD/to/INR
```

Respuesta:

```json
{
  "id": 10001,
  "from": "USD",
  "to": "INR",
  "conversionMultiple": 65,
  "environment": "8000"
}
```

---

### Currency Conversion Service

**Puerto:** `8100`  
**Carpeta:** `currency-coonversion-service/`  
**Rol:** Convierte un monto entre monedas llamando a Currency Exchange.

#### Configuracion

```properties
spring.application.name=currency-coonversion
server.port=8100
eureka.client.service-url.defaultZone=http://localhost:8761/eureka
spring.config.import=optional:configserver:http://localhost:8888
```

#### Como llama a Currency Exchange (Feign)

Primero se habilita en la clase principal:

```java
@SpringBootApplication
@EnableFeignClients
public class CurrencyCoonversionServiceApplication { ... }
```

Luego se define el proxy con la interfaz Feign. El nombre en `@FeignClient` debe coincidir exactamente con el `spring.application.name` del servicio destino en Eureka:

```java
@FeignClient(name = "currency-exchange")  // nombre en Eureka
public interface CurrencyExchangeProxy {

    @GetMapping("/currency-exchange/from/{from}/to/{to}")
    public CurrencyConversion retrieveExchangeValues(
        @PathVariable String from,
        @PathVariable String to
    );
}
```

Feign + Eureka + LoadBalancer resuelven automaticamente la IP:puerto del servicio destino.

#### Endpoint principal

```
GET /currency-conversion-feing/from/{from}/to/{to}/quantity/{quantity}
GET /currency-conversion-feing/from/USD/to/INR/quantity/10
```

Respuesta:

```json
{
  "from": "USD",
  "to": "INR",
  "quantity": 10,
  "conversionMultiple": 65,
  "totalCalculatedAmount": 650,
  "environment": "8000"
}
```

---

### Limits Service

**Puerto:** `8080`  
**Carpeta:** `limits-service/`  
**Rol:** Ejemplo de cliente del Config Server. No llama a otros microservicios de negocio.

#### Configuracion

```properties
spring.application.name=limits-service
spring.profiles.active=qa

# Sin Config Server, este servicio no arranca (fail-fast=true)
spring.config.import=configserver:http://localhost:8888
spring.cloud.config.fail-fast=true
```

#### Como consume la configuracion

```java
@ConfigurationProperties(prefix = "limits-service")
@Component
public class Configuration {
    private int minimum;
    private int maximum;
    // getters y setters
}
```

Los valores `minimum` y `maximum` se inyectan automaticamente desde el Config Server.

---

## Orden de arranque

El orden importa. Los servicios core deben estar listos antes que los de negocio.

```
1. naming-server         (Eureka - sin dependencias)
2. spring_cloud_config_server  (Config Server - sin dependencias)
3. currency-exchange-service   (necesita Eureka, Config Server opcional)
4. currency-coonversion-service (necesita Eureka + Currency Exchange para operar)
5. api-gateway           (necesita Eureka)
6. limits-service        (necesita Config Server obligatoriamente)
```

> Si `limits-service` arranca antes que el Config Server y tiene `spring.cloud.config.fail-fast=true`, **fallara en el startup**. Esto es intencional para detectar problemas de configuracion temprano.

---

## Comunicacion entre servicios

### Patron recomendado: Feign Client

Feign genera el cliente HTTP automaticamente a partir de una interfaz. Se integra con Eureka y LoadBalancer sin configuracion extra.

```
Client → Feign Proxy → Eureka Lookup → Load Balancer → Instancia del servicio
```

**Ventajas sobre RestTemplate:**
- Codigo declarativo (solo una interfaz)
- Load balancing automatico
- Integracion nativa con Resilience4j para fallbacks

### Patron legacy: RestTemplate

```java
// Hardcoded - evitar en produccion
Map<String, String> uriVariables = new HashMap<>();
uriVariables.put("from", from);
uriVariables.put("to", to);

ResponseEntity<CurrencyConversion> responseEntity = new RestTemplate().getForEntity(
    "http://localhost:8000/currency-exchange/from/{from}/to/{to}",
    CurrencyConversion.class,
    uriVariables
);
```

Tiene la desventaja de que la URL esta hardcodeada. Si hay multiples instancias del servicio destino, no hace load balancing automaticamente.

### Resolucion via API Gateway

Los clientes externos nunca llaman directamente a los microservicios. Todo pasa por el gateway:

```
# Directo al servicio (solo para desarrollo/debug)
GET http://localhost:8000/currency-exchange/from/USD/to/INR

# Via API Gateway (uso normal)
GET http://localhost:8765/currency-exchange/from/USD/to/INR
```

---

## Patrones de resiliencia

Todos implementados con **Resilience4j** en `currency-exchange-service`.

### Circuit Breaker

Abre el circuito cuando hay demasiados fallos, evitando llamadas en cascada a un servicio caido.

```java
@GetMapping("/sample-api")
@CircuitBreaker(name = "sample-api", fallbackMethod = "hardcodedResponse")
public String sampleApi() {
    // Llama a otro servicio que puede fallar
    return new RestTemplate().getForEntity("http://localhost:8080/some-api", String.class).getBody();
}

public String hardcodedResponse(Exception ex) {
    return "fallback-response";  // Se devuelve cuando el circuito esta abierto
}
```

### Rate Limiter

Limita cuantas requests puede recibir el endpoint en un periodo de tiempo.

```java
@GetMapping("/sample-api")
@RateLimiter(name = "default")
public String sampleApi() { ... }
```

Configuracion: 2 requests cada 10 segundos.

```properties
resilience4j.ratelimiter.instances.default.limit-for-period=2
resilience4j.ratelimiter.instances.default.limit-refresh-period=10s
```

### Bulkhead

Limita la concurrencia maxima para evitar que un endpoint consuma todos los recursos del sistema.

```java
@GetMapping("/sample-api")
@Bulkhead(name = "default")
public String sampleApi() { ... }
```

Configuracion: maximo 10 llamadas concurrentes.

```properties
resilience4j.bulkhead.instances.default.max-concurrent-calls=10
```

### Retry con backoff exponencial

Reintenta automaticamente en caso de fallo, con tiempo de espera creciente entre intentos.

```java
@GetMapping("/sample-api")
@Retry(name = "sample-api")
public String sampleApi() { ... }
```

Configuracion: 5 intentos, espera de 2s con backoff exponencial.

```properties
resilience4j.retry.instances.sample-api.max-attempts=5
resilience4j.retry.instances.sample-api.wait-duration=2s
resilience4j.retry.instances.sample-api.enable-exponential-backoff=true
```

---

## URLs de referencia

| Servicio | URL | Descripcion |
|----------|-----|-------------|
| Eureka Dashboard | http://localhost:8761 | Ver todos los servicios registrados |
| Config Server | http://localhost:8888/limits-service/qa/master | Config de limits-service en perfil qa |
| H2 Console | http://localhost:8000/h2-console | Base de datos de currency-exchange |
| Currency Exchange (directo) | http://localhost:8000/currency-exchange/from/USD/to/INR | Tasa USD→INR |
| Currency Exchange (gateway) | http://localhost:8765/currency-exchange/from/USD/to/INR | Via API Gateway |
| Currency Conversion (Feign) | http://localhost:8100/currency-conversion-feing/from/USD/to/INR/quantity/10 | Conversion 10 USD a INR |
| Limits Service | http://localhost:8080/limits | Limites desde Config Server |
| Resilience demo | http://localhost:8000/sample-api | Circuit breaker / rate limiter / bulkhead |

---

## Resumen de puertos

| Servicio | Puerto | Tipo |
|----------|--------|------|
| naming-server | 8761 | Core - Infraestructura |
| spring_cloud_config_server | 8888 | Core - Infraestructura |
| api-gateway | 8765 | Core - Infraestructura |
| limits-service | 8080 | Negocio |
| currency-exchange-service | 8000 | Negocio |
| currency-coonversion-service | 8100 | Negocio |
