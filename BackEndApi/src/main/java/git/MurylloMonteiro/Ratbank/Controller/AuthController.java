package git.MurylloMonteiro.Ratbank.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import git.MurylloMonteiro.Ratbank.Model.DTO.LoginDTO.LoginResponse;

import git.MurylloMonteiro.Ratbank.Model.DTO.LoginDTO.LoginResquest;
import git.MurylloMonteiro.Ratbank.Model.DTO.UserDTO.UserRequest;
import git.MurylloMonteiro.Ratbank.Service.AccountService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AccountService accountService;

    @PostMapping("/create-account")
    public ResponseEntity<String> createAccount(@RequestBody UserRequest user) {

        return accountService.createAccount(user);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginAasdccount(@RequestBody LoginResquest login) {
        return accountService.loginAccount(login.getEmail(), login.getPassword());
    }

}
