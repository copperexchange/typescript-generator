package cz.habarta.typescript.generator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Objects;


/**
 * This is dummy class
 */
@TestAnnotation
public class DummyClassEnum {

    /**
     * A type comment
     */
    public static final DummyClassEnum ATYPE  = new DummyClassEnum("a-type");
    /**
     * B type comment
     */
    public static final DummyClassEnum BTYPE  = new DummyClassEnum("b-type");
    /**
     * C type comment
     */
    public static final DummyClassEnum CTYPE  = new DummyClassEnum("c-type");

    private final String value;

    @JsonCreator
    public DummyClassEnum(String value) {
        this.value = Objects.requireNonNull(value);
    }

    @JsonValue
    public String getValue() { return value; }

    @Override
    public String toString() { return value; }

}
