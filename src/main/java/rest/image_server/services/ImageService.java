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
import java.sql.*;
import java.util.*;

public class ImageService {

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
                  query = "SELECT  imageuuid, title, width, height, selflink, userlink, userimageslink, imageslink, userslink FROM Images";
                  ResultSet res = stmt.executeQuery(query);
                  while(res.next()) {
                        image = new Image(res.getString("imageuuid"), res.getString("title"));
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
                  query = "SELECT  imageuuid, title, width, height, selflink, userlink, userimageslink, imageslink, userslink FROM Images " +
                          "WHERE useruuid = '" + userUuid + "'";
                  ResultSet res = stmt.executeQuery(query);
                  while(res.next()) {
                        image = new Image(res.getString("imageuuid"), res.getString("title"));
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
                  query = "SELECT  imageuuid, title, width, height, selflink, userlink, userimageslink, imageslink, userslink FROM Images " +
                          "WHERE useruuid = '" + userUuid + "' AND imageuuid = '" + imageUuid + "'";
                  ResultSet res = stmt.executeQuery(query);
                  while(res.next()) {
                        image = new Image(res.getString("imageuuid"), res.getString("title"));
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


      public InputStream getImageRaw(String userUuid, String imageUuid) {
            InputStream inputStream = null;
            try {
                  conn = Database.getConnection();
                  if(conn == null) {
                        throw new GenericException("Cannot connect to db");
                  }
                  PreparedStatement ps = conn.prepareStatement("SELECT picture FROM images WHERE useruuid = ? AND imageuuid = ?");
                  ps.setString(1, userUuid);
                  ps.setString(2, imageUuid);
                  ResultSet rs = ps.executeQuery();
                  while (rs.next()) {
                        inputStream = rs.getBinaryStream("picture");
                        // use the data in some way here
                  }
                  rs.close();
                  ps.close();
            } catch (SQLException e) {
                  e.printStackTrace();
            } finally {
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
			if(inputStream == null) {
                  throw new DataNotFoundException("Image with uuid " + imageUuid + " not found");
            }
            return inputStream;
      }


      public Image uploadImage(InputStream fileInputStream, FormDataContentDisposition fileMetaData, String userUuid) {
            String insertQuery;

            String extension = fileMetaData.getFileName().substring(fileMetaData.getFileName().lastIndexOf(".")+1);
            if(!extension.equals("jpg")) {
                  throw new GenericException("Cannot upload ." + extension + " files, just .jpg files");
            }

            Image image = new Image(fileMetaData.getFileName());
            String uuid = UUID.randomUUID().toString().split("-")[0];
            image.setUuid(uuid);

            //set height and width

            int byteCount;
            byte[] buffer = new byte[1024];
            byte[] source = null;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try {
                  while ((byteCount = fileInputStream.read(buffer)) != -1) {
                        output.write(buffer, 0, byteCount);
                  }
                  source = output.toByteArray();

            } catch (IOException e){
                  e.printStackTrace();
            }

            setImageDim(source, image);

            try {

                  conn = Database.getConnection();
                  if(conn == null) {
                        throw new GenericException("Cannot connect to db");
                  }
                  stmt = conn.createStatement();
                  query = "SELECT * FROM Users WHERE uuid = '" + userUuid + "'";
                  if (!stmt.execute(query)){
                        throw new GenericException("Specified user is not present");
                  }

                  insertQuery = "INSERT INTO Images (useruuid, imageuuid, title, width, height, picture) VALUES (?, ?, ?, ?, ?, ?)";
                  PreparedStatement ps = conn.prepareStatement(insertQuery);
                  ps.setString(1, userUuid);
                  ps.setString(2, uuid);
                  ps.setString(3, image.getTitle());
                  ps.setInt(4, image.getWidth());
                  ps.setInt(5, image.getHeight());
                  ps.setBytes(6, source);

                  ps.executeUpdate();
                  ps.close();
            } catch (SQLException e) {
                  e.printStackTrace();
            }finally {
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
            return image;
      }

      private void setImageDim(byte[] imgByte, Image image) {

            try {
                  InputStream in = new ByteArrayInputStream(imgByte);

                  BufferedImage buf = ImageIO.read(in);
                  image.setHeight(buf.getHeight());
                  image.setWidth(buf.getWidth());
            } catch (IOException e) {
                  e.printStackTrace();
            }

      }

      public void removeImagesByUser(String userUuid) {
            String deleteImages;
            int deletedImagesNum = 0;
            try {
                  conn = Database.getConnection();
                  if(conn == null) {
                        throw new GenericException("Cannot connect to db");
                  }

                  stmt = conn.createStatement();
                  deleteImages = "DELETE FROM Images WHERE useruuid = '" + userUuid + "'";
                  deletedImagesNum = stmt.executeUpdate(deleteImages);

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

            if(deletedImagesNum == 0) {
                  throw new DataNotFoundException("Images of user " + userUuid + " not found");
            }
      }

      public void removeImage(String userUuid, String imageUuid) {
            String deleteQuery;
            int removed = 0;
            try {
                  conn = Database.getConnection();
                  if(conn == null) {
                        throw new GenericException("Cannot connect to db");
                  }

                  stmt = conn.createStatement();
                  deleteQuery = "DELETE FROM Images WHERE useruuid = '" + userUuid + "' AND imageuuid = '" + imageUuid + "'";
                  removed = stmt.executeUpdate(deleteQuery);

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

            if(removed == 0) {
                  throw new DataNotFoundException("Image of user " + userUuid + " not found");
            }
      }

      public Image renameImage(String userUuid, String imageUuid, String newTitle) {
            int updatesNumber = 0;
            Image image = null;
            String selectQuery;
            try {
                  conn = Database.getConnection();
                  if(conn == null) {
                        throw new GenericException("Cannot connect to db");
                  }
                  stmt = conn.createStatement();
                  selectQuery = "SELECT  imageuuid, title, width, height, selflink, userlink, userimageslink, imageslink, userslink FROM Images " +
                          "WHERE useruuid = '" + userUuid + "' AND imageuuid = '" + imageUuid + "'";
                  ResultSet res = stmt.executeQuery(selectQuery);
                  while(res.next()) {
                        image = new Image(res.getString("imageuuid"), res.getString("title"));
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
                          "SET title = '" + newTitle +"' WHERE useruuid = '" + userUuid + "' AND imageuuid = '" + imageUuid + "'";
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
            if(updatesNumber == 0) {
                  throw new DataNotFoundException("Image " + imageUuid + " of user " + userUuid + " not found");
            }
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
