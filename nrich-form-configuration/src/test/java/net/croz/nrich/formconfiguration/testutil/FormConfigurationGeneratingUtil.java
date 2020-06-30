package net.croz.nrich.formconfiguration.testutil;

import net.croz.nrich.formconfiguration.api.model.ConstrainedProperty;
import net.croz.nrich.formconfiguration.api.request.FetchFormConfigurationRequest;

import javax.validation.constraints.NotNull;
import javax.validation.metadata.ConstraintDescriptor;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class FormConfigurationGeneratingUtil {

    private FormConfigurationGeneratingUtil() {
    }

    public static FetchFormConfigurationRequest fetchFormConfigurationRequest(final String... formIdList) {
        final FetchFormConfigurationRequest request = new FetchFormConfigurationRequest();

        request.setFormIdList(Arrays.asList(formIdList));

        return request;
    }

    public static ConstrainedProperty createConstrainedProperty(final Class<?> parentType) {
        return createConstrainedProperty(parentType, Collections.emptyMap());
    }

    public static ConstrainedProperty createConstrainedProperty(final Class<?> parentType, final Map<String, Object> attributeMap) {
        @SuppressWarnings("unchecked")
        final ConstraintDescriptor<Annotation> constraintDescriptor = mock(ConstraintDescriptor.class);
        final Annotation annotation = mock(Annotation.class);

        doReturn(NotNull.class).when(annotation).annotationType();

        when(constraintDescriptor.getAttributes()).thenReturn(attributeMap);
        when(constraintDescriptor.getAnnotation()).thenReturn(annotation);

        return ConstrainedProperty.builder()
                .constraintDescriptor(constraintDescriptor)
                .name("propertyName")
                .path("propertyPath")
                .type(String.class)
                .parentType(parentType).build();

    }
}
