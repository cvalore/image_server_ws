package rest.image_server.services;

import rest.image_server.exceptions.DataNotFoundException;
import rest.image_server.model.Database;
import rest.image_server.model.User;
import rest.image_server.resources.ImageResource;
import rest.image_server.resources.UserResource;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UserService {
//      private Map<String, User> users = Database.getUsers();
      private ImageService imageService = new ImageService();

      Connection conn = null;
      Statement stmt = null;
      String query = null;

      public UserService() {
      }

      public List<User> getUsers() {

            List<User> users = new ArrayList();

            try {
                  conn = Database.getConnection();
                  stmt = conn.createStatement();
                  query = "SELECT * FROM Users;";
                  ResultSet res = stmt.executeQuery(query);
                  while(res.next()){
                        User newuser = new User(res.getString("uuid"), res.getString("name"));
                        newuser.addLink(res.getString("selfLink"), "self");
                        newuser.addLink(res.getString("usersLink"), "users");
                        newuser.addLink(res.getString("imagesLink"), "images");
                        newuser.addUploadFolderLink(res.getString("uploadFolderLink"), "upload_folder");
                        users.add(newuser);
                  }
                  res.close();
            } catch (SQLException e) {}
            finally {
                  try {
                        if (stmt != null){
                              conn.close();
                        }
                  } catch (SQLException e) {}
                  try {
                        if (conn != null) {
                              conn.close();
                        }
                  } catch (SQLException e) {
                        e.printStackTrace();
                  }
            }
            return users;
      }

      public User getUser(String uuid) {
            User user = null;
            try {
                  conn = Database.getConnection();
                  stmt = conn.createStatement();
                  query = "SELECT * FROM Users WHERE uuid = '" + uuid + "'";
                  ResultSet res = stmt.executeQuery(query);
                  while(res.next()){
                        user = new User(res.getString("uuid"), res.getString("name"));
                        user.addLink(res.getString("selfLink"), "self");
                        user.addLink(res.getString("usersLink"), "users");
                        user.addLink(res.getString("imagesLink"), "images");
                        user.addUploadFolderLink(res.getString("uploadFolderLink"), "upload_folder");
                  }
                  res.close();
            } catch (SQLException e) {}
            finally {
                  try {
                        if (stmt != null){
                              conn.close();
                        }
                  } catch (SQLException e) {}
                  try {
                        if (conn != null) {
                              conn.close();
                        }
                  } catch (SQLException e) {
                        e.printStackTrace();
                  }
            }

            if(user == null) {
                  throw new DataNotFoundException("User with uuid " + uuid + " not found");
            }
            return user;
      }

      public User addUser(User user) {

            String uuid = UUID.randomUUID().toString().split("-")[0];

            try {
                  conn = Database.getConnection();
                  stmt = conn.createStatement();
                  query = "INSERT INTO Users (uuid, name) VALUES ('" + uuid + "', '" + user.getName() + "')";
                  stmt.executeUpdate(query);

            } catch (SQLException e) {}
            finally {
                  try {
                        if (stmt != null){
                              conn.close();
                        }
                  } catch (SQLException e) {}
                  try {
                        if (conn != null) {
                              conn.close();
                        }
                  } catch (SQLException e) {
                        e.printStackTrace();
                  }
            }

            user.setUuid(uuid);
            imageService.addImage(user.getUuid(), null, null);
            return user;
      }

      public User updateUser(User user) {

            int updatedRows = 0;
            try {
                  conn = Database.getConnection();
                  stmt = conn.createStatement();
                  query = "UPDATE Users " +
                          "SET name = '" + user.getName() +"' WHERE uuid = '" + user.getUuid() + "'";
                  updatedRows = stmt.executeUpdate(query);

            } catch (SQLException e) {}
            finally {
                  try {
                        if (stmt != null){
                              conn.close();
                        }
                  } catch (SQLException e) {}
                  try {
                        if (conn != null) {
                              conn.close();
                        }
                  } catch (SQLException e) {
                        e.printStackTrace();
                  }
            }

            if(updatedRows == 0)
                  return null;
            return user;
      }

      public User removeUser(String uuid) {
            /*Delete directory*/
            User user = null;
            String retrieveUser;
            String query2;
          try {
              conn = Database.getConnection();
              stmt = conn.createStatement();
              retrieveUser = "SELECT * FROM Users WHERE uuid = '" + uuid + "'";
              ResultSet res = stmt.executeQuery(retrieveUser);
              while(res.next()){
                  user = new User(res.getString("uuid"), res.getString("name"));
                  user.addLink(res.getString("selfLink"), "self");
                  user.addLink(res.getString("usersLink"), "users");
                  user.addLink(res.getString("imagesLink"), "images");
                  user.addUploadFolderLink(res.getString("uploadFolderLink"), "upload_folder");
              }
              res.close();
              query = "DELETE FROM Users WHERE uuid = '" + uuid + "'";
              query2 = "DELETE FROM Images WHERE uuid = '" + uuid + "'";        //TODO DA RIVEDERE
              stmt.executeUpdate(query);
              stmt.executeUpdate(query2);

          } catch (SQLException e) {}
          finally {
              try {
                  if (stmt != null){
                      conn.close();
                  }
              } catch (SQLException e) {}
              try {
                  if (conn != null) {
                      conn.close();
                  }
              } catch (SQLException e) {
                  e.printStackTrace();
              }
          }

            if(user != null) {
                  String path = "upload_" + uuid + File.separator;
                  File file = new File(path);
                  String[] entries = file.list();
                  for (String s : entries) {
                        File currentFile = new File(file.getPath(), s);
                        currentFile.delete();
                  }
                  file.delete();
            }
            //Database.getImagesByUsers().remove(uuid);
            return user;
      }


      public void addLinks(UriInfo uriInfo, User user) {
            String uuid = user.getUuid();

            try {
                  conn = Database.getConnection();
                  stmt = conn.createStatement();
                  query = "UPDATE Users " +
                          "SET (selfLink, userslink, imageslink, uploadfolderlink) = ('" + getUriForSelf(uriInfo, user) +"', '" + getUriForUsers(uriInfo) +"', '" +
                           getUriForImages(uriInfo) +"', '" + getUriForUploadFolder(uriInfo, user) +"') WHERE uuid = '" + uuid + "'";
                  stmt.executeUpdate(query);
            } catch (SQLException e) {}
            finally {
                  try {
                        if (stmt != null){
                              conn.close();
                        }
                  } catch (SQLException e) {}
                  try {
                        if (conn != null) {
                              conn.close();
                        }
                  } catch (SQLException e) {
                        e.printStackTrace();
                  }
            }
            user.addLink(getUriForSelf(uriInfo, user), "self");
            user.addLink(getUriForUsers(uriInfo), "users");
            user.addLink(getUriForImages(uriInfo), "images");
            user.addUploadFolderLink(getUriForUploadFolder(uriInfo, user), "upload_folder");
      }

      private String getUriForSelf(UriInfo uriInfo, User user) {
            return uriInfo
                    .getBaseUriBuilder()
                    .path(UserResource.class)
                    .path(user.getUuid())
                    .build()
                    .toString();
      }

      private String getUriForUsers(UriInfo uriInfo) {
            return uriInfo
                    .getBaseUriBuilder()
                    .path(UserResource.class)
                    .build()
                    .toString();
      }

      private String getUriForUploadFolder(UriInfo uriInfo, User user) {
            return uriInfo
                    .getBaseUriBuilder()
                    .path(ImageResource.class)
                    .path(user.getUuid())
                    .build()
                    .toString();
      }

      private String getUriForImages(UriInfo uriInfo) {
            return uriInfo
                    .getBaseUriBuilder()
                    .path(ImageResource.class)
                    .build()
                    .toString();
      }
}
