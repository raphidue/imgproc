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
import java.util.Arrays;

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
			double[][] filter;
			String id = json.findPath("id").toString();
			filter = convertJsonToMatrix(json);
			String uploadPath = Play.application().path().getAbsolutePath() + "/public/uploads/" + id + ".jpg";
			try {
				BufferedImage im = ImageIO.read(new File(uploadPath));
				
				// Histogramm erstellen
				int w = im.getWidth();
				int h = im.getHeight();

				//from here---------------------------------------------
				// copyImage() function?!
				BufferedImage copy;
				copy = im;
			
				// Filteroperation
				for (int v = 1; v <= h-2; v++) {
					for (int u = 1; u <= w-2; u++) {
						double sum = 0;
						for (int j = -1; j <= 1; j++) {
							for (int i = -1; i <= 1; i++) {
								//int p = copy.getPixel(u+i,v+j);
								int p = copy.getRaster().getPixel(u+i, v+j, (int[]) null)[0];
								// get the corresponding filter coefficient:
								double c = filter[j+1][i+1];
								sum = sum + c * p;
							}
						}
						
						int q = (int) Math.round(sum);
						q = checkPixel(q);
						im.getRaster().setSample(u,v,0,q);
						//orig.putPixel(u,v,q);
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

	public static Result median(){
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

				//-----------------to here
				BufferedImage copy;
				copy = im;
				// Filteroperation
				int[] P = new int[9];

			    for (int v=1; v<=h-2; v++) {
			        for (int u=1; u<=w-2; u++) {
			 
				        //fill the pixel vector P for filter position (u,v)
				        int k = 0;
				        for (int j=-1; j<=1; j++) {
				            for (int i=-1; i<=1; i++) {
				                P[k] = copy.getRaster().getPixel(u+i, v+j, (int[]) null)[0];
				                k++;
				            }
				        }
				        //sort the pixel vector and take center element
				        Arrays.sort(P);
				        im.getRaster().setSample(u,v,0,P[4]);
			        }
			    }


				ImageIO.write(im,"JPG",new File(uploadPath)); 
				
				// Histogramm erstellen
				respJSON = generateHisto(id + ".jpg");
			} catch(IOException ioe) {
				respJSON.put("error", "Error on processing median filter...");
			}
			return ok(respJSON);			
		}
	}
	
	public static Result minimum() {
		ObjectNode respJSON = Json.newObject();
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest("Expecting Json data");
		} else {
			String id = json.findPath("id").toString();
			String uploadPath = Play.application().path().getAbsolutePath() + "/public/uploads/" + id + ".jpg";
			try {
				BufferedImage dst = ImageIO.read(new File(uploadPath));
				
				
				int width = dst.getWidth();
        		int height = dst.getHeight();

				//-----------------to here
				BufferedImage copy;
				copy = dst;
				// Filteroperation
				

				//int index = 0;
				//int[] outPixels = new int[width * height];

				
				//int[] inPixels = copy.getRGB(0, 0, width, height, null, 0, 0);

				//filter pixels
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pixel = 0xffffffff;
						int pixel2 = 0xffffffff;
						int pixel3 = 0xffffffff;
						for (int dy = -1; dy <= 1; dy++) {
							int iy = y+dy;
							if (0 <= iy && iy < height) {
								for (int dx = -1; dx <= 1; dx++) {
									int ix = x+dx;
									if (0 <= ix && ix < width) {
										pixel2 = copy.getRaster().getPixel(y+dy, x+dx, (int[]) null)[0];
										pixel3 = copy.getRaster().getPixel(y, x, (int[]) null)[0];
										pixel = (int) Math.min(pixel2, pixel3);
										pixel = checkPixel(pixel);
									}
								}
							}
						}
						//inPixels[index++] = pixel;
						
						
						
						dst.getRaster().setSample(x,y,0,pixel);
					}
				}


				//inPixels = outPixels;
				
				//dst.setRGB(0, 0, width, height, inPixels, 0, 0 );


				ImageIO.write(dst,"JPG",new File(uploadPath)); 
				
				// Histogramm erstellen
				respJSON = generateHisto(id + ".jpg");
			} catch(IOException ioe) {
				respJSON.put("error", "Error on processing minimum filter...");
			}
			return ok(respJSON);			
		}
	}

	public static Result maximum() {
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

				//-----------------to here
				BufferedImage copy;
				copy = im;
				// Filteroperation
				
				

				ImageIO.write(im,"JPG",new File(uploadPath)); 
				
				// Histogramm erstellen
				respJSON = generateHisto(id + ".jpg");
			} catch(IOException ioe) {
				respJSON.put("error", "Error on processing median filter...");
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
	// Wandelt ein JSON in eine Matrix
	private static double[][] convertJsonToMatrix(JsonNode json) {
		double[][] filter = new double[3][3];

		for (int r = 1; r <= 3; r++) {
			for (int c = 1; c <= 3; c++) {
				String str = "r" + r + "c" + c;
				double tmp = Double.parseDouble(json.findPath(str).toString());
				filter[r-1][c-1] = tmp;
			}
		}

		return filter;
	}
	
	// Pixelgrenzen beachten 
	private static int checkPixel(int pixel) {
		if (pixel > 255)
			pixel = 255;
		if (pixel < 0)
			pixel = 0; 
		return pixel;
	}
}
