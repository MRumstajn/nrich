package net.croz.nrich.registry.configuration.service;

import net.croz.nrich.registry.RegistryTestConfiguration;
import net.croz.nrich.registry.api.configuration.model.RegistryGroupConfiguration;
import net.croz.nrich.registry.api.configuration.model.RegistryEntityConfiguration;
import net.croz.nrich.registry.api.configuration.model.property.JavascriptType;
import net.croz.nrich.registry.api.configuration.model.property.RegistryPropertyConfiguration;
import net.croz.nrich.registry.configuration.stub.RegistryConfigurationTestEntity;
import net.croz.nrich.registry.configuration.stub.RegistryConfigurationTestEntityWithAssociationAndEmbeddedId;
import net.croz.nrich.registry.configuration.stub.RegistryConfigurationTestEntityWithIdClass;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitWebConfig(RegistryTestConfiguration.class)
public class DefaultRegistryConfigurationServiceTest {

    @Autowired
    private DefaultRegistryConfigurationService registryConfigurationService;

    @Test
    void shouldResolveRegistryConfiguration() {
        // when
        final List<RegistryGroupConfiguration> result = registryConfigurationService.fetchRegistryGroupConfigurationList();

        // then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(3);
        assertThat(result).extracting("groupId").containsExactly("CONFIGURATION", "DATA", "HISTORY");
        assertThat(result).extracting("groupIdDisplayName").containsExactly("Configuration group", "Data group", "HISTORY");
    }

    @Test
    void shouldResolveConfigurationWithOverrideDefined() {
        // when
        final List<RegistryGroupConfiguration> result = registryConfigurationService.fetchRegistryGroupConfigurationList();
        final RegistryGroupConfiguration registryTestEntityConfiguration = result.get(0);
        final RegistryEntityConfiguration registryEntityConfiguration = registryTestEntityConfiguration.getEntityConfigurationList().stream()
                .filter(entityConfig -> RegistryConfigurationTestEntity.class.getName().equals(entityConfig.getClassFullName()))
                .findFirst()
                .orElse(null);

        // then
        assertThat(registryEntityConfiguration).isNotNull();

        assertThat(registryEntityConfiguration.getGroupId()).isEqualTo("CONFIGURATION");

        assertThat(registryEntityConfiguration.getName()).isEqualTo(RegistryConfigurationTestEntity.class.getSimpleName());
        assertThat(registryEntityConfiguration.getDisplayName()).isEqualTo("Test entity");
        assertThat(registryEntityConfiguration.isReadOnly()).isFalse();
        assertThat(registryEntityConfiguration.isCreatable()).isTrue();
        assertThat(registryEntityConfiguration.isUpdateable()).isTrue();
        assertThat(registryEntityConfiguration.isDeletable()).isFalse();
        assertThat(registryEntityConfiguration.isIdentifierAssigned()).isTrue();
        assertThat(registryEntityConfiguration.isIdClassIdentity()).isFalse();
        assertThat(registryEntityConfiguration.isEmbeddedIdentity()).isFalse();
        assertThat(registryEntityConfiguration.getIdClassPropertyNameList()).isNullOrEmpty();
        assertThat(registryEntityConfiguration.isHistoryAvailable()).isFalse();

        assertThat(registryEntityConfiguration.getPropertyConfigurationList()).hasSize(5);
        assertThat(registryEntityConfiguration.getPropertyConfigurationList()).extracting("name").containsExactly("name", "id", "nonEditableProperty", "floatNumber", "doubleNumber");
        assertThat(registryEntityConfiguration.getPropertyConfigurationList()).extracting("isDecimal").containsExactly(false, false, false, true, true);

        // and when
        final RegistryPropertyConfiguration nameConfiguration = registryEntityConfiguration.getPropertyConfigurationList().get(0);

        // then
        assertThat(nameConfiguration.getJavascriptType()).isEqualTo(JavascriptType.STRING);
        assertThat(nameConfiguration.getOriginalType()).isEqualTo(String.class.getName());
        assertThat(nameConfiguration.isId()).isFalse();
        assertThat(nameConfiguration.isDecimal()).isFalse();
        assertThat(nameConfiguration.isSingularAssociation()).isFalse();
        assertThat(nameConfiguration.getFormLabel()).isEqualTo("Name of property");
        assertThat(nameConfiguration.getColumnHeader()).isEqualTo("Header of property");
        assertThat(nameConfiguration.isEditable()).isTrue();
        assertThat(nameConfiguration.isSortable()).isTrue();

        // and when
        final RegistryPropertyConfiguration idPropertyConfiguration = registryEntityConfiguration.getPropertyConfigurationList().get(1);

        // then
        assertThat(idPropertyConfiguration.isId()).isTrue();

        // and when
        final RegistryPropertyConfiguration nonEditablePropertyConfiguration = registryEntityConfiguration.getPropertyConfigurationList().get(2);

        // then
        assertThat(nonEditablePropertyConfiguration.getJavascriptType()).isEqualTo(JavascriptType.STRING);
        assertThat(nonEditablePropertyConfiguration.getOriginalType()).isEqualTo(String.class.getName());
        assertThat(nonEditablePropertyConfiguration.isId()).isFalse();
        assertThat(nameConfiguration.isDecimal()).isFalse();
        assertThat(nameConfiguration.isSingularAssociation()).isFalse();
        assertThat(nonEditablePropertyConfiguration.isEditable()).isFalse();
        assertThat(nonEditablePropertyConfiguration.isSortable()).isFalse();

        // and when
        final List<RegistryPropertyConfiguration> registryHistoryPropertyConfigurationList = registryEntityConfiguration.getHistoryPropertyConfigurationList();

        // then
        assertThat(registryHistoryPropertyConfigurationList).isNotEmpty();
        assertThat(registryHistoryPropertyConfigurationList).extracting("name").containsExactlyInAnyOrder("revisionNumber", "revisionTimestamp", "revisionType", "revisionProperty");
        assertThat(registryHistoryPropertyConfigurationList).extracting("formLabel").containsExactlyInAnyOrder("Revision number", "Revision timestamp", "Revision type", "Revision property");
        assertThat(registryHistoryPropertyConfigurationList).extracting("columnHeader").containsExactlyInAnyOrder("Revision number", "Revision timestamp", "Revision type", "Revision property");
    }

