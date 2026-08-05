package com.hrflow.hrflow_backend.exceptionHandler.mails;

import com.hrflow.hrflow_backend.exceptionHandler.BaseException;

public class EmailSendingException extends BaseException {
  public EmailSendingException(String message) {
    super(message, "EMAIL_SENDING");
  }
}
