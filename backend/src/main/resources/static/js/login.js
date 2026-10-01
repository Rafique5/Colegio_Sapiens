const formulario = document.getElementById('formulario-login');
const botaoEntrar = document.getElementById('botao-entrar');
const mensagem = document.getElementById('mensagem');

function mostrarErro(texto) {
    mensagem.textContent = texto;
    mensagem.hidden = false;
}

formulario.addEventListener('submit', async (evento) => {
    evento.preventDefault();
    mensagem.hidden = true;

    const identificador = document.getElementById('identificador').value.trim();
    const senha = document.getElementById('senha').value;

    if (!identificador || !senha) {
        mostrarErro('Preencha o e-mail (ou telefone) e a senha.');
        return;
    }

    botaoEntrar.disabled = true;
    try {
        const corpo = new URLSearchParams({ identificador, senha });
        const resposta = await fetch(`${API_URL}/api/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: corpo,
            credentials: 'include'
        });
        if (resposta.ok) {
            window.location.href = 'painel.html';
        } else if (resposta.status === 401) {
            mostrarErro('Credenciais inválidas ou conta desactivada.');
        } else {
            mostrarErro('Não foi possível iniciar sessão. Tente novamente.');
        }
    } catch (erro) {
        mostrarErro('Sem ligação ao servidor. Verifique se o backend está a correr.');
    } finally {
        botaoEntrar.disabled = false;
    }
});