    @Test
    void shouldResolveRegistryConfigurationForComplexEntitiesWithAssociationsAndEmbeddedId() {
        // when
        final List<RegistryGroupConfiguration> result = registryConfigurationService.fetchRegistryGroupConfigurationList();
        final RegistryGroupConfiguration registryTestEntityConfiguration = result.get(0);
        final RegistryEntityConfiguration registryEntityConfiguration = registryTestEntityConfiguration.getEntityConfigurationList().stream()
                .filter(entityConfig -> RegistryConfigurationTestEntityWithAssociationAndEmbeddedId.class.getName().equals(entityConfig.getClassFullName()))
                .findFirst()
                .orElse(null);


        // then
        assertThat(registryEntityConfiguration).isNotNull();

        assertThat(registryEntityConfiguration.isIdentifierAssigned()).isTrue();
        assertThat(registryEntityConfiguration.isIdClassIdentity()).isFalse();
        assertThat(registryEntityConfiguration.isEmbeddedIdentity()).isTrue();
        assertThat(registryEntityConfiguration.getIdClassPropertyNameList()).isEmpty();

        assertThat(registryEntityConfiguration.getPropertyConfigurationList()).extracting("name").containsExactly("id", "amount", "registryConfigurationTestEntityManyToOne", "registryConfigurationTestEntityOneToOne");
        assertThat(registryEntityConfiguration.getEmbeddedIdPropertyConfigurationList()).extracting("name").containsExactlyInAnyOrder("id.firstId", "id.secondId");

        // and when
        final RegistryPropertyConfiguration numberRegistryConfiguration = registryEntityConfiguration.getPropertyConfigurationList().get(1);

        // then
        assertThat(numberRegistryConfiguration.isDecimal()).isTrue();
        assertThat(numberRegistryConfiguration.getJavascriptType()).isEqualTo(JavascriptType.NUMBER);

        // and when
        final RegistryPropertyConfiguration manyToOnePropertyConfiguration = registryEntityConfiguration.getPropertyConfigurationList().get(2);

        // then
        assertThat(manyToOnePropertyConfiguration.isSingularAssociation()).isTrue();
        assertThat(manyToOnePropertyConfiguration.getSingularAssociationReferencedClass()).isEqualTo(RegistryConfigurationTestEntity.class.getName());

        // and when
        final RegistryPropertyConfiguration oneToOnePropertyConfiguration = registryEntityConfiguration.getPropertyConfigurationList().get(3);

        // then
        assertThat(oneToOnePropertyConfiguration.isSingularAssociation()).isTrue();
        assertThat(oneToOnePropertyConfiguration.getSingularAssociationReferencedClass()).isEqualTo(RegistryConfigurationTestEntity.class.getName());
    }

    @Test
    void shouldResolveRegistryConfigurationForComplexEntitiesWithIdClass() {
        // when
        final List<RegistryGroupConfiguration> result = registryConfigurationService.fetchRegistryGroupConfigurationList();
        final RegistryGroupConfiguration registryTestEntityConfiguration = result.get(0);
        final RegistryEntityConfiguration registryEntityConfiguration = registryTestEntityConfiguration.getEntityConfigurationList().stream()
                .filter(entityConfig -> RegistryConfigurationTestEntityWithIdClass.class.getName().equals(entityConfig.getClassFullName()))
                .findFirst()
                .orElse(null);


        // then
        assertThat(registryEntityConfiguration).isNotNull();

        assertThat(registryEntityConfiguration.isIdentifierAssigned()).isTrue();
        assertThat(registryEntityConfiguration.isEmbeddedIdentity()).isFalse();
        assertThat(registryEntityConfiguration.isIdClassIdentity()).isTrue();
        assertThat(registryEntityConfiguration.getIdClassPropertyNameList()).containsExactlyInAnyOrder("firstId", "secondId");

        assertThat(registryEntityConfiguration.getPropertyConfigurationList()).extracting("name").containsExactlyInAnyOrder("firstId", "secondId", "name");
        assertThat(registryEntityConfiguration.getPropertyConfigurationList()).extracting("isId").containsExactlyInAnyOrder(true, true, false);
    }
}
