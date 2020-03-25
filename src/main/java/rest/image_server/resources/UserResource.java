package rest.image_server.resources;

import rest.image_server.exceptions.DataNotFoundException;
import rest.image_server.exceptions.GenericException;
import rest.image_server.frontend.UserFrontend;
import rest.image_server.model.User;
import rest.image_server.services.UserService;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.util.List;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.TEXT_HTML)
public class UserResource {
      private UserService userService = new UserService();

      @GET
      public String getUsers() {
            List<User> users = userService.getUsers();
            return UserFrontend.getUsersRepresentation(users);
      }

      @POST
      public String addUser(User user, @Context UriInfo uriInfo) {
            User newUser = userService.addUser(user);

            /*Create directory*/
            String path = "upload_" + newUser.getUuid() + File.separator;

            File file = new File(path);
            boolean created = file.mkdir();
            if(!created) {
                  throw new GenericException("Cannot create " + path + " folder");
            }

            /*Add links*/
            userService.addLinks(uriInfo, newUser);

            return UserFrontend.getUserRepresentation(newUser, "User added");
      }

      @GET
      @Path("/{user_uuid}")
      public String getUser(@PathParam("user_uuid") String uuid) {
            User user = userService.getUser(uuid);
            return UserFrontend.getUserRepresentation(user, "User requested");
      }

      @PUT
      @Path("/{user_uuid}")
      public String updateUser(@PathParam("user_uuid") String uuid, User user, @Context UriInfo uriInfo) {
            if(userService.getUser(uuid) == null) {
                  throw new DataNotFoundException("User with uuid " + uuid + " not found");
            }
            user.setUuid(userService.getUser(uuid).getUuid());
            User updatedUser = userService.updateUser(user);

            userService.addLinks(uriInfo, updatedUser);           //TODO

            return UserFrontend.getUserRepresentation(updatedUser, "User updated");
      }

      @DELETE
      @Path("/{user_uuid}")
      public String deleteUser(@PathParam("user_uuid") String uuid) {
            User user = userService.removeUser(uuid);
            return UserFrontend.getUserRepresentation(user, "User deleted");
      }


}
