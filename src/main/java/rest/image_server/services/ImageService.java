package rest.image_server.services;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import rest.image_server.exceptions.DataNotFoundException;
import rest.image_server.exceptions.GenericException;
import rest.image_server.model.Database;
import rest.image_server.model.Image;
import rest.image_server.resources.ImageResource;
import rest.image_server.resources.UserResource;

import javax.imageio.ImageIO;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ImageService {
      private Map<String, List<Image>> imagesByUsers = Database.getImagesByUsers();

      private Connection conn = null;
      private Statement stmt = null;
      private String query = null;

      public List<Image> getImages() {
            List<Image> images = new ArrayList<>();
            Image image = null;
            try {
                  conn = Database.getConnection();
                  if(conn == null) {
                        throw new GenericException("Cannot connect to db");
                  }
                  stmt = conn.createStatement();
                  query = "SELECT * FROM Images;";
                  ResultSet res = stmt.executeQuery(query);
                  while(res.next()) {
                        image = new Image(res.getString("imageuuid"), res.getString("title"));
                        image.setPath(res.getString("path"));
                        image.setWidth(res.getInt("width"));
                        image.setHeight(res.getInt("height"));
                        image.addLink(res.getString("selfLink"), "self");
                        image.addLink(res.getString("usersLink"), "users");
                        image.addLink(res.getString("imagesLink"), "images");
                        image.addLink(res.getString("userlink"), "user");
                        image.addLink(res.getString("userimageslink"), "images_user");
                        images.add(image);
                  }
                  res.close();
            } catch (SQLException e) {e.printStackTrace();}
            finally {
                  try {
                        if (stmt != null){
                              stmt.close();
                        }
                  } catch (SQLException e) {e.printStackTrace();}
                  try {
                        if (conn != null) {
                              conn.close();
                        }
                  } catch (SQLException e) {
                        e.printStackTrace();
                  }
            }

//            for(List<Image> imageList : imagesByUsers.values()) {
//                  images.addAll(imageList);
//            }
//          DO WE NEED TO ADD DATA NOT FOUND EXCEPTION??
            return images;
      }

      public List<Image> getImagesByUser(String userUuid) {
            List<Image> images = new ArrayList<>();
            Image image;
            try {
                  conn = Database.getConnection();
                  if(conn == null) {
                        throw new GenericException("Cannot connect to db");
                  }
                  stmt = conn.createStatement();
                  query = "SELECT * FROM Images " +
                          "WHERE useruuid = '" + userUuid + "'";
                  ResultSet res = stmt.executeQuery(query);
                  while(res.next()) {
                        image = new Image(res.getString("imageuuid"), res.getString("title"));
                        image.setPath(res.getString("path"));
                        image.setWidth(res.getInt("width"));
                        image.setHeight(res.getInt("height"));
                        image.addLink(res.getString("selfLink"), "self");
                        image.addLink(res.getString("usersLink"), "users");
                        image.addLink(res.getString("imagesLink"), "images");
                        image.addLink(res.getString("userlink"), "user");
                        image.addLink(res.getString("userimageslink"), "images_user");
                        images.add(image);
                  }
                  res.close();
            } catch (SQLException e) {e.printStackTrace();}
            finally {
                  try {
                        if (stmt != null){
                              stmt.close();
                        }
                  } catch (SQLException e) {e.printStackTrace();}
                  try {
                        if (conn != null) {
                              conn.close();
                        }
                  } catch (SQLException e) {
                        e.printStackTrace();
                  }
            }
            if(images == null) {
                  throw new DataNotFoundException("User with uuid " + userUuid + " not found");
            }
            return images;
      }

      public Image getImage(String userUuid, String imageUuid) {
            Image image = null;
            try {
                  conn = Database.getConnection();
                  if(conn == null) {
                        throw new GenericException("Cannot connect to db");
                  }
                  stmt = conn.createStatement();
                  query = "SELECT * FROM Images " +
                          "WHERE useruuid = '" + userUuid + "' AND imageuuid = '" + imageUuid + "'";
                  ResultSet res = stmt.executeQuery(query);
                  while(res.next()) {
                        image = new Image(res.getString("imageuuid"), res.getString("title"));
                        image.setPath(res.getString("path"));
                        image.setWidth(res.getInt("width"));
                        image.setHeight(res.getInt("height"));
                        image.addLink(res.getString("selfLink"), "self");
                        image.addLink(res.getString("usersLink"), "users");
                        image.addLink(res.getString("imagesLink"), "images");
                        image.addLink(res.getString("userlink"), "user");
                        image.addLink(res.getString("userimageslink"), "images_user");
                  }
                  res.close();
            } catch (SQLException e) {e.printStackTrace();}
            finally {
                  try {
                        if (stmt != null){
                              stmt.close();
                        }
                  } catch (SQLException e) {e.printStackTrace();}
                  try {
                        if (conn != null) {
                              conn.close();
                        }
                  } catch (SQLException e) {
                        e.printStackTrace();
                  }
            }

            if(image == null) {
                  throw new DataNotFoundException("Image with uuid " + imageUuid + " not found");
            }
            return image;
      }

      public Image addImage(String userUuid, String fileName, String path) {
            if(fileName == null) {
//                  if(imagesByUsers.get(userUuid) == null) {
//                        imagesByUsers.put(userUuid, new ArrayList<>());
//                  }
                  return null;
            }
            Image image = new Image(fileName);
            String uuid = UUID.randomUUID().toString().split("-")[0];
            image.setUuid(uuid);
            image.setPath(path);

            //set height and width

            //BufferedImage bufferedImage = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
            BufferedImage bufferedImage;
            try {
                  bufferedImage = ImageIO.read(
                        new File(image.getPath())
                  );
            } catch (IOException e) {
                  throw new GenericException("Cannot read the file: " + e.getMessage());
            }

            image.setWidth(bufferedImage.getWidth());
            image.setHeight(bufferedImage.getHeight());


            try {
                  conn = Database.getConnection();
                  if(conn == null) {
                        throw new GenericException("Cannot connect to db");
                  }
                  stmt = conn.createStatement();
                  query = "INSERT INTO Images (useruuid, imageuuid, title, path, width, height) VALUES ('" + userUuid +"', '" + uuid + "', '" +
                        image.getTitle() + "', '" + path + "', " + image.getWidth() + ", " + image.getHeight() + ")";
                  stmt.executeUpdate(query);
            } catch (SQLException e) {e.printStackTrace();}
            finally {
                  try {
                        if (stmt != null){
                              stmt.close();
                        }
                  } catch (SQLException e) {e.printStackTrace();}
                  try {
                        if (conn != null) {
                              conn.close();
                        }
                  } catch (SQLException e) {
                        e.printStackTrace();
                  }
            }

//            List<Image> images = getImagesByUser(userUuid);
//            images.add(image);
            return image;
      }

      public Image uploadImage(InputStream fileInputStream, FormDataContentDisposition fileMetaData, String userUuid) {
            String extension = fileMetaData.getFileName().substring(fileMetaData.getFileName().lastIndexOf(".")+1);
            if(!extension.equals("jpg")) {
                  throw new GenericException("Cannot upload ." + extension + " files, just .jpg files");
            }

//            if(getImagesByUser(userUuid) == null) {
//                  throw new DataNotFoundException("User with uuid " + userUuid + " not found");
//            }

//            File file = new File(fileMetaData.getFileName());
//            Image newImage = addImage(userUuid, fileMetaData.getFileName(), file.getAbsolutePath());
//
//            try {
//                  FileInputStream fis = new FileInputStream(file);
//                  PreparedStatement ps = conn.prepareStatement("UPDATE Images SET images = ?");
//                  ps.setString(1, file.getName());
//                  ps.setBinaryStream(2, fis, (int) file.length());
//                  ps.executeUpdate();
//                  ps.close();
//                  fis.close();
//            } catch (SQLException e) {
//                  e.printStackTrace();
//            } catch (FileNotFoundException e) {
//                  e.printStackTrace();
//            } catch (IOException e) {
//                  e.printStackTrace();
//            }

            String UPLOAD_PATH = "upload_"+ userUuid + File.separator;

            try
            {
                  int read;
                  byte[] bytes = new byte[1024];

                  File file = new File(UPLOAD_PATH + fileMetaData.getFileName());
                  OutputStream out = new FileOutputStream(file);
                  while ((read = fileInputStream.read(bytes)) != -1)
                  {
                        out.write(bytes, 0, read);
                  }
                  out.flush();
                  out.close();

                  /*-------------------------------------*/
                  /*Create and add image instance*/
                  return addImage(userUuid, fileMetaData.getFileName(), file.getAbsolutePath());

            } catch (IOException e)
            {
                  throw new GenericException("Exception while loading the file:\n" + e.getMessage());
            }
      }

      public List<Image> removeImagesByUser(String userUuid) {
            List<Image> images = new ArrayList<>();
            String deleteImages = null;
            Image image = null;
            try {
                  conn = Database.getConnection();
                  if(conn == null) {
                        throw new GenericException("Cannot connect to db");
                  }
                  stmt = conn.createStatement();
                  query = "SELECT * FROM Images " +
                          "WHERE useruuid = '" + userUuid + "'";
                  ResultSet res = stmt.executeQuery(query);
                  while(res.next()) {
                        image = new Image(res.getString("imageuuid"), res.getString("title"));
                        image.setPath(res.getString("path"));
                        image.setWidth(res.getInt("width"));
                        image.setHeight(res.getInt("height"));
                        image.addLink(res.getString("selfLink"), "self");
                        image.addLink(res.getString("usersLink"), "users");
                        image.addLink(res.getString("imagesLink"), "images");
                        image.addLink(res.getString("userlink"), "user");
                        image.addLink(res.getString("userimageslink"), "images_user");
                        images.add(image);
                  }
                  res.close();

                  deleteImages = "DELETE * FROM Images WHERE useruuid = '" + userUuid + "'";
                  stmt.executeUpdate(deleteImages);

            } catch (SQLException e) {e.printStackTrace();}
            finally {
                  try {
                        if (stmt != null){
                              stmt.close();
                        }
                  } catch (SQLException e) {e.printStackTrace();}
                  try {
                        if (conn != null) {
                              conn.close();
                        }
                  } catch (SQLException e) {
                        e.printStackTrace();
                  }
            }
            //List<Image> images = imagesByUsers.get(userUuid);

            //remove all the files but not the directory
            if(images != null) {
                  String path = "upload_" + userUuid + File.separator;
                  File file = new File(path);
                  String[] entries = file.list();
                  if(entries != null) {
                        for (String s : entries) {
                              File currentFile = new File(file.getPath(), s);
                              boolean del = currentFile.delete();
                        }
                  }

//                  List<Image> imagesToReturn = imagesByUsers.get(userUuid);
//                  imagesByUsers.replace(userUuid, new ArrayList<>());
                  return images;
            }
            throw new DataNotFoundException("Images of user " + userUuid + " not found");
      }

      public Image removeImage(String userUuid, String imageUuid) {

            Image image = null;
            try {
                  conn = Database.getConnection();
                  if(conn == null) {
                        throw new GenericException("Cannot connect to db");
                  }
                  stmt = conn.createStatement();
                  query = "SELECT * FROM Images " +
                          "WHERE useruuid = '" + userUuid + " AND imageuuid = '" + imageUuid + "'";
                  ResultSet res = stmt.executeQuery(query);
                  while(res.next()) {
                        image = new Image(res.getString("imageuuid"), res.getString("title"));
                        image.setPath(res.getString("path"));
                        image.setWidth(res.getInt("width"));
                        image.setHeight(res.getInt("height"));
                        image.addLink(res.getString("selfLink"), "self");
                        image.addLink(res.getString("usersLink"), "users");
                        image.addLink(res.getString("imagesLink"), "images");
                        image.addLink(res.getString("userlink"), "user");
                        image.addLink(res.getString("userimageslink"), "images_user");
                  }
                  res.close();

            } catch (SQLException e) {e.printStackTrace();}
            finally {
                  try {
                        if (stmt != null){
                              stmt.close();
                        }
                  } catch (SQLException e) {e.printStackTrace();}
                  try {
                        if (conn != null) {
                              conn.close();
                        }
                  } catch (SQLException e) {
                        e.printStackTrace();
                  }
            }
            //List<Image> images = imagesByUsers.get(userUuid);
            if(image == null) {
                  throw new DataNotFoundException("Images of user " + userUuid + " not found");
            }
//            Image image = null;
//            for(Image i : images) {
//                  if(i.getUuid().equals(imageUuid)) {
//                        image = i;
//                        images.remove(i);

                        //delete also the actual file
            String path = "upload_" + userUuid + File.separator;
            File file = new File(path);
            String[] entries = file.list();
            if(entries != null) {
                  for (String s : entries) {
                        if (s.equals(image.getTitle())) {
                              File currentFile = new File(file.getPath(), s);
                              boolean del = currentFile.delete();
                        }
                  }
            }
//                        break;
//                  }
//            }
//            if(image == null) {
//                  throw new DataNotFoundException("Image " + imageUuid + " of user " + userUuid + " not found");
//            }
            return image;
      }

      public Image renameImage(String userUuid, String imageUuid, String newTitle) {
            int updatesNumber = 0;
            Image image = null;
            String selectQuery = null;
            try {
                  conn = Database.getConnection();
                  if(conn == null) {
                        throw new GenericException("Cannot connect to db");
                  }
                  stmt = conn.createStatement();
                  selectQuery = "SELECT * FROM Images WHERE useruuid = '" + userUuid + " AND imageuuid = '" + imageUuid + "'";         //TODO
                  ResultSet res = stmt.executeQuery(selectQuery);
                  while(res.next()) {
                        image = new Image(res.getString("imageuuid"), res.getString("title"));
                        image.setPath(res.getString("path"));
                        image.setWidth(res.getInt("width"));
                        image.setHeight(res.getInt("height"));
                        image.addLink(res.getString("selfLink"), "self");
                        image.addLink(res.getString("usersLink"), "users");
                        image.addLink(res.getString("imagesLink"), "images");
                        image.addLink(res.getString("userlink"), "user");
                        image.addLink(res.getString("userimageslink"), "images_user");
                  }
                  res.close();
                  query = "UPDATE Images " +
                          "SET title = '" + newTitle +"' WHERE useruuid = '" + userUuid + " AND imageuuid = '" + imageUuid + "'";
                  updatesNumber = stmt.executeUpdate(query);
            } catch (SQLException e) {e.printStackTrace();}
            finally {
                  try {
                        if (stmt != null){
                              stmt.close();
                        }
                  } catch (SQLException e) {e.printStackTrace();}
                  try {
                        if (conn != null) {
                              conn.close();
                        }
                  } catch (SQLException e) {
                        e.printStackTrace();
                  }
            }

            //List<Image> images = imagesByUsers.get(userUuid);
//            if(images == null) {
//                  throw new DataNotFoundException("Images of user " + userUuid + " not found");
//            }
            if(updatesNumber == 0) {
                  throw new DataNotFoundException("Image " + imageUuid + " of user " + userUuid + " not found");
            }

//            Image image = null;
//            for(Image i : images) {
//                  if(i.getUuid().equals(imageUuid)) {
                        //rename also the actual file
                        String path = "upload_" + userUuid + File.separator;
                        File file = new File(path);
                        String[] entries = file.list();
                        if(entries != null && image != null) {
                              for (String s : entries) {
                                    if (s.equals(image.getTitle())) {
                                          File currentFile = new File(file.getPath(), s);

                                          //adjust newTitle extension
                                          if (!newTitle.contains(".") ||
                                                !newTitle.substring(newTitle.lastIndexOf(".") + 1).equals("jpg")) {
                                                newTitle = newTitle + ".jpg";
                                          }

                                          File newFile = new File(file.getPath(), newTitle);
                                          boolean ren = currentFile.renameTo(newFile);
                                    }
                              }
                              image.setTitle(newTitle);
                        }
//                        image = i;
//                        break;
//                  }
//            }

//            if(image == null) {
//                  throw new DataNotFoundException("Image " + imageUuid + " of user " + userUuid + " not found");
//            }
            return image;
      }


      public void addLinks(@Context UriInfo uriInfo, Image image, String userUuid) {

            try {
                  conn = Database.getConnection();
                  if(conn == null) {
                        throw new GenericException("Cannot connect to db");
                  }
                  stmt = conn.createStatement();

                  query = "UPDATE Images " +
                          "SET (selfLink, userslink, userLink, imageslink, userImagesLink) = ('" + getUriForSelf(uriInfo, image, userUuid) +"', '" + getUriForUsers(uriInfo) +"', '" +
                          getUriForUser(uriInfo, userUuid) + "', '" + getUriForImages(uriInfo) +"', '" + getUriForImagesUser(uriInfo, userUuid) +"') " +
                          "WHERE useruuid = '" + userUuid + "' AND imageuuid = '" + image.getUuid() + "'";
                  stmt.executeUpdate(query);

            } catch (SQLException e) {e.printStackTrace();}
            finally {
                  try {
                        if (stmt != null){
                              stmt.close();
                        }
                  } catch (SQLException e) {e.printStackTrace();}
                  try {
                        if (conn != null) {
                              conn.close();
                        }
                  } catch (SQLException e) {
                        e.printStackTrace();
                  }
            }
            image.addLink(getUriForSelf(uriInfo, image, userUuid), "self");
            image.addLink(getUriForImagesUser(uriInfo, userUuid), "images_user");
            image.addLink(getUriForImages(uriInfo), "images");
            image.addLink(getUriForUser(uriInfo, userUuid), "user");
            image.addLink(getUriForUsers(uriInfo), "users");
      }

      private String getUriForSelf(UriInfo uriInfo, Image image, String userUuid) {
            return uriInfo
                    .getBaseUriBuilder()
                    .path(ImageResource.class)
                    .path(userUuid)
                    .path(image.getUuid())
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

      private String getUriForUser(UriInfo uriInfo, String userUuid) {
            return uriInfo
                    .getBaseUriBuilder()
                    .path(UserResource.class)
                    .path(userUuid)
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

      private String getUriForImagesUser(UriInfo uriInfo, String userUuid) {
            return uriInfo
                    .getBaseUriBuilder()
                    .path(ImageResource.class)
                    .path(userUuid)
                    .build()
                    .toString();
      }
}
