# Guía de Kafka en TuPrivada App

## 📋 Resumen

Esta aplicación ahora usa **Apache Kafka** para manejar eventos de pagos de forma asíncrona y desacoplada. Cuando un pago se completa exitosamente (vía Stripe), se publica un evento a Kafka y un consumidor se encarga de enviar las notificaciones (push + email).

## 🎯 Casos de Uso Implementados

### 1. Flujo de Pago Completado (Stripe)
1. Usuario paga con Stripe (tarjeta/OXXO)
2. Webhook de Stripe notifica a `StripePaymentsServiceImp`
3. Se marca el pago como completado en BD
4. Se publica `PaymentCompletedEvent` al topic `payments.completed` en Kafka
5. `PaymentEventsNotificationConsumer` consume el evento
6. Se envían push notification y email al usuario

### 2. Flujo de Cargo Creado
1. Admin crea y aplica un cargo a residentes
2. `ChargeController.applyCharge` crea el cargo en BD
3. Se publica `ChargeCreatedEvent` al topic `charge.created` en Kafka
4. `NotificationEventsConsumer` consume el evento
5. Se envían push notifications a todos los residentes afectados

### 3. Flujo de Pago Marcado Como Pagado (Manual)
1. Admin marca un pago como pagado desde el panel
2. `PaymentServiceImp.updateIsPaidStatusV2` actualiza el pago en BD
3. Se publica `PaymentMarkedAsPaidEvent` al topic `payment.marked.paid` en Kafka
4. `NotificationEventsConsumer` consume el evento
5. Se envía push notification al residente

## 🚀 Cómo Ejecutar Kafka Localmente

### Paso 1: Levantar Kafka con Docker

En la raíz del proyecto:

```bash
docker compose up -d zookeeper kafka
```

Verificar que estén corriendo:

```bash
docker ps
```

Deberías ver los contenedores `zookeeper` y `kafka` activos.

### Paso 2: Activar el Perfil de Kafka en Spring Boot

En tu configuración de ejecución (IDE o línea de comandos), activa el perfil `kafka`:

**Opción A - Variables de entorno:**
```bash
export SPRING_PROFILES_ACTIVE=kafka
./mvnw spring-boot:run
```

