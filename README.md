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
- `componentes`: reservado para los futuros sistemas de la taberna.

Esta etapa incluye configuración, bus y eventos. Los sistemas se implementarán después.
