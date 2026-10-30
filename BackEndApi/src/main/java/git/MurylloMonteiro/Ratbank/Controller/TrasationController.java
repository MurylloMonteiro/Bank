package git.MurylloMonteiro.Ratbank.Controller;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import git.MurylloMonteiro.Ratbank.Model.DTO.TrasationDTO.TransationRequest;
import git.MurylloMonteiro.Ratbank.Service.AccountService;
import git.MurylloMonteiro.Ratbank.Service.Jwt.JwtService;

@RestController
@RequestMapping("/transation")
public class TrasationController {
    private final AccountService accounts;
    private final JwtService jwt;

    public TrasationController(AccountService accounts, JwtService jwt) { this.accounts = accounts; this.jwt = jwt; }

    @PostMapping
    public ResponseEntity<Void> pix(@RequestHeader("Authorization") String authorization, @RequestBody TransationRequest request) {
        if (request == null || request.getValue() == null || request.getPixReceived() == null || request.getPixReceived().isBlank())
            throw new IllegalArgumentException("Informe uma chave PIX e um valor válido.");
        UUID userId = UUID.fromString(jwt.getClaims(authorization.replace("Bearer ", "")).getSubject());
        accounts.transfer(userId, request.getPixReceived(), request.getValue());
        return ResponseEntity.accepted().build();
    }
}
