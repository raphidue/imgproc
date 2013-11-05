package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html._include.*;
import views.html.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import java.io.*;
import play.*;

public class Image extends Controller {
	
	public static Result upload() {
	  MultipartFormData body = request().body().asMultipartFormData();
	  FilePart picture = body.getFile("picture");
	  if (picture != null) {
	    String fileName = picture.getFilename();
	    String contentType = picture.getContentType(); 
	    File file = picture.getFile();

        String myUploadPath = Play.application().path().getAbsolutePath() + "/public/uploads/" + fileName;
        file.renameTo(new File(myUploadPath));
		
	    return ok("File uploaded");
	  } else {
	    flash("error", "Missing file");
	    return redirect(routes.Application.index());    
	  }
	}
}