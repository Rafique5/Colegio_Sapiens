const ADM = 'ADMINISTRADOR', SEC = 'SECRETARIA';
let perfil = '';
const $ = s => document.querySelector(s);

function h(tag, a = {}, ...k) {
    const e = document.createElement(tag);
    for (const [n, v] of Object.entries(a)) {
        if (n === 'class') e.className = v;
        else if (n.startsWith('on') && typeof v === 'function') e[n.toLowerCase()] = v;
        else e.setAttribute(n, v);
    }
    e.append(...k.flat().filter(x => x != null && x !== false));
    return e;
}

async function api(c, o = {}) {
    const r = await fetch(API_URL + c, { credentials: 'include', ...o });
    if (r.status === 401) {
        location.href = 'login.html';
        throw new Error('Sessão expirada.');
    }
    const b = await r.json().catch(() => null);
    if (!r.ok) {
        throw new Error((b && b.mensagem || 'Erro ' + r.status)
            + (b && b.detalhes && b.detalhes.length ? ' (' + b.detalhes.join('; ') + ')' : ''));
    }
    return b;
}
const post = (c, d) => api(c, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(d) });
const cab = (t, s) => [h('h1', {}, t), h('p', { class: 'sub' }, s)];
const pill = (t, c = '') => h('span', { class: 'pill ' + c }, t);
const tabela = (cols, linhas) => h('table', { class: 't' },
    h('thead', {}, h('tr', {}, cols.map(c => h('th', {}, c)))),
    h('tbody', {}, linhas));
const td = (x, m) => h('td', { class: m ? 'mut' : '' }, x);
const msg = () => h('p', { role: 'alert' });
const dizer = (p, t, ok) => { p.className = ok ? 'ok' : 'erro'; p.textContent = t; };

function pillEstado(situacao, ativo) {
    const s = situacao || (ativo === false ? 'INATIVO' : 'ATIVO');
    if (s === 'TRANSFERIDO') return pill('Transferido', 'trf');
    if (s === 'INATIVO') return pill('Inativo', 'off');
    return pill('Ativo', 'ok');
}

function listaComBusca(v, { titulo, sub, placeholder, textoBotao, onNovo, cols, linhas }) {
    const busca = h('input', { class: 'inp', placeholder });
    const corpo = h('tbody');
    const tb = tabela(cols, []);
    tb.replaceChild(corpo, tb.tBodies[0]);
    const desenhar = () => {
        const q = busca.value.toLowerCase().trim();
        corpo.replaceChildren(...linhas.filter(l => l.chave.includes(q)).map(l => l.tr()));
    };
    busca.oninput = desenhar;
    desenhar();
    const accoes = [busca];
    if (textoBotao && onNovo) accoes.push(h('button', { class: 'btn', onclick: onNovo }, textoBotao));
    if (titulo) v.append(...cab(titulo, sub));
    v.append(h('div', { class: 'card' }, h('div', { class: 'barra' }, ...accoes), tb));
}

async function pPerfis(v) {
    const alunos = await api('/api/alunos');
    listaComBusca(v, {
        titulo: 'Cadastro de Perfis',
        sub: 'Consulte e faça a gestão dos perfis de alunos do colégio.',
        placeholder: 'Pesquisar aluno por nome ou nº de estudante',
        textoBotao: '+ Novo aluno',
        onNovo: () => ir('criar', 'Aluno'),
        cols: ['Nome', 'Nº estudante', 'Turma', 'Estado'],
        linhas: alunos.map(a => ({
            chave: (a.nomeCompleto + a.codigoEstudante).toLowerCase(),
            tr: () => h('tr', {}, td(a.nomeCompleto), td(a.codigoEstudante, 1), td(a.nomeTurma || '—', 1),
                td(pillEstado(a.situacao, a.ativo)))
        }))
    });
}

