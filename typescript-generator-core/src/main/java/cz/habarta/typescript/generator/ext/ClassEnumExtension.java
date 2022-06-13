
package cz.habarta.typescript.generator.ext;

import java.lang.annotation.Annotation;

import cz.habarta.typescript.generator.Extension;
import cz.habarta.typescript.generator.compiler.EnumKind;
import cz.habarta.typescript.generator.compiler.EnumMemberModel;
import cz.habarta.typescript.generator.compiler.ModelCompiler;
import cz.habarta.typescript.generator.compiler.ModelTransformer;
import cz.habarta.typescript.generator.compiler.SymbolTable;
import cz.habarta.typescript.generator.emitter.EmitterExtensionFeatures;
import cz.habarta.typescript.generator.emitter.TsBeanModel;
import cz.habarta.typescript.generator.emitter.TsEnumModel;
import cz.habarta.typescript.generator.emitter.TsModel;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class ClassEnumExtension extends Extension {

    public static final String CFG_CLASS_ENUM_PATTERN = "classEnumPattern";

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
                            try {
                               value = declaredField.get(declaredField.getClass()).toString();
                            } catch (IllegalAccessException ignored) { }
                            members.add(new EnumMemberModel(declaredField.getName(), value, declaredField, tsBeanModel.getComments()));
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
                    stringEnums.add(temp);
                }

                stringEnums.addAll(model.getEnums());
                return model.withEnums(stringEnums).withoutBeans(classEnums);
            }
        }));
    }
}
