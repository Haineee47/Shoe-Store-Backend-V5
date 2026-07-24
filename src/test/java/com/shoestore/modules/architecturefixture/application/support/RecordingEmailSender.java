package com.shoestore.modules.architecturefixture.application.support;

import com.shoestore.modules.architecturefixture.application.port.EmailSenderPort;
import java.util.Objects;

public final class RecordingEmailSender implements EmailSenderPort {

  private int sendCallCount;
  private String lastRecipient;
  private String lastSubject;
  private String lastContent;

  @Override
  public void send(String recipient, String subject, String content) {

    lastRecipient = Objects.requireNonNull(recipient, "recipient must not be null");

    lastSubject = Objects.requireNonNull(subject, "subject must not be null");

    lastContent = Objects.requireNonNull(content, "content must not be null");

    sendCallCount++;
  }

  public int sendCallCount() {
    return sendCallCount;
  }

  public String lastRecipient() {
    return lastRecipient;
  }

  public String lastSubject() {
    return lastSubject;
  }

  public String lastContent() {
    return lastContent;
  }
}