async function pTurmas(v) {
    v.append(...cab('Cadastro de Turmas', 'Consulte e faça a gestão das turmas do colégio.'));
    const form = h('div', { class: 'card form-turma' });
    const n = h('input', { class: 'inp', placeholder: 'Ex: 10ª A' });
    const niv = h('input', { class: 'inp', placeholder: 'Ex: 10º Ano' });
    const a = h('input', { class: 'inp', type: 'number', value: String(new Date().getFullYear()) });
    const s = h('input', { class: 'inp', placeholder: 'Sala (opcional)' });
    const m = msg();
    form.append(
        h('h2', {}, 'Nova turma'),
        h('div', { class: 'duas' }, h('div', {}, h('label', {}, 'Turma'), n), h('div', {}, h('label', {}, 'Nível'), niv)),
        h('div', { class: 'duas' }, h('div', {}, h('label', {}, 'Ano lectivo'), a), h('div', {}, h('label', {}, 'Sala'), s)),
        m,
        h('br'),
        h('button', { class: 'btn', onclick: async () => {
            try {
                await post('/api/turmas', {
                    nomeTurma: n.value.trim(),
                    nivel: niv.value.trim() || null,
                    anoLectivo: Number(a.value),
                    sala: s.value.trim() || null,
                    ativo: true
                });
                n.value = niv.value = s.value = '';
                dizer(m, 'Turma criada.', 1);
                await ir('turmas');
            } catch (e) { dizer(m, e.message); }
        } }, 'Criar turma'),
        ' ',
        h('button', { class: 'btn sec', onclick: () => form.classList.remove('aberto') }, 'Cancelar')
    );
    if (perfil === ADM) v.append(form);

    const turmas = await api('/api/turmas');
    listaComBusca(v, {
        placeholder: 'Pesquisar turma por nome ou código',
        textoBotao: perfil === ADM ? '+ Nova turma' : null,
        onNovo: perfil === ADM ? () => form.classList.toggle('aberto') : null,
        cols: ['Turma', 'Nível', 'Nº Alunos', 'Estado'],
        linhas: turmas.map(t => ({
            chave: ((t.nomeTurma || '') + (t.nivel || '')).toLowerCase(),
            tr: () => h('tr', {}, td(t.nomeTurma), td(t.nivel || '—', 1), td(String(t.numeroAlunos ?? 0), 1),
                td(pillEstado(t.ativo ? 'ATIVO' : 'INATIVO', t.ativo)))
        }))
    });
}

async function pProf(v) {
    const lista = await api('/api/professores');
    listaComBusca(v, {
        titulo: 'Cadastro de Professores',
        sub: 'Consulte e faça a gestão dos professores do colégio.',
        placeholder: 'Pesquisar professor por nome',
        textoBotao: perfil === ADM ? '+ Novo professor' : null,
        onNovo: perfil === ADM ? () => ir('criar', 'Professor') : null,
        cols: ['Nome', 'Email', 'Disciplina', 'Estado'],
        linhas: lista.map(p => ({
            chave: (p.nomeCompleto + (p.email || '') + (p.especialidade || '')).toLowerCase(),
            tr: () => h('tr', {}, td(p.nomeCompleto), td(p.email || '—', 1), td(p.especialidade || '—', 1),
                td(pillEstado(p.ativo ? 'ATIVO' : 'INATIVO', p.ativo)))
        }))
    });
}

async function pEnc(v) {
    const lista = await api('/api/encarregados');
    listaComBusca(v, {
        titulo: 'Cadastro de Encarregados',
        sub: 'Consulte e faça a gestão dos encarregados de educação.',
        placeholder: 'Pesquisar encarregado por nome',
        textoBotao: '+ Novo encarregado',
        onNovo: () => ir('criar', 'Encarregado'),
        cols: ['Nome', 'Contacto', 'Aluno associado', 'Estado'],
        linhas: lista.map(e => ({
            chave: (e.nomeCompleto + (e.telefone || '') + (e.alunoAssociado || '')).toLowerCase(),
            tr: () => h('tr', {}, td(e.nomeCompleto), td(e.telefone || e.email || '—', 1),
                td(e.alunoAssociado || '—', 1), td(pillEstado(e.ativo ? 'ATIVO' : 'INATIVO', e.ativo)))
        }))
    });
}

function temp() {
    return 'Sp@' + [...crypto.getRandomValues(new Uint8Array(6))]
        .map(b => 'abcdefghjkmnpqrstuvwxyz23456789'[b % 31]).join('') + '9';
}
const codigo = () => Date.now().toString(36).toUpperCase();

