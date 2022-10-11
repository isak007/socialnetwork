package com.ftn.socialnetwork.util.mail;

public interface IEmailService {

    void sendMessage(String to, String subject, String text);
}
