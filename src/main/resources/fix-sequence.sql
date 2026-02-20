-- Script para resetear la secuencia de payment_gateway_accounts
-- Ejecutar este script cuando aparezca el error "duplicate key value violates unique constraint"

-- Resetear la secuencia al máximo ID actual + 1
SELECT setval('payment_gateway_accounts_id_seq', 
    COALESCE((SELECT MAX(id) FROM payment_gateway_accounts), 0) + 1, 
    false);

-- Verificar el valor actual de la secuencia
SELECT currval('payment_gateway_accounts_id_seq') as current_sequence_value;

-- Verificar el máximo ID en la tabla
SELECT MAX(id) as max_id FROM payment_gateway_accounts;
