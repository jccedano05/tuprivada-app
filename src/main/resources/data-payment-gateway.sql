-- Script para configurar cuentas de pasarela de pago para Conekta
-- Este archivo debe ejecutarse después de tener condominios creados

-- Crear cuenta de pasarela para condominio ID 1 (ajustar según tu condominio)
INSERT INTO payment_gateway_accounts (
    id,
    condominium_id,
    provider,
    account_id,
    account_name,
    account_email,
    is_active,
    is_verified,
    onboarding_status,
    capabilities,
    commission_percentage,
    fixed_fee,
    created_at,
    updated_at,
    activated_at
) VALUES (
    1,
    1, -- ID del condominio
    'CONEKTA',
    'acc_2tYqGJBkKqJsVH5Zw', -- ID de cuenta en Conekta (usar tu ID real)
    'Condominio Demo',
    'admin@condominio.com',
    true,
    true,
    'COMPLETED',
    '["card_payments", "oxxo_payments", "spei"]',
    2.9, -- Comisión porcentual
    3.00, -- Tarifa fija
    NOW(),
    NOW(),
    NOW()
) ON CONFLICT (id) DO UPDATE SET
    is_active = EXCLUDED.is_active,
    is_verified = EXCLUDED.is_verified,
    updated_at = NOW();

-- Si tienes más condominios, agregar más registros aquí
-- INSERT INTO payment_gateway_accounts (...) VALUES (...);

-- Verificar que se creó correctamente
SELECT id, condominium_id, provider, account_name, is_active, is_verified 
FROM payment_gateway_accounts 
WHERE provider = 'CONEKTA';
