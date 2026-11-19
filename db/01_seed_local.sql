-- ===================================================================
-- Rota Vital — Dados de demonstração para o Postgres local do Compose.
--
-- Executado por db/00_rotavital.sh depois de todas as migrations.
-- As datas das bolsas são relativas a current_date para manter os cenários
-- de FEFO, vencimento e alertas úteis quando o banco for inicializado.
--
-- Pode ser executado novamente: conflitos são ignorados para não substituir
-- alterações feitas durante testes ou demonstrações.
-- ===================================================================

begin;

-- -------------------------------------------------------------------
-- Rede: dois bancos de sangue e hospitais de Recife e Caruaru.
-- -------------------------------------------------------------------
insert into public.ponto_rede (id, tipo, nome, logradouro, cidade, uf, latitude, longitude)
values ('BS-01',          'BANCO_DE_SANGUE', 'Hemope Central',                    'Av. Cruz Cabugá, 1563',                              'Recife',  'PE', -8.0442, -34.8711),
       ('BS-02',          'BANCO_DE_SANGUE', 'Hemocentro Regional do Agreste',    'Av. Oswaldo Cruz, 163',                              'Caruaru', 'PE', -8.2820, -35.9750),
       ('HOSP-01',        'HOSPITAL',        'Hospital das Clínicas de Pernambuco','Av. Prof. Moraes Rego, 1235 - Cidade Universitária', 'Recife',  'PE', -8.0512, -34.9478),
       ('HOSP-02',        'HOSPITAL',        'Real Hospital Português',           'Av. Gov. Agamenon Magalhães, 4760 - Paissandu',      'Recife',  'PE', -8.0479, -34.8993),
       ('HOSP-03',        'HOSPITAL',        'Hospital Barão de Lucena',          'Av. Caxangá, 3860 - Iputinga',                       'Recife',  'PE', -8.0432, -34.9331),
       ('HOSP-04',        'HOSPITAL',        'Hospital Getúlio Vargas',            'Av. General San Martin, s/n - Cordeiro',             'Recife',  'PE', -8.0638, -34.9195),
       ('HOSP-05',        'HOSPITAL',        'Hospital Regional do Agreste',       'BR-104, km 62 - Bairro Luiz Gonzaga',                'Caruaru', 'PE', -8.2786, -35.9708),
       ('HOSP-06',        'HOSPITAL',        'UPA Norte - Macaxeira',              'Av. Norte Miguel Arraes, 7200 - Macaxeira',          'Recife',  'PE', -8.0098, -34.9296)
on conflict do nothing;

-- Arestas dirigidas de ida e volta entre os hemocentros, hospitais e rotas
-- alternativas da rede. Distâncias e tempos são aproximações para demonstração.
with arestas (origem_id, destino_id, distancia_km, tempo_estimado_min) as
(
    values ('BS-01', 'HOSP-01',  7.8::numeric, 18::numeric),
           ('BS-01', 'HOSP-02',  3.2,           9),
           ('BS-01', 'HOSP-03',  5.4,          14),
           ('BS-01', 'HOSP-04',  4.1,          11),
           ('BS-01', 'HOSP-06',  8.4,          20),
           ('BS-02', 'HOSP-05',  2.3,           7),
           ('BS-01', 'BS-02',  135.0,         115),
           ('HOSP-01', 'HOSP-03', 4.6,          12),
           ('HOSP-02', 'HOSP-04', 3.8,          10),
           ('HOSP-03', 'HOSP-06', 4.9,          12),
           ('HOSP-05', 'BS-01', 135.0,         115)
)
insert into public.conexao (origem_id, destino_id, distancia_km, tempo_estimado_min)
select origem_id, destino_id, distancia_km, tempo_estimado_min
from arestas
union all
select destino_id, origem_id, distancia_km, tempo_estimado_min
from arestas
on conflict (origem_id, destino_id) do nothing;

