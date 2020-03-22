package rest.image_server.services;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import rest.image_server.exceptions.DataNotFoundException;
import rest.image_server.exceptions.GenericException;
import rest.image_server.model.Database;
import rest.image_server.model.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ImageService {
      private Map<String, List<Image>> imagesByUsers = Database.getImagesByUsers();

      public List<Image> getImages() {
            List<Image> images = new ArrayList<>();
            for(List<Image> imageList : imagesByUsers.values()) {
                  images.addAll(imageList);
            }
            return images;
      }

      public List<Image> getImagesByUser(String userUuid) {
            List<Image> images = imagesByUsers.get(userUuid);
            if(images == null) {
                  throw new DataNotFoundException("User with uuid " + userUuid + " not found");
            }
            return images;
      }

      public Image getImage(String userUuid, String imageUuid) {
            List<Image> images = imagesByUsers.get(userUuid);
            if(images == null) {
                  throw new DataNotFoundException("User with uuid " + userUuid + " not found");
            }
            for(Image i : images) {
                  if(i.getUuid().equals(imageUuid)) {
                        return i;
                  }
            }
            throw new DataNotFoundException("Image with uuid " + imageUuid + " not found");
      }

      public Image addImage(String userUuid, String fileName, String path) {
            if(fileName == null) {
                  if(imagesByUsers.get(userUuid) == null) {
                        imagesByUsers.put(userUuid, new ArrayList<>());
                  }
                  return null;
            }
            Image image = new Image(fileName);
            String uuid = UUID.randomUUID().toString().split("-")[0];
            image.setUuid(uuid);
            image.setPath(path);

            //set height and width
            BufferedImage bufferedImage = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
            try {
                  bufferedImage = ImageIO.read(
                        new File(image.getPath())
                  );
            } catch (IOException e) {
                  throw new GenericException("Cannot read the file: " + e.getMessage());
            }

            image.setWidth(bufferedImage.getWidth());
            image.setHeight(bufferedImage.getHeight());

            List<Image> images = getImagesByUser(userUuid);
            images.add(image);
            return image;
      }

      public Image uploadImage(InputStream fileInputStream, FormDataContentDisposition fileMetaData, String userUuid) {
            String extension = fileMetaData.getFileName().substring(fileMetaData.getFileName().lastIndexOf(".")+1);
            if(!extension.equals("jpg")) {
                  throw new GenericException("Cannot upload ." + extension + " files, just .jpg files");
            }

            if(getImagesByUser(userUuid) == null) {
                  throw new DataNotFoundException("User with uuid " + userUuid + " not found");
            }

            String UPLOAD_PATH = "upload_"+ userUuid + File.separator;

            try
            {
                  int read = 0;
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
            List<Image> images = imagesByUsers.get(userUuid);

            //remove all the files but not the directory
            if(images != null) {
                  String path = "upload_" + userUuid + File.separator;
                  File file = new File(path);
                  String[] entries = file.list();
                  for (String s : entries) {
                        File currentFile = new File(file.getPath(), s);
                        currentFile.delete();
                  }

                  List<Image> imagesToReturn = imagesByUsers.get(userUuid);
                  imagesByUsers.replace(userUuid, new ArrayList<>());
                  return imagesToReturn;
            }

            throw new DataNotFoundException("Images of user " + userUuid + " not found");
      }

      public Image removeImage(String userUuid, String imageUuid) {
            List<Image> images = imagesByUsers.get(userUuid);
            if(images == null) {
                  throw new DataNotFoundException("Images of user " + userUuid + " not found");
            }

            Image image = null;
            for(Image i : images) {
                  if(i.getUuid().equals(imageUuid)) {
                        image = i;
                        images.remove(i);

                        //delete also the actual file
                        String path = "upload_" + userUuid + File.separator;
                        File file = new File(path);
                        String[] entries = file.list();
                        for (String s : entries) {
                              if(s.equals(i.getTitle())) {
                                    File currentFile = new File(file.getPath(), s);
                                    currentFile.delete();
                              }
                        }

                        break;
                  }
            }

            if(image == null) {
                  throw new DataNotFoundException("Image " + imageUuid + " of user " + userUuid + " not found");
            }
            return image;
      }

      public Image renameImage(String userUuid, String imageUuid, String newTitle) {
            List<Image> images = imagesByUsers.get(userUuid);
            if(images == null) {
                  throw new DataNotFoundException("Images of user " + userUuid + " not found");
            }

            Image image = null;
            for(Image i : images) {
                  if(i.getUuid().equals(imageUuid)) {
                        //rename also the actual file
                        String path = "upload_" + userUuid + File.separator;
                        File file = new File(path);
                        String[] entries = file.list();
                        for (String s : entries) {
                              if(s.equals(i.getTitle())) {
                                    File currentFile = new File(file.getPath(), s);

                                    //adjust newTitle extension
                                    if(!newTitle.contains(".") ||
                                          !newTitle.substring(newTitle.lastIndexOf(".")+1).equals("jpg")) {
                                         newTitle = newTitle + ".jpg";
                                    }

                                    File newFile = new File(file.getPath(), newTitle);
                                    currentFile.renameTo(newFile);
                              }
                        }
                        i.setTitle(newTitle);
                        image = i;
                        break;
                  }
            }

            if(image == null) {
                  throw new DataNotFoundException("Image " + imageUuid + " of user " + userUuid + " not found");
            }
            return image;
      }
}
