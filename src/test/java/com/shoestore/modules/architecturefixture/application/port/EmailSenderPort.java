package com.shoestore.modules.architecturefixture.application.port;

public interface EmailSenderPort {

  void send(String recipient, String subject, String content);
}
