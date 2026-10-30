# Ratbank

Aplicação educacional que simula uma conta bancária. O front-end usa Angular; a API usa Java, Spring Boot, Spring Security, JWT, Spring Data JPA e SQLite. Não movimenta dinheiro real.

## Funcionalidades

- Cadastro e login com senha protegida por BCrypt.
- Consulta do saldo, dados da conta e chave PIX.
- Depósito e saque na conta autenticada.
- Transferência PIX entre contas Ratbank.
- Histórico de depósitos, saques e transferências recebidas/enviadas.
- Interface responsiva para celular e desktop.

## Requisitos

- Java 25 e Maven (ou Maven Wrapper).
- Node.js compatível com Angular 21 (Node 20.19 ou superior) e npm.

## Configuração e execução

O SQLite cria o arquivo `BackEndApi/ratbank.db` automaticamente na primeira execução. Não é necessário instalar ou iniciar um servidor de banco de dados. Opcionalmente, configure a chave JWT:

```sh
export JWT_SECRET='uma-chave-secreta-com-pelo-menos-32-caracteres'
```

Inicie a API:

```sh
cd BackEndApi
./mvnw spring-boot:run
```

Em outro terminal, instale as dependências e inicie o Angular:

```sh
cd FrontEnd
npm install
npm start
```

O Angular fica disponível em `http://localhost:4200` e a API em `http://localhost:8080`. O endereço da API está definido em `FrontEnd/src/app/app.component.ts`.

## Rotas principais

| Método | Rota | Acesso |
| --- | --- | --- |
| `POST` | `/auth/create-account` | Público |
| `POST` | `/auth/login` | Público |
| `GET` | `/account` | JWT |
| `GET` | `/account/transactions` | JWT |
| `PUT` | `/account/deposit` | JWT |
| `PUT` | `/account/withdraw` | JWT |
| `POST` | `/transation` | JWT |

Operações protegidas recebem `Authorization: Bearer <token>`. Depósito e saque aceitam `{ "value": 25.50 }`. PIX aceita `{ "pixReceived": "chave-do-destinatario", "value": 25.50 }`; a conta remetente é identificada pelo token.

O saldo inicial é R$ 100,00 para facilitar a demonstração. Valores são armazenados com precisão decimal de duas casas. O histórico registra os movimentos de forma transacional junto às alterações de saldo. O arquivo do banco fica localmente em `BackEndApi/ratbank.db` e está ignorado pelo Git.
