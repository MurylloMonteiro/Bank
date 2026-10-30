package git.MurylloMonteiro.Ratbank.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import git.MurylloMonteiro.Ratbank.Model.AccountModel;
import git.MurylloMonteiro.Ratbank.Model.TransationModel;
import git.MurylloMonteiro.Ratbank.Model.DTO.LoginDTO.LoginResponse;
import git.MurylloMonteiro.Ratbank.Model.DTO.UserDTO.UserRequest;
import git.MurylloMonteiro.Ratbank.Model.DTO.UserDTO.UserResponse;
import git.MurylloMonteiro.Ratbank.Model.UserModel;
import git.MurylloMonteiro.Ratbank.Repository.AccountRepository;
import git.MurylloMonteiro.Ratbank.Repository.TrasationRepository;
import git.MurylloMonteiro.Ratbank.Repository.UserRepository;
import git.MurylloMonteiro.Ratbank.Service.Jwt.JwtService;
import git.MurylloMonteiro.Ratbank.Utilits.PassWordUtil;

@Service
public class AccountService {
    private final AccountRepository accounts;
    private final UserRepository users;
    private final TrasationRepository transactions;
    private final PassWordUtil passwords;
    private final JwtService jwt;

    public AccountService(AccountRepository accounts, UserRepository users,
            TrasationRepository transactions, PassWordUtil passwords, JwtService jwt) {
        this.accounts = accounts;
        this.users = users;
        this.transactions = transactions;
        this.passwords = passwords;
        this.jwt = jwt;
    }

    @Transactional
    public ResponseEntity<String> createAccount(UserRequest input) {
        if (input == null || blank(input.getUsername()) || blank(input.getEmail()) || blank(input.getCpf())
                || input.getPassword() == null || input.getPassword().length() < 8) {
            throw new IllegalArgumentException("Preencha os dados e use uma senha com pelo menos 8 caracteres.");
        }
        String email = input.getEmail().trim().toLowerCase();
        String cpf = input.getCpf().trim();
        if (accounts.findByPixkey(cpf).isPresent())
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Este CPF já está cadastrado. Entre na conta existente.");
        if (users.findByEmail(email).isPresent())
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Este e-mail já está cadastrado. Entre na conta existente.");

        // Calcule o hash antes de qualquer escrita para não segurar o bloqueio de escrita do SQLite.
        String passwordHash = passwords.hashPassword(input.getPassword());
        AccountModel account = new AccountModel();
        account.setNumberAccount(generateAccountNumber());
        account.setPixkey(cpf);
        account.setDateTimeCreate(LocalDate.now());
        account.setBalance(BigDecimal.valueOf(100));
        account.setNegativeBalance(BigDecimal.ZERO);
        accounts.save(account);

        UserModel user = new UserModel();
        user.setUsername(input.getUsername().trim());
        user.setEmail(email);
        user.setCpf(cpf);
        user.setPhone_number(input.getPhone_number());
        user.setPassword(passwordHash);
        user.setAccount(account);
        users.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body("Conta criada com sucesso.");
    }

    public String generateAccountNumber() {
        String number;
        do { number = String.format("%08d", ThreadLocalRandom.current().nextInt(100_000_000)); }
        while (accounts.findByNumberAccount(number).isPresent());
        return number;
    }

    public ResponseEntity<LoginResponse> loginAccount(String email, String password) {
        if (blank(email) || blank(password)) throw new IllegalArgumentException("Informe e-mail e senha.");
        UserModel user = users.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("E-mail ou senha inválidos."));
        if (!passwords.verifyPassWord(password, user.getPassword()))
            throw new IllegalArgumentException("E-mail ou senha inválidos.");
        LoginResponse response = new LoginResponse();
        response.setToken(jwt.generateToken(user.getId()));
        return ResponseEntity.ok(response);
    }

    public UserResponse getAll(UUID userId) {
        UserModel user = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        AccountModel account = user.getAccount();
        UserResponse result = new UserResponse();
        result.setUsername(user.getUsername());
        result.setNumberAccount(account.getNumberAccount());
        result.setPixKey(account.getPixkey());
        result.setBalance(account.getBalance().doubleValue());
        result.setNegativeBalance(account.getNegativeBalance().doubleValue());
        return result;
    }

    @Transactional
    public void deposit(UUID userId, double amount) {
        requireAmount(amount);
        AccountModel account = ownAccount(userId);
        account.setBalance(account.getBalance().add(money(amount)));
        accounts.save(account);
        saveTransaction(account, account, amount, "DEPOSITO");
    }

    @Transactional
    public void withdraw(UUID userId, double amount) {
        requireAmount(amount);
        AccountModel account = ownAccount(userId);
        if (money(amount).compareTo(account.getBalance()) > 0) throw new IllegalArgumentException("Saldo insuficiente.");
        account.setBalance(account.getBalance().subtract(money(amount)));
        accounts.save(account);
        saveTransaction(account, account, amount, "SAQUE");
    }

    @Transactional
    public void transfer(UUID userId, String pixKey, double amount) {
        requireAmount(amount);
        AccountModel sender = ownAccount(userId);
        AccountModel receiver = accounts.findByPixkey(pixKey)
                .orElseThrow(() -> new IllegalArgumentException("Chave PIX não encontrada."));
        if (sender.getId().equals(receiver.getId())) throw new IllegalArgumentException("Não é possível transferir para sua própria conta.");
        if (money(amount).compareTo(sender.getBalance()) > 0) throw new IllegalArgumentException("Saldo insuficiente.");
        sender.setBalance(sender.getBalance().subtract(money(amount)));
        receiver.setBalance(receiver.getBalance().add(money(amount)));
        accounts.save(sender);
        accounts.save(receiver);
        saveTransaction(sender, receiver, amount, "PIX");
    }

    public List<TransationModel> history(UUID userId) {
        String number = ownAccount(userId).getNumberAccount();
        return transactions.findByNumberAccountEnvoyOrNumberAccoutReceivedOrderByDateTimeDesc(number, number);
    }

    private AccountModel ownAccount(UUID userId) {
        return users.findById(userId).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado.")).getAccount();
    }

    private void requireAmount(double amount) {
        if (!Double.isFinite(amount) || amount <= 0) throw new IllegalArgumentException("Informe um valor válido maior que zero.");
        if (BigDecimal.valueOf(amount).scale() > 2) throw new IllegalArgumentException("Use no máximo duas casas decimais.");
    }

    private boolean blank(String value) { return value == null || value.isBlank(); }

    private BigDecimal money(double amount) { return BigDecimal.valueOf(amount).setScale(2); }

    private void saveTransaction(AccountModel sender, AccountModel receiver, double amount, String type) {
        TransationModel item = new TransationModel();
        item.setDateTime(LocalDateTime.now());
        item.setNumberAccountEnvoy(sender.getNumberAccount());
        item.setNumberAccoutReceived(receiver.getNumberAccount());
        item.setPixKeyEnvoy(sender.getPixkey());
        item.setPixKeyReceived(receiver.getPixkey());
        item.setTransationValue(money(amount));
        item.setType(type);
        transactions.save(item);
    }
}
