package wiremock.myPojos;

import lombok.*;

@Builder
@Data
@EqualsAndHashCode(exclude = "std")
@ToString(includeFieldNames = false)

public class Student {
    String name;
    @NonNull
    int roll;
    int std;

}

