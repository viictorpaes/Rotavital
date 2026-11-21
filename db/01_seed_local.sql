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
-- Amostra maior do BS-01 para a tela de Estatísticas: tipos sanguíneos
-- em proporções diferentes, validades espalhadas, duas temperaturas fora
-- da faixa ideal e seis descartes com motivo e data, para o painel de perdas.
-- Os descartes por validade expirada acontecem depois da data de validade.
-- -------------------------------------------------------------------
insert into public.bolsa_hemocomponente
    (id, banco_origem_id, tipo_componente, tipo_sanguineo, data_coleta, data_validade,
     lote_sintetico, volume_ml, temperatura_celsius, localizacao, status, motivo_descarte,
     categoria_descarte, data_descarte)
values
    ('CH-1201', 'BS-01', 'HEMACIAS', 'O_POSITIVO', current_date - 5, current_date - 5 + 42, 'SEED-LOCAL-CH-1201', 315.0, 4.1, 'R2 · P2 · N1', 'DISPONIVEL', null, null, null),
    ('CH-1202', 'BS-01', 'HEMACIAS', 'O_POSITIVO', current_date - 12, current_date - 12 + 42, 'SEED-LOCAL-CH-1202', 298.0, 3.6, 'R2 · P2 · N2', 'DISPONIVEL', null, null, null),
    ('CH-1203', 'BS-01', 'HEMACIAS', 'O_POSITIVO', current_date - 19, current_date - 19 + 42, 'SEED-LOCAL-CH-1203', 330.0, 4.4, 'R2 · P2 · N3', 'DISPONIVEL', null, null, null),
    ('CH-1204', 'BS-01', 'HEMACIAS', 'O_POSITIVO', current_date - 27, current_date - 27 + 42, 'SEED-LOCAL-CH-1204', 287.0, 5.1, 'R2 · P2 · N4', 'DISPONIVEL', null, null, null),
    ('CH-1205', 'BS-01', 'HEMACIAS', 'O_POSITIVO', current_date - 33, current_date - 33 + 42, 'SEED-LOCAL-CH-1205', 305.0, 3.9, 'R2 · P3 · N1', 'DISPONIVEL', null, null, null),
    ('CH-1206', 'BS-01', 'HEMACIAS', 'A_POSITIVO', current_date - 3, current_date - 3 + 42, 'SEED-LOCAL-CH-1206', 322.0, 4.0, 'R2 · P3 · N2', 'DISPONIVEL', null, null, null),
    ('CH-1207', 'BS-01', 'HEMACIAS', 'A_POSITIVO', current_date - 9, current_date - 9 + 42, 'SEED-LOCAL-CH-1207', 276.0, 4.8, 'R2 · P3 · N3', 'DISPONIVEL', null, null, null),
    ('CH-1208', 'BS-01', 'HEMACIAS', 'O_POSITIVO', current_date - 16, current_date - 16 + 42, 'SEED-LOCAL-CH-1208', 341.0, 3.4, 'R2 · P3 · N4', 'DISPONIVEL', null, null, null),
    ('CH-1209', 'BS-01', 'HEMACIAS', 'A_POSITIVO', current_date - 30, current_date - 30 + 42, 'SEED-LOCAL-CH-1209', 309.0, 4.2, 'R2 · P4 · N1', 'DISPONIVEL', null, null, null),
    ('CH-1210', 'BS-01', 'HEMACIAS', 'O_NEGATIVO', current_date - 8, current_date - 8 + 42, 'SEED-LOCAL-CH-1210', 296.0, 4.5, 'R2 · P4 · N2', 'DISPONIVEL', null, null, null),
    ('CH-1211', 'BS-01', 'HEMACIAS', 'O_NEGATIVO', current_date - 22, current_date - 22 + 42, 'SEED-LOCAL-CH-1211', 318.0, 3.7, 'R2 · P4 · N3', 'DISPONIVEL', null, null, null),
    ('CH-1212', 'BS-01', 'HEMACIAS', 'B_POSITIVO', current_date - 14, current_date - 14 + 42, 'SEED-LOCAL-CH-1212', 284.0, 4.3, 'R2 · P4 · N4', 'DISPONIVEL', null, null, null),
    ('CH-1213', 'BS-01', 'HEMACIAS', 'B_POSITIVO', current_date - 36, current_date - 36 + 42, 'SEED-LOCAL-CH-1213', 327.0, 6.4, 'R2 · P5 · N1', 'DISPONIVEL', null, null, null),
    ('CH-1214', 'BS-01', 'HEMACIAS', 'A_NEGATIVO', current_date - 6, current_date - 6 + 42, 'SEED-LOCAL-CH-1214', 301.0, 4.0, 'R2 · P5 · N2', 'DISPONIVEL', null, null, null),
    ('CH-1215', 'BS-01', 'HEMACIAS', 'B_NEGATIVO', current_date - 25, current_date - 25 + 42, 'SEED-LOCAL-CH-1215', 292.0, 3.8, 'R2 · P5 · N3', 'DISPONIVEL', null, null, null),
    ('CH-1216', 'BS-01', 'HEMACIAS', 'AB_POSITIVO', current_date - 18, current_date - 18 + 42, 'SEED-LOCAL-CH-1216', 312.0, 4.6, 'R2 · P5 · N4', 'DISPONIVEL', null, null, null),
    ('PL-2301', 'BS-01', 'PLAQUETAS', 'O_POSITIVO', current_date - 1, current_date - 1 + 5, 'SEED-LOCAL-PL-2301', 62.0, 22.4, 'Agitador 2 · B1', 'DISPONIVEL', null, null, null),
    ('PL-2302', 'BS-01', 'PLAQUETAS', 'A_POSITIVO', current_date - 2, current_date - 2 + 5, 'SEED-LOCAL-PL-2302', 55.0, 21.8, 'Agitador 2 · B2', 'DISPONIVEL', null, null, null),
    ('PL-2303', 'BS-01', 'PLAQUETAS', 'O_POSITIVO', current_date - 3, current_date - 3 + 5, 'SEED-LOCAL-PL-2303', 58.0, 23.1, 'Agitador 2 · B3', 'DISPONIVEL', null, null, null),
    ('PL-2304', 'BS-01', 'PLAQUETAS', 'B_POSITIVO', current_date - 4, current_date - 4 + 5, 'SEED-LOCAL-PL-2304', 51.0, 22.0, 'Agitador 2 · B4', 'DISPONIVEL', null, null, null),
    ('PL-2305', 'BS-01', 'PLAQUETAS', 'O_NEGATIVO', current_date - 2, current_date - 2 + 5, 'SEED-LOCAL-PL-2305', 60.0, 19.2, 'Agitador 2 · B5', 'DISPONIVEL', null, null, null),
    ('PF-3201', 'BS-01', 'PLASMA', 'O_POSITIVO', current_date - 40, current_date - 40 + 365, 'SEED-LOCAL-PF-3201', 215.0, -25.0, 'F1 · P2 · N1', 'DISPONIVEL', null, null, null),
    ('PF-3202', 'BS-01', 'PLASMA', 'A_POSITIVO', current_date - 95, current_date - 95 + 365, 'SEED-LOCAL-PF-3202', 198.0, -26.0, 'F1 · P2 · N2', 'DISPONIVEL', null, null, null),
    ('PF-3203', 'BS-01', 'PLASMA', 'O_POSITIVO', current_date - 150, current_date - 150 + 365, 'SEED-LOCAL-PF-3203', 231.0, -24.0, 'F1 · P2 · N3', 'DISPONIVEL', null, null, null),
    ('PF-3204', 'BS-01', 'PLASMA', 'B_POSITIVO', current_date - 210, current_date - 210 + 365, 'SEED-LOCAL-PF-3204', 207.0, -25.0, 'F1 · P2 · N4', 'DISPONIVEL', null, null, null),
    ('PF-3205', 'BS-01', 'PLASMA', 'AB_POSITIVO', current_date - 280, current_date - 280 + 365, 'SEED-LOCAL-PF-3205', 224.0, -26.0, 'F1 · P2 · N5', 'DISPONIVEL', null, null, null),
    ('PF-3206', 'BS-01', 'PLASMA', 'O_NEGATIVO', current_date - 320, current_date - 320 + 365, 'SEED-LOCAL-PF-3206', 190.0, -24.0, 'F1 · P2 · N6', 'DISPONIVEL', null, null, null),
    ('CR-4201', 'BS-01', 'CRIOPRECIPITADO', 'O_POSITIVO', current_date - 60, current_date - 60 + 365, 'SEED-LOCAL-CR-4201', 28.0, -26.0, 'F2 · P2 · N1', 'DISPONIVEL', null, null, null),
    ('CR-4202', 'BS-01', 'CRIOPRECIPITADO', 'A_POSITIVO', current_date - 130, current_date - 130 + 365, 'SEED-LOCAL-CR-4202', 31.0, -26.0, 'F2 · P2 · N2', 'DISPONIVEL', null, null, null),
    ('CR-4203', 'BS-01', 'CRIOPRECIPITADO', 'B_POSITIVO', current_date - 200, current_date - 200 + 365, 'SEED-LOCAL-CR-4203', 24.0, -26.0, 'F2 · P2 · N3', 'DISPONIVEL', null, null, null),
    ('CH-1290', 'BS-01', 'HEMACIAS', 'O_POSITIVO', current_date - 44, current_date - 44 + 42, 'SEED-LOCAL-CH-1290', 300.0, 4.0, 'Descarte', 'DESCARTADA', null, 'VALIDADE_EXPIRADA', current_timestamp - interval '1 days'),
    ('CH-1291', 'BS-01', 'HEMACIAS', 'A_POSITIVO', current_date - 50, current_date - 50 + 42, 'SEED-LOCAL-CH-1291', 310.0, 4.2, 'Descarte', 'DESCARTADA', null, 'VALIDADE_EXPIRADA', current_timestamp - interval '7 days'),
    ('CH-1292', 'BS-01', 'HEMACIAS', 'B_POSITIVO', current_date - 20, current_date - 20 + 42, 'SEED-LOCAL-CH-1292', 295.0, 9.8, 'Descarte', 'DESCARTADA', 'Geladeira R2 marcou 9,8 °C durante a madrugada', 'TEMPERATURA_FORA_DO_PADRAO', current_timestamp - interval '20 days'),
    ('PL-2390', 'BS-01', 'PLAQUETAS', 'O_POSITIVO', current_date - 8, current_date - 8 + 5, 'SEED-LOCAL-PL-2390', 58.0, 22.0, 'Descarte', 'DESCARTADA', null, 'VALIDADE_EXPIRADA', current_timestamp - interval '2 days'),
    ('PL-2391', 'BS-01', 'PLAQUETAS', 'A_POSITIVO', current_date - 9, current_date - 9 + 5, 'SEED-LOCAL-PL-2391', 54.0, 22.3, 'Descarte', 'DESCARTADA', null, 'VALIDADE_EXPIRADA', current_timestamp - interval '3 days'),
    ('PF-3290', 'BS-01', 'PLASMA', 'A_POSITIVO', current_date - 100, current_date - 100 + 365, 'SEED-LOCAL-PF-3290', 205.0, -25.0, 'Descarte', 'DESCARTADA', 'Bolsa rompida no descongelamento', 'BOLSA_DANIFICADA', current_timestamp - interval '45 days')
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