function pCriar(v, pre) {
    const tipos = perfil === ADM
        ? ['Administrador', 'Professor', 'Aluno', 'Encarregado']
        : ['Aluno', 'Encarregado'];
    let sel = tipos.includes(pre) ? pre : tipos[0];
    const nome = h('input', { class: 'inp', placeholder: 'Ex: Prof. Fátima Sitoe' });
    const email = h('input', { class: 'inp', type: 'email', placeholder: 'nome@colegiosapiens.co.mz' });
    const area = h('input', { class: 'inp', placeholder: 'Ex: 10ª A, ou N/A' });
    const nasc = h('input', { class: 'inp', placeholder: 'DD/MM/AAAA' });
    const m = msg();
    const cartas = h('div', { class: 'perfis' });
    const marcar = () => cartas.replaceChildren(...tipos.map(t =>
        h('button', { type: 'button', class: t === sel ? 'on' : '', onclick: () => { sel = t; marcar(); } }, t)));
    marcar();

    const criar = async () => {
        const dados = { nomeCompleto: nome.value.trim(), email: email.value.trim().toLowerCase(), senha: temp() };
        if (!dados.nomeCompleto || !dados.email) throw new Error('Preencha o nome e o e-mail.');
        const a = area.value.trim() === 'N/A' ? '' : area.value.trim();
        if (sel === 'Administrador') {
            await post('/api/utilizadores', { ...dados, perfil: 'ADMINISTRADOR', cargoOuSetor: a || null });
        } else if (sel === 'Professor') {
            await post('/api/professores', { ...dados, codigoDocente: 'DOC-' + codigo(), especialidade: a || null });
        } else if (sel === 'Encarregado') {
            await post('/api/encarregados', dados);
        } else {
            const t = (await api('/api/turmas')).find(x => x.nomeTurma.toLowerCase() === a.toLowerCase());
            if (!t) throw new Error('Turma "' + a + '" não encontrada. Crie-a primeiro em Turmas.');
            const d = nasc.value.match(/^(\d{2})\/(\d{2})\/(\d{4})$/);
            if (!d) throw new Error('Data de nascimento no formato DD/MM/AAAA.');
            await post('/api/alunos', {
                ...dados,
                codigoEstudante: new Date().getFullYear() + '-' + codigo(),
                dataNascimento: d[3] + '-' + d[2] + '-' + d[1],
                turmaId: t.id,
                situacao: 'ATIVO'
            });
        }
        return dados.senha;
    };

    v.append(...cab('Criar conta de utilizador', 'Registe uma nova conta institucional para um membro do colégio.'),
        h('div', { class: 'card' },
            h('h2', {}, 'Nova conta'),
            h('p', { class: 'mut' }, 'A conta é criada com uma palavra-passe temporária (o envio por e-mail ainda não está ligado: será mostrada aqui).'),
            h('label', {}, 'Nome completo'), nome,
            h('label', {}, 'Email institucional'), email,
            h('label', {}, 'Perfil'), cartas,
            h('div', { class: 'duas' },
                h('div', {}, h('label', {}, 'Turma / Área'), area),
                h('div', {}, h('label', {}, 'Data de nascimento'), nasc)),
            m, h('br'),
            h('button', { class: 'btn', onclick: async () => {
                try {
                    const s = await criar();
                    dizer(m, 'Conta criada. Palavra-passe temporária: ' + s + ' (anote-a).', 1);
                    nome.value = email.value = area.value = nasc.value = '';
                } catch (e) { dizer(m, e.message); }
            } }, 'Criar conta'),
            ' ',
            h('button', { class: 'btn sec', onclick: () => ir('perfis') }, 'Cancelar')));
}