-- -------------------------------------------------------------------
-- Bolsas: prazos FEFO de 1, 3 e 7 dias; vencidas; temperatura fora da
-- faixa ideal mas dentro da faixa física aceita pelo sensor.
-- Os prazos de armazenamento respeitam os limites da migration:
-- hemácias 42 dias, plaquetas 7, plasma 730 e crioprecipitado 365.
-- -------------------------------------------------------------------
insert into public.bolsa_hemocomponente
    (id, banco_origem_id, tipo_componente, tipo_sanguineo, data_coleta, data_validade,
     lote_sintetico, volume_ml, temperatura_celsius, localizacao, status)
values
    -- O- para exercício FEFO: mesmas características, validades crescentes.
    ('CH-1101', 'BS-01', 'HEMACIAS',        'O_NEGATIVO', current_date - 41, current_date + 1,  'SEED-LOCAL-CH-1101', 450.0, 4.0,  'R1 · P1 · N1', 'DISPONIVEL'),
    ('CH-1102', 'BS-01', 'HEMACIAS',        'O_NEGATIVO', current_date - 39, current_date + 3,  'SEED-LOCAL-CH-1102', 450.0, 3.8,  'R1 · P1 · N2', 'DISPONIVEL'),
    ('CH-1103', 'BS-01', 'HEMACIAS',        'O_NEGATIVO', current_date - 35, current_date + 7,  'SEED-LOCAL-CH-1103', 450.0, 4.2,  'R1 · P1 · N3', 'DISPONIVEL'),
    -- Vencidas permanecem disponíveis no registro para validar alertas e bloqueio no backend.
    ('CH-1104', 'BS-01', 'HEMACIAS',        'O_NEGATIVO', current_date - 43, current_date - 1,  'SEED-LOCAL-CH-1104', 450.0, 4.0,  'R1 · P2 · N1', 'DISPONIVEL'),
    ('CH-1105', 'BS-01', 'HEMACIAS',        'A_POSITIVO', current_date - 47, current_date - 5,  'SEED-LOCAL-CH-1105', 450.0, 4.1,  'R1 · P2 · N2', 'DISPONIVEL'),
    -- Sensor dentro da faixa física, mas temperatura acima da faixa ideal de hemácias (2–6 °C).
    ('CH-1106', 'BS-01', 'HEMACIAS',        'A_POSITIVO', current_date - 12, current_date + 18, 'SEED-LOCAL-CH-1106', 450.0, 8.5,  'R1 · P3 · N1', 'DISPONIVEL'),
    ('CH-1107', 'BS-01', 'HEMACIAS',        'B_POSITIVO', current_date - 10, current_date + 25, 'SEED-LOCAL-CH-1107', 450.0, 4.0,  'R2 · P1 · N1', 'DISPONIVEL'),
    ('PL-2101', 'BS-01', 'PLAQUETAS',       'A_POSITIVO', current_date - 1,  current_date + 4,  'SEED-LOCAL-PL-2101', 300.0, 25.6, 'R3 · P1 · N1', 'DISPONIVEL'),
    ('PL-2102', 'BS-01', 'PLAQUETAS',       'O_POSITIVO', current_date - 1,  current_date + 6,  'SEED-LOCAL-PL-2102', 300.0, 22.0, 'R3 · P1 · N2', 'DISPONIVEL'),
    ('PF-3101', 'BS-01', 'PLASMA',          'AB_POSITIVO',current_date - 60,current_date + 365, 'SEED-LOCAL-PF-3101', 250.0,-25.0, 'F1 · P1 · N1', 'DISPONIVEL'),
    ('CR-4101', 'BS-01', 'CRIOPRECIPITADO', 'O_NEGATIVO', current_date - 30,current_date + 300, 'SEED-LOCAL-CR-4101',  30.0,-25.0, 'F2 · P1 · N1', 'DISPONIVEL'),
    ('CH-2101', 'BS-02', 'HEMACIAS',        'O_NEGATIVO', current_date - 20,current_date + 22,  'SEED-LOCAL-CH-2101', 450.0, 4.0,  'R1 · P1 · N1', 'DISPONIVEL'),
    ('PL-2201', 'BS-02', 'PLAQUETAS',       'B_NEGATIVO', current_date - 1, current_date + 5,   'SEED-LOCAL-PL-2201', 300.0, 21.5, 'R2 · P1 · N1', 'DISPONIVEL')
on conflict do nothing;

