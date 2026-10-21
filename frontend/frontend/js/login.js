// Lógica do ecrã de início de sessão (US32)

const formulario = document.getElementById('formulario-login');
const botaoEntrar = document.getElementById('botao-entrar');
const mensagem = document.getElementById('mensagem');

/** Mostra uma mensagem de erro por baixo do formulário. */
function mostrarErro(texto) {
    mensagem.textContent = texto;
    mensagem.hidden = false;
}

formulario.addEventListener('submit', async (evento) => {
    // Impede o envio normal do formulário; o envio é feito por fetch
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
        // O Spring Security espera os campos em formato de formulário
        const corpo = new URLSearchParams({ identificador, senha });

        const resposta = await fetch(`${API_URL}/api/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: corpo,
            credentials: 'include'
        });

        if (resposta.ok) {
            // Sessão criada: o navegador já guardou o cookie
            window.location.href = 'painel.html';
        } else if (resposta.status === 401) {
            mostrarErro('Credenciais inválidas ou conta desactivada.');
        } else {
            mostrarErro('Não foi possível iniciar sessão. Tente novamente.');
        }
    } catch (erro) {
        mostrarErro('Sem ligação ao servidor. Verifique a sua rede.');
    } finally {
        botaoEntrar.disabled = false;
    }
});
