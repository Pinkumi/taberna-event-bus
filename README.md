# El Gólem Alquímico

Proyecto universitario de Análisis y Diseño de Sistemas (Meta 3.1): comunicación
entre componentes de una taberna mediante Publisher/Subscriber y un EventBus propio.

Utiliza JDK 24 y Maven con `release 17` para generar código compatible con Java 17.
Con JDK 24 en `JAVA_HOME`, ejecutar `mvn test` y `mvn compile`.
Si Maven no está instalado, utilizar `./mvnw` (Linux/macOS) o `.\mvnw.cmd` (Windows).

Paquetes bajo `com.taverneventvus`:
- Raíz: aplicación principal (`Launcher`).
- `bus`: bus de eventos.
- `eventos`: eventos inmutables.
- `componentes`: pedidos, barra, cocina, catálogo, banda, sonido y panel LED.

La tercera etapa incorpora `Banda`, `SistemaDeSonido` y `PanelLED`.
Todavía no se implementa el sistema de humo ni se generan los diagramas finales.

El bus notifica síncronamente en el hilo publicador por tipo exacto, con acceso
al registro protegido entre hilos. Los callbacks se ejecutan fuera del bloqueo
y deben tolerar publicaciones concurrentes. Para desuscribirse,
se debe conservar y pasar el mismo `Consumer` registrado. Las modificaciones de
suscripciones durante una notificación se aplican a las publicaciones posteriores.
Las excepciones de los suscriptores se propagan al publicador.

Los eventos son records: `PedidoRealizadoEvent`, `BebidaServidaEvent`,
`ComidaPreparadaEvent` y `BandaTocandoEvent`. Los identificadores son `String`,
los productos son una copia inmutable de `List<String>` y la duración musical
es un entero en segundos, siguiendo los ejemplos de `Instrucciones.pdf`.

## Simulación de pedidos

Después de `mvn compile`, ejecutar:

```shell
java -cp target/classes com.taverneventvus.Launcher
```

El ejemplo publica el pedido `pedido_123` de `mesa_5` con cerveza y hamburguesa.
La barra programa la bebida a 2 segundos y la cocina el alimento a 5 segundos,
en ejecutores independientes; registrar el pedido no espera las preparaciones.
Cada producto reconocido genera un evento y conserva el texto original.
El catálogo reconoce cerveza, refresco, agua, hamburguesa, pizza y papas fritas,
ignorando mayúsculas y espacios en los extremos. Los desconocidos se anuncian
y no generan eventos de preparación.

`Barra` y `Cocina` implementan `AutoCloseable`: al cerrar se desuscriben y esperan
sus tareas pendientes, hasta 30 segundos antes de solicitar su cancelación.
El ejemplo termina automáticamente en unos 5 segundos. Cada constructor admite
un `Duration` alternativo para las pruebas, que usan tiempos de 0 a 10 milisegundos.
La utilidad interna `PreparacionProgramada` comparte la programación y cierre
de ejecutores. Las pruebas verifican filtrado, datos, cierre, desuscripción y
preparación paralela sin bloquear al publicador.

## Música y notificaciones

El mismo `Launcher` inicia a Los Seguidores de Dijkstra con El Algoritmo del Amor
(180 segundos como dato del evento), publicando inmediatamente y cada segundo
mientras se preparan los productos. Sonido ajusta el ecualizador y el panel
muestra los cuatro tipos de eventos. La demostración sigue durando unos 5 segundos.

`Banda.tocarCancion(nombreCancion, duracion)` publica una canción directamente.
`Banda.iniciar(nombreCancion, duracion, intervalo)` repite esa canción con un
`Duration` configurable de al menos un milisegundo. Solo se inicia una vez;
`detener()` o `close()` cancela la repetición y cierra su ejecutor, esperando
hasta 5 segundos por una notificación en curso antes de solicitar interrupción.
Una banda detenida no puede reiniciarse.

Sonido y panel implementan `AutoCloseable` para cancelar sus suscripciones.
El cierre de la aplicación espera primero las preparaciones, detiene la banda
y finalmente desuscribe sonido y panel. El panel usa texto sin colores por
defecto; `new PanelLED(bus, true)` habilita ANSI opcionalmente.
Las pruebas usan intervalos musicales de 10 milisegundos y sincronización con
`CountDownLatch` para comprobar la convivencia de música y preparaciones.
