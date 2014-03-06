package controllers;

import play.*;
import play.mvc.Controller;
import play.mvc.Result;
import java.io.File;

public class FileService extends Controller {
       static String path = "/tmp/";
       public static Result getFile(String file){
              File myfile = new File (Play.application().path().getAbsolutePath() + path + file);
              return ok(myfile);
       }
}