**Opción B - Parámetro al ejecutar:**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=kafka
```

**Opción C - En tu IDE:**
- IntelliJ: Run > Edit Configurations > Active profiles: `kafka`
- Eclipse: Run As > Run Configurations > Arguments > Program arguments: `--spring.profiles.active=kafka`

### Paso 3: Verificar que Todo Funciona

Revisa los logs de tu app. Deberías ver algo como:

```
INFO  o.a.k.c.c.ConsumerConfig - ConsumerConfig values: bootstrap.servers = [localhost:9092]
INFO  o.s.k.l.KafkaMessageListenerContainer - Container ... started
INFO  PaymentEventProducer - Evento PaymentCompletedEvent publicado a Kafka para paymentId=...
INFO  PaymentEventsNotificationConsumer - Recibido PaymentCompletedEvent desde Kafka...
```

## 📂 Archivos Creados/Modificados

### Nuevos archivos - Eventos:

1. **`/src/main/java/com/jccv/tuprivadaapp/dto/events/payment/PaymentCompletedEvent.java`**
   - Evento de pago completado (Stripe). Contiene datos del pago, usuario, monto, etc.

2. **`/src/main/java/com/jccv/tuprivadaapp/dto/events/payment/PaymentMarkedAsPaidEvent.java`**
   - Evento de pago marcado como pagado manualmente por admin

3. **`/src/main/java/com/jccv/tuprivadaapp/dto/events/charge/ChargeCreatedEvent.java`**
   - Evento de cargo creado/aplicado a residentes

### Nuevos archivos - Productores:

4. **`/src/main/java/com/jccv/tuprivadaapp/messaging/payment/PaymentEventProducer.java`**
   - Productor específico para eventos de pago completado (Stripe)

5. **`/src/main/java/com/jccv/tuprivadaapp/messaging/notification/NotificationEventProducer.java`**
   - Productor genérico para eventos de notificaciones (cargos y pagos manuales)

### Nuevos archivos - Consumidores:

6. **`/src/main/java/com/jccv/tuprivadaapp/messaging/payment/PaymentEventsNotificationConsumer.java`**
   - Consumidor para `payments.completed`: envía push + email

7. **`/src/main/java/com/jccv/tuprivadaapp/messaging/notification/NotificationEventsConsumer.java`**
   - Consumidor genérico para `charge.created` y `payment.marked.paid`: envía push notifications

### Nuevos archivos - Configuración:

8. **`/src/main/resources/application-kafka.yml`**
   - Configuración completa de Kafka (bootstrap servers, serializadores, deserializadores, type mappings)

### Tests unitarios (17 tests, 100% passing):

9. **`/src/test/java/com/jccv/tuprivadaapp/messaging/payment/PaymentEventProducerTest.java`** (3 tests)
10. **`/src/test/java/com/jccv/tuprivadaapp/messaging/payment/PaymentEventsNotificationConsumerTest.java`** (3 tests)
11. **`/src/test/java/com/jccv/tuprivadaapp/messaging/notification/NotificationEventProducerTest.java`** (5 tests)
12. **`/src/test/java/com/jccv/tuprivadaapp/messaging/notification/NotificationEventsConsumerTest.java`** (6 tests)

### Archivos modificados:

13. **`docker-compose.yaml`**
    - Añadidos servicios `zookeeper` y `kafka`

14. **`StripePaymentsServiceImp.java`**
    - Inyectado `PaymentEventProducer`
    - Publica `PaymentCompletedEvent` cuando un pago se completa exitosamente

15. **`ChargeController.java`**
    - Inyectado `NotificationEventProducer`
    - Publica `ChargeCreatedEvent` cuando se crea/aplica un cargo

16. **`PaymentServiceImp.java`**
    - Inyectado `NotificationEventProducer`
    - Publica `PaymentMarkedAsPaidEvent` cuando se marca un pago como pagado manualmente

## 🔍 Conceptos Clave de Kafka (Para Aprender)

### 1. **Topic (Tópico)**
Un "canal" o "categoría" donde se publican mensajes. En nuestra app:
- `payments.completed` - para eventos de pagos completados vía Stripe
- `charge.created` - para eventos de cargos creados/aplicados a residentes
- `payment.marked.paid` - para eventos de pagos marcados como pagados manualmente

### 2. **Producer (Productor)**
Quien **publica** mensajes a un topic. En nuestra app:
- `PaymentEventProducer` publica `PaymentCompletedEvent`

### 3. **Consumer (Consumidor)**
Quien **lee/consume** mensajes de un topic. En nuestra app:
- `PaymentEventsNotificationConsumer` lee de `payments.completed`

### 4. **Consumer Group**
Un identificador que agrupa consumidores. Si varios consumidores tienen el mismo `groupId`, Kafka balancea los mensajes entre ellos (cada mensaje va a uno solo). Útil para escalar.

### 5. **Offset**
Posición del mensaje en el topic. Kafka lleva control de qué offset ya consumió cada consumer group.

### 6. **Serialización/Deserialización**
- **Productor**: convierte objetos Java → JSON (con `JsonSerializer`)
- **Consumidor**: convierte JSON → objetos Java (con `JsonDeserializer`)

## 🛠️ Buenas Prácticas Implementadas

### ✅ 1. Manejo de Excepciones
- El productor captura errores y los loggea, evitando que la app crashee si Kafka falla momentáneamente.
- El consumidor relanza excepciones para que Kafka reintente o envíe a Dead Letter Topic.

### ✅ 2. Logging de Auditoría
- Cada evento publicado loggea: `paymentId`, `eventId`, `topic`, `partition`, `offset`
- Cada evento consumido loggea los mismos datos
- Útil para debugging y auditoría en producción

### ✅ 3. Separación de Responsabilidades (SRP)
- `PaymentEventProducer`: solo se encarga de publicar eventos
- `PaymentEventsNotificationConsumer`: solo se encarga de notificaciones
- `StripePaymentsServiceImp`: maneja la lógica de negocio de pagos

### ✅ 4. Desacoplamiento
- El productor no sabe quién consume los eventos
- Puedes agregar más consumidores sin modificar el productor
- En el futuro, otros microservicios pueden suscribirse al mismo topic

### ✅ 5. Tests Unitarios
- Todos los componentes tienen tests
- Usan mocks para no depender de Kafka real
- Verifican que se llamen los métodos correctos con los parámetros correctos

## 🧪 Ejecutar Tests

```bash
# Solo tests de Kafka
./mvnw test -Dtest=PaymentEventProducerTest,PaymentEventsNotificationConsumerTest

