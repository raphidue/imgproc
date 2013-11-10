package controllers;

import play.*;
import play.mvc.*;
import play.mvc.Http.MultipartFormData;


import java.sql.*;
import java.util.*;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import javax.sql.*;
import play.libs.Json;
import play.data.DynamicForm;
import play.data.Form;

import play.libs.Json;
import com.fasterxml.jackson.databind.node.ObjectNode;
import views.html.*;
import views.html._include.*;

public class Contact extends Controller {


	public static Result test() {
		
		DynamicForm data = Form.form().bindFromRequest();
		ObjectNode respJSON = Json.newObject();

		String name = data.get("name");
		String email = data.get("email");
		String imessage = data.get("message");

		
		final String username = "imgproc.contact@gmail.com";
		final String password = "imgproc.mailpass";
		final String recipient = "imgproc.contact@gmail.com";
		
		
		
		  //Get the session object  
		Properties props = new Properties();  
		props.put("mail.smtp.host", "smtp.gmail.com");  
		props.put("mail.smtp.socketFactory.port", "465");  
		props.put("mail.smtp.socketFactory.class",  
			"javax.net.ssl.SSLSocketFactory");  
		props.put("mail.smtp.auth", "true");  
		props.put("mail.smtp.port", "465");  
		
		Session session = Session.getDefaultInstance(props,  
			new javax.mail.Authenticator() {  
				protected PasswordAuthentication getPasswordAuthentication() {  
					return new PasswordAuthentication(username,password);
				}  
			});  
		
		  //compose message  
		try {  
			MimeMessage message = new MimeMessage(session);  
			message.setFrom(new InternetAddress(username));
			message.addRecipient(Message.RecipientType.TO,new InternetAddress(recipient));  
			message.setSubject("Hello");  
			message.setText("Testing.......");  
			
		   //send message  
			Transport.send(message);   
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}  
		

		respJSON.put("name", name);
		respJSON.put("email", email);
		respJSON.put("message", imessage);
		return ok(respJSON);
	}
}
