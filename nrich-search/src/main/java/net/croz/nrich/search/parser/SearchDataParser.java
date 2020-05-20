package net.croz.nrich.search.parser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.croz.nrich.search.model.Restriction;
import net.croz.nrich.search.model.SearchDataParserConfiguration;
import net.croz.nrich.search.model.SearchOperator;
import net.croz.nrich.search.model.SearchOperatorImpl;
import net.croz.nrich.search.model.SearchPropertyMapping;
import net.croz.nrich.search.properties.SearchProperties;
import net.croz.nrich.search.support.JpaEntityAttributeResolver;
import org.springframework.data.util.DirectFieldAccessFallbackBeanWrapper;
import org.springframework.util.StringUtils;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// TODO decide what should be used as constructor and what should be used as method arguments
@RequiredArgsConstructor
@Slf4j
public class SearchDataParser {

    private final SearchProperties searchProperties;

    private final ManagedType<?> managedType;

    private final Object searchData;

    private final SearchDataParserConfiguration searchConfiguration;

    public Set<Restriction> resolveRestrictionList() {
        return resolveRestrictionList(null);
    }

    public Set<Restriction> resolveRestrictionList(final String propertyPrefix) {
        return resolveRestrictionListInternal(new DirectFieldAccessFallbackBeanWrapper(searchData), propertyPrefix, null, managedType, new HashSet<>(), false);
    }

    private Set<Restriction> resolveRestrictionListInternal(final DirectFieldAccessFallbackBeanWrapper wrapper, final String propertyPrefix, final String path, final ManagedType<?> managedType, final Set<Restriction> restrictionList, final boolean isPluralAttribute) {
        final List<Field> fieldList = resolveFieldList(wrapper);
        final JpaEntityAttributeResolver attributeResolver = new JpaEntityAttributeResolver(managedType);

        fieldList.forEach(field -> {
            final String originalFieldName = field.getName();
            final String fieldNameWithoutPrefixAndRangeSuffix = fieldNameWithoutRangeSuffixAndPrefix(originalFieldName, propertyPrefix);
            final Object value = wrapper.getPropertyValue(originalFieldName);

            if (value == null) {
                return;
            }

            JpaEntityAttributeResolver.AttributeHolder attributeHolder = attributeResolver.resolveAttributeByPath(fieldNameWithoutPrefixAndRangeSuffix);

            if (attributeHolder.getAttribute() != null) {
                final String currentPath = path == null ? fieldNameWithoutPrefixAndRangeSuffix : path + "." + fieldNameWithoutPrefixAndRangeSuffix;

                if (attributeHolder.getManagedType() != null) {
                    resolveRestrictionListInternal(new DirectFieldAccessFallbackBeanWrapper(value), propertyPrefix, currentPath, attributeHolder.getManagedType(), restrictionList, attributeHolder.isPlural());
                    return;
                }
                // TODO add support for conversion of properties (converter classes)
                if (!attributeHolder.getAttribute().getJavaType().isAssignableFrom(field.getType())) {
                    log.info("Skipping searching by attribute {} class doesn't match with expected and no converter has been found", field.getName());
                    return;
                }

                restrictionList.add(createAttributeRestriction(attributeHolder.getAttribute().getJavaType(), originalFieldName, currentPath, value, isPluralAttribute));
            }
            else if (searchUsingPropertyMapping(searchConfiguration)) {
                String mappedPath = null;
                if (searchConfiguration.getPropertyMappingList() != null) {
                    mappedPath = searchConfiguration.getPropertyMappingList().stream().filter(mapping -> originalFieldName.equals(mapping.getName())).map(SearchPropertyMapping::getPath).findAny().orElse(null);
                }

                if (mappedPath == null) {
                    mappedPath = findPathUsingAttributePrefix(fieldList, managedType);
                }

                if (mappedPath != null) {
                    attributeHolder = attributeResolver.resolveAttributeByPath(mappedPath);

                    if (attributeHolder.getAttribute() != null) {
                        restrictionList.add(createAttributeRestriction(attributeHolder.getAttribute().getJavaType(), originalFieldName, mappedPath, value, attributeHolder.isPlural()));
                    }
                }
            }
        });

        return restrictionList;
    }

