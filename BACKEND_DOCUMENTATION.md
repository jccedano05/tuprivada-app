# 📚 Documentación Backend - TuPrivada App

## 🏗️ Arquitectura y Tecnologías

### Stack Tecnológico
- **Framework**: Spring Boot 3.2.3
- **Lenguaje**: Java 17
- **Base de Datos**: PostgreSQL
- **ORM**: Spring Data JPA / Hibernate
- **Seguridad**: Spring Security + JWT
- **Gestión de Dependencias**: Maven

### Servicios Externos Integrados
- **AWS S3**: Almacenamiento de archivos e imágenes
- **AWS SES**: Servicio de email
- **Firebase Admin**: Push notifications
- **Stripe**: Pasarela de pagos (versión 28.4.0)
- **Conekta**: Pasarela de pagos alternativa
- **OneSignal**: Push notifications
- **Bucket4j**: Rate limiting

## 🎯 Arquitectura y Patrones

### Patrón de Capas
```
┌─────────────────────────────────────┐
│         Controller Layer            │ ← REST Controllers
├─────────────────────────────────────┤
│          Service Layer              │ ← Business Logic
├─────────────────────────────────────┤
│         Repository Layer            │ ← Data Access
├─────────────────────────────────────┤
│            Model Layer              │ ← Entities
└─────────────────────────────────────┘
```

### Patrones Implementados
1. **Repository Pattern**: Para acceso a datos
2. **DTO Pattern**: Transferencia de datos entre capas
3. **Service Pattern**: Lógica de negocio encapsulada
4. **Facade Pattern**: UserFacade para operaciones complejas
5. **Strategy Pattern**: Para diferentes pasarelas de pago
6. **Dependency Injection**: IoC con Spring

### Estructura del Proyecto
```
src/main/java/com/jccv/tuprivadaapp/
├── configuration/      # Configuraciones (Security, AWS, CORS)
├── controller/         # REST Controllers
│   ├── admin/
│   ├── amenity/
│   ├── charge/
│   ├── condominium/
│   ├── event/
│   ├── finance/
│   ├── notice/
│   ├── payment/
│   │   └── gateway/
│   └── resident/
├── dto/               # Data Transfer Objects
├── exception/         # Excepciones personalizadas
├── jwt/              # Configuración JWT
├── model/            # Entidades JPA
├── repository/       # Repositorios JPA
├── service/          # Servicios de negocio
│   └── implementation/
├── scheduler/        # Tareas programadas
└── utils/           # Utilidades
```

## 🔒 Seguridad y Autenticación

### JWT Configuration
- **Secret Key**: Configurada en application.properties
- **Expiration Time**: 24 horas
- **Header**: Authorization Bearer {token}

### Niveles de Autorización
```java
MAX_LEVEL = "hasAnyRole('ADMIN', 'USER', 'RESIDENT')"
CONDOMINIUM_LEVEL = "hasAnyRole('ADMIN', 'RESIDENT')"  
USER_LEVEL = "hasAnyRole('RESIDENT')"
```

### Rate Limiting
- **Login**: 50 solicitudes cada 10 minutos
- **Register**: 10 solicitudes cada 15 minutos
- **Validate Token**: 100 solicitudes cada 5 minutos
- **Reset Password**: 60 solicitudes cada 30 minutos

## 📡 API Endpoints

### Base URL
```
http://localhost:8080
Production: https://api.tuprivada.com
```

### Configuración de Headers
```
Content-Type: application/json
Authorization: Bearer {jwt_token}
```

## 📋 Endpoints Principales

### 🔐 Authentication (`/auth`)

#### Login
```http
POST /auth/login
```
**Request:**
```json
{
    "username": "user@example.com",
    "password": "password123"
}
```
**Response:**
```json
{
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "username": "johndoe",
    "role": "RESIDENT",
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "condominium": {
        "id": 1,
        "name": "Condominio Vista Hermosa"
    },
    "phone": "1234567890",
    "bankPersonalReference": "REF123456"
}
```

#### Register User
```http
POST /auth/registerUser
```
**Request:**
```json
{
    "email": "newuser@example.com",
    "password": "securePassword123",
    "firstName": "Jane",
    "lastName": "Smith",
    "username": "janesmith",
    "phone": "9876543210",
    "condominiumId": 1
}
```

#### Forgot Password
```http
POST /auth/forgot-password?email={email}
```

#### Validate Reset Token
```http
GET /auth/validate-reset-token?token={token}
```

