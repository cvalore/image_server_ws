package rest.image_server.frontend;

import rest.image_server.model.Image;
import rest.image_server.model.Link;

import java.util.List;

public class ImageFrontend {
      public static String getImagesRepresentation(List<Image> images, String title, String uploadUser) {
            if (title.equals("Images deleted")) {
                  return "<html>" +
                          "<head> <title> " + title + " - Image Server RESTful API service </title> </head>" +
                          "<body> <h1> " + title + "</h1>" +
                  "</body> </html>";
            }

            StringBuilder stringBuilder = new StringBuilder();
            for(Image i : images) {
                  stringBuilder.append(getImageRepresentation(i, ""));
            }
            String uploadHtml = "";
            if(! uploadUser.isEmpty()) {
                  uploadHtml =
                              "<h1> Upload an image </h1>" +
                              "<form action=\""+ uploadUser + "\" method=\"post\" enctype=\"multipart/form-data\">" +
                              "<p>" +
                                    "Select a file : <input type=\"file\" name=\"file\" size=\"45\" />" +
                              "</p>" +
                              "<input type=\"submit\" value=\"Upload it\" />" +
                              "</form>";
            }

            return
                    "<html>" +
                            "<head> <title> "+ title + " - Image Server RESTful API service </title> </head>" +
                            "<body> <h1> " + title + "</h1>" + uploadHtml + stringBuilder.toString() + "</body>" +
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

            if (title.equals("Image deleted")) {
                  return
                          pre + last;
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


            return pre +
                            "<h2> Image " + image.getUuid() + " - " + image.getTitle() + " </h2>" +

                            "<img src=\"" + stringBuilder.toString() + "\" alt=\"" + image.getTitle() +
                            "\" width=\"" + image.getWidth() + "\" height=\"" + image.getHeight() + "\">" +

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