    private List<Field> resolveFieldList(final DirectFieldAccessFallbackBeanWrapper wrapper) {
        final List<String> ignoredFieldList = searchProperties.getSearchIgnoredFieldList() == null ? Collections.emptyList() : searchProperties.getSearchIgnoredFieldList();

        return Arrays.stream(wrapper.getRootClass().getDeclaredFields())
                .filter(field -> !ignoredFieldList.contains(field.getName()) && !Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers()))
                .collect(Collectors.toList());
    }

    private String fieldNameWithoutRangeSuffixAndPrefix(final String originalFieldName, final String prefix) {
        final String[] suffixListToRemove = new String[]{searchProperties.getRangeQueryFromIncludingSuffix(), searchProperties.getRangeQueryFromSuffix(), searchProperties.getRangeQueryToIncludingSuffix(), searchProperties.getRangeQueryToSuffix()};

        String fieldName = originalFieldName;
        for (final String suffix : suffixListToRemove) {
            if (originalFieldName.endsWith(suffix)) {
                fieldName = originalFieldName.substring(0, originalFieldName.lastIndexOf(suffix));
                break;
            }
        }

        if (prefix != null && fieldName.length() > prefix.length()) {
            return StringUtils.uncapitalize(fieldName.substring(prefix.length()));
        }

        return fieldName;
    }

    private Restriction createAttributeRestriction(final Class<?> attributeType, final String attributeName, final String path, final Object value, final boolean isPluralAttribute) {
        final boolean isRangeSearchSupported = isRangeSearchSupported(attributeType);
        final SearchOperator resolvedOperator = resolveFromSearchConfiguration(searchConfiguration, path, attributeType);

        SearchOperator operator = SearchOperatorImpl.EQ;
        if (resolvedOperator != null) {
            operator = resolvedOperator;
        }
        else if (String.class.isAssignableFrom(attributeType)) {
            operator = SearchOperatorImpl.ILIKE;
        }
        if (isRangeSearchSupported) {
            if (attributeName.endsWith(searchProperties.getRangeQueryFromIncludingSuffix())) {
                operator = SearchOperatorImpl.GE;
            }
            else if (attributeName.endsWith(searchProperties.getRangeQueryFromSuffix())) {
                operator = SearchOperatorImpl.GT;
            }
            else if (attributeName.endsWith(searchProperties.getRangeQueryToIncludingSuffix())) {
                operator = SearchOperatorImpl.LE;
            }
            else if (attributeName.endsWith(searchProperties.getRangeQueryToSuffix())) {
                operator = SearchOperatorImpl.LT;
            }
        }

        return new Restriction(path, operator, value, isPluralAttribute);
    }

    private boolean isRangeSearchSupported(final Class<?> attributeType) {
        return searchProperties.getRangeQuerySupportedClassList() != null && searchProperties.getRangeQuerySupportedClassList().stream().anyMatch(type -> type.isAssignableFrom(attributeType));
    }

    private String findPathUsingAttributePrefix(final List<Field> fieldList, final ManagedType<?> managedType) {
        final List<String> attributeNameList = managedType.getAttributes().stream().filter(Attribute::isAssociation).map(Attribute::getName).collect(Collectors.toList());
        final List<String> fieldNameList = fieldList.stream().map(Field::getName).collect(Collectors.toList());

        String foundPath = null;

        for (final String attribute : attributeNameList) {
            final String foundFieldName = fieldNameList.stream()
                    .filter(field -> field.startsWith(attribute) && field.length() > attribute.length())
                    .findAny().orElse(null);

            if (foundFieldName != null) {
                foundPath = attribute + "." + StringUtils.uncapitalize(foundFieldName.substring(attribute.length()));
                break;
            }
        }

        return foundPath;
    }

    private boolean searchUsingPropertyMapping(final SearchDataParserConfiguration searchConfiguration) {
        return searchConfiguration.isResolveFieldMappingUsingPrefix() || searchConfiguration.getPropertyMappingList() != null;
    }

    private SearchOperator resolveFromSearchConfiguration(final SearchDataParserConfiguration searchConfiguration, final String path, final Class<?> attributeType) {
        SearchOperator operator = null;

        if (searchConfiguration.getPathSearchOperatorMap() != null && searchConfiguration.getPathSearchOperatorMap().containsKey(path)) {
            operator = searchConfiguration.getPathSearchOperatorMap().get(path);
        }
        else if (searchConfiguration.getTypeSearchOperatorMap() != null && searchConfiguration.getTypeSearchOperatorMap().containsKey(attributeType)) {
            operator = searchConfiguration.getTypeSearchOperatorMap().get(attributeType);
        }

        return operator;
    }
}
