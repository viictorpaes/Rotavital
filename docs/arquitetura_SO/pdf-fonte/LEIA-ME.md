# Fonte do PDF

`doc.html` é a versão diagramada (até 12 páginas) das Seções A a F. Os diagramas `d*.svg` saem dos `d*.mmd`, que são
os mesmos blocos Mermaid dos `.md` da pasta (`d02b` é o diagrama de classes da Seção B na horizontal).

Para gerar de novo, com Node e Playwright instalados:

```bash
npx -p @mermaid-js/mermaid-cli mmdc -c mmcfg.json -i d01.mmd -o d01.svg -b white   # repita para cada diagrama alterado
node render.js                                                                       # grava ../Rota-Vital-U2-Parte2-Planejamento.pdf
```
