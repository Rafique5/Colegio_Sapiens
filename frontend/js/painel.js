const ADM = 'ADMINISTRADOR', SEC = 'SECRETARIA', ENC = 'ENCARREGADO', PRO = 'PROFESSOR';
let perfil = '';
const $ = s => document.querySelector(s);
function h(tag, a = {}, ...k) {
  const e = document.createElement(tag);
  for (const [n, v] of Object.entries(a)) n === 'class' ? e.className = v : n.startsWith('on') ? e[n] = v : e.setAttribute(n, v);
  e.append(...k.flat().filter(x => x != null)); return e;
}
async function api(c, o = {}) {
  const r = await fetch(API_URL + c, { credentials: 'include', ...o });
  if (r.status === 401) { location.href = 'login.html'; throw new Error('Sessão expirada.'); }
  const b = await r.json().catch(() => null);
  if (!r.ok) throw new Error((b && b.mensagem || 'Erro ' + r.status) + (b && b.detalhes && b.detalhes.length ? ' (' + b.detalhes.join('; ') + ')' : ''));
  return b;
}
const post = (c, d) => api(c, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(d) });
const cab = (t, s) => [h('h1', {}, t), h('p', { class: 'sub' }, s)];
const pill = (t, c = '') => h('span', { class: 'pill ' + c }, t);
const tabela = (cols, linhas) => h('table', { class: 't' }, h('thead', {}, h('tr', {}, cols.map(c => h('th', {}, c)))), h('tbody', {}, linhas));
const td = (x, m) => h('td', { class: m ? 'mut' : '' }, x);
const msg = () => h('p', { role: 'alert' });
const dizer = (p, t, ok) => { p.className = ok ? 'ok' : 'erro'; p.textContent = t; };

// ---- Listas com pesquisa e botão (Perfis, Turmas, Professores, Encarregados) ----
const estado = ativo => pill(ativo ? 'Ativo' : 'Inativo', ativo ? 'ok' : 'off');
function lista(o) {
  return async v => {
    let linhas = await o.carregar();
    const busca = h('input', { class: 'inp', placeholder: o.busca }), corpo = h('tbody');
    const desenhar = () => {
      const q = busca.value.toLowerCase(), r = linhas.filter(l => l.busca.toLowerCase().includes(q));
      corpo.replaceChildren(...(r.length ? r.map(l => h('tr', {}, l.cel.map((c, i) => td(c, i > 0)))) : [h('tr', {}, h('td', { class: 'mut' }, 'Sem registos.'))]));
    };
    const recarregar = async () => { linhas = await o.carregar(); desenhar(); };
    busca.oninput = desenhar; desenhar();
    const tb = tabela(o.cols, []); tb.replaceChild(corpo, tb.tBodies[0]);
    const form = o.form ? o.form(recarregar) : null; if (form) form.hidden = true;
    const botao = o.criar ? h('button', { class: 'btn', onclick: () => form ? (form.hidden = !form.hidden) : ir('criar', o.criar) }, o.botao) : null;
    v.append(...cab(o.titulo, o.sub), form, h('div', { class: 'card' }, h('div', { class: 'barra' }, busca, botao), tb));
  };
}

// Menu de estado do aluno (Ativo / Inativo / Transferido): grava no backend ao escolher
const SIT = { ATIVO: ['Ativo', 'ok'], INATIVO: ['Inativo', 'off'], TRANSFERIDO: ['Transferido', 'tr'] };
function seletorSituacao(a) {
  const s = h('select', { class: 'sel ' + SIT[a.situacao][1] }, Object.entries(SIT).map(([k, [t]]) => h('option', { value: k }, t)));
  s.value = a.situacao;
  s.onchange = async () => { try { await api('/api/alunos/' + a.id + '/situacao', { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ situacao: s.value }) }); ir('perfis'); } catch (e) { alert(e.message); s.value = a.situacao; } };
  return s;
}

const pPerfis = lista({ titulo: 'Cadastro de Perfis', sub: 'Consulte e faça a gestão dos perfis de alunos do colégio.', busca: 'Pesquisar aluno por nome ou nº de estudante',
  botao: '+ Criar conta', criar: 'Aluno', cols: ['Nome', 'Nº estudante', 'Turma', 'Estado'],
  carregar: async () => (await api('/api/alunos')).map(a => ({ busca: a.nomeCompleto + ' ' + a.codigoEstudante, cel: [a.nomeCompleto, a.codigoEstudante, a.nomeTurma, seletorSituacao(a)] })) });

const pTurmas = v => lista({ titulo: 'Cadastro de Turmas', sub: 'Consulte e faça a gestão das turmas do colégio.', busca: 'Pesquisar turma por nome ou código',
  botao: '+ Nova turma', criar: perfil === ADM ? 'x' : null, cols: ['Turma', 'Nível', 'Nº Alunos', 'Estado'],
  carregar: async () => {
    const [ts, as] = await Promise.all([api('/api/turmas'), api('/api/alunos')]), ano = new Date().getFullYear();
    return ts.map(t => { const n = (t.nomeTurma.match(/^\d+/) || [])[0];
      return { busca: t.nomeTurma, cel: [t.nomeTurma, n ? n + 'º Ano' : '—', (q => h('button', { class: 'btn sec', onclick: () => ir('turma', t.id) }, q + ' · Ver alunos'))(as.filter(a => a.turmaId === t.id).length), estado(t.anoLectivo >= ano)] }; });
  },
  form: perfil !== ADM ? null : recarregar => {
    const n = h('input', { class: 'inp', placeholder: 'Ex: 10ª A' }), a = h('input', { class: 'inp', type: 'number', value: new Date().getFullYear() }), s = h('input', { class: 'inp', placeholder: 'Sala (opcional)' }), m = msg();
    return h('div', { class: 'card' }, h('h2', {}, 'Nova turma'), h('div', { class: 'duas' }, h('div', {}, h('label', {}, 'Nome da turma'), n), h('div', {}, h('label', {}, 'Ano lectivo'), a)), h('label', {}, 'Sala'), s, m, h('br'),
      h('button', { class: 'btn', onclick: async ev => { const b = ev.target; b.disabled = true; try { await post('/api/turmas', { nomeTurma: n.value.trim(), anoLectivo: Number(a.value), sala: s.value.trim() || null }); n.value = s.value = ''; dizer(m, 'Turma criada.', 1); await recarregar(); } catch (e) { dizer(m, e.message); } b.disabled = false; } }, 'Criar turma'));
  } })(v);

