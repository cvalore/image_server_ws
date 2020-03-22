package rest.image_server.frontend;

import rest.image_server.model.Link;

public class LinkFrontend {
      public static String getLinkRepresentation(Link l) {
            return "<a href=\"" + l.getLink() + "\"> " + l.getRel() + "</a>" + "  " + l.getLink() + "<br>";
      }
}
