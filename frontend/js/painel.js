const ADM = 'ADMINISTRADOR', SEC = 'SECRETARIA';
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

const pPerfis = lista({ titulo: 'Cadastro de Perfis', sub: 'Consulte e faça a gestão dos perfis de alunos do colégio.', busca: 'Pesquisar aluno por nome ou nº de estudante',
  botao: '+ Criar conta', criar: 'Aluno', cols: ['Nome', 'Nº estudante', 'Turma', 'Estado'],
  carregar: async () => (await api('/api/alunos')).map(a => ({ busca: a.nomeCompleto + ' ' + a.codigoEstudante, cel: [a.nomeCompleto, a.codigoEstudante, a.nomeTurma, estado(a.ativo)] })) });

const pTurmas = v => lista({ titulo: 'Cadastro de Turmas', sub: 'Consulte e faça a gestão das turmas do colégio.', busca: 'Pesquisar turma por nome ou código',
  botao: '+ Nova turma', criar: perfil === ADM ? 'x' : null, cols: ['Turma', 'Nível', 'Nº Alunos', 'Estado'],
  carregar: async () => {
    const [ts, as] = await Promise.all([api('/api/turmas'), api('/api/alunos')]), ano = new Date().getFullYear();
    return ts.map(t => { const n = (t.nomeTurma.match(/^\d+/) || [])[0];
      return { busca: t.nomeTurma, cel: [t.nomeTurma, n ? n + 'º Ano' : '—', as.filter(a => a.turmaId === t.id).length, estado(t.anoLectivo >= ano)] }; });
  },
  form: perfil !== ADM ? null : recarregar => {
    const n = h('input', { class: 'inp', placeholder: 'Ex: 10ª A' }), a = h('input', { class: 'inp', type: 'number', value: new Date().getFullYear() }), s = h('input', { class: 'inp', placeholder: 'Sala (opcional)' }), m = msg();
    return h('div', { class: 'card' }, h('h2', {}, 'Nova turma'), h('div', { class: 'duas' }, h('div', {}, h('label', {}, 'Nome da turma'), n), h('div', {}, h('label', {}, 'Ano lectivo'), a)), h('label', {}, 'Sala'), s, m, h('br'),
      h('button', { class: 'btn', onclick: async () => { try { await post('/api/turmas', { nomeTurma: n.value.trim(), anoLectivo: Number(a.value), sala: s.value.trim() || null }); n.value = s.value = ''; dizer(m, 'Turma criada.', 1); await recarregar(); } catch (e) { dizer(m, e.message); } } }, 'Criar turma'));
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

const PAGINAS = { perfis: ['Cadastro de Perfis', pPerfis, [ADM, SEC]], turmas: ['Turmas', pTurmas, [ADM, SEC]], professores: ['Professores', pProf, [ADM, SEC]],
  criar: ['Criar Conta', pCriar, [ADM, SEC]], encarregados: ['Encarregados', pEnc, [ADM, SEC]], assoc: ['Associação Aluno–Encarregado', pAssoc, [ADM, SEC]] };

async function ir(k, arg) {
  document.querySelectorAll('#menu button').forEach(b => b.classList.toggle('on', b.dataset.k === k));
  const v = $('#vista'); v.replaceChildren();
  try { await PAGINAS[k][1](v, arg); } catch (e) { v.append(h('p', { class: 'erro' }, e.message)); }
}

(async () => {
  try {
    const u = await api('/api/auth/eu'); perfil = u.perfil; const menu = $('#menu');
    Object.entries(PAGINAS).filter(([, p]) => p[2].includes(perfil)).forEach(([k, p]) => menu.append(h('button', { 'data-k': k, onclick: () => ir(k) }, p[0])));
    menu.append(h('div', { class: 'fim' }, u.nomeCompleto, h('br'), h('button', { onclick: async () => { try { await fetch(API_URL + '/api/auth/logout', { method: 'POST', credentials: 'include' }); } catch (e) {} location.href = 'login.html'; } }, 'Sair')));
    const primeira = menu.querySelector('button'); primeira ? ir(primeira.dataset.k) : $('#vista').append(h('p', {}, 'O seu perfil ainda não tem módulos nesta versão.'));
  } catch (e) { location.href = 'login.html'; }
})();