#### Reset Password
```http
POST /auth/reset-password?token={token}
```
**Request:**
```json
{
    "newPassword": "newSecurePassword123",
    "confirmPassword": "newSecurePassword123"
}
```

### 🏘️ Condominiums (`/condominiums`)

#### Get All Condominiums
```http
GET /condominiums
Authorization: Required (ADMIN)
```

#### Get Condominium by ID
```http
GET /condominiums/{id}
Authorization: Required (RESIDENT, ADMIN)
```

#### Create Condominium
```http
POST /condominiums
Authorization: Required (ADMIN)
```
**Request:**
```json
{
    "name": "Condominio Las Flores",
    "address": {
        "street": "Av. Principal 123",
        "city": "Ciudad",
        "state": "Estado",
        "zipCode": "12345",
        "country": "México"
    },
    "totalHouses": 150,
    "adminEmail": "admin@condominio.com"
}
```

#### Update Condominium
```http
PUT /condominiums/{id}
Authorization: Required (ADMIN)
```

#### Delete Condominium
```http
DELETE /condominiums/{id}
Authorization: Required (ADMIN)
```

### 💰 Payments (`/payments`)

#### Create Payment
```http
POST /payments
Authorization: Required (ADMIN, RESIDENT)
```
**Request:**
```json
{
    "chargeId": 123,
    "residentId": 456,
    "amount": 500.00,
    "paymentDate": "2024-01-26T10:00:00",
    "isPaid": false,
    "description": "Cuota de mantenimiento enero"
}
```

#### Get Payment by ID
```http
GET /payments/{id}
Authorization: Required (RESIDENT, ADMIN)
```

#### Get Unpaid Payments for Resident
```http
GET /payments/resident/{residentId}/unpaid?page=0&size=10
Authorization: Required (RESIDENT, ADMIN)
```
**Response:**
```json
{
    "content": [
        {
            "paymentId": 1,
            "chargeId": 100,
            "residentId": 456,
            "amount": 500.00,
            "titleTypePayment": "Mantenimiento",
            "description": "Cuota mensual",
            "chargeDate": "2024-01-01T00:00:00",
            "dueDate": "2024-01-15T23:59:59",
            "isPaid": false,
            "penaltyValue": 50.00
        }
    ],
    "totalElements": 5,
    "totalPages": 1,
    "size": 10,
    "number": 0
}
```

#### Get Paid Payments with Deposits
```http
GET /payments/resident/{residentId}/paid-with-deposits-v2?page=0&size=10
Authorization: Required (RESIDENT, ADMIN)
```

#### Update Payment Status
```http
PUT /payments/charges/{chargeId}/residents/{residentId}/updateIsPaidStatusV2
Authorization: Required (ADMIN, RESIDENT)
```
**Request:**
```json
{
    "isPaid": true,
    "datePaid": "2024-01-26T15:30:00",
    "paymentMethod": "TRANSFER",
    "referenceNumber": "REF123456",
    "amount": 500.00
}
```

#### Get Payment Summary
```http
GET /payments/resident/{residentId}/summary
Authorization: Required (RESIDENT, ADMIN)
```
**Response:**
```json
{
    "totalBalance": 5000.00,
    "totalPaid": 3000.00,
    "totalPending": 2000.00,
    "lastPaymentDate": "2024-01-15T10:30:00"
}
```

#### Delete Payment
```http
DELETE /payments/charges/{chargeId}/residents/{residentId}
Authorization: Required (ADMIN)
```

### 💸 Charges (`/api/charges`)

#### Apply Charge to Specific Residents
```http
POST /api/charges/apply
Authorization: Required (ADMIN)
```
**Request:**
```json
{
    "titleTypePayment": "Mantenimiento",
    "description": "Cuota mensual de mantenimiento",
    "amount": 500.00,
    "chargeDate": "2024-01-01T00:00:00",
    "dueDate": "2024-01-15T23:59:59",
    "penaltyType": "FIXED_AMOUNT",
    "penaltyValue": 50.00,
    "condominiumId": 1,
    "residentIds": [1, 2, 3, 4, 5]
}
```

#### Apply Charge to All Residents
```http
POST /api/charges/apply/all
Authorization: Required (ADMIN)
```
**Request:** Same as above but applies to all residents in condominium

#### Get Charges by Condominium
```http
GET /api/charges/condominium/{condominiumId}?startDate=2024-01-01&endDate=2024-12-31
Authorization: Required (ADMIN, RESIDENT)
```

#### Update Charge
```http
PUT /api/charges/{chargeId}
Authorization: Required (ADMIN)
```

