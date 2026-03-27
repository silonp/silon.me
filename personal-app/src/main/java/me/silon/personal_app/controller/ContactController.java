package me.silon.personal_app.controller;

import com.google.cloud.recaptchaenterprise.v1.RecaptchaEnterpriseServiceClient;
import com.google.recaptchaenterprise.v1.Assessment;
import com.google.recaptchaenterprise.v1.CreateAssessmentRequest;
import com.google.recaptchaenterprise.v1.Event;
import com.google.recaptchaenterprise.v1.ProjectName;
import com.resend.core.exception.ResendException;
import me.silon.personal_app.service.ResendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    @Autowired
    ResendService resendService;

    @Value("${recaptcha.sitekey}")
    private String recaptchaSiteKey;

    @Value("${spring.cloud.gcp.project-id}")
    private String projectID;

    @PostMapping
    public ResponseEntity<?> submitContactForm(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("message") String message,
            @RequestParam("g-recaptcha-response") String recaptchaToken) {

        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name is required!"));
        }
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required!"));
        }
        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message is required!"));
        }
        if (recaptchaToken == null || recaptchaToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "reCAPTCHA token is missing!"));
        }

        try (RecaptchaEnterpriseServiceClient client = RecaptchaEnterpriseServiceClient.create()) {

            Event event = Event.newBuilder().setSiteKey(recaptchaSiteKey).setToken(recaptchaToken).build();

            CreateAssessmentRequest createAssessmentRequest =
                    CreateAssessmentRequest.newBuilder()
                            .setParent(ProjectName.of(projectID).toString())
                            .setAssessment(Assessment.newBuilder().setEvent(event).build())
                            .build();

            Assessment response = client.createAssessment(createAssessmentRequest);

            if (!response.getTokenProperties().getValid()) {
                return ResponseEntity.internalServerError().body(Map.of("error", "reCAPTCHA verification failed!"));
            }

            // TODO: Check if Action matches the action from FE.

            if (response.getRiskAnalysis().getScore() < 0.5) {
                return ResponseEntity.status(400).body(Map.of("error", "Suspicious activity detected!"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error!"));
        }

        try {
            resendService.sendEmail(name, email, message);
        } catch (ResendException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to send the message!"));
        }

        return ResponseEntity.ok(Map.of("success", true, "message", "Contact form submitted successfully!"));
    }
}