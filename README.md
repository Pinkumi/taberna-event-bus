# El Gólem Alquímico

Proyecto de Análisis y Diseño de Sistemas, Meta 3.1. Simula una taberna mediante
el patrón Publisher/Subscriber con un EventBus propio. Los sistemas intercambian
eventos sin conocerse directamente. La salida es por consola y no se reproduce audio.

## Requisitos y ejecución

- JDK 24, con `JAVA_HOME` configurado. Maven genera código compatible con Java 17.
- Maven 3.8.5 o posterior. El proyecto incluye Maven Wrapper.
- Internet en la primera compilación para descargar las herramientas y JUnit 5.

Desde la raíz del proyecto:

```shell
mvn test
mvn compile
mvn exec:java
```

Sin Maven instalado, sustituir `mvn` por `.\mvnw.cmd` en Windows o `./mvnw` en
Linux/macOS (si hace falta, habilitarlo con `chmod +x mvnw`). También se puede
lanzar directamente después de compilar:

```shell
java -cp target/classes com.taverneventvus.Launcher
```

## Componentes

Las clases se organizan bajo `com.taverneventvus`: `Launcher` compone la aplicación,
`bus` contiene EventBus, `eventos` los records y `componentes` los sistemas.

| Componente | Responsabilidad |
| --- | --- |
| SistemaDePedidos | Registra y publica pedidos con mesa, identificador y productos. |
| Barra | Recibe pedidos, prepara bebidas y publica bebidas servidas después de 2 segundos. |
| Cocina | Recibe pedidos, prepara alimentos y publica comidas preparadas después de 5 segundos. |
| Banda | Publica canciones directamente o a intervalos configurables. |
| SistemaDeSonido | Ajusta el ecualizador al recibir eventos musicales. |
| PanelLED | Muestra pedidos, bebidas, comidas y música. Colores opcionales con `new PanelLED(bus, true)`. |
| SistemaDeHumo | Activa humo únicamente con el título exacto `Through the Fire and Flames`. |
| CatalogoProductos | Reconoce cerveza, refresco, agua, hamburguesa, pizza y papas fritas. |
| PreparacionProgramada | Utilidad interna para programar preparaciones y cerrar sus ejecutores. |

El catálogo ignora mayúsculas y espacios en los extremos, pero los eventos conservan
el texto original. Los productos desconocidos se anuncian y no se preparan.

## EventBus y eventos

`EventBus` ofrece `suscribir`, `desuscribir` y `publicar`, usando clases y `Consumer`.
Cada suscriptor recibe solo el tipo exacto registrado. Publicar sin suscriptores
no genera errores. Para cancelar una suscripción se conserva el mismo `Consumer`.
El registro está protegido entre hilos y cada notificación es síncrona en el hilo
publicador, fuera del bloqueo. Los callbacks deben tolerar publicaciones concurrentes;
las excepciones se propagan al publicador. Una notificación ya iniciada utiliza
una copia de los suscriptores registrados en ese momento.

Los cuatro eventos son records inmutables: `PedidoRealizadoEvent`, `BebidaServidaEvent`,
`ComidaPreparadaEvent` y `BandaTocandoEvent`. Los identificadores son `String`, los
productos se copian con `List.copyOf` y la duración musical se expresa en segundos.

SistemaDeHumo se incorpora registrándolo en el mismo bus, sin modificar Banda ni
EventBus. Esto permite agregar una reacción nueva sin cambiar al publicador.

## Ejemplo de funcionamiento

Launcher registra `pedido_123` para `mesa_5` con cerveza y hamburguesa. Barra y
Cocina trabajan en paralelo. La banda publica primero `Through the Fire and Flames`
(440 segundos como dato), que activa humo. Después publica `El Algoritmo del Amor`
(180 segundos como dato) inmediatamente y cada segundo; esta canción no activa humo.
Sonido y panel reaccionan a ambas canciones.

La bebida termina aproximadamente a los 2 segundos y la comida a los 5. El panel
muestra sus notificaciones y la aplicación termina automáticamente en unos 5 segundos,
sin esperar la duración real de las canciones.

Barra, Cocina y Banda utilizan ejecutores independientes. El cierre con
`try-with-resources` espera las preparaciones, detiene la banda y cancela las
suscripciones de humo, sonido y panel. Las preparaciones esperan hasta 30 segundos
al cerrar y la banda hasta 5 antes de solicitar interrupción. Una banda detenida
no se reinicia. Los constructores de Barra y Cocina admiten tiempos alternativos;
`Banda.iniciar` permite configurar el intervalo (al menos un milisegundo).

## Pruebas y entrega

JUnit 5 verifica suscripciones, inmutabilidad, filtrado de productos, datos de los
eventos, preparaciones paralelas, publicaciones periódicas, notificaciones y humo.
Las pruebas usan tiempos reducidos y `CountDownLatch`, sin esperar 2 o 5 segundos.

- [Documento de diagramas UML](docs/Diagramas_UML.pdf): tres páginas, con una explicación por diagrama.
- [Clases](docs/uml/clases.png) y [fuente PlantUML](docs/uml/clases.puml).
- [Secuencia](docs/uml/secuencia.png) y [fuente PlantUML](docs/uml/secuencia.puml).
- [Componentes](docs/uml/componentes.png) y [fuente PlantUML](docs/uml/componentes.puml).
- [Instrucciones originales](src/main/resources/com/taverneventvus/Instrucciones.pdf).

El PDF usa páginas ajustadas al tamaño de los diagramas para mantener el texto
legible y evitar recortes. Para regenerarlo se necesitan Python 3, Java y el JAR
de PlantUML (generado con la versión 1.2025.2; no requiere Graphviz externo):

```shell
python -m pip install -r docs/requirements.txt
python docs/generar_documentacion.py --plantuml /ruta/plantuml.jar
```

Estas herramientas solo generan la documentación; no son dependencias de la aplicación.
