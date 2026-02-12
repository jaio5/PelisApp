package alicanteweb.pelisapp.controller;
import alicanteweb.pelisapp.service.EmailConfirmationService;
import alicanteweb.pelisapp.service.EmailConfirmationService.EmailConfirmationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/confirm-account")
public class ConfirmAccountController {

    private final EmailConfirmationService emailConfirmationService;

    @Autowired
    public ConfirmAccountController(EmailConfirmationService emailConfirmationService) {
        this.emailConfirmationService = emailConfirmationService;
    }

    @GetMapping
    public ResponseEntity<String> confirmAccount(@RequestParam("token") String token) {
        EmailConfirmationResult result = emailConfirmationService.confirmAccount(token);
        if (result.success()) {
            return ResponseEntity.ok(result.message());
        } else {
            return ResponseEntity.badRequest().body(result.message());
        }
    }
}
