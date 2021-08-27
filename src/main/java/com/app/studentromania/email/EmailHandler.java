package com.app.studentromania.email;

import java.util.Properties;

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

import com.app.studentromania.enumtype.ErrorsEnum;
import com.app.studentromania.util.LogUtils;

public class EmailHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailHandler.class);

	private static final String user = "unistart@gmail.com";
	private static final String pass = "Cabalache_2107";

	private ErrorsEnum handleSendMail(String to, String subject, String message) {

		Session session = null;
		Properties mailProps = getEmailProperties();

		try {
			session = Session.getInstance(mailProps, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(user, pass);
				}
			});
		} catch (Exception e) {
			LogUtils.logError(LOGGER, "handleSendMail", e);
			return ErrorsEnum.EMAIL_AUTH_ERROR;
		}
		LogUtils.logMessage(LOGGER, "--> Mail session created = " + session.getProperties().toString());

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
			messageBodyPart.setContent(emailMessage.toString(), "text/html");
			multipart.addBodyPart(messageBodyPart);

			mimeMessage.setContent(multipart);

			Transport.send(mimeMessage);
		} catch (MessagingException e) {
			LogUtils.logError(LOGGER, "handleSendMail", e);
			return ErrorsEnum.EMAIL_SENDING_ERROR;
		}

		LogUtils.logMessage(LOGGER, "<-- Email was sent succesfully!");
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
