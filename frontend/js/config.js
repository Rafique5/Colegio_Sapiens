// Configuracao do frontend.
// Em producao (mesmo dominio do backend) a URL e relativa.
// Em desenvolvimento (Live Server noutra porta) aponta para localhost:8080.
const API_URL = (location.port === '8080' || location.port === '') ? '' : 'http://localhost:8080';