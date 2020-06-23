package net.croz.nrich.registry.starter.configuration.stub;

import net.croz.nrich.registry.starter.configuration.NrichRegistryAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration(proxyBeanMethods = false)
public class RegistryUserFormConfiguration {

    @Bean(name = NrichRegistryAutoConfiguration.FORM_CONFIGURATION_MAPPING_BEAN_NAME)
    public Map<String, Class<?>> formConfigurationMapping() {
        return new HashMap<>();
    }

}
