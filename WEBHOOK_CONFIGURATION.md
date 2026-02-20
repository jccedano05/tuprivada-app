# Configuración de Webhooks de Conekta

## URL del Webhook

El webhook debe configurarse en el dashboard de Conekta con la siguiente URL:

```
https://tu-dominio.com/api/v1/webhooks/conekta
```

**Importante:** Reemplaza `tu-dominio.com` con tu dominio real.

## Eventos a Suscribir

Configura los siguientes eventos en Conekta para recibir notificaciones de cambios de estado:

### Eventos Críticos (Obligatorios)
- `order.paid` - Cuando un pago se completa exitosamente (OXXO o tarjeta)
- `charge.paid` - Alternativa para pagos completados

### Eventos Recomendados
- `order.pending_payment` - Cuando se genera una ficha OXXO
- `order.expired` - Cuando expira una orden de pago
- `order.canceled` - Cuando se cancela una orden
- `charge.refunded` - Cuando se hace un reembolso

## Validación de Firma

Por seguridad, el webhook valida la firma de Conekta. Actualmente está **desactivada** para pruebas locales.

Para activar la validación en producción:

1. Ve a `application-conekta.properties`
2. Cambia `conekta.skipWebhookSignatureValidation=false`
3. Configura tu `conekta.webhookKey` con la clave secreta de Conekta

## Flujo de Pago OXXO

1. Usuario solicita pago OXXO desde el frontend
2. Backend crea orden en Conekta y retorna ficha OXXO
3. Usuario paga en tienda OXXO
4. **Conekta envía webhook `order.paid`**
5. Backend actualiza Payment como pagado automáticamente
6. Frontend se actualiza en el siguiente refresh o polling

## Problema Resuelto

**Antes:** El Payment no se actualizaba en la base de datos después del webhook porque faltaba guardar explícitamente con `paymentRepository.save(payment)`.

**Ahora:** El Payment se guarda correctamente en `handleOrderPaid()` y `handleChargeRefunded()`.

## Testing del Webhook

### Localmente con ngrok

```bash
# Instalar ngrok
brew install ngrok

# Exponer tu servidor local
ngrok http 8080

# Usar la URL de ngrok en Conekta
https://xxxx-xx-xx-xx-xx.ngrok.io/api/v1/webhooks/conekta
```

### Simular Webhook Manualmente

```bash
curl -X POST http://localhost:8080/api/v1/webhooks/conekta \
  -H "Content-Type: application/json" \
  -d '{
    "type": "order.paid",
    "data": {
      "object": {
        "id": "ord_test_123",
        "metadata": {
          "transactionReference": "TPAY-20260126134908-SHJ78M"
        }
      }
    }
  }'
```

## Logs Importantes

Busca estos logs para verificar que el webhook funciona:

```
[ConektaWebhook] Webhook recibido
[Conekta] Procesando evento: order.paid
[Conekta] Payment ID X marcado como pagado automáticamente
[ConektaWebhook] Webhook procesado exitosamente
```

## Troubleshooting

### El pago OXXO no se actualiza

1. Verifica que el webhook esté configurado en Conekta
2. Revisa los logs del backend para ver si llegó el webhook
3. Verifica que la transacción tenga `transactionReference` en metadata
4. Confirma que el Payment existe en la base de datos

### Error "Transacción no encontrada"

El webhook busca la transacción por `transactionReference` en metadata. Asegúrate de que:
- El metadata se envía correctamente al crear la orden
- El campo se llama exactamente `transactionReference`
- La transacción existe en la tabla `payment_transactions`
