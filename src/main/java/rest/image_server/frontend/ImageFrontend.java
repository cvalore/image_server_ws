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
                        break;
                  }
            }
            return
                    pre +
                    "<h2> Image " + image.getUuid() + " - " + image.getTitle() + " </h2>" +

                        "<img src=\""+ selfLink + "\" alt=\"" + title +
                        "\" width=\"400\" height=\"400\">" +

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
