package net.croz.nrich.formconfiguration.service;

import net.croz.nrich.formconfiguration.FormConfigurationTestConfiguration;
import net.croz.nrich.formconfiguration.api.model.ConstrainedProperty;
import net.croz.nrich.formconfiguration.api.model.ConstrainedPropertyClientValidatorConfiguration;
import net.croz.nrich.formconfiguration.stub.FormConfigurationServiceTestRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static net.croz.nrich.formconfiguration.testutil.FormConfigurationGeneratingUtil.createConstrainedProperty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SpringJUnitConfig(FormConfigurationTestConfiguration.class)
class DefaultConstrainedPropertyValidatorConverterServiceTest {

    @Autowired
    private DefaultConstrainedPropertyValidatorConverterService defaultConstrainedPropertyValidatorConverterService;

    @Test
    void shouldConvertConstrainedPropertyToValidatorConfiguration() {
        // given
        ConstrainedProperty constrainedProperty = createConstrainedProperty(FormConfigurationServiceTestRequest.class);

        // when
        List<ConstrainedPropertyClientValidatorConfiguration> validationConfigurationList = defaultConstrainedPropertyValidatorConverterService.convert(constrainedProperty);

        // then
        assertThat(validationConfigurationList).hasSize(1);

        // and when
        ConstrainedPropertyClientValidatorConfiguration validatorConfiguration = validationConfigurationList.get(0);

        // then
        assertThat(validatorConfiguration).isNotNull();
        assertThat(validatorConfiguration.getArgumentMap()).isEmpty();
        assertThat(validatorConfiguration.getName()).isEqualTo("NotNull");
        assertThat(validatorConfiguration.getErrorMessage()).isEqualTo("Really cannot be null");
    }

    @Test
    void shouldSupportAll() {
        // given
        ConstrainedProperty property = mock(ConstrainedProperty.class);

        // when
        boolean result = defaultConstrainedPropertyValidatorConverterService.supports(property);

        // then
        assertThat(result).isTrue();
    }
}