#### Delete Charge
```http
DELETE /api/charges/{chargeId}
Authorization: Required (ADMIN)
```

#### Get Annual Summary
```http
GET /api/charges/annual-summary/{condominiumId}?year=2024
Authorization: Required (ADMIN)
```

### 🏠 Residents (`/residents`)

#### Get All Residents by Condominium
```http
GET /residents/condominiums/{condominiumId}
Authorization: Required (ADMIN, RESIDENT)
```

#### Get Resident by ID
```http
GET /residents/{id}
Authorization: Required (ADMIN, RESIDENT)
```

#### Create Resident
```http
POST /residents
Authorization: Required (ADMIN)
```
**Request:**
```json
{
    "firstName": "Carlos",
    "lastName": "González",
    "email": "carlos@example.com",
    "phone": "5551234567",
    "houseNumber": "A-101",
    "condominiumId": 1,
    "userId": 123,
    "isActive": true,
    "moveInDate": "2024-01-01T00:00:00"
}
```

#### Update Resident
```http
PUT /residents/{id}
Authorization: Required (ADMIN)
```

#### Delete Resident
```http
DELETE /residents/{id}
Authorization: Required (ADMIN)
```

#### Get Resident by User ID
```http
GET /residents/users/{userId}
Authorization: Required (ADMIN, RESIDENT)
```

### 💳 Payment Gateway (`/api/v1/payment-gateway`)

#### Initiate Payment (CONEKTA)
```http
POST /api/v1/payment-gateway/initiate
Authorization: Required (RESIDENT, ADMIN)
```
**Request:**
```json
{
    "paymentId": 123,
    "paymentMethod": "OXXO",
    "email": "resident@example.com",
    "provider": "CONEKTA",
    "expiresInDays": 3,
    "ipAddress": "192.168.1.1"
}
```
**Response:**
```json
{
    "transactionReference": "TXN-123456789",
    "gatewayReference": "CONEKTA-REF-123",
    "status": "PENDING",
    "paymentMethod": "OXXO",
    "amount": 500.00,
    "currency": "MXN",
    "oxxoReference": "98765432109876543210",
    "expiresAt": "2024-01-29T23:59:59",
    "createdAt": "2024-01-26T10:00:00"
}
```

#### Get Transaction Status
```http
GET /api/v1/payment-gateway/payment/{transactionReference}/status
Authorization: Required (RESIDENT, ADMIN)
```

#### Confirm Payment
```http
POST /api/v1/payment-gateway/payment/{transactionReference}/confirm
Authorization: Required (RESIDENT, ADMIN)
```

### 📊 Finances (`/finances`)

#### Create Finance Record
```http
POST /finances
Authorization: Required (ADMIN)
```
**Request:**
```json
{
    "condominiumId": 1,
    "month": "ENERO",
    "year": 2024,
    "totalIncome": 50000.00,
    "totalExpenses": 35000.00,
    "balance": 15000.00,
    "details": [
        {
            "category": "MAINTENANCE",
            "description": "Mantenimiento general",
            "amount": 10000.00,
            "type": "EXPENSE"
        }
    ]
}
```

#### Get Finance by ID
```http
GET /finances/{id}
Authorization: Required (ADMIN, RESIDENT)
```

#### Get Finances by Condominium (Paginated)
```http
GET /finances/condominiums/{condominiumId}?page=0&size=10
Authorization: Required (ADMIN, RESIDENT)
```

#### Get Latest Finance Summary
```http
GET /finances/condominiums/{condominiumId}/latest
Authorization: Required (ADMIN, RESIDENT)
```

#### Get Annual Finances
```http
GET /finances/annual/{condominiumId}
Authorization: Required (ADMIN, RESIDENT)
```

### 📅 Events (`/events`)

#### Create Event
```http
POST /events
Authorization: Required (ADMIN)
```
**Request:**
```json
{
    "title": "Junta de vecinos",
    "description": "Reunión mensual",
    "eventDate": "2024-02-01T18:00:00",
    "location": "Salón de eventos",
    "condominiumId": 1,
    "isRecurring": false,
    "eventType": "MEETING"
}
```

#### Update Event
```http
PUT /events/{id}
Authorization: Required (ADMIN)
```

#### Delete Event
```http
DELETE /events/{id}
Authorization: Required (ADMIN)
```

#### Get Events by Month
```http
GET /events/condominiums/{condominiumId}/month/{month}/year/{year}
Authorization: Required (RESIDENT, ADMIN)
```

#### Get Next Events
```http
GET /events/next/{condominiumId}
Authorization: Required (RESIDENT, ADMIN)
```

