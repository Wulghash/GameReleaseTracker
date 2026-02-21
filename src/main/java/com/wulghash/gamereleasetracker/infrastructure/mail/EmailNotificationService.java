package com.wulghash.gamereleasetracker.infrastructure.mail;

import com.wulghash.gamereleasetracker.domain.model.Game;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.base-url}")
    private String baseUrl;

    public void sendReleaseToday(String to, Game game, UUID unsubscribeToken) {
        String subject = game.getTitle() + " is out today!";
        String body = """
                Great news! %s is available now.

                Release date: %s
                Platforms: %s
                %s

                ---
                Unsubscribe: %s/api/v1/unsubscribe/%s
                """.formatted(
                game.getTitle(),
                game.getReleaseDate(),
                game.getPlatforms(),
                game.getShopUrl() != null ? "Shop: " + game.getShopUrl() : "",
                baseUrl, unsubscribeToken
        );

        send(to, subject, body);
    }

    public void sendReleaseSoon(String to, Game game, UUID unsubscribeToken) {
        String subject = game.getTitle() + " releases in 7 days!";
        String body = """
                Heads up! %s releases in one week.

                Release date: %s
                Platforms: %s
                %s

                ---
                Unsubscribe: %s/api/v1/unsubscribe/%s
                """.formatted(
                game.getTitle(),
                game.getReleaseDate(),
                game.getPlatforms(),
                game.getShopUrl() != null ? "Shop: " + game.getShopUrl() : "",
                baseUrl, unsubscribeToken
        );

        send(to, subject, body);
    }

    public void sendCancellation(String to, Game game) {
        String subject = game.getTitle() + " has been cancelled";
        String body = """
                We're sorry to let you know that %s has been cancelled and will no longer be released.

                Your subscription has been removed automatically.
                """.formatted(game.getTitle());

        send(to, subject, body);
    }

    private void send(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to {} — {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
