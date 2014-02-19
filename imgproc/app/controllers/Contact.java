package controllers;

import play.mvc.*;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import play.libs.Json;
import play.data.DynamicForm;
import play.data.Form;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class Contact extends Controller {


	public static Result sendEmail() {
		
		DynamicForm data = Form.form().bindFromRequest();
		ObjectNode respJSON = Json.newObject();

		String name = data.get("name");
		String email = data.get("email");
		String imessage = data.get("message");


		final String username = "imgproc.contact@gmail.com";
		final String password = "imgproc.mailpass";
		final String recipient = "imgproc.contact@gmail.com";
		String subject = "Name: " + name + " Email: " + email;
		

		  //Get the session object  
		Properties props = new Properties();  
		props.put("mail.smtp.host", "smtp.gmail.com");  
		props.put("mail.smtp.socketFactory.port", "465");  
		props.put("mail.smtp.socketFactory.class",  
			"javax.net.ssl.SSLSocketFactory");  
		props.put("mail.smtp.auth", "true");  
		props.put("mail.smtp.port", "465");  

		Session session = Session.getInstance(props,  
			new javax.mail.Authenticator() {  
				protected PasswordAuthentication getPasswordAuthentication() {  
					return new PasswordAuthentication(username,password);
				}  
			});

		  //compose message  
		try {  
			MimeMessage message = new MimeMessage(session);  
			message.setFrom(new InternetAddress(username));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));  
			message.setSubject(subject);  
			message.setText("You have got the following message from your contact form:\n"
			+ imessage);  

		   //send message  
			Transport.send(message);
			respJSON.put("status", "success");
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}  
		

		
		return ok(respJSON);
	}
}
