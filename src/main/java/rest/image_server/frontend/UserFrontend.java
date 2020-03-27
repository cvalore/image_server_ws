package rest.image_server.frontend;

import rest.image_server.model.Link;
import rest.image_server.model.User;

import java.util.List;

public class UserFrontend {
      public static String getUsersRepresentation(List<User> users) {
            StringBuilder stringBuilder = new StringBuilder();
            for(User u : users) {
                  stringBuilder.append(getUserRepresentation(u, ""));
            }
            String uploadHtml =
                        "<h3> Insert name of the user to create </h3>" +
                        "<form id = \"my_form\" method=\"post\" enctype=\"text/plain\" onsubmit=\"myFunction()\">" +
                        "    <input type=\"text\" name=\"name\">" +
                        "    <input type=\"submit\" value=\"Create it\">" +
                        "</form>" +
                        "<script>" +
                        "function myFunction(){" +
                        "    var action_src = window.location.href + \"/raw_add/\" + document.getElementsByName(\"name\")[0].value;" +
                        "    var my_form = document.getElementById('my_form');" +
                        "    my_form.action = action_src ;" +
                        "}" +
                        "</script>";
            return
                    "<html>" +
                            "<head> <title> Registered users - Image Server RESTful API service </title> </head>" +
                            "<body> <h1> Registered users </h1>" + uploadHtml + stringBuilder.toString() + "</body>" +
                            "</html>";
      }

      public static String getUserRepresentation(User u, String title) {
            String pre = "";
            String last = "";
            if(!title.isEmpty()) {
                  pre =  "<html>" +
                          "<head> <title> " + title + " - Image Server RESTful API service </title> </head>" +
                          "<body> <h1> " + title + "</h1>";
                  last = "</body> </html>";
            }
            return
                    pre +
                            "<h2> User " + u.getUuid() + "</h2>" +
                            "<h3> Name </h3>" + u.getName() +
                            getLinksRepresentation(u) + "<br>" +
                            last;
      }

      private static String getLinksRepresentation(User u) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("<h3> Useful links </h3>");
            for(Link l : u.getLinks()) {
                  stringBuilder.append(LinkFrontend.getLinkRepresentation(l));
            }
            stringBuilder.append(LinkFrontend.getLinkRepresentation(u.getUploadFolderLink()));
            return stringBuilder.toString();
      }
}
