package net.croz.nrich.registry.data.service;

import net.croz.nrich.registry.api.core.service.RegistryEntityFinderService;
import net.croz.nrich.registry.api.data.interceptor.RegistryDataInterceptor;
import net.croz.nrich.registry.api.data.request.ListBulkRegistryRequest;
import net.croz.nrich.registry.api.data.request.ListRegistryRequest;
import net.croz.nrich.registry.api.data.service.RegistryDataService;
import net.croz.nrich.registry.core.model.RegistryDataConfiguration;
import net.croz.nrich.registry.core.model.RegistryDataConfigurationHolder;
import net.croz.nrich.registry.core.support.ManagedTypeWrapper;
import net.croz.nrich.search.api.converter.StringToEntityPropertyMapConverter;
import net.croz.nrich.search.api.model.sort.SortDirection;
import net.croz.nrich.search.api.model.sort.SortProperty;
import net.croz.nrich.search.api.util.PageableUtil;
import net.croz.nrich.search.support.JpaQueryBuilder;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// TODO better error handling and maybe versioning
public class DefaultRegistryDataService implements RegistryDataService {

    private final EntityManager entityManager;

    private final ModelMapper modelMapper;

    private final StringToEntityPropertyMapConverter stringToEntityPropertyMapConverter;

    private final RegistryDataConfigurationHolder registryDataConfigurationHolder;

    private final List<RegistryDataInterceptor> registryDataInterceptorList;

    private final Map<String, JpaQueryBuilder<?>> classNameQueryBuilderMap;

    private final RegistryEntityFinderService registryEntityFinderService;

    public DefaultRegistryDataService(final EntityManager entityManager, final ModelMapper modelMapper, final StringToEntityPropertyMapConverter stringToEntityPropertyMapConverter, final RegistryDataConfigurationHolder registryDataConfigurationHolder, final List<RegistryDataInterceptor> registryDataInterceptorList, final RegistryEntityFinderService registryEntityFinderService) {
        this.entityManager = entityManager;
        this.modelMapper = modelMapper;
        this.stringToEntityPropertyMapConverter = stringToEntityPropertyMapConverter;
        this.registryDataConfigurationHolder = registryDataConfigurationHolder;
        this.registryDataInterceptorList = registryDataInterceptorList;
        this.classNameQueryBuilderMap = initializeQueryBuilderMap(registryDataConfigurationHolder);
        this.registryEntityFinderService = registryEntityFinderService;
    }

    @Transactional(readOnly = true)
    @Override
    public Map<String, Page<?>> listBulk(final ListBulkRegistryRequest request) {
        return request.getRegistryRequestList().stream()
                .collect(Collectors.toMap(ListRegistryRequest::getClassFullName, this::list));
    }

    @Transactional(readOnly = true)
    @Override
    public <P> Page<P> list(final ListRegistryRequest request) {
        interceptorList().forEach(registryDataInterceptor -> registryDataInterceptor.beforeRegistryList(request));

        return registryListInternal(request);
    }

    @Transactional
    @Override
    public <T> T create(final String classFullName, final Object entityData) {
        interceptorList().forEach(registryDataInterceptor -> registryDataInterceptor.beforeRegistryCreate(classFullName, entityData));

        @SuppressWarnings("unchecked")
        final RegistryDataConfiguration<T, ?> registryDataConfiguration = (RegistryDataConfiguration<T, ?>) registryDataConfigurationHolder.findRegistryConfigurationForClass(classFullName);

        final T instance = resolveEntityInstance(registryDataConfiguration.getRegistryType(), entityData);

        modelMapper.map(entityData, instance);

        return entityManager.merge(instance);
    }

    @Transactional
    @Override
    public <T> T update(final String classFullName, final Object id, final Object entityData) {
        interceptorList().forEach(registryDataInterceptor -> registryDataInterceptor.beforeRegistryUpdate(classFullName, id, entityData));

        @SuppressWarnings("unchecked")
        final RegistryDataConfiguration<T, ?> registryDataConfiguration = (RegistryDataConfiguration<T, ?>) registryDataConfigurationHolder.findRegistryConfigurationForClass(classFullName);

        final ManagedTypeWrapper wrapper = registryDataConfigurationHolder.resolveManagedTypeWrapper(classFullName);

        T instance = registryEntityFinderService.findEntityInstance(registryDataConfiguration.getRegistryType(), id);

        if (wrapper.isIdClassIdentifier() || wrapper.isEmbeddedIdentifier()) {
            entityManager.remove(instance);
            instance = resolveEntityInstance(registryDataConfiguration.getRegistryType(), entityData);
        }

        modelMapper.map(entityData, instance);

        return entityManager.merge(instance);
    }