const pProf = v => lista({ titulo: 'Cadastro de Professores', sub: 'Consulte e faça a gestão dos professores do colégio.', busca: 'Pesquisar professor por nome',
  botao: '+ Novo professor', criar: perfil === ADM ? 'Professor' : null, cols: ['Nome', 'Email', 'Disciplina', 'Estado'],
  carregar: async () => (await api('/api/professores')).map(p => ({ busca: p.nomeCompleto, cel: [p.nomeCompleto, p.email, p.especialidade || '—', estado(p.ativo)] })) })(v);

const pEnc = lista({ titulo: 'Cadastro de Encarregados', sub: 'Consulte e faça a gestão dos encarregados de educação.', busca: 'Pesquisar encarregado por nome',
  botao: '+ Novo encarregado', criar: 'Encarregado', cols: ['Nome', 'Contacto', 'Aluno associado', 'Estado'],
  carregar: async () => {
    const [es, vs] = await Promise.all([api('/api/encarregados'), api('/api/vinculos')]);
    return es.map(e => ({ busca: e.nomeCompleto, cel: [e.nomeCompleto, e.telefone || e.email || '—', vs.filter(x => x.encarregadoId === e.id).map(x => x.nomeAluno).join(', ') || '—', estado(e.ativo)] }));
  } });

// ---- Criar Conta (campos mudam conforme o perfil) ----
function temp() { return 'Sp@' + [...crypto.getRandomValues(new Uint8Array(6))].map(b => 'abcdefghjkmnpqrstuvwxyz23456789'[b % 31]).join('') + '9'; }
const codigo = () => Date.now().toString(36).toUpperCase();
const NOMES = { Administrador: ['Carlos Mabunda', 'Helena Macuácua'], Secretaria: ['Marta Cumbe', 'Sónia Langa'], Professor: ['Fátima Sitoe', 'Jorge Nhantumbo'], Aluno: ['Amélia Cossa', 'Bruno Machava'], Encarregado: ['Regina Cossa', 'Paulo Nhaca'] };
const CAMPO = { // etiqueta e exemplo do 2.º campo, por perfil (null = não aparece)
  Administrador: ['Cargo / Área', 'Ex: Direcção'], Secretaria: ['Setor', 'Ex: Secretaria académica'], Professor: ['Disciplina', 'Ex: Matemática'], Aluno: ['Turma', 'Ex: 10ª A'], Encarregado: null };
function pCriar(v, pre) {
  const tipos = perfil === ADM ? ['Administrador', 'Secretaria', 'Professor', 'Aluno', 'Encarregado'] : ['Aluno', 'Encarregado'];
  let sel = tipos.includes(pre) ? pre : tipos[0];
  const nome = h('input', { class: 'inp' }), email = h('input', { class: 'inp', type: 'email' }), area = h('input', { class: 'inp' }), nasc = h('input', { class: 'inp', placeholder: 'DD/MM/AAAA' }), m = msg();
  const lArea = h('label', {}), lEmail = h('label', {}), bArea = h('div', {}, lArea, area), bNasc = h('div', {}, h('label', {}, 'Data de nascimento'), nasc), duas = h('div', { class: 'duas' }, bArea, bNasc);
  const cartas = h('div', { class: 'perfis' });
  const marcar = () => {
    cartas.replaceChildren(...tipos.map(t => h('button', { type: 'button', class: t === sel ? 'on' : '', onclick: () => { sel = t; marcar(); } }, t)));
    const nomes = NOMES[sel], c = CAMPO[sel];
    nome.placeholder = 'Escreva o nome aqui. Ex: ' + nomes[Math.floor(Math.random() * nomes.length)];
    const staff = sel === 'Administrador' || sel === 'Secretaria' || sel === 'Professor';
    lEmail.textContent = staff ? 'Email institucional' : 'Email'; email.placeholder = staff ? 'nome@colegiosapiens.co.mz' : 'nome@exemplo.com';
    bArea.hidden = !c; if (c) { lArea.textContent = c[0] + (sel === 'Aluno' ? ' *' : ''); area.placeholder = c[1]; }
    bNasc.hidden = sel !== 'Aluno'; duas.hidden = !c && sel !== 'Aluno';
  };
  const criar = async () => {
    const dados = { nomeCompleto: nome.value.trim(), email: email.value.trim().toLowerCase(), senha: temp() };
    if (!dados.nomeCompleto || !dados.email) throw new Error('Preencha o nome e o e-mail.');
    const a = area.value.trim();
    if (sel === 'Administrador' || sel === 'Secretaria') await post('/api/utilizadores', { ...dados, perfil: sel.toUpperCase(), cargoOuSetor: a || null });
    else if (sel === 'Professor') await post('/api/professores', { ...dados, codigoDocente: 'DOC-' + codigo(), especialidade: a || null });
    else if (sel === 'Encarregado') await post('/api/encarregados', dados);
    else {
      const t = (await api('/api/turmas')).find(x => x.nomeTurma.toLowerCase() === a.toLowerCase());
      if (!t) throw new Error('Turma "' + a + '" não encontrada. Crie-a primeiro em Turmas.');
      const d = nasc.value.match(/^(\d{2})\/(\d{2})\/(\d{4})$/); if (!d) throw new Error('Data de nascimento no formato DD/MM/AAAA.');
      await post('/api/alunos', { ...dados, codigoEstudante: new Date().getFullYear() + '-' + codigo(), dataNascimento: d[3] + '-' + d[2] + '-' + d[1], turmaId: t.id });
    }
    return dados.senha;
  };
  v.append(...cab('Criar conta de utilizador', 'Registe uma nova conta institucional para um membro do colégio.'),
    h('div', { class: 'card' }, h('h2', {}, 'Nova conta'), h('p', { class: 'mut' }, 'É gerada uma palavra-passe temporária (o envio por e-mail ainda não está ligado: será mostrada aqui).'),
      h('label', {}, 'Nome completo'), nome, lEmail, email, h('label', {}, 'Perfil'), cartas, duas, m, h('br'),
      h('button', { class: 'btn', onclick: async () => { try { const s = await criar(); dizer(m, 'Conta criada. Palavra-passe temporária: ' + s + ' (anote-a).', 1); nome.value = email.value = area.value = nasc.value = ''; } catch (e) { dizer(m, e.message); } } }, 'Criar conta'), ' ',
      h('button', { class: 'btn sec', onclick: () => ir('perfis') }, 'Cancelar')));
  marcar();
}

