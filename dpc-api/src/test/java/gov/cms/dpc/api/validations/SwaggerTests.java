package gov.cms.dpc.api.validations;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SwaggerTests  {

    private SwaggerTests() {

    }


    @Test
    void testDownloadSwagger() {
        String FILE_URL = "http://localhost:3002/swagger.json";
        String SWAGGER_FILE_NAME = "/Users/richardbraman/IdeaProjects/dpc-app/swagger/swagger.json";

        try (BufferedInputStream in = new BufferedInputStream(new URL(FILE_URL).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(SWAGGER_FILE_NAME)) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
            assertEquals( "Downloaded Swagger", "Downloaded Swagger");

        } catch (IOException e) {
            // handle exception
            assertEquals( "Downloaded Swagger", "Could not download swagger");

        }


    }

    @Test
    void testValidateSwagger() {

        String FILE_NAME = "";
        String url = "https://validator.swagger.io/validator/"; //Uses online validator that returns an image badge
        String charset = "UTF-8";
        String param = "value";
        File textFile = new File("/Users/richardbraman/IdeaProjects/dpc-app/swagger/swagger.json");
        try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();;
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "image/png");
                OutputStream os = connection.getOutputStream();
                byte[] input = readContentIntoByteArray(textFile);
                os.write(input, 0, input.length);
                int responseCode = connection.getResponseCode();
                //System.out.println(responseCode); // Should be 200
                if (responseCode==200) {
                    MessageDigest md_1 = MessageDigest.getInstance("MD5");
                    MessageDigest md_2 = MessageDigest.getInstance("MD5");
                    InputStream is_1 = connection.getInputStream();
                    InputStream is_2 = new FileInputStream("/Users/richardbraman/IdeaProjects/dpc-app/swagger/valid.png");
                    try {
                        is_1 = new DigestInputStream(is_1, md_1);
                        is_2 = new DigestInputStream(is_2, md_2);
                    } finally {
                        is_1.close();
                        is_2.close();
                    }
                    byte[] digest_1 = md_1.digest();
                    byte[] digest_2 = md_2.digest();

                    if (digest_1 == digest_2) {
                        assertEquals("validated swagger file", "validated swagger file");
                    } else {
                        assertEquals("validated swagger file", "invalid swagger file");
                    }
                }
                else{
                    assertEquals("validated swagger file","error validating swagger file");

                }
                connection.disconnect();

            } catch (Exception e) {
                assertEquals("validated swagger file", "could not validate swagger file");

            }
            finally {

        }

    }

    private static byte[] readContentIntoByteArray(File file)
    {
        FileInputStream fileInputStream = null;
        byte[] bFile = new byte[(int) file.length()];
        try
        {
            //convert file into array of bytes
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bFile);
            fileInputStream.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return bFile;
    }
}

