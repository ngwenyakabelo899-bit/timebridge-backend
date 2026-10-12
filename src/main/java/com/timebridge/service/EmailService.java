package com.timebridge.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final RestClient restClient;
    private final String apiKey;
    private final String fromEmail;
    private final String fromName;

    public EmailService(@Value("${app.mail.brevo-api-key}") String apiKey,
                        @Value("${app.mail.from-email}") String fromEmail,
                        @Value("${app.mail.from-name}") String fromName) {
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
        this.fromName = fromName;

        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = RestClient.builder()
                .baseUrl("https://api.brevo.com/v3")
                .requestFactory(factory)
                .build();
    }

    // Returns true only if the email provider accepted the message.
    public boolean sendVerificationEmail(String toEmail, String firstName, String verifyUrl, int validMinutes) {
        Map<String, Object> payload = Map.of(
                "sender", Map.of("name", fromName, "email", fromEmail),
                "to", List.of(Map.of("email", toEmail)),
                "subject", "Verify your TimeBridge email address",
                "htmlContent", buildHtml(firstName, verifyUrl, validMinutes),
                "textContent", buildText(firstName, verifyUrl, validMinutes)
        );

        try {
            restClient.post()
                    .uri("/smtp/email")
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientResponseException e) {
            log.warn("Email provider rejected the request (HTTP {}): {}",
                    e.getStatusCode().value(), e.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            log.warn("Could not reach the email provider: {}", e.getClass().getSimpleName());
            return false;
        }
    }

    private String buildHtml(String firstName, String verifyUrl, int validMinutes) {
        String template = """
                <div style="background:#f4f6fb;padding:32px 16px;font-family:Arial,Helvetica,sans-serif;">
                  <table role="presentation" style="width:560px;max-width:100%;margin:0 auto;background:#ffffff;border-radius:8px;border:1px solid #e2e8f0;">
                    <tr><td style="padding:28px 32px 8px;">
                      <div style="font-size:22px;font-weight:700;color:#1d4ed8;">TimeBridge</div>
                      <div style="font-size:12px;color:#64748b;">Smart Queue Management</div>
                    </td></tr>
                    <tr><td style="padding:16px 32px;color:#0f172a;font-size:15px;line-height:1.6;">
                      <p style="margin:0 0 12px;">Hello {{name}},</p>
                      <p style="margin:0 0 12px;">We received a registration for a TimeBridge account using this email address. To keep accounts secure, please confirm that this address belongs to you.</p>
                      <p style="margin:24px 0;"><a href="{{url}}" style="background:#1d4ed8;color:#ffffff;text-decoration:none;padding:12px 24px;border-radius:6px;font-weight:600;display:inline-block;">Verify email address</a></p>
                      <p style="margin:0 0 8px;font-size:13px;color:#64748b;">This link expires in {{minutes}} minutes and can be used once. If the button does not work, copy this address into your browser:</p>
                      <p style="margin:0;font-size:12px;word-break:break-all;color:#1d4ed8;">{{url}}</p>
                    </td></tr>
                    <tr><td style="padding:16px 32px 28px;border-top:1px solid #e2e8f0;font-size:12px;color:#64748b;">
                      If you did not register for TimeBridge, you can safely ignore this email. No account access is granted until the address is verified.
                    </td></tr>
                  </table>
                </div>
                """;
        return template
                .replace("{{name}}", escapeHtml(firstName))
                .replace("{{url}}", escapeHtml(verifyUrl))
                .replace("{{minutes}}", String.valueOf(validMinutes));
    }

    private String buildText(String firstName, String verifyUrl, int validMinutes) {
        return "Hello " + firstName + ",\n\n"
                + "We received a registration for a TimeBridge account using this email address. "
                + "Please confirm that this address belongs to you by opening this link:\n\n"
                + verifyUrl + "\n\n"
                + "The link expires in " + validMinutes + " minutes and can be used once.\n\n"
                + "If you did not register for TimeBridge, you can safely ignore this email.";
    }

    // Names come from users, so escape them before putting them in HTML
    private String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
