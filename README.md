Autores:
Juan David Gutierrez Florez - 2060104
Camilo Valencia Romero – 2259497

Instrucciones de ejecución

-Verificar que estén instalados Java 17 o superior y Apache Maven.
Para comprobarlo, se pueden usar los siguientes comandos en la terminal o PowerShell:
java -version
mvn -version

-Ubicarse en la carpeta raíz del proyecto.
Por ejemplo:
cd C:\Users\juang\OneDrive\Desktop\ygo-duel-lite

-Compilar el proyecto y generar el archivo ejecutable con el comando:
mvn clean package

-Una vez completada la compilación, ejecutar el juego con el siguiente comando:
java -jar target\ygo-duel-lite-1.0-SNAPSHOT.jar

-Al abrirse la aplicación, presionar el botón “Iniciar duelo”.
El sistema obtendrá tres cartas tipo Monster reales desde la API pública de YGOPRODeck para el jugador y tres para la máquina.

-En cada turno, el jugador podrá seleccionar la carta que desea usar y elegir entre los botones “Atacar” o “Defender”.
El sistema comparará los valores de ataque (ATK) y defensa (DEF) de ambas cartas para determinar el ganador del turno.

-Las cartas utilizadas desaparecerán visualmente del campo después de cada enfrentamiento.
El duelo finaliza cuando uno de los jugadores logra ganar dos rondas.



El proyecto está construido con Java 17, empleando Swing para la interfaz gráfica y Maven para la gestión de dependencias.
La lógica del duelo se organiza con una clase central Duel, que gestiona las rondas, el puntaje y las reglas de combate, mientras que MainWindow maneja la interfaz visual y las interacciones del jugador.

Cada carta es representada por un objeto Card, obtenido dinámicamente desde la API de YGOPRODeck, garantizando variedad en cada partida. La comunicación entre la lógica y la interfaz se realiza mediante el patrón Observer, usando la interfaz BattleListener para actualizar la UI (log, resultados y eliminación de cartas) en tiempo real.
El diseño modular permite modificar fácilmente las reglas o añadir nuevos modos de juego en el futuro.