async function pAssoc(v) {
    const [alunos, encs] = await Promise.all([api('/api/alunos'), api('/api/encarregados')]);
    const mapa = (l, dl) => {
        const m = new Map();
        l.forEach(x => {
            const r = x.nomeCompleto + (x.codigoEstudante ? ' · ' + x.codigoEstudante : ' · #' + x.id);
            m.set(r, x.id);
            dl.append(h('option', { value: r }));
        });
        return m;
    };
    const dA = h('datalist', { id: 'dA' }), dE = h('datalist', { id: 'dE' });
    const mA = mapa(alunos, dA), mE = mapa(encs, dE);
    const ia = h('input', { class: 'inp', list: 'dA', placeholder: 'Pesquisar por nome' });
    const ie = h('input', { class: 'inp', list: 'dE', placeholder: 'Pesquisar por nome' });
    const par = h('select', { class: 'inp' },
        ['Mãe', 'Pai', 'Tio / Tia', 'Avô / Avó', 'Outro'].map(o => h('option', {}, o)));
    const m = msg();
    const lista = h('div');

    const carregar = async () => {
        const l = await api('/api/vinculos');
        lista.replaceChildren(...(l.length
            ? l.map(x => h('div', { class: 'linha' },
                h('strong', {}, x.nomeAluno),
                h('span', { class: 'seta' }, '↔'),
                x.nomeEncarregado,
                x.parentesco ? pill(x.parentesco) : null))
            : [h('p', { class: 'mut' }, 'Sem associações.')]));
    };

    v.append(...cab('Associação Aluno ↔ Encarregado', 'Ligue cada aluno ao respectivo encarregado de educação.'),
        h('div', { class: 'card' },
            h('h2', {}, 'Nova associação'),
            h('p', { class: 'mut' }, 'Pesquise pelo nome para encontrar o aluno e o encarregado.'),
            h('label', {}, 'Aluno'), ia,
            h('label', {}, 'Encarregado de educação'), ie,
            h('label', {}, 'Parentesco'), par, dA, dE, m, h('br'),
            h('button', { class: 'btn', onclick: async () => {
                const a = mA.get(ia.value), e = mE.get(ie.value);
                if (!a || !e) return dizer(m, 'Escolha o aluno e o encarregado a partir da lista de sugestões.');
                try {
                    await post('/api/vinculos', { alunoId: a, encarregadoId: e, parentesco: par.value });
                    ia.value = ie.value = '';
                    dizer(m, 'Associação criada.', 1);
                    await carregar();
                } catch (x) { dizer(m, x.message); }
            } }, 'Associar')),
        h('div', { class: 'card' }, h('h2', {}, 'Associações existentes'), lista));
    await carregar();
}

const PAGINAS = {
    perfis: ['Cadastro de Perfis', pPerfis, [ADM, SEC]],
    turmas: ['Turmas', pTurmas, [ADM, SEC]],
    professores: ['Professores', pProf, [ADM, SEC]],
    criar: ['Criar Conta', pCriar, [ADM, SEC]],
    encarregados: ['Encarregados', pEnc, [ADM, SEC]],
    assoc: ['Associação Aluno–Encarregado', pAssoc, [ADM, SEC]]
};

async function ir(k, arg) {
    document.querySelectorAll('#menu button[data-k]').forEach(b => b.classList.toggle('on', b.dataset.k === k));
    const v = $('#vista');
    v.replaceChildren();
    try { await PAGINAS[k][1](v, arg); } catch (e) { v.append(h('p', { class: 'erro' }, e.message)); }
}

(async () => {
    try {
        const u = await api('/api/auth/eu');
        perfil = u.perfil;
        const menu = $('#menu');
        Object.entries(PAGINAS).filter(([, p]) => p[2].includes(perfil)).forEach(([k, p]) =>
            menu.append(h('button', { 'data-k': k, onclick: () => ir(k) }, p[0])));
        menu.append(h('div', { class: 'fim' }, u.nomeCompleto, h('br'),
            h('button', { onclick: async () => {
                try { await fetch(API_URL + '/api/auth/logout', { method: 'POST', credentials: 'include' }); } catch (e) {}
                location.href = 'login.html';
            } }, 'Sair')));
        const primeira = menu.querySelector('button[data-k]');
        primeira ? ir(primeira.dataset.k) : $('#vista').append(h('p', {}, 'O seu perfil ainda não tem módulos nesta versão.'));
    } catch (e) {
        location.href = 'login.html';
    }
})();