### 🏊 Amenities (`/amenities`)

#### Get All Amenities by Condominium
```http
GET /amenities/condominium/{condominiumId}
Authorization: Required (RESIDENT, ADMIN)
```

#### Create Amenity
```http
POST /amenities
Authorization: Required (ADMIN)
```
**Request:**
```json
{
    "name": "Alberca",
    "description": "Alberca principal",
    "capacity": 50,
    "openTime": "08:00",
    "closeTime": "20:00",
    "condominiumId": 1,
    "isActive": true,
    "rules": "No correr, No clavados",
    "pricePerHour": 100.00
}
```

### 🎫 Reservations (`/api/reservations`)

#### Create Reservation
```http
POST /api/reservations
Authorization: Required (RESIDENT, ADMIN)
```
**Request:**
```json
{
    "residentId": 123,
    "amenityId": 456,
    "reservationDate": "2024-02-01",
    "startTime": "14:00",
    "endTime": "16:00",
    "numberOfGuests": 5,
    "notes": "Cumpleaños infantil"
}
```

#### Get Active Reservations by Resident
```http
GET /api/reservations/resident/{residentId}/active
Authorization: Required (RESIDENT, ADMIN)
```

#### Get Reservations by Condominium and Month
```http
GET /api/reservations/condominium/{condominiumId}/active/{year}/{month}
Authorization: Required (ADMIN)
```

#### Cancel Reservation
```http
DELETE /api/reservations/{id}
Authorization: Required (RESIDENT, ADMIN)
```

### 📢 Notices (`/notices`)

#### Create Notice
```http
POST /notices
Authorization: Required (ADMIN)
```
**Request:**
```json
{
    "title": "Corte de agua programado",
    "content": "Se suspenderá el servicio de agua el día...",
    "priority": "HIGH",
    "category": "MAINTENANCE",
    "condominiumId": 1,
    "publishDate": "2024-01-26T08:00:00",
    "expiryDate": "2024-01-27T23:59:59"
}
```

#### Get Active Notices by Condominium
```http
GET /notices/condominium/{condominiumId}/active
Authorization: Required (RESIDENT, ADMIN)
```

### 📱 Push Notifications

#### Send Notification to Resident
```http
POST /notifications/send/resident/{residentId}
Authorization: Required (ADMIN)
```
**Request:**
```json
{
    "title": "Nuevo aviso",
    "message": "Tienes un nuevo aviso del condominio",
    "data": {
        "type": "NOTICE",
        "id": "123"
    }
}
```

#### Send Notification to All Residents
```http
POST /notifications/send/condominium/{condominiumId}
Authorization: Required (ADMIN)
```

### 📁 File Upload (AWS S3)

#### Get Presigned URL for Upload
```http
GET /files/presigned-url?fileName=document.pdf&fileType=application/pdf
Authorization: Required (RESIDENT, ADMIN)
```
**Response:**
```json
{
    "uploadUrl": "https://s3.amazonaws.com/bucket/...",
    "fileKey": "documents/2024/01/document-123456.pdf",
    "expiresIn": 3600
}
```

#### Delete File
```http
DELETE /files/{fileKey}
Authorization: Required (ADMIN)
```

## 🗄️ Base de Datos

### Entidades Principales

#### User
- id (Long)
- email (String, unique)
- password (String, encrypted)
- firstName (String)
- lastName (String)
- username (String, unique)
- role (Enum: ADMIN, RESIDENT, USER)
- isActive (Boolean)
- phone (String)
- createdAt (LocalDateTime)
- updatedAt (LocalDateTime)

#### Condominium
- id (Long)
- name (String)
- address (Address, embedded)
- totalHouses (Integer)
- adminEmail (String)
- isActive (Boolean)
- createdAt (LocalDateTime)

#### Resident
- id (Long)
- user (User, OneToOne)
- condominium (Condominium, ManyToOne)
- houseNumber (String)
- moveInDate (LocalDateTime)
- moveOutDate (LocalDateTime, nullable)
- isActive (Boolean)
- balance (BigDecimal)

#### Payment
- id (Long)
- charge (Charge, ManyToOne)
- resident (Resident, ManyToOne)
- amount (BigDecimal)
- paymentDate (LocalDateTime)
- isPaid (Boolean)
- datePaid (LocalDateTime, nullable)
- paymentMethod (String)
- referenceNumber (String)

