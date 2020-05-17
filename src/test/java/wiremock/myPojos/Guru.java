package wiremock.myPojos;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.testng.annotations.Test;

import java.util.Objects;

@Getter
@Setter
@ToString

public class Guru {
    int id;
    String name;
    String company;
    String country;
    String tool;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Guru)) return false;
        Guru guru = (Guru) o;
        return getId() == guru.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
