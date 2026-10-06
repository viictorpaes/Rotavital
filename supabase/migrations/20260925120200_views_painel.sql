-- ===================================================================
-- Rota Vital — Views do painel (HU-02) e da tela de estoque (HU-03), conforme docs/DER.md.
-- security_invoker: a view respeita o RLS de quem consulta (a API pública não enxerga nada;
-- o backend, com o role postgres, enxerga tudo).
-- ===================================================================

-- Cards do painel: unidades disponíveis e válidas por banco e tipo sanguíneo.
-- Todos os 8 tipos aparecem para cada banco, inclusive os zerados.
create view public.vw_estoque_por_tipo
    with (security_invoker = true)
as
select b.id                                  as banco_id,
       t.tipo_sanguineo,
       count(bh.id)::integer                 as unidades_disponiveis,
       count(bh.id) < 10                     as abaixo_nivel_seguranca
from public.ponto_rede b
         cross join (values ('A_POSITIVO'), ('A_NEGATIVO'), ('B_POSITIVO'), ('B_NEGATIVO'),
                            ('AB_POSITIVO'), ('AB_NEGATIVO'), ('O_POSITIVO'), ('O_NEGATIVO')) as t (tipo_sanguineo)
         left join public.bolsa_hemocomponente bh
                   on bh.banco_origem_id = b.id
                       and bh.tipo_sanguineo = t.tipo_sanguineo
                       and bh.status = 'DISPONIVEL'
                       and bh.data_validade >= current_date
where b.tipo = 'BANCO_DE_SANGUE'
group by b.id, t.tipo_sanguineo;

-- Tela de estoque: substitui o "lote" agregado do mock do frontend.
-- fora_da_faixa usa a faixa ideal de TipoComponente e acusa o grupo se qualquer bolsa estiver fora.
create view public.vw_estoque_agrupado
    with (security_invoker = true)
as
select bh.banco_origem_id                        as banco_id,
       bh.tipo_componente,
       bh.tipo_sanguineo,
       bh.data_validade,
       bh.localizacao,
       count(*)::integer                         as unidades,
       round(avg(bh.temperatura_celsius), 2)     as temperatura_media,
       coalesce(bool_or(bh.temperatura_celsius not between f.temperatura_minima and f.temperatura_maxima),
                false)                           as fora_da_faixa
from public.bolsa_hemocomponente bh
         join (values ('HEMACIAS', 2.0, 6.0),
                      ('PLASMA', -30.0, -18.0),
                      ('PLAQUETAS', 20.0, 24.0),
                      ('CRIOPRECIPITADO', -30.0, -18.0)) as f (tipo_componente, temperatura_minima, temperatura_maxima)
              on f.tipo_componente = bh.tipo_componente
where bh.status = 'DISPONIVEL'
group by bh.banco_origem_id, bh.tipo_componente, bh.tipo_sanguineo, bh.data_validade, bh.localizacao;
