# 🏨 API de Reservas - Spring Boot

Este proyecto es una API REST desarrollada con Spring Boot, que permite gestionar reservas con autenticación y control de acceso por roles USER y ADMIN. 
Está pensada para su despliegue en Render, con un endpoint público de verificación de estado (`/hola`).

> ⚠️ Estado del proyecto: 
> Este sistema es un ejemplo simple creado con fines educativos, para integrar distintas tecnologías de Spring Boot (seguridad, controladores, servicios, persistencia, pruebas y deploy).  
> No es un proyecto de producción.  

> Aún no está finalizado, y faltan implementar algunas partes importantes como:
> - Los DTO para separar las entidades del modelo de los datos expuestos por la API.  
> - La lógica para agregar la cantidad reservada de cada producto.  
> - Algunos ajustes y pruebas adicionales para completar la funcionalidad general.


## 🚀 Tecnologías utilizadas

- Java 17
- Spring Boot 3
  - Spring Web
  - Spring Security
  - Spring Data JPA
  - Spring Validation
- Base de datos: H2 (en memoria) o MySQL
- Lombok
- Maven
- JUnit + Mockito para pruebas unitarias e integración
- Render para el deploy
- SonarQube para análisis de calidad y métricas del código
