-- ===================================================================
-- Rota Vital — baseline de RLS para a API do Supabase.
-- O frontend ainda não autentica com Supabase Auth. Bloquear anon e
-- authenticated evita conceder acesso antes da implementação de policies
-- vinculadas a auth.uid(). O backend JDBC usa postgres e ignora RLS.
-- ===================================================================

do
$$
declare
    tabela text;
begin
    foreach tabela in array array[
        'ponto_rede',
        'conexao',
        'bolsa_hemocomponente',
        'requisicao_hospitalar',
        'alocacao',
        'entrega',
        'leitura_telemetria',
        'usuario',
        'paciente',
        'remessa',
        'campanha_doacao',
        'agendamento_doacao',
        'procedimento',
        'procedimento_bolsa'
    ]
    loop
        execute format('alter table public.%I enable row level security', tabela);
        execute format(
            'create policy bloqueio_inicial_api on public.%I as restrictive for all to anon, authenticated using (false) with check (false)',
            tabela
        );
    end loop;
end;
$$;
