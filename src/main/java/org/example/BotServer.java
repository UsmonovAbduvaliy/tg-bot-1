package org.example;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.SendMessage;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import static com.pengrad.telegrambot.model.MessageEntity.Type.email;
import static org.example.Main.bot;

public class BotServer {

    public static void server(Update update) {
        try {
            String text = update.message().text();
            Long chatId = update.message().chat().id();
            User user = findUser(chatId);
            if (text != null && text.equals("/start")) {
                SendMessage message = new SendMessage(
                        chatId,
                        "Iltimos email kiriting 🌠"
                );
                message.replyMarkup(new ReplyKeyboardRemove());
                bot.execute(message);
                user.setStatus(Status.EMAIL);
            } else {
                if (user.getStatus().equals(Status.EMAIL)) {
                    if (isValidEmail(text)) {
                        user.setEmail(text);
                        String verificationCode = generateVerificationCode();
                        user.setVerificationCode(verificationCode);


                        sendEmail(
                                text,
                                "Tasdiqlash kodi",
                                "Salom! Sizning tasdiqlash kodingiz: " + verificationCode
                        );


                        SendMessage message = new SendMessage(
                                chatId,
                                "Emailga tasdiqlash kodi yuborildi. Iltimos, kodni kiriting."
                        );
                        bot.execute(message);
                        user.setStatus(Status.VERIFICATION);
                    } else {

                        SendMessage message = new SendMessage(
                                chatId,
                                "Email noto'g'ri formatda. Iltimos, to'g'ri email kiriting."
                        );
                        bot.execute(message);
                    }
                } else if (user.getStatus().equals(Status.VERIFICATION)) {
                    if (text.equals(user.getVerificationCode())) {
                        SendMessage message = new SendMessage(
                                chatId,
                                "Rahmat! Tasdiqlash kodi to'g'ri. ✅\nTizimga kirish uchun 1 -ni bosing."
                        );
                        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup("1");
                        message.replyMarkup(markup);
                        bot.execute(message);
                        user.setStatus(Status.CABINET);
                    } else {
                        SendMessage message = new SendMessage(
                                chatId.toString(),
                                "Tasdiqlash kodi noto'g'ri. Iltimos, qayta urinib ko'ring. ❌"
                        );
                        bot.execute(message);
                    }
                } else if (user.getStatus().equals(Status.CABINET)) {
                    if (text.equals("1") || user.getVerified()) {
                        user.setVerified(true);
                        if (user.getPayments().isEmpty()) {
                            SendMessage message = new SendMessage(
                                    chatId,
                                    "Paymentlar mavjud emas.\n" +
                                            "1 - Payment qoshish.\n" +
                                            "2 - Report"
                            );
                            message.replyMarkup(new ReplyKeyboardMarkup("1", "2"));
                            bot.execute(message);
                        } else {
                            StringBuilder buffer = new StringBuilder();
                            for (Payment payment : user.getPayments()) {
                                buffer.append(payment).append("\n");
                            }
                            SendMessage message = new SendMessage(
                                    chatId,
                                    buffer +
                                            "1 - Payment qoshish.\n" +
                                            "2 - Report"
                            );
                            message.replyMarkup(new ReplyKeyboardMarkup("1", "2"));
                            bot.execute(message);
                        }
                        user.setStatus(Status.TIZIM);
                    }
                } else if (user.getStatus().equals(Status.TIZIM)) {
                    if (text.equals("1")) {
                        SendMessage message = new SendMessage(
                                chatId,
                                "Miqdorni kiriting: "
                        );
                        message.replyMarkup(new ReplyKeyboardRemove());
                        bot.execute(message);
                        user.setStatus(Status.ADDPAYMENT);
                    } else if (text.equals("2")) {
                        SendMessage message = new SendMessage(
                                chatId,
                                reportPayment(user)
                        );
                        message.replyMarkup(new ReplyKeyboardMarkup("0 - Orqaga"));
                        bot.execute(message);
                        user.setStatus(Status.CABINET);
                    }else {
                        SendMessage message = new SendMessage(
                                chatId,
                                "Invalid option"
                        );
                        user.setStatus(Status.CABINET);
                        bot.execute(message);
                    }
                } else if (user.getStatus().equals(Status.ADDPAYMENT)) {
                    Payment payment = user.getCurrentPayment();
                    if (payment == null) {
                        payment = new Payment();
                        user.setCurrentPayment(payment);
                    }

                    if (payment.getNext()) {
                        payment.setAmount(Integer.parseInt(text));
                        payment.setNext(false);
                        payment.setHave(true);
                    }

                    if (payment.getHave()) {
                        payment.setHave(false);
                        SendMessage message = new SendMessage(
                                chatId,
                                "To'lov turini tanlang:\n1-Payme\n2-Uzum\n3-Click"
                        );
                        message.replyMarkup(new ReplyKeyboardMarkup("1","2","3"));
                        bot.execute(message);
                        payment.setCome(true);
                    } else if (payment.getCome()) {
                        switch (text) {
                            case "1":
                                payment.setType(Type.PAYME);
                                break;
                            case "2":
                                payment.setType(Type.UZUM);
                                break;
                            case "3":
                                payment.setType(Type.CLICK);
                                break;
                            default:
                                SendMessage error = new SendMessage(
                                        chatId,
                                        "Noto'g'ri tanlov! Iltimos, qaytadan tanlang."
                                );
                                bot.execute(error);
                                return;
                        }

                        payment.setCome(false);
                        SendMessage message = new SendMessage(
                                chatId,
                                "To'lov muvaffaqiyatli qo'shildi ✅\n0 - Orqaga"
                        );
                        user.getPayments().add(payment);
                        user.setCurrentPayment(null);
                        message.replyMarkup(new ReplyKeyboardMarkup("0"));
                        bot.execute(message);
                        user.setStatus(Status.CABINET);
                    }
                }

            }
        } catch (
                Exception e) {
            e.printStackTrace();
        }
    }



    private static String reportPayment(User user) {

        if(user.getPayments().isEmpty()) {
            return "Payment yoq";
        }
        int payme =0;
        int click =0;
        int uzum =0;

        for (Payment payment : user.getPayments()) {
            if(payment.getType().equals(Type.PAYME)){
                payme+=payment.getAmount();
            }if(payment.getType().equals(Type.CLICK)){
                click+=payment.getAmount();
            }if(payment.getType().equals(Type.UZUM)){
                uzum+=payment.getAmount();
            }
        }

        return "Payme -> "+payme+"\nClick -> "+click+"\nUzum -> "+uzum;
    }


    private static void sendEmail(String toEmail, String subject, String body) {
        final String fromEmail = "inosukebest1@gmail.com";
        final String password = "";

        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("Email muvaffaqiyatli yuborildi!");
        } catch (MessagingException e) {
            System.out.println("kod yuborilmadi: " + e.getMessage());
        }
    }

    private static User findUser(Long chatId) {
        for (User user : DB.USERS) {
            if (user.getChatId().equals(chatId)) {
                return user;
            }
        }
        User user = new User();
        user.setChatId(chatId);
        DB.USERS.add(user);
        DB.export();
        return user;
    }

    private static String generateVerificationCode() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }


    private static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }
}