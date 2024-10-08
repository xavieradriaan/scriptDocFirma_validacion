package scriptFirmaEPDF;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FlujoCompleto {

    private String obtenerTokenSolicitud() { // Generar Token Solicitud 1 PASO
        try {
            System.out.println("====================================");
            System.out.println("||                                ||");
            System.out.println("||          Generando token       ||");
            System.out.println("||          de solicitud...       ||");
            System.out.println("||                                ||");
            System.out.println("====================================");
            Response response = given()
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Cookie",
                            "XSRF-TOKEN=147b317b-f51a-4cb3-87e3-28e00c0a5514; XSRF-T=45ee666e-6e50-49ec-b74d-a2856992c832")
                    .formParam("grant_type", "client_credentials")
                    .formParam("scope", "pasps-ms-onb-cliente-bff-dev-srv/pasps-ms-onb-cliente-bff-dev-scope")
                    .formParam("client_id", "6kc4jfn7q8s4nphq40bhgc6rbp")
                    .formParam("client_secret", "pgb8m00k8t5jubn3l3jms5ibpqeg7rdtfpsi46skf4ne1oumnui")
                    .post("https://pasps-ms-onb-cliente-bff-dev.auth.us-east-1.amazoncognito.com/oauth2/token");

            String token = response.jsonPath().getString("access_token");
            System.out.println("Token de solicitud obtenido: " + token);
            return token;
        } catch (Exception e) {
            System.err.println("Error al obtener el token de solicitud: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String generarSolicitud(String token) { // Generar la solicitud, 2 PASO ESTO DEBES MODIFICAR SEGUN CACHE
                                                    // FLUJO
        try {
            System.out.println("===============================");
            System.out.println("||                           ||");
            System.out.println("||           Generando       ||");
            System.out.println("||           Solicitud       ||");
            System.out.println("||                           ||");
            System.out.println("===============================");
            String requestBody = "{\n" +
                    "    \"transaccionId\": \"bc34dee4-3524-4f6b-aff0-dd375b963675\",\n" +
                    "    \"identificacion\": \"1310193550\",\n" +
                    "    \"respBiometrica\": \"{\\\"serviceResultCode\\\":0,\\\"serviceErrorCode\\\":0,\\\"testBirthDateMatch\\\":false,\\\"testIdentityNumberMatch\\\":false,\\\"testNameMatch\\\":false,\\\"testNationalityMatch\\\":false,\\\"testSurnameMatch\\\":false,\\\"testDocumentValidated\\\":false,\\\"codigoError\\\":\\\"0\\\",\\\"mensajeUsuario\\\":\\\"TRANSACCION EXITOSA\\\",\\\"mensajeSistema\\\":\\\"TRANSACCION EXITOSA\\\",\\\"responseServiceAuthenticateFacial\\\":{\\\"serviceResultCode\\\":0,\\\"serviceTime\\\":\\\"481\\\",\\\"serviceFacialAuthenticationResult\\\":3,\\\"serviceResultLog\\\":\\\"Positive\\\",\\\"serviceFacialSimilarityResult\\\":0.9713,\\\"serviceTransactionId\\\":\\\"dbd20f3f-bf84-4fe0-b5ad-cded57a8d382\\\",\\\"serviceFacialAuthenticationHash\\\":\\\"6ED8008E25ADE27C2E8AF5BD42331507339419F1471B6D529C9BDF3717DF97A90D1199AAC40A9165908DE5ABCD5B9C8F83FDEAF8942DC59A9A02A2F78211F967\\\"}}\"\n"
                    +
                    "}";

            System.out.println("Cuerpo de la solicitud: " + requestBody);

            Response response = given()
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .body(requestBody)
                    .post("https://45hj6q18oa.execute-api.us-east-1.amazonaws.com/dev/solicitud");

            System.out.println("Respuesta de la generación de solicitud: " + response.getBody().asString());

            String solicitudId = response.jsonPath().getString("resultado.solicitud");
            System.out.println("Solicitud generada con ID: " + solicitudId);
            return solicitudId;
        } catch (Exception e) {
            System.err.println("Error al generar la solicitud: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String obtenerTokenFirma() { // Generar Token de firma. 3 PASO
        try {
            System.out.println("=======================================");
            System.out.println("||                                   ||");
            System.out.println("||         Obteniendo el token       ||");
            System.out.println("||         de firma...               ||");
            System.out.println("||                                   ||");
            System.out.println("=======================================");
            Response response = given()
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Cookie", "XSRF-TOKEN=328b6987-68c5-406c-a4ff-bc5798554b2b")
                    .formParam("grant_type", "client_credentials")
                    .formParam("scope", "micro-signature-dev-srv/micro-signature-dev-scope")
                    .formParam("client_id", "1fpmlmgkblig3kk0ft879ei3m9")
                    .formParam("client_secret", "sg2hsqlj8ufnf0a5r23s08ruot2shjm14aq2uqs44ntrofdhele")
                    .post("https://micro-signature-dev.auth.us-east-1.amazoncognito.com/oauth2/token");

            String token = response.jsonPath().getString("access_token");
            System.out.println("Token de firma obtenido: " + token);
            return token;
        } catch (Exception e) {
            System.err.println("Error al obtener el token de firma: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void actualizarSolicitudIdEnJson(String solicitudId) {
        try {
            // Leer el contenido del archivo JSON
            String jsonFilePath = "C:\\Users\\xandrado\\scriptFirmaElectronica\\src\\test\\java\\scriptFirmaEPDF\\documento.json";
            String jsonContent = new String(Files.readAllBytes(Paths.get(jsonFilePath)));

            // Parsear el JSON y actualizar el campo solicitudId
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode rootNode = (ObjectNode) mapper.readTree(jsonContent);
            rootNode.put("solicitudId", solicitudId);

            // Escribir el JSON actualizado de nuevo en el archivo
            mapper.writerWithDefaultPrettyPrinter().writeValue(Paths.get(jsonFilePath).toFile(), rootNode);

            System.out.println("Archivo JSON actualizado con solicitudId: " + solicitudId);
        } catch (IOException e) {
            System.err.println("Error al actualizar el archivo JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void firmarDocumento(String token, String solicitudId) { // Firmar el Documento 4 PASO
        try {
            System.out.println("=================================");
            System.out.println("||                             ||");
            System.out.println("||           FIRMANDO          ||");
            System.out.println("||           DOCUMENTO         ||");
            System.out.println("||                             ||");
            System.out.println("=================================");

            // Actualizar el archivo JSON con el nuevo solicitudId
            actualizarSolicitudIdEnJson(solicitudId);

            // Leer el contenido del archivo JSON actualizado
            String requestBody = new String(Files.readAllBytes(Paths.get(
                    "C:\\Users\\xandrado\\scriptFirmaElectronica\\src\\test\\java\\scriptFirmaEPDF\\documento.json")));

            Response response = given()
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .body(requestBody)
                    .post("https://15rgxii8ld.execute-api.us-east-1.amazonaws.com/dev/firma");

            System.out.println("Respuesta de la firma del documento: " + response.getBody().asString());
            assertEquals(200, response.getStatusCode());

        } catch (IOException e) {
            System.err.println("Error al leer el archivo JSON: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error al firmar el documento: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void consultarFirmaDocumento(String token, String solicitudId) { // Consultar Firma de los documentos 5 PASO
        try {
            System.out.println("=======================================");
            System.out.println("||                                   ||");
            System.out.println("||          Consultando la           ||");
            System.out.println("||          firma del Documento...   ||");
            System.out.println("||                                   ||");
            System.out.println("=======================================");
            String requestBody = "{\n" +
                    "    \"solicitudId\": \"" + solicitudId + "\"\n" +
                    "}";

            Response response = given()
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .body(requestBody)
                    .post("https://15rgxii8ld.execute-api.us-east-1.amazonaws.com/dev/documento");

            // Filtrar la respuesta para omitir "documentoBytes"
            String responseBody = response.getBody().asString();
            responseBody = responseBody.replaceAll("\"documentoBytes\":\"[^\"]*\",", "");

            System.out.println("Respuesta de la consulta de firma (sin documentoBytes): " + responseBody);
            assertEquals(200, response.getStatusCode());
            // Si necesito, agregar más validaciones aquí para verificar la respuesta
        } catch (Exception e) {
            System.err.println("Error al consultar la firma del documento: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testGenerarSolicitudYConsultarFirma() throws InterruptedException {
        System.out.println("Iniciando prueba de generación de solicitud y consulta de firma...");
        String tokenSolicitud = obtenerTokenSolicitud();
        if (tokenSolicitud == null) {
            System.err.println("Error: No se pudo obtener el token de solicitud.");
            return;
        }

        String solicitudId = generarSolicitud(tokenSolicitud);
        if (solicitudId == null || solicitudId.isEmpty()) {
            System.err.println("Error: solicitudId es nulo o vacío.");
            return;
        }

        String tokenFirma = obtenerTokenFirma();
        if (tokenFirma == null) {
            System.err.println("Error: No se pudo obtener el token de firma.");
            return;
        }

        firmarDocumento(tokenFirma, solicitudId);

        // Esperar 2 minutos adicionales para permitir la firma del documento
        System.out.println("Esperando 2 minutos adicionales...");
        Thread.sleep(120000); // 2 minutos

        consultarFirmaDocumento(tokenFirma, solicitudId); // Usar solicitudId del paso 2
        System.out.println("Prueba de generación de solicitud y consulta de firma completada.");
    }
}