# Colegio Sapiens - Frontend

Páginas em HTML, CSS e JavaScript que consomem a API do projecto `backend`.

## Como executar
1. Arrancar primeiro o backend (http://localhost:8080).
2. Servir esta pasta numa porta permitida pelo backend (5500, 3000, 5173 ou 8081).
   Qualquer uma destas opções serve:
   - VS Code com a extensão Live Server. Em Settings, procurar `liveServer.settings.host`
     e definir `localhost` (por omissão usa 127.0.0.1, o que impede o login).
   - Python: na pasta do frontend, `python -m http.server 5500`
   - Node: `npx serve -l 5500`
3. Abrir **http://localhost:5500** (sempre `localhost`, nunca `127.0.0.1`).
4. Entrar com `admin@sapiens.co.mz` e `Admin@12345`.

## Configuração
O endereço do backend está em `js/config.js` (`API_URL`).
Se usar outra porta para o frontend, acrescente-a em `sapiens.cors.origens` no
`application.properties` do backend.
