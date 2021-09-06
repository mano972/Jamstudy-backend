package com.app.studentromania.email;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.app.studentromania.enumtype.ErrorsEnum;
import com.app.studentromania.util.LogUtils;

@Component
public class EmailHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailHandler.class);

	private static final String user = "unistart@gmail.com";
	private static final String pass = "Cabalache_2107";

	public ErrorsEnum sendEmail(String to, String subject, String message) {
		return handleSendEmail(to, subject, message, null);
	}

	public ErrorsEnum sendEmail(String to, String subject, String message, Map<String, String> mapInlineImage) {
		return handleSendEmail(to, subject, message, mapInlineImage);
	}

	private ErrorsEnum handleSendEmail(String to, String subject, String message, Map<String, String> mapInlineImages) {

		Session session = null;
		Properties mailProps = getEmailProperties();

		try {
			session = Session.getInstance(mailProps, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(user, pass);
				}
			});
		} catch (Exception e) {
			LogUtils.logMessage(LOGGER, "Email authentication error. " + to);
			return ErrorsEnum.EMAIL_AUTH_ERROR;
		}
		LogUtils.logMessage(LOGGER, "Mail session created = " + session.getProperties().toString());

		try {

			Message mimeMessage = new MimeMessage(session);
			mimeMessage.setFrom(new InternetAddress(user));
			mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			mimeMessage.setSubject(subject);

			Multipart multipart = new MimeMultipart();
			StringBuilder emailMessage = new StringBuilder(message);

			emailMessage.append("\n");

			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(emailMessage.toString());
//			messageBodyPart.setContent(emailMessage.toString(), "text/html");
			multipart.addBodyPart(messageBodyPart);

			// adds inline image attachments
			if (mapInlineImages != null && mapInlineImages.size() > 0) {
				Set<String> setImageID = mapInlineImages.keySet();

				for (String contentId : setImageID) {
					MimeBodyPart imagePart = new MimeBodyPart();
					imagePart.setHeader("Content-ID", "<" + contentId + ">");
					imagePart.setDisposition(MimeBodyPart.INLINE);

					String imageFilePath = mapInlineImages.get(contentId);
					try {
						imagePart.attachFile(imageFilePath);
					} catch (IOException ex) {
						ex.printStackTrace();
					}

					multipart.addBodyPart(imagePart);
				}
			}

			mimeMessage.setContent(multipart);

			Transport.send(mimeMessage);
		} catch (MessagingException e) {
			LogUtils.logMessage(LOGGER, "Error. Email was not sent to email address: " + to);
			ErrorsEnum.EMAIL_SENDING_ERROR.setErrorDescription(e.getStackTrace().toString());
			return ErrorsEnum.EMAIL_SENDING_ERROR;
		}

		LogUtils.logMessage(LOGGER, "Email was sent succesfully to email address: " + to);
		return ErrorsEnum.NO_ERROR;
	}

	private Properties getEmailProperties() {
		Properties properties = new Properties();
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.port", "587");
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		return properties;
	}

}
