package wiremock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.jayway.jsonpath.JsonPath;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.log4j.Log4j2;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.restassured.RestAssured.given;

@Log4j2
public class MockBase {

    private WireMockServer wireMockServer;
    private RequestSpecification requestSpecification;
    PrintStream printStream;

    public RequestSpecification setRALogFilter() {
        try {
            printStream = new PrintStream(new FileOutputStream("log/app.log", true));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        requestSpecification = new RequestSpecBuilder()
                .addFilter(RequestLoggingFilter.logRequestTo(printStream))
                .addFilter(ResponseLoggingFilter.logResponseTo(printStream, LogDetail.ALL))
                .build();
        return requestSpecification;
    }

    public RequestSpecification setPort(){
        requestSpecification = new RequestSpecBuilder()
                .setPort(wireMockServer.port())
                .build();
        return requestSpecification;
    }

    public void closePrintStream() {
        if (null != printStream) {
            log.info("Closing PS...");
            printStream.flush();
            printStream.close();
        }
    }

    private boolean isPortInUse(String host, int port) {
        boolean result = false;
        try {
            (new Socket(host, port)).close();
            result = true;
        } catch (SocketException e) {
            // Could not connect.
        } catch (UnknownHostException e) {
            // Host not found
        } catch (IOException e) {
            // IO exception
        }
        return result;
    }

    public void startWireMockServer() {
        log.info("Starting WireMockServer");
        if (isPortInUse(null, 8080)) {
            log.info("Port 8080 is Busy");
            wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        } else {
            wireMockServer = new WireMockServer();
        }
        wireMockServer.start();
        log.info("Started WireMockServer at Port: " + wireMockServer.port());
    }

    /*@Test
    public void test001_dynamicPort() {
        WireMockServer wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
        log.info("Started WireMockServer at Port: " + wireMockServer.port());
        wireMockServer.stubFor(get(urlEqualTo("/some/thing"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "text/plain")
                        .withBody("Hello World!")));
        Response response = given().spec(mockBase.setRALogFilter())
                .port(wireMockServer.port())
                .when()
                .get("/some/thing")
                .then()
                .extract()
                .response();
        Assert.assertEquals(response.getBody().asString(), "Hello World!");
        wireMockServer.shutdown();
    }*/

    public void startWireMockServerOnThisPort(int port) {
        wireMockServer = new WireMockServer(wireMockConfig().port(port));
        wireMockServer.start();
        log.info("WireMock Started on Port: " + wireMockServer.port());
        //return wireMockServer;
    }

    public void stopWireMockServer() {
        if (null != wireMockServer && wireMockServer.isRunning()) {
            log.info("Shutting Down WireMock...");
            wireMockServer.shutdownServer();
        }
    }

    public String getAuthToken(String userName, String passWord) {
        log.info("Setting User Credentials...");
        new Stubs().getStubForBasicAuthPreemptiveAuthToken(wireMockServer);
        Response response = given().spec(setPort()).
                auth().preemptive().basic(userName, passWord).
                when().
                get("/basic/auth/preemptive").
                then().extract().response();
        Assert.assertEquals(response.getStatusCode(), 200, "Auth Token didn't generated...");
        return JsonPath.read(response.asString(), "$.auth_token");
    }

    public void tearDown() {
        if (null != wireMockServer && wireMockServer.isRunning()) {
            // graceful shutdown
            log.info("Shutting Down WireMock...");
            wireMockServer.shutdownServer();
        }
        // force stop
        // wireMockServer.stop();
    }

    @BeforeMethod
    public void beforeTestMethod(ITestResult result) {
        log.info("Executing -> " + result.getMethod().getMethodName());
    }

    @AfterMethod
    public void afterTestMethod(ITestResult result) {
        log.info("Finished Executing -> " + result.getMethod().getMethodName());
    }

    public void startWireMockOnThisPort(int port) {
        wireMockServer = new WireMockServer(wireMockConfig().port(port));
        wireMockServer.start();
        log.info("Started WireMock on Port: " + wireMockServer.port());
    }

    public void turnOffWiremockLogging() {
        System.setProperty("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.StdErrLog");
        //System.setProperty("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.Log");
        //org.eclipse.jetty.util.log.Log;
        System.setProperty("org.eclipse.jetty.LEVEL", "OFF");
        System.setProperty("org.eclipse.jetty.util.log.announce", "false");
        /*org.eclipse.jetty.util.log.Log.getProperties().setProperty("org.eclipse.jetty.LEVEL", "OFF");
        org.eclipse.jetty.util.log.Log.getProperties().setProperty("org.eclipse.jetty.util.log.announce", "false");
        org.eclipse.jetty.util.log.Log.getRootLogger().setDebugEnabled(false);*/
    }

    public void printJson(String obj) {
        ObjectMapper objectMapper = new ObjectMapper();
        //objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            Object json = objectMapper.readValue(obj, Object.class);
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void removeAllStub() {
        List<StubMapping> stubMappings = wireMockServer.getStubMappings();
        log.info("Total Stub Found: " + stubMappings.size());
        log.info("Removing Stubs...");
        stubMappings.forEach(wireMockServer::removeStub);
    }

    public WireMockServer getWireMockServer() {
        return wireMockServer;
    }

}
