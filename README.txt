SmartSave - Guía de Inicio Rápido
¡Hola! Soy Leonel Yupanqui, y te doy la bienvenida a SmartSave. Gracias por interesarte en mi proyecto de fin de grado.

Para que la aplicación funcione perfectamente, solo tienes que asegurarte de cumplir un par de requisitos. ¡Del resto se encarga la propia app!

Requisitos Previos (¡Muy importante!)
Para que la instalación automática funcione, tu sistema necesita:

Java 21 o superior: La aplicación está construida sobre la plataforma Java.
Python 3.12 o superior: Necesitas tener Python instalado y añadido al PATH de tu sistema.
¿Por qué? SmartSave necesita esta versión para una función de su script que se conecta a la API de Mercadona. Además, necesita poder ejecutar el comando pip.
Instalación de Dependencias
¡No tienes que hacer nada!

Al arrancar, SmartSave intentará instalar automáticamente las librerías de Python (mercapy y requests) que necesita. Si tienes Python y una conexión a internet, la aplicación se configurará sola.

Si por algún motivo la instalación automática falla, la aplicación te mostrará una advertencia. En ese caso, puedes instalar las dependencias manualmente abriendo una terminal en la carpeta de la aplicación y ejecutando:

pip install -r requirements.txt