// ---- Associação Aluno-Encarregado ----
async function pAssoc(v) {
  const [alunos, encs] = await Promise.all([api('/api/alunos'), api('/api/encarregados')]);
  const mapa = (l, dl) => { const m = new Map(); l.forEach(x => { const r = x.nomeCompleto + ' (#' + x.id + ')'; m.set(r, x.id); dl.append(h('option', { value: r })); }); return m; };
  const dA = h('datalist', { id: 'dA' }), dE = h('datalist', { id: 'dE' }), mA = mapa(alunos, dA), mE = mapa(encs, dE);
  const ia = h('input', { class: 'inp', list: 'dA', placeholder: 'Pesquisar por nome' }), ie = h('input', { class: 'inp', list: 'dE', placeholder: 'Pesquisar por nome' });
  const par = h('select', { class: 'inp' }, ['Mãe', 'Pai', 'Tio / Tia', 'Avô / Avó', 'Outro'].map(o => h('option', {}, o))), m = msg(), lista = h('div');
  const carregar = async () => { const l = await api('/api/vinculos'); lista.replaceChildren(...(l.length ? l.map(x => h('div', { class: 'linha' }, h('strong', {}, x.nomeAluno), h('span', { style: 'color:#b98a3e' }, '↔'), x.nomeEncarregado, x.parentesco ? pill(x.parentesco) : null)) : [h('p', { class: 'mut' }, 'Sem associações.')])); };
  v.append(...cab('Associação Aluno ↔ Encarregado', 'Ligue cada aluno ao respectivo encarregado de educação.'),
    h('div', { class: 'card' }, h('h2', {}, 'Nova associação'), h('p', { class: 'mut' }, 'Pesquise pelo nome para encontrar o aluno e o encarregado.'), h('label', {}, 'Aluno'), ia, h('label', {}, 'Encarregado de educação'), ie, h('label', {}, 'Parentesco'), par, dA, dE, m, h('br'),
      h('button', { class: 'btn', onclick: async () => { const a = mA.get(ia.value), e = mE.get(ie.value); if (!a || !e) return dizer(m, 'Escolha o aluno e o encarregado a partir da lista de sugestões.');
        try { await post('/api/vinculos', { alunoId: a, encarregadoId: e, parentesco: par.value }); ia.value = ie.value = ''; dizer(m, 'Associação criada.', 1); await carregar(); } catch (x) { dizer(m, x.message); } } }, 'Associar')),
    h('div', { class: 'card' }, h('h2', {}, 'Associações existentes'), lista));
  await carregar();
}

async function pAlunosTurma(v, id) {
  const [ts, as] = await Promise.all([api('/api/turmas'), api('/api/turmas/' + id + '/alunos')]), t = ts.find(x => x.id === id);
  v.append(...cab('Alunos da turma ' + (t ? t.nomeTurma : ''), as.length + ' aluno(s) nesta turma.'),
    h('div', { class: 'card' }, h('div', { class: 'barra' }, h('button', { class: 'btn sec', onclick: () => ir('turmas') }, '← Voltar às turmas'), h('button', { class: 'btn', onclick: () => ir('criar', 'Aluno') }, '+ Criar conta')),
      tabela(['Nome', 'Nº estudante', 'Estado'], as.length ? as.map(a => h('tr', {}, td(a.nomeCompleto), td(a.codigoEstudante, 1), td(pill(SIT[a.situacao][0], SIT[a.situacao][1])))) : [h('tr', {}, h('td', { class: 'mut' }, 'Esta turma ainda não tem alunos.'))])));
}
// ---- Portal do encarregado: Meus educandos, Notas, Notificações ----
const fmt = n => n == null ? '—' : String(n).replace('.', ',');
async function pEducandos(v) {
  const es = await api('/api/portal/educandos');
  v.append(...cab('Meus educandos', 'Todos os educandos associados à sua conta.'),
    es.length ? h('div', { class: 'cartoes' }, es.map(a => h('div', { class: 'card' }, h('h2', {}, a.nomeCompleto), h('p', { class: 'mut' }, a.nomeTurma + ' · Nº ' + a.codigoEstudante),
      pill(SIT[a.situacao][0], SIT[a.situacao][1]), h('br'), h('br'), h('button', { class: 'btn sec', onclick: () => ir('notas', a.id) }, 'Ver notas'))))
      : h('p', { class: 'mut' }, 'Ainda não há educandos associados à sua conta. Contacte a secretaria.'));
}

