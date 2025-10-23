# 🏨 API de Reservas - Spring Boot

Este proyecto es una API REST desarrollada con Spring Boot, que permite gestionar reservas con autenticación y control de acceso por roles USER y ADMIN. 
Está pensada para su despliegue en Render.

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


## 🧩 Buenas prácticas aplicadas

- Implementación de principios **SOLID**.  
- Enfoque en **Clean Code**: código legible, mantenible y con responsabilidades claras.  
- Separación de capas (Controller, Service, Repository, DTOs, Model, Helpers y validaciones personalizadas).  
- Pruebas unitarias y de integración para asegurar la calidad del software.  
