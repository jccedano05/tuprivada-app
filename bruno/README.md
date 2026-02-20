# TuPrivada API - Colección Bruno

Esta es la colección de Bruno para la API de TuPrivada. Bruno es un cliente API moderno que usa archivos de texto plano, ideal para control de versiones.

## 📁 Estructura

```
bruno/
├── bruno.json                    # Configuración de la colección
├── environments/                 # Entornos de ejecución
│   ├── local.bru                # Desarrollo local (localhost:8080)
│   ├── dev.bru                  # Ambiente de desarrollo
│   └── prod.bru                 # Ambiente de producción
├── Authentication/              # Endpoints de autenticación
├── Payment Gateway/             # Pasarela de pagos (Conekta)
├── Payment Gateway Accounts/    # Gestión de cuentas de pasarela
├── Webhooks/                    # Webhooks de pasarelas
├── Amenities/                   # Amenidades del condominio
├── Reservations/                # Reservas de amenidades
├── Residents/                   # Gestión de residentes
├── Contacts/                    # Contactos
├── Users/                       # Administración de usuarios
└── Stripe/                      # Integración con Stripe
```

## 🚀 Inicio Rápido

### 1. Instalar Bruno

```bash
# macOS
brew install bruno

# O descarga desde: https://www.usebruno.com/downloads
```

### 2. Abrir la Colección

1. Abre Bruno
2. Click en "Open Collection"
3. Selecciona la carpeta `bruno/` de este proyecto

### 3. Configurar Ambiente

1. Selecciona el ambiente apropiado (local/dev/prod)
2. Actualiza las variables según tu configuración:
   - `base_url`: URL base de la API
   - `auth_token`: Token JWT (se genera automáticamente al hacer login)
   - `condominium_id`: ID del condominio
   - `resident_id`: ID del residente
   - `user_id`: ID del usuario

### 4. Autenticación

1. Ve a la carpeta **Authentication**
2. Ejecuta el request **Login**
3. El token se guardará automáticamente en `auth_token`
4. Todos los demás requests usarán este token automáticamente

## 📝 Uso de Variables

Las variables de ambiente se usan con la sintaxis `{{variable_name}}`:

```
{{base_url}}/api/{{api_version}}/payment-gateway/health
```

### Variables Disponibles

- `base_url`: URL base de la API
- `api_version`: Versión de la API (v1)
- `auth_token`: Token JWT de autenticación
- `condominium_id`: ID del condominio actual
- `resident_id`: ID del residente actual
- `user_id`: ID del usuario actual

## 🔐 Autenticación

La mayoría de los endpoints requieren autenticación Bearer Token:

```
Authorization: Bearer {{auth_token}}
```

El token se obtiene automáticamente al ejecutar el endpoint de Login.

## 📦 Módulos Principales

### Authentication
- Login
- Register User
- Validate Token

### Payment Gateway
- Create Payment Intent
- Confirm Payment
- Get Transaction Status
- Refund Payment
- Initiate Payment
- Get Payment Transactions (paginado)
- Get Transaction Details
- Download Receipt (PDF)
- Health Check

### Payment Gateway Accounts
- Create Gateway Account (Admin)
- Update Gateway Account (Admin)
- Get Gateway Account (Admin)

### Webhooks
- Conekta Webhook (recibe eventos de Conekta)
- Conekta Webhook Verify

### Amenities
- CRUD completo de amenidades

### Reservations
- CRUD de reservas de amenidades

### Residents
- Gestión de residentes
- Consulta de balances

### Contacts
- Gestión de contactos de residentes y condominios

### Users
- Administración de usuarios

### Stripe
- Gestión de cuentas Stripe Connect

## 🧪 Testing

Para probar los endpoints:

1. **Autenticación**: Ejecuta Login primero
2. **Variables**: Actualiza los IDs según tu base de datos
3. **Orden**: Algunos endpoints dependen de otros (ej: crear antes de actualizar)

## 📚 Documentación Adicional

- [Documentación de Conekta](https://developers.conekta.com/)
- [Documentación de Stripe](https://stripe.com/docs)
- [Bruno Documentation](https://docs.usebruno.com/)

## 🔧 Troubleshooting

### Token Expirado
Si recibes error 401, ejecuta Login nuevamente para obtener un nuevo token.

### Variables No Definidas
Asegúrate de seleccionar el ambiente correcto y que las variables estén configuradas.

### CORS Errors
Si estás en local, verifica que el backend tenga CORS habilitado para `localhost`.

## 📞 Soporte

Para reportar problemas o sugerencias, contacta al equipo de desarrollo.
