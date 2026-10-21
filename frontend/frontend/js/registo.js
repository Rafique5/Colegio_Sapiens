const $ = id => document.getElementById(id);
$('f').addEventListener('submit', async ev => {
  ev.preventDefault(); const msg = $('msg'); msg.className = 'erro'; msg.textContent = '';
  const nomeCompleto = $('nome').value.trim(), email = $('email').value.trim(), senha = $('s1').value;
  if (!nomeCompleto || !email || !senha) { msg.textContent = 'Preencha todos os campos.'; return; }
  if (senha.length < 8) { msg.textContent = 'A palavra-passe deve ter pelo menos 8 caracteres.'; return; }
  if (senha !== $('s2').value) { msg.textContent = 'As palavras-passe não coincidem.'; return; }
  $('b').disabled = true;
  try {
    const r = await fetch(API_URL + '/api/auth/registo', { method: 'POST', credentials: 'include',
      headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ nomeCompleto, email, senha }) });
    if (r.ok) { msg.className = 'ok'; msg.textContent = 'Conta criada! A redireccionar...'; setTimeout(() => location.href = 'login.html', 1200); return; }
    const c = await r.json().catch(() => ({}));
    msg.textContent = (c.mensagem || 'Erro ' + r.status) + (c.detalhes && c.detalhes.length ? ' (' + c.detalhes.join('; ') + ')' : '');
  } catch (e) { msg.textContent = 'Sem ligação ao servidor.'; }
  $('b').disabled = false;
});
