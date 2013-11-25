package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html._include.*;
import views.html.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import java.io.File;
import play.*;
import play.libs.Json;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

//for buffered image
import java.awt.Graphics2D; 
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Image extends Controller {

	public static Result upload(String id) {		
		MultipartFormData body = request().body().asMultipartFormData();
		FilePart picture = body.getFile("picture");
		String path = Play.application().path().getAbsolutePath() + "/public/uploads";
		File theDir = new File(path);
		ObjectNode respJSON = Json.newObject();
				
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
			
			try {
				// Bild einlesen
				BufferedImage input = ImageIO.read(new File(myUploadPath));
				
				// in 8bit Bild konvertieren 
				BufferedImage im = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_BYTE_GRAY); 
				Graphics2D g2d = im.createGraphics();
				// Bild rendern
				g2d.drawImage(input,0,0,null);
				// das resultierende Bild Speichern
				ImageIO.write(im,"JPG",new File(myUploadPath));
				
				// Histogramm erstellen
				int[] H = new int[256];
				int w = im.getWidth();
				int h = im.getHeight();			
				
				for (int v = 0; v < h; v++) {
					for (int u = 0; u < w; u++) {
						// Pixel abspeichern 
						int i = im.getRaster().getPixel(u, v, (int[]) null)[0];
						H[i] = H[i] + 1;						
					}
				}
				respJSON.put("test", "Hallo");
			} catch(IOException ioe) {
			}			
			return ok(respJSON);
		} else {
			flash("error", "Missing file");
			return redirect(routes.Application.index());    
		}
	}
}
