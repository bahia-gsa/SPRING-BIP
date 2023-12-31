package afpa.mra.controllers;

import afpa.mra.entities.Utilisateur;
import afpa.mra.entities.PasswordUpdate;
import afpa.mra.entities.RezMdpObject;
import afpa.mra.repositories.UtilisateurRepository;
import afpa.mra.security.DecryptService;
import afpa.mra.services.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/reset")
public class PasswordController {

    @Autowired
    private UtilisateurRepository utilisateurRepository;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private PasswordService passwordService;
    @Autowired
    private DecryptService decryptService;


    @PostMapping("/resetform")
    public ResponseEntity<Map<String, String>> sendResetEmail(@RequestParam("email") String email) {
        Optional<Utilisateur> utilisateur = utilisateurRepository.findByEmail(email);
        if (utilisateur.isPresent()) {
            System.out.println(utilisateur.get().getNom());
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(utilisateur.get().getEmail());
            message.setSubject("Réinitialisation du mot de passe");
            Random random = new Random();
            int randomNumber = random.nextInt(999999) + 100000;
            passwordService.saveResetCode(utilisateur.get(), randomNumber);
            message.setText("Le code de réinitialisation du mot de passe est : " + randomNumber);
            mailSender.send(message);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Email evoyé");
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Utilisateur introuvable");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping("/change")
    public ResponseEntity<?> resetpassword(@RequestBody RezMdpObject rezMdpObject){
        String checkParam = passwordService.isCodeValid(rezMdpObject.getCode(), rezMdpObject.getEmail());
        switch (checkParam) {
            case "Utilisateur non trouvé":
                return ResponseEntity.badRequest().body("Utilisateur non trouvé");
            case "Code non trouvé":
                return ResponseEntity.badRequest().body("Code non trouvé");
            case "Code expired":
                return ResponseEntity.badRequest().body("Code expired");
        }
        Optional<Utilisateur> utilisateur = utilisateurRepository.findByEmail(rezMdpObject.getEmail());
        String encryptedPassword = passwordEncoder.encode(rezMdpObject.getPassword());
        Utilisateur user = utilisateur.get();
        user.setMdp(encryptedPassword);
        utilisateurRepository.save(user);
        return ResponseEntity.status(HttpStatus.OK).body("{\"message\": \"Password reset completed with success\"}");
    }

    @PutMapping("/updatePwd/{userId}")
    public ResponseEntity<?> updatePassword(@PathVariable Long userId, @RequestBody PasswordUpdate passwordUpdate) {

        Optional<Utilisateur> optionalUtilisateur = utilisateurRepository.findById(userId);
        System.out.println(passwordUpdate.getAncienMdp());

        if (optionalUtilisateur.isPresent()) {
            Utilisateur utilisateur = optionalUtilisateur.get();
            String ancienMdpDecrypt = this.decryptService.decrypt(passwordUpdate.getAncienMdp());
            String nouveauMdpDecrypt = this.decryptService.decrypt(passwordUpdate.getNouveauMdp());
            String mdpInBdd = utilisateur.getMdp();
            System.out.println("ancienMdpDecrypt =>" + ancienMdpDecrypt);
            System.out.println("nouveauMdpDecrypt =>" + nouveauMdpDecrypt);
            System.out.println("mdpInBdd =>" + mdpInBdd);

            if (this.passwordEncoder.matches(ancienMdpDecrypt, mdpInBdd)) {
                String nouveauMdpRecrypt = passwordEncoder.encode(nouveauMdpDecrypt);
                utilisateur.setMdp(nouveauMdpRecrypt);
                System.out.println("nouveauMdpRecrypt =>" + nouveauMdpRecrypt);

                utilisateurRepository.save(utilisateur);
                return ResponseEntity.ok("Mot de passe mis à jour avec succès!");
            }
            else {
                return ResponseEntity.badRequest().body("Ancien mot de passe incorrect!");
            }
        }
        else {
            return ResponseEntity.notFound().build();
        }
    }
}
