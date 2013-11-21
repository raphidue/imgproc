package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html._include.*;
import views.html.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import java.io.File;
import play.*;

//for buffered image
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class Image extends Controller {
	
	private static BufferedImage img = null;
	private static File fileToConvert = null;

	public static Result upload(String id) {
		MultipartFormData body = request().body().asMultipartFormData();
		FilePart picture = body.getFile("picture");
		String path = Play.application().path().getAbsolutePath() + "/public/uploads";
		
		File theDir = new File(path);

		// erstelle Ordner uploads wenn nicht existiert
		if (!theDir.exists()) {
			boolean result = theDir.mkdir();  
		}

	  
		if (picture != null) {
			String fileName = picture.getFilename();
			String contentType = picture.getContentType(); 
			File file = picture.getFile();



			String myUploadPath = path + "/" + id;
			file.renameTo(new File(myUploadPath));
			fileToConvert = file;

			return ok(views.html.image.render(id));
		} else {
			flash("error", "Missing file");
			return redirect(routes.Application.index());    
		}
	}

	private static void convertBufImage() {
		try {
			img = ImageIO.read(fileToConvert);
		} catch (IOException e){ 
			//to do
		}
	}

	public BufferedImage getBufImage() {
		return img;
	}

}
