package wiremock.helpers;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class RestAssuredRequestFilter implements Filter {

    @Override
    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
        Response response = ctx.next(requestSpec, responseSpec);
        if (response.statusCode() != 200) {
            log.error(requestSpec.getMethod() + " " + requestSpec.getURI() + " => " +
                    response.getStatusCode() + " " + response.getStatusLine());
        }
        log.info(
                "\n Method: " + requestSpec.getMethod()
                        + " "
                        + requestSpec.getURI()
                        + " \n Request Body =>"
                        + requestSpec.getBody()
                        + "\n Response Status => "
                        +
                        response.getStatusCode()
                        + " "
                        + response.getStatusLine()
                        + " \n Response Body => "
                        + response.getBody().prettyPrint());
        return response;
    }

}
