package org.example;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Vector;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class User {
    private Long chatId;
    private Status status= Status.START;
    private String email;
    private String verificationCode;
    private Boolean verified;
    private Payment currentPayment;
    List<Payment> payments = new Vector<>();
}
