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

public class Image extends Controller {

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
				
			} catch(IOException ioe) {
			}			
			return ok(views.html.image.render(id));
		} else {
			flash("error", "Missing file");
			return redirect(routes.Application.index());    
		}
	}
	
	public static Result showHist(String id) {
		ObjectNode respJSON = Json.newObject();
		
		respJSON = generateHisto(id);
						
		return ok(respJSON);	
	}
	
	public static Result smoothing() {
		ObjectNode respJSON = Json.newObject();
		JsonNode json = request().body().asJson();
		
		if(json == null) {
			return badRequest("Expecting Json data");
		} else {
			String id = json.findPath("id").toString();
			String uploadPath = Play.application().path().getAbsolutePath() + "/public/uploads/" + id + ".jpg";
			try {
				BufferedImage im = ImageIO.read(new File(uploadPath));
				
				// Histogramm erstellen
				int w = im.getWidth();
				int h = im.getHeight();
				
				// 3x3 Filtermatrix
				double[][] filter = {
					{0.075, 0.125, 0.075},
					{0.125, 1, 0.125},
					{0.075, 0.125, 0.075}
				};
				
				BufferedImage copy;
				copy = copyImage(im);
				
				WritableRaster raster = im.getRaster();
				
				// Filteroperation
				for (int v = 1; v <= h-2; v++) {
					for (int u = 1; u <= w-2; u++) {
						double sum = 0;
						for (int j = -1; j <= 1; j++) {
							for (int i = -1; i <= 1; i++) {
								int p = copy.getRaster().getPixel(u+i, v+j, (int[]) null)[0];
								double c = filter[j+1][i+1];
								sum = sum + c * p;
							}
						}
						
						int q = (int) Math.round(sum);
						q = checkPixel(q);
						raster.setSample(u,v,0,q);    
					}
				}
				ImageIO.write(im,"JPG",new File(uploadPath)); 
				
				// Histogramm erstellen
				respJSON = generateHisto(id + ".jpg");
			} catch(IOException ioe) {
				respJSON.put("error", "Error on processing smoothing filter...");
			}
			return ok(respJSON);			
		}
	}
	
	// generiert ein Histogramm
	public static ObjectNode generateHisto(String id) {
		int[] H = new int[256];
		String tmp;
		Integer tmpInt;
		ObjectNode respJSON = Json.newObject();
		
		try {
			// Bild einlesen
			BufferedImage im = ImageIO.read(new File(Play.application().path().getAbsolutePath() + "/public/uploads/" + id));
		
			// Histogramm erstellen
			int w = im.getWidth();
			int h = im.getHeight();			
		
			for (int v = 0; v < h; v++) {
				for (int u = 0; u < w; u++) {
					// Pixel abspeichern 
					int i = im.getRaster().getPixel(u, v, (int[]) null)[0];
					H[i] = H[i] + 1;						
				}
			}
			
			// Histogramm in JSON Object verpacken
			for (int i = 0; i < H.length; i++) {	
				tmpInt = new Integer(i);
				tmp = tmpInt.toString();			
				respJSON.put(tmp, new Integer(H[i]));
			}
		} catch(IOException ioe) {
			respJSON.put("error", "Error on creating histogram...");
		}
		return respJSON;
	} 
	
	// speichert das Bild in einen vergrößertes Bild für die Randbehandlung
	public static BufferedImage copyImage(BufferedImage src) {

		int w = src.getWidth();
		int h = src.getHeight();
		
		// Kopie des Bildes sowie randbehandlung des bildes
		BufferedImage copy = new BufferedImage(w+2, h+2, BufferedImage.TYPE_BYTE_GRAY);
			
		// Kopiebild auf komplett auf Weiß setzen
		for (int v = 0; v < h+2; v++) {
			for (int u = 0; u < w+2; u++) {
				copy.getRaster().setSample(u, v, 0, 255);
			}
		} 
		
		// Bild in das vergrößerte Bild kopieren
		for (int v = 0; v < h; v++) {
			for (int u = 0; u < w; u++) {
				copy.getRaster().setSample(u+1, v+1, 0, src.getRaster().getPixel(u, v, (int[]) null)[0]);
			}
		} 
		return copy;		
	}
	
	// Pixelgrenzen beachten 
	public static int checkPixel(int pixel) {
		if (pixel > 255)
			pixel = 255;
		if (pixel < 0)
			pixel = 0; 
		return pixel;
	}
}
