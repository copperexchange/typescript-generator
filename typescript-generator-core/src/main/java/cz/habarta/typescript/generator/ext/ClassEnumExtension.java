
package cz.habarta.typescript.generator.ext;

import java.io.File;
import java.lang.annotation.Annotation;

import cz.habarta.typescript.generator.Extension;
import cz.habarta.typescript.generator.Settings;
import cz.habarta.typescript.generator.compiler.EnumKind;
import cz.habarta.typescript.generator.compiler.EnumMemberModel;
import cz.habarta.typescript.generator.compiler.ModelCompiler;
import cz.habarta.typescript.generator.compiler.ModelTransformer;
import cz.habarta.typescript.generator.compiler.SymbolTable;
import cz.habarta.typescript.generator.emitter.*;
import cz.habarta.typescript.generator.parser.Javadoc;

import java.lang.reflect.Field;
import java.util.*;


public class ClassEnumExtension extends Extension {

    public static final String CFG_CLASS_ENUM_PATTERN = "classEnumPattern";
    public static final String CFG_JAVADOC_FILE_PATH = "javadocFilePath";

    private Javadoc javadoc;
    private String classEnumPattern = "Enum";

    @Override
    public EmitterExtensionFeatures getFeatures() {
        final EmitterExtensionFeatures features = new EmitterExtensionFeatures();
        features.worksWithPackagesMappedToNamespaces = true;
        return features;
    }

    @Override
    public void setConfiguration(Map<String, String> configuration) throws RuntimeException {
        if (configuration.containsKey(CFG_CLASS_ENUM_PATTERN)) {
            classEnumPattern = configuration.get(CFG_CLASS_ENUM_PATTERN);
        }
        if (configuration.containsKey(CFG_JAVADOC_FILE_PATH)) {
            Settings settings = new Settings();
            settings.javadocXmlFiles = Collections.singletonList(new File(configuration.get(CFG_JAVADOC_FILE_PATH)));
            String newline = String.format("%n");
            settings.newline = newline;

            javadoc = new Javadoc(settings);
        }
    }

    @Override
    public List<TransformerDefinition> getTransformers() {
        return Arrays.asList(new TransformerDefinition(ModelCompiler.TransformationPhase.BeforeEnums, new ModelTransformer() {
            @Override
            public TsModel transformModel(SymbolTable symbolTable, TsModel model) {
                List<TsBeanModel> beans = model.getBeans();
                List<TsBeanModel> classEnums = new ArrayList<>();
                for (TsBeanModel bean : beans) {
                    Annotation[] annotations = bean.getOrigin().getDeclaredAnnotations();
                    if (Arrays.stream(annotations).anyMatch(a -> a.annotationType().getName().contains(classEnumPattern))) {
                        classEnums.add(bean);
                    }
                }

                List<TsEnumModel> stringEnums = new ArrayList<>();
                for (TsBeanModel tsBeanModel : classEnums) {
                    List<EnumMemberModel> members = new ArrayList<>();
                    for (Field declaredField : tsBeanModel.getOrigin().getDeclaredFields()) {
                        if (declaredField.getType().getName().equals(tsBeanModel.getOrigin().getName())) {
                            String value = declaredField.getName();
                            TsPropertyModel property = null;
                            try {
                               value = declaredField.get(declaredField.getClass()).toString();
                               property = tsBeanModel.getProperties().stream()
                                    .filter((p) -> p.getName().equals(declaredField.getName()))
                                    .findFirst().orElse(null);
                            } catch (IllegalAccessException ignored) { }
                            members.add(new EnumMemberModel(declaredField.getName(), value, null, property != null ? property.getComments() : null));
                        }
                    }
                    TsEnumModel temp = new TsEnumModel(
                            tsBeanModel.getOrigin(),
                            tsBeanModel.getName(),
                            EnumKind.StringBased,
                            members,
                            tsBeanModel.getComments(),
                            false
                    );

                    stringEnums.add(javadoc != null ? javadoc.enrichClassEnumModel(temp) : temp);
                }

                stringEnums.addAll(model.getEnums());
                return model.withEnums(stringEnums).withoutBeans(classEnums);
            }
        }));
    }
}
