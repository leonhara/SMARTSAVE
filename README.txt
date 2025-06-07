# SmartSave - Instrucciones de Instalación y Ejecución

Gracias por usar SmartSave! Para que la aplicación funcione correctamente hay que seguir estos pasos.

## Requisitos Previos

1.  Java: Necesitas tener Java (JDK) 21 o superior instalado.

2.  Python: Necesitas Python 3.13 o superior. Para que la api funcione bien es totalmente necesario este paso. 
    ```

## Pasos de Instalación de Dependencias de Python

1.  **Navega a la carpeta de distribución**: Una vez que hayas construido y descomprimido el paquete de SmartSave (o si estás ejecutando desde el JAR directamente y tienes el archivo `requirements.txt` al lado), abre una terminal o `cmd` en esa carpeta.
2.  **Instalar librerías de Python**: Ejecuta el siguiente comando para instalar las dependencias necesarias:
    ```
    pip install -r requirements.txt
    ```
    Este comando instalará `mercapy==1.0.3` y `requests>=2.25.0`.

## Ejecución de la Aplicación

Una vez completados los pasos anteriores, puedes ejecutar la aplicación con el siguiente comando desde la carpeta donde se encuentra el archivo JAR: