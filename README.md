👥 Autores
Juan David Gutierrez Florez - 2060104
Camilo Valencia Romero - 2259497

🚀 Instrucciones de Ejecución

📋 Prerrequisitos
Verificar que estén instalados Java 17 o superior y Apache Maven.
Para comprobarlo, ejecutar los siguientes comandos en la terminal o PowerShell:
bashjava -version
mvn -version

⚙️ Pasos para ejecutar el juego
1. 📂 Ubicarse en la carpeta raíz del proyecto
Por ejemplo:
bashcd C:\Users\juang\OneDrive\Desktop\ygo-duel-lite
2. 🔨 Compilar el proyecto y generar el archivo ejecutable
bashmvn clean package
3. ▶️ Ejecutar el juego
bashjava -jar target\ygo-duel-lite-1.0-SNAPSHOT.jar
4. 🎮 Iniciar el duelo
Al abrirse la aplicación, presionar el botón "Iniciar duelo".
El sistema obtendrá automáticamente tres cartas tipo Monster reales desde la API pública de YGOPRODeck para el jugador y tres para la máquina.
5. ⚔️ Mecánica de juego
En cada turno, el jugador selecciona la carta que desea usar y elige entre los botones "Atacar" o "Defender".
El sistema compara los valores de ataque (ATK) y defensa (DEF) de ambas cartas para determinar el ganador del turno.
Las cartas utilizadas desaparecen visualmente del campo después de cada enfrentamiento.
El duelo finaliza cuando uno de los jugadores logra ganar dos rondas.


🏗️ Breve Explicación de Diseño
El proyecto está construido con Java 17, empleando Swing para la interfaz gráfica y Maven para la gestión de dependencias. La lógica del duelo se organiza con una clase central Duel, que gestiona las rondas, el puntaje y las reglas de combate, mientras que MainWindow maneja la interfaz visual y las interacciones del jugador. Cada carta es representada por un objeto Card, obtenido dinámicamente desde la API de YGOPRODeck, garantizando variedad en cada partida.
La comunicación entre la lógica y la interfaz se realiza mediante el patrón Observer, usando la interfaz BattleListener para actualizar la UI (log, resultados y eliminación de cartas) en tiempo real. El diseño modular permite modificar fácilmente las reglas o añadir nuevos modos de juego en el futuro, facilitando la extensibilidad y el mantenimiento del código.


📸 Capturas del proyecto

![Captura 1](Capturas/Captura%201.png)

![Captura 2](Capturas/Captura%202.png)

![Captura 3](Capturas/Captura%203.png)

![Captura 4](Capturas/Captura%204.png)

![Captura 5](Capturas/Captura%205.png)


