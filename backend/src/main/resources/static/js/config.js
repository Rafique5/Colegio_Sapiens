// Endereço da API. Vazio quando as páginas são servidas pelo próprio Spring Boot (:8080).
const API_URL = (location.port === '8080' || location.port === '') ? '' : 'http://localhost:8080';
