-- ===================================================================
-- Rota Vital — Carga inicial (seed) com os dados de exemplo do backend.
-- Mesmo conteúdo que hoje está fixo no código (PI3-149):
--   * BancosEmMemoria            -> hemocentro BS-01 e as 6 bolsas do estoque;
--   * RedeDistribuicaoEmMemoria  -> 6 hospitais e as conexões da rede.
--
-- Rodar no SQL Editor do Supabase DEPOIS das 5 migrations de supabase/migrations/.
--
-- Idempotente: pode ser executado mais de uma vez. Linhas que já existem
-- (mesmo id ou mesma chave única) são mantidas como estão — o script nunca
-- sobrescreve dados que a aplicação já alterou (ex.: bolsa RESERVADA).
--
-- As datas das bolsas são relativas ao dia da primeira carga (current_date ± N),
-- como em BancosEmMemoria, para os alertas de vencimento fazerem sentido.
-- ===================================================================

begin;

-- -------------------------------------------------------------------
-- ponto_rede — 1 banco de sangue + 6 hospitais (Recife/PE)
-- O endereço do domínio ("Rua X, 100 - Recife/PE") foi separado em
-- logradouro, cidade e uf, conforme as colunas do DER.
-- -------------------------------------------------------------------
insert into public.ponto_rede (id, tipo, nome, logradouro, cidade, uf, latitude, longitude)
values ('BS-01',          'BANCO_DE_SANGUE', 'Hemope Central',              'Av. Central, 100',                                  'Recife', 'PE', -8.0578, -34.8829),
       ('HOSP-01',        'HOSPITAL',        'Hospital das Clinicas',       'Rua das Flores, 500',                               'Recife', 'PE', -8.0476, -34.8770),
       ('hc-pe',          'HOSPITAL',        'Hospital das Clínicas de PE', 'Av. Prof. Moraes Rego, 1235 - Cidade Universitária', 'Recife', 'PE', -8.0512, -34.9478),
       ('real-portugues', 'HOSPITAL',        'Real Hospital Português',     'Av. Gov. Agamenon Magalhães, 4760 - Paissandu',     'Recife', 'PE', -8.0479, -34.8993),
       ('barao-lucena',   'HOSPITAL',        'Hospital Barão de Lucena',    'Av. Caxangá, 3860 - Iputinga',                      'Recife', 'PE', -8.0432, -34.9331),
       ('getulio-vargas', 'HOSPITAL',        'Hospital Getúlio Vargas',     'R. Cons. Portela, 1034 - Afogados',                 'Recife', 'PE', -8.0812, -34.9127),
       ('upa-norte',      'HOSPITAL',        'UPA Norte - Macaxeira',       'Av. Norte Miguel Arraes, 7200 - Macaxeira',         'Recife', 'PE', -8.0098, -34.9296)
on conflict do nothing;

-- -------------------------------------------------------------------
-- conexao — topologia em estrela a partir do BS-01.
-- Distância/tempo sintéticos, iguais aos de RedeDistribuicaoEmMemoria.
-- Ida e volta são duas linhas (aresta dirigida), como em
-- RedeDistribuicao.adicionarConexao.
-- -------------------------------------------------------------------
insert into public.conexao (origem_id, destino_id, distancia_km, tempo_estimado_min)
select origem_id, destino_id, distancia_km, tempo_estimado_min
from (values ('BS-01', 'HOSP-01',        3.2,  9),
             ('BS-01', 'hc-pe',          7.8, 18),
             ('BS-01', 'real-portugues', 2.1,  7),
             ('BS-01', 'barao-lucena',   1.9,  6),
             ('BS-01', 'getulio-vargas', 7.1, 16),
             ('BS-01', 'upa-norte',      8.4, 20)) as ida (origem_id, destino_id, distancia_km, tempo_estimado_min)
union all
select destino_id, origem_id, distancia_km, tempo_estimado_min
from (values ('BS-01', 'HOSP-01',        3.2,  9),
             ('BS-01', 'hc-pe',          7.8, 18),
             ('BS-01', 'real-portugues', 2.1,  7),
             ('BS-01', 'barao-lucena',   1.9,  6),
             ('BS-01', 'getulio-vargas', 7.1, 16),
             ('BS-01', 'upa-norte',      8.4, 20)) as volta (origem_id, destino_id, distancia_km, tempo_estimado_min)
on conflict do nothing;

-- -------------------------------------------------------------------
-- bolsa_hemocomponente — estoque do Hemope Central (BS-01).
-- PQ-3014 está a 25,6 °C de propósito: fora da faixa ideal das plaquetas
-- (20–24 °C), para o alerta "FORA DA FAIXA" da HU-03 aparecer.
-- -------------------------------------------------------------------
insert into public.bolsa_hemocomponente (id, banco_origem_id, tipo_componente, tipo_sanguineo, data_coleta, data_validade,
                                         lote_sintetico, volume_ml, temperatura_celsius, localizacao, status)
values ('CH-1042', 'BS-01', 'HEMACIAS',        'O_NEGATIVO',  current_date - 32, current_date + 3,   'LOTE-SIM-0001', 450.0,   4.0, 'R1 · P3 · N2', 'DISPONIVEL'),
       ('CH-1043', 'BS-01', 'HEMACIAS',        'O_NEGATIVO',  current_date - 15, current_date + 20,  'LOTE-SIM-0002', 450.0,   3.5, 'R1 · P3 · N3', 'DISPONIVEL'),
       ('CR-4002', 'BS-01', 'CRIOPRECIPITADO', 'O_NEGATIVO',  current_date - 10, current_date + 355, 'LOTE-SIM-0003',  30.0, -25.0, 'F2 · P1 · N1', 'DISPONIVEL'),
       ('PQ-3014', 'BS-01', 'PLAQUETAS',       'A_POSITIVO',  current_date - 2,  current_date + 3,   'LOTE-SIM-0004', 300.0,  25.6, 'R3 · P2 · N1', 'DISPONIVEL'),
       ('CH-1080', 'BS-01', 'HEMACIAS',        'AB_POSITIVO', current_date - 29, current_date + 6,   'LOTE-SIM-0005', 450.0,   5.0, 'R2 · P1 · N1', 'DISPONIVEL'),
       ('CH-1061', 'BS-01', 'HEMACIAS',        'A_NEGATIVO',  current_date - 26, current_date + 9,   'LOTE-SIM-0006', 450.0,   4.5, 'R2 · P2 · N1', 'DISPONIVEL')
on conflict do nothing;

commit;

-- -------------------------------------------------------------------
-- Conferência (esperado: 7 pontos, 12 conexões, 6 bolsas do seed).
-- -------------------------------------------------------------------
select 'ponto_rede' as tabela, count(*) as linhas
from public.ponto_rede
where id in ('BS-01', 'HOSP-01', 'hc-pe', 'real-portugues', 'barao-lucena', 'getulio-vargas', 'upa-norte')
union all
select 'conexao', count(*)
from public.conexao
where 'BS-01' in (origem_id, destino_id)
union all
select 'bolsa_hemocomponente', count(*)
from public.bolsa_hemocomponente
where id in ('CH-1042', 'CH-1043', 'CR-4002', 'PQ-3014', 'CH-1080', 'CH-1061');
