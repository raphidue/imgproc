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
	}
}