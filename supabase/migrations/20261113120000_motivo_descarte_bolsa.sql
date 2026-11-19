-- Guarda a justificativa de cada bolsa descartada para consulta e auditoria operacional.
alter table public.bolsa_hemocomponente
    add column motivo_descarte varchar(500);
