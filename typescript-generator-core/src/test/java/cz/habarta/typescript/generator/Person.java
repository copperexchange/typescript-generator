
package cz.habarta.typescript.generator;

import java.util.List;
import java.util.Map;

import lombok.NonNull;


public class Person {

    @NonNull
    public String name;
    public int age;
    public boolean hasChildren;
    public List<String> tags;
    public Map<String, String> emails;

}