async function pNotas(v, id) {
  const es = await api('/api/portal/educandos');
  v.append(...cab('Notas', 'Notas por disciplina — 1.º trimestre (parcial).'));
  if (!es.length) { v.append(h('p', { class: 'mut' }, 'Ainda não há educandos associados à sua conta.')); return; }
  const sel = h('select', { class: 'inp', style: 'width:auto' }, es.map(a => h('option', { value: a.id }, a.nomeCompleto))), caixa = h('div');
  sel.value = id || es[0].id;
  const SITC = { Positiva: 'ok', 'Em risco': 'warn', Negativa: 'tr' };
  const carregar = async () => {
    const ls = await api('/api/portal/educandos/' + sel.value + '/notas?trimestre=1');
    caixa.replaceChildren(tabela(['Disciplina', 'Teste 1', 'Teste 2', 'Média parcial', 'Situação'],
      ls.length ? ls.map(l => h('tr', {}, td(l.disciplina), td(fmt(l.teste1), 1), td(fmt(l.teste2), 1), td(fmt(l.mediaParcial), 1), td(pill(l.situacao, SITC[l.situacao]))))
        : [h('tr', {}, h('td', { class: 'mut' }, 'Ainda não há notas lançadas.'))]), h('p', { class: 'mut' }, 'Média final calculada no fim do trimestre.'));
  };
  sel.onchange = () => carregar().catch(e => { caixa.textContent = e.message; });
  v.append(h('div', { class: 'card' }, sel, caixa)); await carregar();
}

async function pNotif(v) {
  const caixa = h('div', { class: 'card' });
  v.append(...cab('Notificações', 'Solicitações enviadas pelos professores.'), caixa);
  const desenhar = async aberta => {
    const ls = await api('/api/portal/notificacoes');
    caixa.replaceChildren(...(ls.length ? ls.flatMap(n => [
      h('div', { class: 'linha' },
        h('div', { style: 'flex:1' }, h('div', {}, h('strong', {}, 'Prof. ' + n.nomeProfessor), ' pede reunião sobre ' + n.nomeAluno), h('div', { class: 'mut' }, n.motivo + ' · sugerida para ' + n.dataSugerida)),
        pill(n.estado === 'ENVIADA' ? 'Nova' : 'Lida', n.estado === 'ENVIADA' ? 'warn' : 'azul'),
        h('button', { class: 'btn sec', onclick: () => api('/api/portal/notificacoes/' + n.id + '/lida', { method: 'PUT' }).then(() => desenhar(n.id)).catch(e => alert(e.message)) }, 'Abrir')),
      aberta === n.id ? h('p', { class: 'mut' }, 'Pedido recebido em ' + n.criadaEm.replace('T', ' ').slice(0, 16) + '. Motivo: ' + n.motivo + '. Data sugerida: ' + n.dataSugerida + '.') : null])
      : [h('p', { class: 'mut' }, 'Não tem notificações.')]));
  };
  await desenhar();
}

// ---- Administrador: lançamentos de teste (depois passam para o professor) ----
async function pLancar(v) {
  const [as, ps] = await Promise.all([api('/api/alunos'), api('/api/professores')]);
  const opc = l => l.map(x => h('option', { value: x.id }, x.nomeCompleto)), inp = (t, p) => h('input', { class: 'inp', type: t, placeholder: p || '' });
  const nA = h('select', { class: 'inp' }, opc(as)), nD = inp('text', 'Ex: Matemática'), nT = h('select', { class: 'inp' }, ['Teste 1', 'Teste 2'].map(t => h('option', {}, t))), nV = inp('number', '0 a 20'), m1 = msg();
  const sP = h('select', { class: 'inp' }, opc(ps)), sA = h('select', { class: 'inp' }, opc(as)), sM = inp('text', 'Ex: Rendimento em Matemática'), sD = inp('date'), m2 = msg();
  const enviar = (m, caminho, corpo) => async ev => {
    const b = ev.target; b.disabled = true;
    try { const r = await post(caminho, corpo()); dizer(m, r.mensagem, 1); } catch (e) { dizer(m, e.message); }
    b.disabled = false;
  };
  v.append(...cab('Lançamentos (provisório)', 'Ferramenta do administrador para testar o portal do encarregado. Mais tarde passa para o professor.'),
    h('div', { class: 'card' }, h('h2', {}, 'Lançar nota (1.º trimestre)'), h('label', {}, 'Aluno'), nA, h('label', {}, 'Disciplina'), nD,
      h('div', { class: 'duas' }, h('div', {}, h('label', {}, 'Avaliação'), nT), h('div', {}, h('label', {}, 'Nota (0 a 20)'), nV)), m1, h('br'),
      h('button', { class: 'btn', onclick: enviar(m1, '/api/notas', () => {
        if (nV.value === '') throw new Error('Indique a nota.');
        return { alunoId: Number(nA.value), disciplina: nD.value.trim(), avaliacao: nT.value, valor: Number(nV.value), trimestre: 1 };
      }) }, 'Lançar nota')),
    h('div', { class: 'card' }, h('h2', {}, 'Pedir reunião ao encarregado'), h('label', {}, 'Professor'), sP, h('label', {}, 'Aluno'), sA, h('label', {}, 'Motivo'), sM, h('label', {}, 'Data sugerida'), sD, m2, h('br'),
      h('button', { class: 'btn', onclick: enviar(m2, '/api/solicitacoes', () => {
        if (!sD.value) throw new Error('Indique a data sugerida.');
        return { professorId: Number(sP.value), alunoId: Number(sA.value), motivo: sM.value.trim(), dataSugerida: sD.value };
      }) }, 'Enviar pedido')));
}

