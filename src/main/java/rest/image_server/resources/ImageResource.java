package rest.image_server.resources;

import rest.image_server.frontend.ImageFrontend;
import rest.image_server.model.Image;
import rest.image_server.services.ImageService;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.io.InputStream;
import java.util.List;

@Path("/images")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.TEXT_HTML)
public class ImageResource {
      private ImageService imageService = new ImageService();

      @GET
      public String getImages() {
            List<Image> images = imageService.getImages();
            return ImageFrontend.getImagesRepresentation(images, "Images");
      }

      @GET
      @Path("/{user_uuid}")
      public String getImagesByUser(@PathParam("user_uuid") String userUuid) {
            List<Image> imagesByUser = imageService.getImagesByUser(userUuid);
            return ImageFrontend.getImagesRepresentation(imagesByUser, "Images requested");
      }

      @GET
      @Path("/{user_uuid}/{image_uuid}")
      public String getImage(@PathParam("user_uuid") String userUuid, @PathParam("image_uuid") String imageUuid) {
            Image image = imageService.getImage(userUuid, imageUuid);
            return ImageFrontend.getImageRepresentation(image, "Image requested");
      }

      @POST
      @Path("/{user_uuid}")
      @Produces(MediaType.APPLICATION_JSON)
      @Consumes({MediaType.MULTIPART_FORM_DATA})
      public Image uploadFile(@FormDataParam("file") InputStream fileInputStream,
                              @FormDataParam("file") FormDataContentDisposition fileMetaData,
                              @PathParam("user_uuid") String userUuid,
                              @Context UriInfo uriInfo) throws Exception {

            Image image = imageService.uploadImage(fileInputStream, fileMetaData, userUuid);
            addLinks(uriInfo, image, userUuid);
            return image;
            //return ImageFrontend.getImageRepresentation(image, "Image uploaded");
      }



      //To modify the file name of an image
      @PUT
      @Path("/{user_uuid}/{image_uuid}")
      public String renameImage(@PathParam("user_uuid") String userUuid, @PathParam("image_uuid") String imageUuid, Image newImage) {
            Image image = imageService.renameImage(userUuid, imageUuid, newImage.getTitle());
            return ImageFrontend.getImageRepresentation(image, "Image renamed");
      }

      @DELETE
      @Path("/{user_uuid}")
      public String removeImagesByUser(@PathParam("user_uuid") String userUuid) {
            List<Image> images = imageService.removeImagesByUser(userUuid);
            return ImageFrontend.getImagesRepresentation(images, "Images deleted");
      }

      @DELETE
      @Path("/{user_uuid}/{image_uuid}")
      public String removeImage(@PathParam("user_uuid") String userUuid, @PathParam("image_uuid") String imageUuid) {
            Image image = imageService.removeImage(userUuid, imageUuid);
            return ImageFrontend.getImageRepresentation(image, "Image deleted");
      }

      private void addLinks(@Context UriInfo uriInfo, Image image, String userUuid) {
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