    @Transactional
    @Override
    public <T> T delete(final String classFullName, final Object id) {
        interceptorList().forEach(registryDataInterceptor -> registryDataInterceptor.beforeRegistryDelete(classFullName, id));

        @SuppressWarnings("unchecked")
        final RegistryDataConfiguration<T, ?> registryDataConfiguration = (RegistryDataConfiguration<T, ?>) registryDataConfigurationHolder.findRegistryConfigurationForClass(classFullName);

        final T instance = registryEntityFinderService.findEntityInstance(registryDataConfiguration.getRegistryType(), id);

        entityManager.remove(instance);

        return instance;
    }

    private Map<String, JpaQueryBuilder<?>> initializeQueryBuilderMap(final RegistryDataConfigurationHolder registryDataConfigurationHolder) {
        return registryDataConfigurationHolder.getRegistryDataConfigurationList().stream()
                .collect(Collectors.toMap(registryDataConfiguration -> registryDataConfiguration.getRegistryType().getName(), registryDataConfiguration -> new JpaQueryBuilder<>(entityManager, registryDataConfiguration.getRegistryType())));
    }

    private List<RegistryDataInterceptor> interceptorList() {
        return Optional.ofNullable(registryDataInterceptorList).orElse(Collections.emptyList());
    }

    private <T, P> Page<P> registryListInternal(final ListRegistryRequest request) {
        @SuppressWarnings("unchecked")
        final RegistryDataConfiguration<T, P> registryDataConfiguration = (RegistryDataConfiguration<T, P>) registryDataConfigurationHolder.findRegistryConfigurationForClass(request.getClassFullName());

        @SuppressWarnings("unchecked")
        final JpaQueryBuilder<T> queryBuilder = (JpaQueryBuilder<T>) classNameQueryBuilderMap.get(request.getClassFullName());

        final ManagedTypeWrapper managedTypeWrapper = registryDataConfigurationHolder.resolveManagedTypeWrapper(request.getClassFullName());

        final String idAttributeName = Optional.ofNullable(managedTypeWrapper.getIdAttributeName()).orElseGet(() -> managedTypeWrapper.getIdClassPropertyNameList().get(0));

        final Pageable pageable = PageableUtil.convertToPageable(request.getPageNumber(), request.getPageSize(), new SortProperty(idAttributeName, SortDirection.ASC), request.getSortPropertyList());

        Map<String, Object> searchRequestMap = Collections.emptyMap();
        if (request.getSearchParameter() != null) {
            searchRequestMap = stringToEntityPropertyMapConverter.convert(request.getSearchParameter().getQuery(), request.getSearchParameter().getPropertyNameList(), managedTypeWrapper.getIdentifiableType());
        }

        final CriteriaQuery<P> query = queryBuilder.buildQuery(searchRequestMap, registryDataConfiguration.getSearchConfiguration(), pageable.getSort());

        final TypedQuery<P> typedQuery = entityManager.createQuery(query);

        typedQuery.setFirstResult((int) pageable.getOffset()).setMaxResults(pageable.getPageSize());

        return PageableExecutionUtils.getPage(typedQuery.getResultList(), pageable, () -> executeCountQuery(queryBuilder, query));
    }

    private long executeCountQuery(final JpaQueryBuilder<?> queryBuilder, final CriteriaQuery<?> query) {
        final CriteriaQuery<Long> countQuery = queryBuilder.convertToCountQuery(query);

        final List<Long> totals = entityManager.createQuery(countQuery).getResultList();

        return totals.stream().mapToLong(value -> value == null ? 0L : value).sum();
    }

    @SuppressWarnings("unchecked")
    private <T> T resolveEntityInstance(final Class<T> type, final Object entityData) {
        if (entityData != null && type.equals(entityData.getClass())) {
            return (T) entityData;
        }

        return BeanUtils.instantiateClass(type);
    }
}