// ---- Secretaria: Contas de acesso, Perfil do encarregado, Solicitações de reunião ----
const PERFIS = { ADMINISTRADOR: 'Administrador', SECRETARIA: 'Secretaria', PROFESSOR: 'Professor', ENCARREGADO: 'Encarregado', ALUNO: 'Aluno' };
const put = (c, d) => api(c, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(d) });

async function pContas(v) {
  let dados = await api('/api/contas');
  const ESTC = { ATIVA: ['Ativa', 'ok'], PENDENTE: ['Pendente', 'warn'], INATIVA: ['Inativa', 'off'] };
  const busca = h('input', { class: 'inp', placeholder: 'Pesquisar por nome ou email' }), corpo = h('tbody'), gerar = h('button', { class: 'btn esc', style: 'width:auto' }), res = h('div');
  const alternar = c => put('/api/contas/' + c.id + '/estado?ativa=' + (c.estado === 'INATIVA'), null).then(recarregar).catch(e => alert(e.message));
  const desenhar = () => {
    const q = busca.value.toLowerCase(), r = dados.filter(c => (c.nomeCompleto + ' ' + (c.email || '')).toLowerCase().includes(q)), n = dados.filter(c => c.estado === 'PENDENTE').length;
    corpo.replaceChildren(...r.map(c => h('tr', {}, td(c.nomeCompleto), td(PERFIS[c.perfil], 1), td(c.codigo || '—', 1), td(pill(ESTC[c.estado][0], ESTC[c.estado][1])),
      perfil === ADM ? td(h('button', { class: 'btn sec', onclick: () => alternar(c) }, c.estado === 'INATIVA' ? 'Activar' : 'Desactivar')) : null)));
    gerar.textContent = 'Gerar contas pendentes (' + n + ')'; gerar.disabled = n === 0;
  };
  const recarregar = async () => { dados = await api('/api/contas'); desenhar(); };
  gerar.onclick = async () => {
    try {
      const cs = await post('/api/contas/gerar-pendentes');
      res.replaceChildren(h('div', { class: 'card' }, h('h2', {}, 'Contas geradas'), h('p', { class: 'mut' }, 'Anote estas senhas temporárias: não voltam a aparecer.'),
        tabela(['Nome', 'Utilizador', 'Senha temporária'], cs.map(c => h('tr', {}, td(c.nome), td(c.identificador, 1), td(c.senhaTemporaria, 1))))));
      await recarregar();
    } catch (e) { alert(e.message); }
  };
  busca.oninput = desenhar; desenhar();
  const cols = ['Nome', 'Perfil', 'Código', 'Conta'].concat(perfil === ADM ? ['Acção'] : []), tb = tabela(cols, []); tb.replaceChild(corpo, tb.tBodies[0]);
  v.append(...cab('Contas de acesso', 'Contas dos utilizadores. Uma conta fica "Pendente" enquanto não tem senha definida.'), res, h('div', { class: 'card' }, h('div', { class: 'barra' }, busca, gerar), tb));
}

