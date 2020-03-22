package rest.image_server.frontend;

import rest.image_server.model.Image;
import rest.image_server.model.Link;

import java.util.List;

public class ImageFrontend {
      public static String getImagesRepresentation(List<Image> images, String title) {
            StringBuilder stringBuilder = new StringBuilder();
            for(Image i : images) {
                  stringBuilder.append(getImageRepresentation(i, ""));
            }
            return
                    "<html>" +
                            "<head> <title> "+ title + " - Image Server RESTful API service </title> </head>" +
                            "<body> <h1> " + title + "</h1>" + stringBuilder.toString() + "</body>" +
                            "</html>";
      }

      public static String getImageRepresentation(Image image, String title) {
            String pre = "";
            String last = "";
            if(!title.isEmpty()) {
                  pre =  "<html>" +
                          "<head> <title> " + title + " - Image Server RESTful API service </title> </head>" +
                          "<body> <h1> " + title + "</h1>";
                  last = "</body> </html>";
            }

            String selfLink = "";
            for(Link l : image.getLinks()) {
                  if(l.getRel().equals("self")) {
                        selfLink = l.getLink();
                  }
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(selfLink);
            stringBuilder.append("/raw");


            return
                    pre +
                    "<h2> Image " + image.getUuid() + " - " + image.getTitle() + " </h2>" +

                        "<img src=\""+ stringBuilder.toString() + "\" alt=\"" + image.getTitle() +
                        "\" width=\"" + image.getWidth() + "\" height=\""+ image.getHeight() + "\">" +

                    getLinksRepresentation(image) + "<br>" +
                    last;
      }

      private static String getLinksRepresentation(Image image) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("<h3> Useful links </h3>");
            for(Link l : image.getLinks()) {
                  stringBuilder.append(LinkFrontend.getLinkRepresentation(l));
            }
            return stringBuilder.toString();
      }
}
