//package com.app.studentromania.email;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import javax.activation.DataHandler;
//import javax.activation.DataSource;
//import javax.mail.MessagingException;
//import javax.mail.Session;
//import javax.mail.internet.InternetAddress;
//import javax.mail.internet.MimeBodyPart;
//import javax.mail.internet.MimeMessage;
//import javax.mail.internet.MimeMultipart;
//import javax.mail.util.ByteArrayDataSource;
//import java.io.ByteArrayOutputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Properties;
//
//import com.google.api.client.auth.oauth2.Credential;
//import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
//import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
//import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
//import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
//import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
//import com.google.api.client.http.javanet.NetHttpTransport;
//import com.google.api.client.json.JsonFactory;
//import com.google.api.client.json.gson.GsonFactory;
//import com.google.api.client.util.Base64;
//import com.google.api.client.util.store.FileDataStoreFactory;
//import com.google.api.services.gmail.Gmail;
//import com.google.api.services.gmail.GmailScopes;
//import com.google.api.services.gmail.model.Label;
//import com.google.api.services.gmail.model.ListLabelsResponse;
//import com.google.api.services.gmail.model.Message;
//
//@Component
//public class EmailHandlerV2 {
//    /**
//     * Application name.
//     */
//    private static final String APPLICATION_NAME = "Gmail API Java Quickstart";
//    /**
//     * Global instance of the JSON factory.
//     */
//    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
//    /**
//     * Directory to store authorization tokens for this application.
//     */
//    private static final String TOKENS_DIRECTORY_PATH = "tokens";
//
//    /**
//     * Global instance of the scopes required by this quickstart.
//     * If modifying these scopes, delete your previously saved tokens/ folder.
//     */
//    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_LABELS);
//    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
//
//    private Gmail gmailService;
//
//    /**
//     * Creates an authorized Credential object.
//     *
//     * @param HTTP_TRANSPORT The network HTTP Transport.
//     * @return An authorized Credential object.
//     * @throws IOException If the credentials.json file cannot be found.
//     */
//    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
//            throws IOException {
//        // Load client secrets.
//        InputStream in = EmailHandlerV2.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
//        if (in == null) {
//            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
//        }
//        GoogleClientSecrets clientSecrets =
//                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
//
//        // Build flow and trigger user authorization request.
//        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
//                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
//                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
//                .setAccessType("offline")
//                .build();
//        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
//        //returns an authorized Credential object.
//        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
//    }
//
//    public Message sendHtmlEmail(String userId, String recipientEmail,
//            String fromEmail, String subject, String htmlBody) throws MessagingException, IOException {
//        // Setup Mail Session
//        Properties props = new Properties();
//        Session session = Session.getDefaultInstance(props, null);
//
//        // Create a MimeMessage using the session created above
//        MimeMessage email = new MimeMessage(session);
//        email.setFrom(new InternetAddress(fromEmail));
//        email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(recipientEmail));
//        email.setSubject(subject);
//        email.setContent(htmlBody, "text/html; charset=utf-8");
//
//        // Encode and wrap the MimeMessage into a Gmail Message
//        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//        email.writeTo(buffer);
//        byte[] bytes = buffer.toByteArray();
//        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
//        Message message = new Message();
//        message.setRaw(encodedEmail);
//
//        // Send the email
//        message = gmailService.users().messages().send(userId, message).execute();
//
//        System.out.println("HTML email sent successfully.");
//        return message;
//    }
//
//    public Message sendEmail(String userId, String to, String subject, String bodyText) throws IOException {
//        String emailBody = createRawEmailString(to, subject, bodyText);
//        byte[] emailBytes = emailBody.getBytes("UTF-8");
//        String encodedEmail = Base64.encodeBase64URLSafeString(emailBytes);
//
//        Message message = new Message();
//        message.setRaw(encodedEmail);
//        message = gmailService.users().messages().send(userId, message).execute();
//
//        System.out.println("Sent email with ID: " + message.getId());
//        return message;
//    }
//
//    public static Message sendEmailWithImage(Gmail service, String userId, String to,
//            String from, String subject, byte[] imageBytes,
//            String imageName) throws MessagingException, IOException {
//        Properties props = new Properties();
//        Session session = Session.getDefaultInstance(props, null);
//
//        MimeMessage email = new MimeMessage(session);
//        email.setFrom(new InternetAddress(from));
//        email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
//        email.setSubject(subject);
//
//        // Create the email body part
//        MimeBodyPart htmlPart = new MimeBodyPart();
//        htmlPart.setContent(htmlBody, "text/html; charset=utf-8");
//
//        // Create the image part
//        MimeBodyPart imagePart = new MimeBodyPart();
//        DataSource fds = new ByteArrayDataSource(imageBytes, "image/png"); // Adjust the type accordingly
//        imagePart.setDataHandler(new DataHandler(fds));
//        imagePart.setHeader("Content-ID", "<imageId>"); // Ensure this matches the CID in your HTML
//        imagePart.setFileName(imageName);
//
//        // Combine the parts
//        MimeMultipart multipart = new MimeMultipart("related");
//        multipart.addBodyPart(htmlPart);
//        multipart.addBodyPart(imagePart);
//
//        // Set the multipart as the email content
//        email.setContent(multipart);
//
//        // Encode and send the email
//        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//        email.writeTo(buffer);
//        byte[] bytes = buffer.toByteArray();
//        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
//        Message message = new Message();
//        message.setRaw(encodedEmail);
//        message = service.users().messages().send(userId, message).execute();
//
//        System.out.println("Email sent with image embedded.");
//        return message;
//    }
//
//    // Helper method to create a raw MIME string for an email
//    private String createRawEmailString(String to, String subject, String bodyText) {
//        List<String> headers = new ArrayList<>();
//        headers.add("To: " + to);
//        headers.add("From: " + from);
//        headers.add("Subject: " + subject);
//        headers.add("Content-Type: text/plain; charset=\"UTF-8\"");
//        headers.add("MIME-Version: 1.0");
//
//        String headerString = String.join("\r\n", headers);
//        return headerString + "\r\n\r\n" + bodyText;
//    }
//
//}