async function pPerfilEnc(v) {
  const encs = await api('/api/encarregados');
  const busca = h('input', { class: 'inp', placeholder: 'Pesquisar encarregado por nome' }), area = h('div'), m = msg();
  const abrir = async id => {
    const p = await api('/api/encarregados/' + id + '/perfil');
    const tel = h('input', { class: 'inp', value: p.telefone || '', placeholder: '+258 84 123 4567' }), em = h('input', { class: 'inp', type: 'email', value: p.email || '', placeholder: 'nome@exemplo.com' }), pr = h('input', { class: 'inp', value: p.profissao || '', placeholder: 'Opcional' }), m2 = msg();
    const marcas = p.educandos.map(e => { const cb = h('input', { type: 'checkbox' }); cb.checked = e.validado; const t = h('strong', {}, e.validado ? ' Validado' : ' Por validar'); cb.onchange = () => { t.textContent = cb.checked ? ' Validado' : ' Por validar'; }; return { e, cb, t }; });
    area.replaceChildren(h('h2', {}, p.nomeCompleto), h('div', { class: 'duas' }, h('div', {}, h('label', {}, 'Telemóvel'), tel), h('div', {}, h('label', {}, 'Email'), em)), h('label', {}, 'Profissão'), pr,
      h('label', {}, 'Educandos vinculados'),
      tabela(['Aluno', 'Turma', 'Parentesco', 'Vínculo'], marcas.length ? marcas.map(x => h('tr', {}, td(x.e.nomeAluno), td(x.e.turma, 1), td(x.e.parentesco || '—', 1), h('td', {}, x.cb, x.t)))
        : [h('tr', {}, h('td', { class: 'mut' }, 'Sem educandos associados. Use "Associação Aluno–Encarregado".'))]), m2, h('br'),
      h('button', { class: 'btn', onclick: async ev => { const b = ev.target; b.disabled = true; try {
        await put('/api/encarregados/' + id + '/perfil', { telefone: tel.value.trim(), email: em.value.trim(), profissao: pr.value.trim(), vinculos: marcas.map(x => ({ vinculoId: x.e.vinculoId, validado: x.cb.checked })) });
        dizer(m2, 'Perfil guardado.', 1); } catch (e) { dizer(m2, e.message); } b.disabled = false; } }, 'Guardar perfil'), ' ',
      h('button', { class: 'btn sec', onclick: () => { area.replaceChildren(); busca.value = ''; } }, 'Cancelar'));
  };
  const pesquisar = () => {
    const q = busca.value.trim().toLowerCase(), r = encs.filter(e => e.nomeCompleto.toLowerCase().includes(q));
    m.textContent = '';
    if (!q || !r.length) { area.replaceChildren(); return dizer(m, q ? 'Nenhum encarregado encontrado.' : 'Escreva o nome do encarregado.'); }
    if (r.length === 1) return abrir(r[0].id).catch(e => dizer(m, e.message));
    area.replaceChildren(h('p', { class: 'mut' }, 'Escolha o encarregado:'), ...r.map(e => h('div', { class: 'linha' }, h('span', { style: 'flex:1' }, e.nomeCompleto), h('button', { class: 'btn sec', onclick: () => abrir(e.id).catch(x => dizer(m, x.message)) }, 'Abrir'))));
  };
  busca.onkeydown = ev => { if (ev.key === 'Enter') pesquisar(); };
  v.append(...cab('Perfil do encarregado', 'Complete os contactos e valide o vínculo com os educandos.'),
    h('div', { class: 'card' }, h('div', { class: 'barra' }, busca, h('button', { class: 'btn sec', onclick: pesquisar }, 'Pesquisar')), m, area));
}

async function pSolicitacoes(v) {
  const dados = await api('/api/solicitacoes');
  const busca = h('input', { class: 'inp', placeholder: 'Pesquisar por aluno ou professor' }), filtro = h('select', { class: 'inp', style: 'width:auto' }, [['', 'Todos os estados'], ['ENVIADA', 'Nova'], ['LIDA', 'Lida']].map(([k, t]) => h('option', { value: k }, t))), corpo = h('tbody');
  const desenhar = () => {
    const q = busca.value.toLowerCase(), r = dados.filter(s => (!filtro.value || s.estado === filtro.value) && (s.nomeAluno + ' ' + s.nomeProfessor).toLowerCase().includes(q));
    corpo.replaceChildren(...(r.length ? r.map(s => h('tr', {}, td('S-' + String(s.id).padStart(3, '0')), td('Prof. ' + s.nomeProfessor, 1), td(s.nomeAluno, 1), td(s.nomeEncarregado, 1), td(s.dataSugerida, 1),
      td(pill(s.estado === 'ENVIADA' ? 'Nova' : 'Lida', s.estado === 'ENVIADA' ? 'warn' : 'azul')))) : [h('tr', {}, h('td', { class: 'mut' }, 'Sem solicitações.'))]));
  };
  busca.oninput = desenhar; filtro.onchange = desenhar; desenhar();
  const tb = tabela(['Ref.', 'Professor', 'Aluno', 'Encarregado', 'Data sugerida', 'Estado'], []); tb.replaceChild(corpo, tb.tBodies[0]);
  v.append(...cab('Solicitações de reunião', 'Acompanhe as comunicações dos professores aos encarregados.'), h('div', { class: 'card', style: 'max-width:none' }, h('div', { class: 'barra' }, busca, filtro), tb));
}

// ---- Professor: Meus alunos, Assiduidade, Solicitar reunião ----
const hoje = (dias = 0) => new Date(Date.now() - new Date().getTimezoneOffset() * 60000 + dias * 864e5).toISOString().slice(0, 10);

async function pMeusAlunos(v) {
  const as = await api('/api/professor/alunos'), turmas = [...new Set(as.map(a => a.nomeTurma))];
  const fT = h('select', { class: 'inp', style: 'width:auto' }, h('option', { value: '' }, 'Todas as turmas'), turmas.map(t => h('option', { value: t }, t)));
  const busca = h('input', { class: 'inp', placeholder: 'Pesquisar aluno' }), corpo = h('tbody');
  const desenhar = () => {
    const q = busca.value.toLowerCase(), r = as.filter(a => (!fT.value || a.nomeTurma === fT.value) && a.nomeCompleto.toLowerCase().includes(q));
    corpo.replaceChildren(...(r.length ? r.map(a => h('tr', {}, td(a.nomeCompleto), td(a.codigoEstudante, 1), td(a.nomeTurma, 1), td(h('button', { class: 'btn sec', onclick: () => ir('solicitar', a.id) }, 'Solicitar reunião'))))
      : [h('tr', {}, h('td', { class: 'mut' }, as.length ? 'Sem resultados.' : 'Ainda não tem turmas associadas. Peça ao administrador.'))]));
  };
  busca.oninput = desenhar; fT.onchange = desenhar; desenhar();
  const tb = tabela(['Nome', 'Nº estudante', 'Turma', 'Ação'], []); tb.replaceChild(corpo, tb.tBodies[0]);
  v.append(...cab('Meus alunos', 'Alunos das suas turmas.'), h('div', { class: 'card', style: 'max-width:none' }, h('div', { class: 'barra' }, fT, busca), tb));
}

