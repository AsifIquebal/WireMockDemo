package wiremock.myPojos;

import lombok.*;

@Builder
@Data
@EqualsAndHashCode(exclude = "std")
@ToString(includeFieldNames = false)

public class Student {
    String name;
    int roll;
    int std;

}

