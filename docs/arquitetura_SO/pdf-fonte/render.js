const { chromium } = require('playwright');
(async () => {
  const b = await chromium.launch();
  const p = await b.newPage();
  await p.goto('file://' + __dirname + '/doc.html', { waitUntil: 'networkidle' });
  await p.pdf({ path: __dirname + '/../Rota-Vital-U2-Parte2-Planejamento.pdf', format: 'A4', printBackground: true, preferCSSPageSize: true,
    displayHeaderFooter: true, headerTemplate: '<div></div>',
    footerTemplate: '<div style="font-size:7px;color:#78716c;width:100%;padding:0 13mm;display:flex;justify-content:space-between;font-family:Arial"><span>Rota Vital · Trabalho U2 · Parte 2 · Planejamento da remodelagem</span><span><span class="pageNumber"></span> / <span class="totalPages"></span></span></div>' });
  await b.close();
})();