async function pAssiduidade(v) {
  const ts = await api('/api/professor/turmas');
  v.append(...cab('Assiduidade', 'Registe a presença ou falta dos alunos.'));
  if (!ts.length) { v.append(h('p', { class: 'mut' }, 'Ainda não tem turmas associadas. Peça ao administrador.')); return; }
  const sel = h('select', { class: 'inp', style: 'width:auto' }, ts.map(t => h('option', { value: t.id }, t.nome))), dia = h('input', { class: 'inp', type: 'date', style: 'width:auto' }), caixa = h('div'), m = msg();
  dia.value = hoje(); dia.max = hoje();
  let estado = {};
  const carregar = async () => {
    if (!dia.value) return;
    m.textContent = '';
    const ls = await api('/api/professor/assiduidade?turmaId=' + sel.value + '&data=' + dia.value);
    estado = {}; ls.forEach(l => { if (l.estado) estado[l.alunoId] = l.estado === 'PRESENTE'; });
    caixa.replaceChildren(tabela(['Aluno', 'Nº', 'Registo'], ls.length ? ls.map(l => {
      const bp = h('button', { type: 'button' }, 'Presente'), bf = h('button', { type: 'button' }, 'Falta');
      const pintar = () => { bp.className = estado[l.alunoId] === true ? 'on p' : ''; bf.className = estado[l.alunoId] === false ? 'on f' : ''; };
      bp.onclick = () => { estado[l.alunoId] = true; pintar(); }; bf.onclick = () => { estado[l.alunoId] = false; pintar(); }; pintar();
      return h('tr', {}, td(l.nomeAluno), td(l.codigo, 1), h('td', {}, h('div', { class: 'seg' }, bp, bf)));
    }) : [h('tr', {}, h('td', { class: 'mut' }, 'Esta turma ainda não tem alunos.'))]));
  };
  const falha = e => dizer(m, e.message);
  sel.onchange = () => carregar().catch(falha); dia.onchange = () => carregar().catch(falha);
  v.append(h('div', { class: 'card', style: 'max-width:none' }, h('div', { class: 'barra' }, sel, dia), caixa, m, h('br'),
    h('button', { class: 'btn', onclick: async ev => {
      const b = ev.target, marcas = Object.entries(estado).map(([id, p]) => ({ alunoId: Number(id), presente: p }));
      if (!marcas.length) return dizer(m, 'Marque a presença ou falta de pelo menos um aluno.');
      b.disabled = true;
      try { const r = await post('/api/professor/assiduidade', { turmaId: Number(sel.value), data: dia.value, marcas }); dizer(m, r.mensagem, 1); } catch (e) { falha(e); }
      b.disabled = false;
    } }, 'Guardar registo')));
  await carregar();
}

async function pSolicitar(v, pre) {
  const as = await api('/api/professor/alunos');
  v.append(...cab('Solicitar reunião', 'Indique o motivo e a data sugerida. O encarregado será notificado.'));
  if (!as.length) { v.append(h('p', { class: 'mut' }, 'Ainda não tem alunos. Peça ao administrador para o associar a uma turma.')); return; }
  const sel = h('select', { class: 'inp' }, as.map(a => h('option', { value: a.id }, a.nomeCompleto + ' · ' + a.nomeTurma))), motivo = h('textarea', { class: 'inp', rows: '5', placeholder: 'Descreva o assunto da reunião' }), data = h('input', { class: 'inp', type: 'date', style: 'width:auto' }), m = msg();
  if (pre) sel.value = pre; data.value = hoje(7); data.min = hoje();
  v.append(h('div', { class: 'card' }, h('label', {}, 'Aluno'), sel, h('label', {}, 'Motivo'), motivo, h('label', {}, 'Data sugerida'), data, m, h('br'),
    h('button', { class: 'btn', onclick: async ev => {
      const b = ev.target; if (!motivo.value.trim()) return dizer(m, 'Descreva o motivo da reunião.');
      b.disabled = true;
      try { const r = await post('/api/solicitacoes', { alunoId: Number(sel.value), motivo: motivo.value.trim(), dataSugerida: data.value }); dizer(m, r.mensagem, 1); motivo.value = ''; } catch (e) { dizer(m, e.message); }
      b.disabled = false;
    } }, 'Enviar solicitação')));
}

// ---- Administrador: associar professores a turmas ----
async function pDocencia(v) {
  const [ps, ts] = await Promise.all([api('/api/professores'), api('/api/turmas')]);
  const sp = h('select', { class: 'inp' }, ps.map(p => h('option', { value: p.id }, p.nomeCompleto))), st = h('select', { class: 'inp' }, ts.map(t => h('option', { value: t.id }, t.nomeTurma + ' (' + t.anoLectivo + ')'))), m = msg(), lista = h('div');
  const carregar = async () => {
    const ls = await api('/api/alocacoes');
    lista.replaceChildren(ls.length ? tabela(['Professor', 'Turma', ''], ls.map(a => h('tr', {}, td(a.nomeProfessor), td(a.nomeTurma, 1),
      td(h('button', { class: 'btn sec', onclick: () => api('/api/alocacoes/' + a.id, { method: 'DELETE' }).then(carregar).catch(e => alert(e.message)) }, 'Remover'))))) : h('p', { class: 'mut' }, 'Sem associações.'));
  };
  v.append(...cab('Turmas do professor', 'Associe cada professor às turmas que lecciona. É isto que define os "Meus alunos" de cada um.'),
    h('div', { class: 'card' }, h('label', {}, 'Professor'), sp, h('label', {}, 'Turma'), st, m, h('br'),
      h('button', { class: 'btn', onclick: async ev => { const b = ev.target; b.disabled = true;
        try { await post('/api/alocacoes', { professorId: Number(sp.value), turmaId: Number(st.value) }); dizer(m, 'Associação criada.', 1); await carregar(); } catch (e) { dizer(m, e.message); }
        b.disabled = false; } }, 'Associar')),
    h('div', { class: 'card' }, h('h2', {}, 'Associações existentes'), lista));
  await carregar();
}

