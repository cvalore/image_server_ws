package rest.image_server.resources;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import rest.image_server.frontend.ImageFrontend;
import rest.image_server.model.Image;
import rest.image_server.services.ImageService;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.*;
import java.util.List;

@Path("/images")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.TEXT_HTML)
public class ImageResource {
      private ImageService imageService = new ImageService();

      @GET
      public String getImages() {
            List<Image> images = imageService.getImages();
            return ImageFrontend.getImagesRepresentation(images, "Images", "");
      }

      @GET
      @Path("/{user_uuid}")
      public String getImagesByUser(@PathParam("user_uuid") String userUuid) {
            List<Image> imagesByUser = imageService.getImagesByUser(userUuid);
            return ImageFrontend.getImagesRepresentation(imagesByUser, "Images requested", userUuid);
      }

      @GET
      @Produces({"image/jpg"})
      @Path("/{user_uuid}/{image_uuid}/raw")
      public Response getImageRaw(@PathParam("user_uuid") String userUuid, @PathParam("image_uuid") String imageUuid) {
            InputStream imageData = imageService.getImageRaw(userUuid, imageUuid);
            return Response.ok(imageData).build();
      }

      @GET
      @Path("/{user_uuid}/{image_uuid}")
      public String getImage(@PathParam("user_uuid") String userUuid, @PathParam("image_uuid") String imageUuid) {
            Image image = imageService.getImage(userUuid, imageUuid);
            return ImageFrontend.getImageRepresentation(image, "Image requested");
      }

      @POST
      @Path("/{user_uuid}")
      @Consumes({MediaType.MULTIPART_FORM_DATA})
      public String uploadFile(@FormDataParam("file") InputStream fileInputStream,
                                 @FormDataParam("file") FormDataContentDisposition fileMetaData,
                                 @PathParam("user_uuid") String userUuid,
                                 @Context UriInfo uriInfo) {

            Image image = imageService.uploadImage(fileInputStream, fileMetaData, userUuid);
            imageService.addLinks(uriInfo, image, userUuid);
            return ImageFrontend.getImageRepresentation(image, "Image uploaded");
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
            imageService.removeImagesByUser(userUuid);
            return ImageFrontend.getImagesRepresentation(null, "Images deleted", "");
      }

      @DELETE
      @Path("/{user_uuid}/{image_uuid}")
      public String removeImage(@PathParam("user_uuid") String userUuid, @PathParam("image_uuid") String imageUuid) {
            imageService.removeImage(userUuid, imageUuid);
            return ImageFrontend.getImageRepresentation(null, "Image deleted");
      }


}
