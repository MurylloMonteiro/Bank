package git.MurylloMonteiro.Ratbank.Controller;

import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import git.MurylloMonteiro.Ratbank.Model.TransationModel;
import git.MurylloMonteiro.Ratbank.Model.DTO.Account.DepositRequest;
import git.MurylloMonteiro.Ratbank.Model.DTO.Account.WithDrawRequest;
import git.MurylloMonteiro.Ratbank.Model.DTO.UserDTO.UserResponse;
import git.MurylloMonteiro.Ratbank.Service.AccountService;
import git.MurylloMonteiro.Ratbank.Service.Jwt.JwtService;

@RestController
@RequestMapping("/account")
public class AccountController {
    private final AccountService accounts;
    private final JwtService jwt;

    public AccountController(AccountService accounts, JwtService jwt) { this.accounts = accounts; this.jwt = jwt; }

    @GetMapping
    public UserResponse details(@RequestHeader("Authorization") String authorization) {
        return accounts.getAll(userId(authorization));
    }

    @GetMapping("/transactions")
    public List<TransationModel> history(@RequestHeader("Authorization") String authorization) {
        return accounts.history(userId(authorization));
    }

    @PutMapping("/deposit")
    public ResponseEntity<Void> deposit(@RequestHeader("Authorization") String authorization, @RequestBody DepositRequest request) {
        if (request == null || request.getValue() == null) throw new IllegalArgumentException("Informe o valor do depósito.");
        accounts.deposit(userId(authorization), request.getValue());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/withdraw")
    public ResponseEntity<Void> withdraw(@RequestHeader("Authorization") String authorization, @RequestBody WithDrawRequest request) {
        if (request == null || request.getValue() == null) throw new IllegalArgumentException("Informe o valor do saque.");
        accounts.withdraw(userId(authorization), request.getValue());
        return ResponseEntity.ok().build();
    }

    private UUID userId(String authorization) {
        return UUID.fromString(jwt.getClaims(authorization.replace("Bearer ", "")).getSubject());
    }
}
