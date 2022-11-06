package com.ftn.socialnetwork.util.mail;

import com.ftn.socialnetwork.util.exception.InvalidEmailException;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import static java.lang.String.format;

@Service
public class EmailService implements IEmailService{

    private static final String FROM = "virtualconnect6500@outlook.com";

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendMessage(String to, String subject, String text) {

        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(FROM);
            mailMessage.setTo(to);
            mailMessage.setSubject(subject);
            mailMessage.setText(text);

            mailSender.send(mailMessage);
        }catch (MailException e){
            throw new InvalidEmailException(format("Email to an address '%s' could not be sent.",to));
        }

    }
}
