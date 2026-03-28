package me.silon.personal_app.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ResendService {

    @Value("${RESEND_API_KEY}")
    private String resendApiKey;

    @Value("${EMAIL_TO}")
    private String emailTo;

    @Value("${EMAIL_FROM}")
    private String emailFrom;

    public void sendEmail(String nameCompany, String returnEmail, String message) throws ResendException {
        Resend resend = new Resend(resendApiKey);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(emailFrom)
                .to(emailTo)
                .replyTo(returnEmail)
                .subject("silon.me - Contact Form")
                .html(buildSimpleHtmlEmail(nameCompany, message))
                .build();

        resend.emails().send(params);
    }

    private String buildSimpleHtmlEmail(String nameCompany, String message) {
        return """
                <h3>New Inquiry</h3>
                <p><b>From:</b> %s</p>
                <hr>
                <p><b>Message Content:</b></p>
                <p>%s</p>
                <br>
                <p><i>Hit reply to answer the user.</i></p>
                """.formatted(nameCompany, message);
    }
}