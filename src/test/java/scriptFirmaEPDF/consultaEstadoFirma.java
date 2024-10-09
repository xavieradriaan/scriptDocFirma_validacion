package scriptFirmaEPDF;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class consultaEstadoFirma {

    private static final String TOKEN_URL = "https://micro-signature-dev.auth.us-east-1.amazoncognito.com/oauth2/token";
    private static final String DOCUMENTO_URL = "https://15rgxii8ld.execute-api.us-east-1.amazonaws.com/dev/documento";
    private static final String JSON_FILE_PATH = "C:\\Users\\xandrado\\scriptFirmaElectronica\\src\\test\\java\\scriptFirmaEPDF\\documento.json";

    private String obtenerTokenFirma() {
        try {
            System.out.println("=======================================");
            System.out.println("||                                   ||");
            System.out.println("||         Obteniendo el token       ||");
            System.out.println("||         de firma...               ||");
            System.out.println("||                                   ||");
            System.out.println("=======================================");
            Response response = RestAssured.given()
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Cookie", "XSRF-TOKEN=328b6987-68c5-406c-a4ff-bc5798554b2b")
                    .formParam("grant_type", "client_credentials")
                    .formParam("scope", "micro-signature-dev-srv/micro-signature-dev-scope")
                    .formParam("client_id", "1fpmlmgkblig3kk0ft879ei3m9")
                    .formParam("client_secret", "sg2hsqlj8ufnf0a5r23s08ruot2shjm14aq2uqs44ntrofdhele")
                    .post(TOKEN_URL);

            String token = response.jsonPath().getString("access_token");
            System.out.println("Token de firma obtenido: " + token);
            return token;
        } catch (Exception e) {
            System.err.println("Error al obtener el token de firma: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String consultarFirmaDocumento(String token, String solicitudId) {
        try {
            // Validar solicitudId
            if (solicitudId.isEmpty()) {
                throw new IllegalArgumentException("La solicitudId no puede estar vacía");
            }

            // Construir el cuerpo de la solicitud
            JSONObject requestBody = new JSONObject();
            requestBody.put("solicitudId", solicitudId);

            // Realizar la solicitud
            Response response = RestAssured.given()
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .body(requestBody.toString())
                    .post(DOCUMENTO_URL);

            // Manejar la respuesta
            if (response.getStatusCode() != 200) {
                throw new RuntimeException("Error en la consulta: " + response.getStatusLine());
            }

            // Parsear la respuesta JSON y eliminar "documentoBytes"
            JSONObject responseJson = new JSONObject(response.getBody().asString());
            responseJson.remove("documentoBytes");

            // Retornar la respuesta formateada
            return responseJson.toString();
        } catch (JSONException e) {
            System.err.println("Error al parsear la respuesta JSON: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Error al consultar la firma: " + e.getMessage());
            return null;
        }
    }

    private void actualizarSolicitudIdEnJson(String solicitudId) {
        try {
            // Leer el contenido del archivo JSON
            String jsonContent = new String(Files.readAllBytes(Paths.get(JSON_FILE_PATH)));

            // Parsear el JSON y actualizar el campo solicitudId
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode rootNode = (ObjectNode) mapper.readTree(jsonContent);
            rootNode.put("solicitudId", solicitudId);

            // Escribir el JSON actualizado de nuevo en el archivo
            mapper.writerWithDefaultPrettyPrinter().writeValue(Paths.get(JSON_FILE_PATH).toFile(), rootNode);

            System.out.println("Archivo JSON actualizado con solicitudId: " + solicitudId);
        } catch (IOException e) {
            System.err.println("Error al actualizar el archivo JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testConsultarFirmaDocumento() {
        System.out.println("Iniciando prueba de consulta de firma del documento...");

        // Obtener token de firma
        String tokenFirma = obtenerTokenFirma();
        if (tokenFirma == null) {
            System.err.println("Error: No se pudo obtener el token de firma.");
            return;
        }

        // Ingresar solicitudId manualmente
        String solicitudId = "MMKfbj6fcE-20241007"; // Deben cambiar este valor según sea necesario

        // Actualizar el archivo JSON con el nuevo solicitudId
        actualizarSolicitudIdEnJson(solicitudId);

        System.out.println("=======================================");
        System.out.println("||                                   ||");
        System.out.println("||    Iniciando consulta de firma    ||");
        System.out.println("||          del documento...         ||");
        System.out.println("||                                   ||");
        System.out.println("=======================================");

        // Consultar firma del documento
        String respuesta = consultarFirmaDocumento(tokenFirma, solicitudId);
        if (respuesta != null) {
            System.out.println("Prueba de consulta de firma del documento completada.");
            System.out.println("Respuesta: " + respuesta);
        } else {
            System.err.println("Error en la consulta de firma del documento.");
        }
    }
}