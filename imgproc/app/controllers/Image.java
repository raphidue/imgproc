package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html._include.*;
import views.html.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import java.io.File;
import play.*;

public class Image extends Controller {
	

	public static Result upload(String id) {
		MultipartFormData body = request().body().asMultipartFormData();
		FilePart picture = body.getFile("picture");
		String path = Play.application().path().getAbsolutePath() + "/public/uploads";
		
<<<<<<< HEAD
		File theDir = new File(path);
=======
		File theDir = new File(Play.application().path().getAbsolutePath() + "/public/uploads");
>>>>>>> origin/ivan_0.1

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
			return ok(views.html.image.render(id));
		} else {
			flash("error", "Missing file");
			return redirect(routes.Application.index());    
		}
	}	
}