# Todos los tests
./mvnw test
```

## 📊 Monitoreo y Debugging

### Ver logs en tiempo real:

```bash
# Logs de Kafka
docker logs -f kafka

# Logs de tu app (filtrando Kafka)
./mvnw spring-boot:run | grep -i kafka
```

### Herramientas útiles:

**Kafka UI (Opcional):**
Puedes agregar un contenedor UI para ver topics, mensajes, consumer groups:

```yaml
# Agregar a docker-compose.yaml
  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    ports:
      - "8090:8080"
    environment:
      KAFKA_CLUSTERS_0_NAME: local
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:29092
    depends_on:
      - kafka
```

Luego acceder a: http://localhost:8090

## 🔧 Troubleshooting

### Problema: "Connection refused to localhost:9092"

**Causa:** Kafka no está corriendo o no está accesible.

**Solución:**
```bash
docker compose up -d zookeeper kafka
docker ps  # verificar que estén UP
```

### Problema: "No se publican eventos"

**Causa:** El perfil `kafka` no está activo.

**Solución:** Verificar que hayas activado el perfil con `-Dspring.profiles.active=kafka`

### Problema: "Eventos no se consumen"

**Causa:** El consumer no se registró correctamente.

**Solución:** 
1. Revisar logs: busca `KafkaMessageListenerContainer ... started`
2. Verificar que el topic existe (con Kafka UI o comandos de Kafka)

## 🚀 Próximos Pasos (Opcional)

Una vez que domines lo básico, puedes:

1. **Dead Letter Topics (DLT):**
   - Configurar un topic especial para mensajes que fallan después de N reintentos
   - Útil para no perder eventos problemáticos

2. **Más eventos:**
   - `UserRegisteredEvent` - cuando un usuario se registra
   - `ChargeCreatedEvent` - cuando se crea un cargo
   - `DepositReceivedEvent` - cuando se recibe un depósito

3. **Particionamiento:**
   - Usar la clave (`paymentId`) para garantizar orden por usuario/pago

4. **Schema Registry (Avro):**
   - Usar Avro en lugar de JSON para schemas más estrictos y eficientes

5. **Kafka Streams:**
   - Para procesamiento de eventos complejos (agregaciones, transformaciones)

6. **Producción:**
   - Cambiar `bootstrap-servers` a tu cluster de Kafka en AWS/GCP/Azure
   - Configurar seguridad (SSL/SASL)
   - Monitoreo con Prometheus + Grafana

## 📚 Recursos para Aprender Más

- [Spring Kafka Documentation](https://docs.spring.io/spring-kafka/reference/)
- [Kafka: The Definitive Guide](https://www.confluent.io/resources/kafka-the-definitive-guide/)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)

## ✅ Checklist de Verificación

- [x] Kafka y Zookeeper corren en Docker
- [x] Perfil `kafka` activado en Spring Boot
- [x] Tests unitarios pasan (6/6)
- [x] Logs muestran eventos publicados y consumidos
- [x] Push notifications y emails se envían correctamente

---

**¡Listo!** Ya tienes Kafka integrado en tu app. Empieza probando con pagos reales (sandbox de Stripe) y observa los logs para ver el flujo completo.
