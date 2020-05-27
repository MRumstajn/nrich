package net.croz.nrich.registry.data.service.impl;

import lombok.SneakyThrows;
import net.croz.nrich.registry.core.model.ManagedTypeWrapper;
import net.croz.nrich.registry.data.constant.RegistryDataConstants;
import net.croz.nrich.registry.data.model.RegistrySearchConfiguration;
import net.croz.nrich.registry.data.request.CreateRegistryServiceRequest;
import net.croz.nrich.registry.data.request.DeleteRegistryRequest;
import net.croz.nrich.registry.data.request.ListRegistryRequest;
import net.croz.nrich.registry.data.request.UpdateRegistryServiceRequest;
import net.croz.nrich.registry.data.service.RegistryDataService;
import net.croz.nrich.search.api.model.SortDirection;
import net.croz.nrich.search.api.model.SortProperty;
import net.croz.nrich.search.converter.StringToEntityPropertyMapConverter;
import net.croz.nrich.search.support.JpaQueryBuilder;
import net.croz.nrich.search.util.PageableUtil;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.ManagedType;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// TODO handling of composite id attributes, better error handling and maybe versioning
public class RegistryDataServiceImpl implements RegistryDataService {

    private final EntityManager entityManager;

    private final ModelMapper modelMapper;

    private final StringToEntityPropertyMapConverter stringToEntityPropertyMapConverter;

    private final List<RegistrySearchConfiguration<?, ?>> registrySearchConfigurationList;

    private final Map<String, JpaQueryBuilder<?>> classNameQueryBuilderMap;

    public RegistryDataServiceImpl(final EntityManager entityManager, final ModelMapper modelMapper, final StringToEntityPropertyMapConverter stringToEntityPropertyMapConverter, final List<RegistrySearchConfiguration<?, ?>> registrySearchConfigurationList) {
        this.entityManager = entityManager;
        this.modelMapper = modelMapper;
        this.stringToEntityPropertyMapConverter = stringToEntityPropertyMapConverter;
        this.registrySearchConfigurationList = registrySearchConfigurationList;
        this.classNameQueryBuilderMap = initializeQueryBuilderMap(registrySearchConfigurationList);
    }

    @Transactional(readOnly = true)
    @Override
    public <P> Page<P> registryList(final ListRegistryRequest request) {
        return registryListInternal(request);
    }

    @Transactional
    @Override
    public <T> T registryCreate(final CreateRegistryServiceRequest request) {
        @SuppressWarnings("unchecked")
        final RegistrySearchConfiguration<T, ?> registrySearchConfiguration = (RegistrySearchConfiguration<T, ?>) findRegistryConfiguration(request.getClassFullName());

        final T instance = instantiate(registrySearchConfiguration.getRegistryType());

        modelMapper.map(request.getCreateData(), instance);

        entityManager.persist(instance);

        return instance;
    }

    @Transactional
    @Override
    public <T> T registryUpdate(final UpdateRegistryServiceRequest request) {
        @SuppressWarnings("unchecked")
        final RegistrySearchConfiguration<T, ?> registrySearchConfiguration = (RegistrySearchConfiguration<T, ?>) findRegistryConfiguration(request.getClassFullName());

        final T instance = entityManager.find(registrySearchConfiguration.getRegistryType(), request.getId());

        modelMapper.map(request.getUpdateData(), instance);

        entityManager.persist(instance);

        return instance;
    }

    @Transactional
    @Override
    public boolean registryDelete(final DeleteRegistryRequest request) {
        findRegistryConfiguration(request.getClassFullName());

        final String fullQuery = String.format(RegistryDataConstants.DELETE_QUERY, request.getClassFullName());

        final int updateCount = entityManager.createQuery(fullQuery).setParameter(RegistryDataConstants.ID_ATTRIBUTE, request.getId()).executeUpdate();

        return updateCount > 0;
    }

    private <T, P> Page<P> registryListInternal(final ListRegistryRequest request) {
        @SuppressWarnings("unchecked")
        final RegistrySearchConfiguration<T, P> registrySearchConfiguration = (RegistrySearchConfiguration<T, P>) findRegistryConfiguration(request.getClassFullName());

        @SuppressWarnings("unchecked")
        final JpaQueryBuilder<T> queryBuilder = (JpaQueryBuilder<T>) classNameQueryBuilderMap.get(request.getClassFullName());

        final ManagedTypeWrapper managedTypeWrapper = new ManagedTypeWrapper(resolveManagedType(registrySearchConfiguration));

        final Pageable pageable = PageableUtil.convertToPageable(request.getPageNumber(), request.getPageSize(), new SortProperty(RegistryDataConstants.ID_ATTRIBUTE, SortDirection.ASC), request.getSortPropertyList());

        Map<String, Object> searchRequestMap = Collections.emptyMap();
        if (request.getSearchParameter() != null) {
            searchRequestMap = stringToEntityPropertyMapConverter.convert(request.getSearchParameter().getQuery(), request.getSearchParameter().getPropertyNameList(), managedTypeWrapper.getIdentifiableType());
        }

        final CriteriaQuery<P> query = queryBuilder.buildQuery(searchRequestMap, registrySearchConfiguration.getSearchConfiguration(), pageable.getSort());

        final TypedQuery<P> typedQuery = entityManager.createQuery(query);

        if (pageable.isPaged()) {
            typedQuery.setFirstResult((int) pageable.getOffset()).setMaxResults(pageable.getPageSize());

            return PageableExecutionUtils.getPage(typedQuery.getResultList(), pageable, () -> executeCountQuery(queryBuilder, query));
        }

        return new PageImpl<>(typedQuery.getResultList());
    }

    private long executeCountQuery(final JpaQueryBuilder<?> queryBuilder, final CriteriaQuery<?> query) {
        final CriteriaQuery<Long> countQuery = queryBuilder.convertToCountQuery(query);

        final List<Long> totals = entityManager.createQuery(countQuery).getResultList();

        return totals.stream().mapToLong(value -> value == null ? 0L : value).sum();
    }

    private Map<String, JpaQueryBuilder<?>> initializeQueryBuilderMap(final List<RegistrySearchConfiguration<?, ?>> registrySearchConfigurationList) {
        if (registrySearchConfigurationList == null) {
            return Collections.emptyMap();
        }

        return registrySearchConfigurationList.stream()
                .collect(Collectors.toMap(registrySearchConfiguration -> registrySearchConfiguration.getRegistryType().getName(), registrySearchConfiguration -> new JpaQueryBuilder<>(entityManager, registrySearchConfiguration.getRegistryType())));
    }

    private RegistrySearchConfiguration<?, ?> findRegistryConfiguration(final String classFullName) {
        return registrySearchConfigurationList.stream()
                .filter(configuration -> configuration.getRegistryType().getName().equals(classFullName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Configuration for registry entity %s is not defined!", classFullName)));
    }

    private ManagedType<?> resolveManagedType(final RegistrySearchConfiguration<?, ?> registrySearchConfiguration) {
        return entityManager.getMetamodel().managedType(registrySearchConfiguration.getRegistryType());
    }

    @SneakyThrows
    private <T> T instantiate(final Class<T> type) {
        return type.newInstance();
    }
}