#### Charge
- id (Long)
- condominium (Condominium, ManyToOne)
- titleTypePayment (String)
- description (String)
- amount (BigDecimal)
- chargeDate (LocalDateTime)
- dueDate (LocalDateTime)
- penaltyType (Enum: FIXED_AMOUNT, PERCENTAGE)
- penaltyValue (BigDecimal)
- isActive (Boolean)
- payments (List<Payment>, OneToMany)

#### PaymentTransaction (Gateway)
- id (Long)
- transactionReference (String, unique)
- gatewayReference (String)
- payment (Payment, ManyToOne)
- provider (Enum: STRIPE, CONEKTA)
- status (Enum: PENDING, PROCESSING, SUCCEEDED, FAILED, CANCELLED)
- paymentMethod (String)
- amount (BigDecimal)
- currency (String)
- metadata (JSON)
- createdAt (LocalDateTime)
- updatedAt (LocalDateTime)
- completedAt (LocalDateTime, nullable)

## 🚀 Despliegue y Configuración

### Variables de Entorno Requeridas

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/tuprivada
spring.datasource.username=postgres
spring.datasource.password=password

# JWT
jwt.secret.key=your-secret-key-here
jwt.expiration=86400000

# AWS S3
aws.access.key=AWS_ACCESS_KEY
aws.secret.key=AWS_SECRET_KEY
aws.s3.bucket=tuprivada-files
aws.region=us-east-1

# AWS SES
aws.ses.from.email=noreply@tuprivada.com

# Firebase
firebase.config.path=path/to/firebase-config.json

# Stripe
stripe.api.key=sk_test_...

# Conekta
conekta.private.key=key_...
conekta.public.key=key_...

# OneSignal
onesignal.app.id=your-app-id
onesignal.rest.api.key=your-api-key
```

### Docker Deployment

```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
    depends_on:
      - postgres
  
  postgres:
    image: postgres:14
    environment:
      - POSTGRES_DB=tuprivada
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=password
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

volumes:
  postgres_data:
```

### Build Commands

```bash
# Build JAR
mvn clean package

# Run locally
java -jar target/tuprivada-app-0.0.1-SNAPSHOT.jar

# Run with specific profile
java -jar -Dspring.profiles.active=prod target/tuprivada-app-0.0.1-SNAPSHOT.jar

# Docker build
docker build -t tuprivada-app .

# Docker run
docker run -p 8080:8080 tuprivada-app
```

## 📈 Monitoreo y Logging

### Logging Configuration
- **Framework**: SLF4J con Logback
- **Niveles**: ERROR, WARN, INFO, DEBUG
- **Archivos**: Rotación diaria, máximo 30 días

### Health Check Endpoint
```http
GET /actuator/health
```

### Metrics Endpoint
```http
GET /actuator/metrics
```

## 🔧 Mantenimiento

### Tareas Programadas (Scheduler)
- Limpieza de tokens expirados: Diaria a las 2 AM
- Generación de reportes: Mensual, día 1 a las 3 AM
- Backup de base de datos: Diaria a las 1 AM

### Índices de Base de Datos Recomendados
```sql
CREATE INDEX idx_payment_resident_id ON payment(resident_id);
CREATE INDEX idx_payment_charge_id ON payment(charge_id);
CREATE INDEX idx_payment_is_paid ON payment(is_paid);
CREATE INDEX idx_charge_condominium_id ON charge(condominium_id);
CREATE INDEX idx_resident_condominium_id ON resident(condominium_id);
CREATE INDEX idx_transaction_reference ON payment_transaction(transaction_reference);
CREATE INDEX idx_transaction_status ON payment_transaction(status);
```

## 🛡️ Mejores Prácticas de Seguridad

1. **Validación de Entrada**: Usar @Valid y DTOs para validación
2. **SQL Injection**: Usar consultas parametrizadas con JPA
3. **XSS Protection**: Sanitizar toda entrada de usuario
4. **CORS**: Configurar dominios permitidos específicamente
5. **Rate Limiting**: Implementado con Bucket4j
6. **Encriptación**: BCrypt para passwords, AES para datos sensibles
7. **HTTPS**: Obligatorio en producción
8. **Auditoría**: Log de todas las operaciones críticas

## 📝 Notas Importantes

1. **Transacciones**: Todas las operaciones de pago son transaccionales
2. **Soft Delete**: Implementado para mantener histórico
3. **Timezone**: Todos los timestamps en UTC
4. **Paginación**: Por defecto 10 elementos, máximo 100
5. **Caché**: Implementar Redis para mejorar performance
6. **Backup**: Configurar backup automático de BD
7. **Monitoring**: Implementar APM (Application Performance Monitoring)
