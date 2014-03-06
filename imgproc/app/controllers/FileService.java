package controllers;

import play.*;
import play.mvc.Controller;
import play.mvc.Result;
import java.io.File;
import plugins.S3Plugin;

public class FileService {
       //static String path = "/tmp/";
       public static String getFile(String file){
//              File myfile = new File ("http://imgproc.s3.amazonaws.com/" + file);
              String url = "http://imgproc.s3.amazonaws.com/" + file;
              return url;
       }
}