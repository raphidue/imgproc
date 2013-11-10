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
	
<<<<<<< HEAD
	public static Result upload() {
	  MultipartFormData body = request().body().asMultipartFormData();
	  FilePart picture = body.getFile("picture");
	  if (picture != null) {
	    String fileName = picture.getFilename();
	    String contentType = picture.getContentType(); 
	    File file = picture.getFile();

        String myUploadPath = Play.application().path().getAbsolutePath() + "/public/uploads/" + fileName;
        file.renameTo(new File(myUploadPath));
		
	    return ok(views.html.processing.render(scripts.render(), navigation.render("processing"), footer.render(), "uploads/" + fileName));
	  } else {
	    flash("error", "Missing file");
	    return redirect(routes.Application.index());    
	  }
=======
	public static Result upload(String id) {
		MultipartFormData body = request().body().asMultipartFormData();
		FilePart picture = body.getFile("picture");
		
		File theDir = new File(Play.application().path().getAbsolutePath() + "/public" + uploads);

		// erstelle Ordner uploads wenn nicht existiert
		if (!theDir.exists()) {
			boolean result = theDir.mkdir();  
		}

	  
		if (picture != null) {
			String fileName = picture.getFilename();
			String contentType = picture.getContentType(); 
			File file = picture.getFile();

			String myUploadPath = Play.application().path().getAbsolutePath() + "/public/uploads/" + id;
			file.renameTo(new File(myUploadPath));
			return ok(views.html.image.render(id));
		} else {
			flash("error", "Missing file");
			return redirect(routes.Application.index());    
		}
>>>>>>> origin/master
	}
}