// ---- Secretaria: Sincronização SGA ----
const dtf = x => new Date(x).toLocaleString('pt-PT', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' }).replace(',', '');
async function pSga(v) {
  const topo = h('div'), historico = h('div'), cartoes = h('div', { class: 'stats' }), m = msg();
  const botao = h('button', { class: 'btn' }, 'Sincronizar agora');
  const desenhar = r => {
    cartoes.replaceChildren(...[['Alunos', r.alunos], ['Turmas', r.turmas], ['Professores', r.professores], ['Encarregados', r.encarregados]].map(([t, n]) => h('div', { class: 'stat' }, h('span', {}, t), h('strong', {}, String(n)))));
    const ult = r.historico[0];
    topo.replaceChildren(h('h2', {}, 'Última sincronização'), h('p', { class: 'mut' }, ult ? dtf(ult.dataHora) + ' · ' + (ult.estado === 'CONCLUIDA' ? ult.mensagem.toLowerCase() : 'falhou: ' + ult.mensagem) : 'Ainda não foi feita nenhuma sincronização.'));
    historico.replaceChildren(r.historico.length ? tabela(['Data', 'Registos', 'Novos', 'Alterados', 'Estado'], r.historico.map(x => {
      const ok = x.estado === 'CONCLUIDA';
      return h('tr', {}, td(dtf(x.dataHora)), td(x.registos || '—', 1), td(ok ? x.novos : '—', 1), td(ok ? x.alterados : '—', 1), td(pill(ok ? 'Concluída' : 'Falhou: ' + x.mensagem, ok ? 'ok' : 'tr')));
    })) : h('p', { class: 'mut' }, 'Sem histórico.'));
  };
  botao.onclick = async () => {
    botao.disabled = true; botao.textContent = 'A sincronizar...'; m.textContent = '';
    try { await post('/api/sga/sincronizar'); desenhar(await api('/api/sga')); } catch (e) { dizer(m, e.message); }
    botao.disabled = false; botao.textContent = 'Sincronizar agora';
  };
  desenhar(await api('/api/sga'));
  v.append(...cab('Sincronização SGA', 'O SGA é a fonte única de alunos, turmas, professores, encarregados e vínculos.'), cartoes,
    h('div', { class: 'card', style: 'max-width:1000px' }, h('div', { class: 'topo-sga' }, topo, botao), m, historico,
      h('p', { class: 'mut' }, 'Ligação de demonstração: os dados vêm de um SGA simulado. As contas novas ficam "Pendentes" em Contas de acesso.')));
}

const VISTAS = { turma: ['', pAlunosTurma] }; // páginas fora do menu

const PAGINAS = { perfis: ['Cadastro de Perfis', pPerfis, [ADM, SEC]], turmas: ['Turmas', pTurmas, [ADM, SEC]], professores: ['Professores', pProf, [ADM, SEC]],
  criar: ['Criar Conta', pCriar, [ADM, SEC]], encarregados: ['Encarregados', pEnc, [ADM, SEC]], assoc: ['Associação Aluno–Encarregado', pAssoc, [ADM, SEC]],
  educandos: ['Meus educandos', pEducandos, [ENC]], notas: ['Notas', pNotas, [ENC]], notificacoes: ['Notificações', pNotif, [ENC]], lancar: ['Lançamentos (teste)', pLancar, [ADM]],
  sga: ['Sincronização SGA', pSga, [ADM, SEC]], contas: ['Contas de acesso', pContas, [ADM, SEC]], perfilEnc: ['Perfil do encarregado', pPerfilEnc, [ADM, SEC]], solicitacoes: ['Solicitações', pSolicitacoes, [ADM, SEC]],
  docencia: ['Turmas do professor', pDocencia, [ADM]],
  meusAlunos: ['Meus alunos', pMeusAlunos, [PRO]], assiduidade: ['Assiduidade', pAssiduidade, [PRO]], solicitar: ['Solicitar reunião', pSolicitar, [PRO]] };

async function ir(k, arg) {
  document.querySelectorAll('#menu button').forEach(b => b.classList.toggle('on', b.dataset.k === (k === 'turma' ? 'turmas' : k)));
  const v = $('#vista'); v.replaceChildren();
  try { await (PAGINAS[k] || VISTAS[k])[1](v, arg); } catch (e) { v.append(h('p', { class: 'erro' }, e.message)); }
}

(async () => {
  try {
    const u = await api('/api/auth/eu'); perfil = u.perfil; const menu = $('#menu');
    Object.entries(PAGINAS).filter(([, p]) => p[2].includes(perfil)).forEach(([k, p]) => menu.append(h('button', { 'data-k': k, onclick: () => ir(k) }, p[0])));
    menu.append(h('div', { class: 'fim' }, u.nomeCompleto, h('br'), h('button', { onclick: async () => { try { await fetch(API_URL + '/api/auth/logout', { method: 'POST', credentials: 'include' }); } catch (e) {} location.href = 'login.html'; } }, 'Sair')));
    const primeira = menu.querySelector('button'); primeira ? ir(primeira.dataset.k) : $('#vista').append(h('p', {}, 'O seu perfil ainda não tem módulos nesta versão.'));
  } catch (e) { location.href = 'login.html'; }
})();