-- -------------------------------------------------------------------
-- Pacientes fictícios aguardando atendimento em hospitais da rede.
-- Os UUIDs estáveis e ON CONFLICT tornam a carga repetível.
-- -------------------------------------------------------------------
insert into public.paciente
    (id, hospital_id, nome, sexo, data_nascimento, tipo_sanguineo, tipo_componente,
     unidades_necessarias, causa, gravidade, status, criado_em, atualizado_em)
values
    ('10000000-0000-4000-8000-000000000001', 'HOSP-01', 'Mariana Costa (demo)', 'FEMININO',
     date '1987-04-12', 'O_NEGATIVO', 'HEMACIAS', 2, 'Hemorragia pós-operatória', 'CRITICO', 'AGUARDANDO',
     current_timestamp - interval '2 hours', current_timestamp - interval '2 hours'),
    ('10000000-0000-4000-8000-000000000002', 'HOSP-02', 'Rafael Almeida (demo)', 'MASCULINO',
     date '1974-09-03', 'A_POSITIVO', 'HEMACIAS', 1, 'Anemia aguda em investigação', 'ATENCAO', 'AGUARDANDO',
     current_timestamp - interval '5 hours', current_timestamp - interval '5 hours'),
    ('10000000-0000-4000-8000-000000000003', 'HOSP-03', 'Beatriz Santos (demo)', 'FEMININO',
     date '2001-01-27', 'B_POSITIVO', 'PLAQUETAS', 1, 'Plaquetopenia associada a tratamento', 'CRITICO', 'AGUARDANDO',
     current_timestamp - interval '1 hour', current_timestamp - interval '1 hour'),
    ('10000000-0000-4000-8000-000000000004', 'HOSP-05', 'Lucas Ferreira (demo)', 'MASCULINO',
     date '1995-06-18', 'AB_POSITIVO', 'PLASMA', 2, 'Coagulopatia em acompanhamento', 'ESTAVEL', 'AGUARDANDO',
     current_timestamp - interval '1 day', current_timestamp - interval '1 day'),
    ('10000000-0000-4000-8000-000000000005', 'HOSP-06', 'Helena Oliveira (demo)', 'FEMININO',
     date '1958-11-09', 'O_POSITIVO', 'HEMACIAS', 1, 'Anemia sintomática', 'ATENCAO', 'AGUARDANDO',
     current_timestamp - interval '3 hours', current_timestamp - interval '3 hours')
on conflict do nothing;

commit;

-- Conferência rápida dos dados carregados e dos cenários de validade.
select 'pontos_rede' as conjunto, count(*) as registros
from public.ponto_rede
where id in ('BS-01', 'BS-02', 'HOSP-01', 'HOSP-02', 'HOSP-03', 'HOSP-04', 'HOSP-05', 'HOSP-06')
union all
select 'conexoes', count(*)
from public.conexao
where origem_id in ('BS-01', 'BS-02', 'HOSP-01', 'HOSP-02', 'HOSP-03', 'HOSP-04', 'HOSP-05', 'HOSP-06')
  and destino_id in ('BS-01', 'BS-02', 'HOSP-01', 'HOSP-02', 'HOSP-03', 'HOSP-04', 'HOSP-05', 'HOSP-06')
union all
select 'bolsas', count(*)
from public.bolsa_hemocomponente
where id in ('CH-1101', 'CH-1102', 'CH-1103', 'CH-1104', 'CH-1105', 'CH-1106', 'CH-1107',
             'PL-2101', 'PL-2102', 'PF-3101', 'CR-4101', 'CH-2101', 'PL-2201')
union all
select 'pacientes', count(*)
from public.paciente
where id in ('10000000-0000-4000-8000-000000000001', '10000000-0000-4000-8000-000000000002',
             '10000000-0000-4000-8000-000000000003', '10000000-0000-4000-8000-000000000004',
             '10000000-0000-4000-8000-000000000005');

select id, tipo_componente, tipo_sanguineo, data_validade, data_validade - current_date as dias_ate_validade,
       temperatura_celsius, status
from public.bolsa_hemocomponente
where id in ('CH-1101', 'CH-1102', 'CH-1103', 'CH-1104', 'CH-1105', 'CH-1106', 'PL-2101')
order by data_validade, id;
