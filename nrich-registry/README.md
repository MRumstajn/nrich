# nrich-registry

## Overview

nrich-registry is a library whose purpose is to make editing of registry entities from client side easier.
It transforms JPA entities in a format that client can interpret to create dynamic forms and grids for editing of entities without additional implementation on server side.
Library provides REST API for searching, creating, updating and deleting entities. Users are only required to provide regexes for including entities and optionally provide display labels and headers for forms
and grids in `messages.properties` files. For searching, it relies on `nrich-search` and provides capability of overriding
`SearchConfiguration` for each entity (default configuration performs a join fetch on each association attribute). 
 

## Setting up Spring beans

To be able to use this library following configuration is required. If Jacksons `ObjectMapper` is not available it should also be defined and if `nrich-form-configuration` (provides client side validation of registry entities) libary is
not on classpath then `RegistryDataFormConfigurationResolverService` bean is not needed. History requires `hibernate-envers` dependency and if history is not needed then all beans with history suffix can be omitted.

```

    @Bean
    public RegistryConfiguration registryConfiguration() {
        final RegistryConfiguration registryConfiguration = new RegistryConfiguration();

        final RegistryCategoryDefinitionConfiguration RegistryCategoryDefinitionConfiguration = new RegistryCategoryDefinitionConfiguration();

        RegistryCategoryDefinitionConfiguration.setRegistryCategoryId("DEFAULT");
        RegistryCategoryDefinitionConfiguration.setIncludeEntityPatternList(Collections.singletonList("^.*\\.demoregistry\\..*$"));

        registryConfiguration.setRegistryCategoryDefinitionConfigurationList(Collections.singletonList(RegistryCategoryDefinitionConfiguration));

        return registryConfiguration;
    }

    @Bean
    public ModelMapper registryDataModelMapper() {
        final ModelMapper modelMapper = new ModelMapper();
        final Condition<Object, Object> skipIds = context -> !context.getMapping().getLastDestinationProperty().getName().equals("id");

        modelMapper.getConfiguration().setPropertyCondition(skipIds);
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        return modelMapper;
    }

    @Bean
    public ModelMapper registryBaseModelMapper() {
        final ModelMapper modelMapper = new ModelMapper();

        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        return modelMapper;
    }

    @Bean
    public StringToTypeConverter<?> defaultStringToTypeConverter() {
        return new DefaultStringToTypeConverter(Arrays.asList("dd.MM.yyyy", "yyyy-MM-dd'T'HH:mm"), Arrays.asList("#0.00", "#0,00"), "^(?i)\\s*(true|yes)\\s*$", "^(?i)\\s*(false|no)\\s*$");
    }

    @Bean
    public StringToEntityPropertyMapConverter registryStringToEntityPropertyMapConverter(final List<StringToTypeConverter<?>> stringToTypeConverterList) {
        return new DefaultStringToEntityPropertyMapConverter(stringToTypeConverterList);
    }

    @Bean
    public RegistryConfigurationResolverService registryConfigurationResolverService(final RegistryConfiguration registryConfiguration) {
        return new DefaultRegistryConfigurationResolverService(entityManager, registryConfiguration);
    }

    @Bean
    public RegistryConfigurationUpdateInterceptor registryConfigurationUpdateInterceptor(final RegistryConfigurationResolverService registryConfigurationResolverService) {
        return new RegistryConfigurationUpdateInterceptor(registryConfigurationResolverService.resolveRegistryOverrideConfigurationMap());
    }

    @Bean
    public RegistryConfigurationService registryConfigurationService(final MessageSource messageSource, final RegistryConfigurationResolverService registryConfigurationResolverService) {
        return new DefaultRegistryConfigurationService(messageSource, Collections.emptyList(), registryConfigurationResolverService.resolveRegistryGroupDefinition(), registryConfigurationResolverService.resolveRegistryHistoryConfiguration(), registryConfigurationResolverService.resolveRegistryOverrideConfigurationMap());
    }

    @Bean
    public RegistryConfigurationController registryConfigurationController(final RegistryConfigurationService registryConfigurationService) {
        return new RegistryConfigurationController(registryConfigurationService);
    }

    @Bean
    public RegistryEntityFinderService registryEntityFinderService(final ModelMapper registryBaseModelMapper, final RegistryConfigurationResolverService registryConfigurationResolverService) {
        return new EntityManagerRegistryEntityFinderService(entityManager, registryBaseModelMapper, registryConfigurationResolverService.resolveRegistryDataConfiguration().getClassNameManagedTypeWrapperMap());
    }

    @Bean
    public RegistryDataRequestConversionService registryDataRequestConversionService(final ObjectMapper objectMapper, final RegistryConfigurationResolverService registryConfigurationResolverService) {
        return new DefaultRegistryDataRequestConversionService(objectMapper, registryConfigurationResolverService.resolveRegistryDataConfiguration());
    }

    @Bean
    public RegistryDataService registryDataService(final ModelMapper registryDataModelMapper, final StringToEntityPropertyMapConverter registryStringToEntityPropertyMapConverter, final RegistryConfigurationResolverService registryConfigurationResolverService, @Autowired(required = false) final List<RegistryDataInterceptor> interceptorList, final RegistryEntityFinderService registryEntityFinderService) {
        return new DefaultRegistryDataService(entityManager, registryDataModelMapper, registryStringToEntityPropertyMapConverter, registryConfigurationResolverService.resolveRegistryDataConfiguration(), Optional.ofNullable(interceptorList).orElse(Collections.emptyList()), registryEntityFinderService);
    }

    @Bean
    public RegistryDataController registryDataController(final RegistryDataService registryDataService, final RegistryDataRequestConversionService registryDataRequestConversionService, final Validator validator) {
        return new RegistryDataController(registryDataService, registryDataRequestConversionService, validator);
    }

    @Bean
    public RegistryHistoryService registryHistoryService(final RegistryConfigurationResolverService registryConfigurationResolverService, final ModelMapper registryBaseModelMapper, final RegistryEntityFinderService registryEntityFinderService) {
        return new DefaultRegistryHistoryService(entityManager, registryConfigurationResolverService.resolveRegistryDataConfiguration(), registryConfigurationResolverService.resolveRegistryHistoryConfiguration(), registryBaseModelMapper, registryEntityFinderService);
    }

    @Bean
    public RegistryHistoryController registryHistoryController(final RegistryHistoryService registryHistoryService) {
        return new RegistryHistoryController(registryHistoryService);
    }

    @Bean
    public RegistryDataFormConfigurationResolverService registryFormConfigurationRegistrationService(final RegistryConfigurationResolverService registryConfigurationResolverService, @Qualifier(FORM_CONFIGURATION_MAPPING_BEAN_NAME) final Map<String, Class<?>> formConfigurationMapping) {
        final List<Class<?>> registryClassList = registryConfigurationResolverService.resolveRegistryDataConfiguration().getRegistryDataConfigurationList().stream()
                .map(RegistryDataConfiguration::getRegistryType)
                .collect(Collectors.toList());

        return new DefaultRegistryDataFormConfigurationResolverService(registryClassList, formConfigurationMapping);
    }


```

`RegistryConfiguration` defines for the what entities will the client side configuration be generated for and searching, creating and updating enabled.

`ModelMapper registryDataModelMapper` is used to map request data to entity instances.

`ModelMapper registryBaseModelMapper` is used for other mappings in library.

`StringToTypeConverter<?>` is an interface from `nrich-search` library that performs conversion from string to typed instances and is used when querying registry entities. Default implementation (`DefaultStringToTypeConverter`)
accepts a list of data formats and regexes that are used to convert string to types found in properties of entity classes.

`StringToEntityPropertyMapConverter` is also an interface from `nrich-search` library that is used for querying registry entities, it is responsible for assembling conditions Map from query string and a list of properties to search (
conversion to typed instances is delegated to `StringToTypeConverter<?>`). When querying registry entities client API accepts a query (string), and a list of properties to be searched. 

`RegistryConfigurationResolverService` is a service that parses `RegistryConfiguration` and returns data in format required by other registry services.

`RegistryConfigurationUpdateInterceptor` is an interface that is invoked before registry entity is created, updated or deleted. `RegistryConfigurationUpdateInterceptor` is a implementation
that checks if these operations are permitted according to defined `RegistryConfiguration`. Users can define their own interceptors since `RegistryDataService` accepts a list of interceptors.

`RegistryConfigurationService` transforms `RegistryConfiguration` in a format that clients can use for creating dynamic forms and grids.

`RegistryConfigurationController` is a REST endpoint with single url `nrich/registry/configuration/fetch` that returns transformed `RegistryConfiguration` (a list of `RegistryCategoryConfiguration` instances)
to client.

`RegistryEntityFinderService` is used by `RegistryDataService` and `RegistryHistoryService` for resolving primary key of entity instances and/or finding them.

`RegistryDataService` is used for searching, creating, updating and deleting entity instances.

`RegistryDataRequestConversionService` is used to convert raw json data received from client to typed instances. It binds either to registry entity or (when defined) it can bind 
to other class instances (for example when wanting to update only part of properties), it searches for classes in same package as registry entity with same name and following 
suffixes `CreateRequest`, `UpdateRequest`  and `Request`.  

`RegistryDataController` is REST API for `RegistryDataService` it has five POST methods:

- `nrich/registry/data/list-bulk` - loads multiple registry entities

- `nrich/registry/data/list`  - searches single registry entity

- `nrich/registry/data/create`  - creates registry entity

- `nrich/registry/data/update`  - update registry entity

- `nrich/registry/data/delete`  - deletes registry entity


`RegistryHistoryService` is service that is reponsible for listing changes on an entity, default implementation uses `hibernate-envers` to find changes. 

`RegistryHistoryController` is a REST endpoint with single url `nrich/registry/history/fetch` that returns history of changes on a registry entity  (a list of `EntityWithRevision`) to client.

`RegistryDataFormConfigurationResolverService` is used to register registry entities with `nrich-form-configuration` library so clients can fetch validations for specific entities.


## Usage

Central configuration clients should define is:

```

    @Bean
    public RegistryConfiguration registryConfiguration() {
        final RegistryConfiguration registryConfiguration = new RegistryConfiguration();

        final RegistryCategoryDefinitionConfiguration RegistryCategoryDefinitionConfiguration = new RegistryCategoryDefinitionConfiguration();

        RegistryCategoryDefinitionConfiguration.setRegistryCategoryId("DEFAULT");
        RegistryCategoryDefinitionConfiguration.setIncludeEntityPatternList(Collections.singletonList("^.*\\.demoregistry\\..*$"));

        registryConfiguration.setRegistryCategoryDefinitionConfigurationList(Collections.singletonList(RegistryCategoryDefinitionConfiguration));

        return registryConfiguration;
    }

``` 

this defines configuration that will scan JPA entities in package containing `demoregistry` string. It will make available all entities in that package for resolving configuration, 
searching, creating, updating and deleting. For each registry entity override configuration `RegistryOverrideConfiguration` can be defined deciding if 
a property is editable, sortable, property display order etc. Similarly custom `SearchConfiguration` can be defined for each entity overriding default 
`SearchConfiguration` for that entity.

After that if localization is required for registry name, form labels and column headers etc. (by default class names and property names are used) they should be defined in appropriate
`messages.properties` files. 

`DefaultRegistryConfigurationService` resolves messages from following key:

`registryCategoryId.registryCategoryIdDisplay` - text displayed for registry category (a list of registry entities)

`net.croz.nrich.registry.configuration.stub.RegistryConfigurationTestEntity.registryNameDisplay` - text displayed as `RegistryConfigurationTestEntity` name

`net.croz.nrich.registry.configuration.stub.RegistryConfigurationTestEntity.name.label` - form label for name property of `RegistryConfigurationTestEntity` entity 

`net.croz.nrich.registry.configuration.stub.RegistryConfigurationTestEntity.name.header` - column header for name property of `RegistryConfigurationTestEntity` entity


Client then invokes REST API POST method `nrich/registry/configuration/fetch` and receives configuration in following form (client is then responsible for building forms and grids):
 
 
```json

[
  {
    "registryCategoryId": "DEFAULT",
    "registryCategoryIdDisplay": "Registry default category",
    "registryEntityConfigurationList": [
      {
        "registryId": "net.croz.demoregistry.model.Author",
        "registryName": "Author",
        "registryDisplayName": "Author",
        "category": "DEFAULT",
        "readOnly": false,
        "creatable": true,
        "updateable": true,
        "deletable": true,
        "idClassPropertyNameList": [],
        "registryPropertyConfigurationList": [
          {
            "name": "id",
            "javascriptType": "NUMBER",
            "originalType": "java.lang.Long",
            "formLabel": "Id",
            "columnHeader": "Id",
            "editable": false,
            "sortable": true,
            "searchable": true,
            "decimal": false,
            "singularAssociation": false,
            "id": true
          },
          {
            "name": "firstName",
            "javascriptType": "STRING",
            "originalType": "java.lang.String",
            "formLabel": "First name",
            "columnHeader": "First name",
            "editable": true,
            "sortable": true,
            "searchable": true,
            "decimal": false,
            "singularAssociation": false,
            "id": false
          },
          {
            "name": "lastName",
            "javascriptType": "STRING",
            "originalType": "java.lang.String",
            "formLabel": "Last name",
            "columnHeader": "Last name",
            "editable": true,
            "sortable": true,
            "searchable": true,
            "decimal": false,
            "singularAssociation": false,
            "id": false
          },
          {
            "name": "similarAuthor",
            "javascriptType": "OBJECT",
            "originalType": "net.croz.demoregistry.model.Author",
            "singularAssociationReferencedClass": "net.croz.demoregistry.model.Author",
            "formLabel": "Similar author",
            "columnHeader": "Similar author",
            "editable": true,
            "sortable": true,
            "searchable": true,
            "decimal": false,
            "singularAssociation": true,
            "id": false
          }
        ],
        "registryEmbeddedIdPropertyConfigurationList": [],
        "registryHistoryPropertyConfigurationList": [
          {
            "name": "revisionNumber",
            "javascriptType": "NUMBER",
            "originalType": "java.lang.Integer",
            "formLabel": "revisionNumber",
            "columnHeader": "revisionNumber",
            "editable": false,
            "sortable": true,
            "searchable": false,
            "decimal": false,
            "singularAssociation": false,
            "id": false
          },
          {
            "name": "revisionTimestamp",
            "javascriptType": "DATE",
            "originalType": "java.util.Date",
            "formLabel": "revisionTimestamp",
            "columnHeader": "revisionTimestamp",
            "editable": false,
            "sortable": true,
            "searchable": false,
            "decimal": false,
            "singularAssociation": false,
            "id": false
          },
          {
            "name": "revisionType",
            "javascriptType": "STRING",
            "originalType": "java.lang.String",
            "formLabel": "revisionType",
            "columnHeader": "revisionType",
            "editable": false,
            "sortable": true,
            "searchable": false,
            "decimal": false,
            "singularAssociation": false,
            "id": false
          }
        ],
        "historyAvailable": false,
        "idClassIdentity": false,
        "embeddedIdentity": false,
        "identifierAssigned": false
      },
      {
        "registryId": "net.croz.demoregistry.model.Book",
        "registryName": "Book",
        "registryDisplayName": "Book",
        "category": "DEFAULT",
        "readOnly": false,
        "creatable": true,
        "updateable": true,
        "deletable": true,
        "idClassPropertyNameList": [],
        "registryPropertyConfigurationList": [
          {
            "name": "id",
            "javascriptType": "NUMBER",
            "originalType": "java.lang.Long",
            "formLabel": "Id",
            "columnHeader": "Id",
            "editable": false,
            "sortable": true,
            "searchable": true,
            "decimal": false,
            "singularAssociation": false,
            "id": true
          },
          {
            "name": "isbn",
            "javascriptType": "STRING",
            "originalType": "java.lang.String",
            "formLabel": "Isbn",
            "columnHeader": "Isbn",
            "editable": true,
            "sortable": true,
            "searchable": true,
            "decimal": false,
            "singularAssociation": false,
            "id": false
          },
          {
            "name": "title",
            "javascriptType": "STRING",
            "originalType": "java.lang.String",
            "formLabel": "Title",
            "columnHeader": "Title",
            "editable": true,
            "sortable": true,
            "searchable": true,
            "decimal": false,
            "singularAssociation": false,
            "id": false
          }
        ],
        "registryEmbeddedIdPropertyConfigurationList": [],
        "registryHistoryPropertyConfigurationList": [
          {
            "name": "revisionNumber",
            "javascriptType": "NUMBER",
            "originalType": "java.lang.Integer",
            "formLabel": "revisionNumber",
            "columnHeader": "revisionNumber",
            "editable": false,
            "sortable": true,
            "searchable": false,
            "decimal": false,
            "singularAssociation": false,
            "id": false
          },
          {
            "name": "revisionTimestamp",
            "javascriptType": "DATE",
            "originalType": "java.util.Date",
            "formLabel": "revisionTimestamp",
            "columnHeader": "revisionTimestamp",
            "editable": false,
            "sortable": true,
            "searchable": false,
            "decimal": false,
            "singularAssociation": false,
            "id": false
          },
          {
            "name": "revisionType",
            "javascriptType": "STRING",
            "originalType": "java.lang.String",
            "formLabel": "revisionType",
            "columnHeader": "revisionType",
            "editable": false,
            "sortable": true,
            "searchable": false,
            "decimal": false,
            "singularAssociation": false,
            "id": false
          }
        ],
        "historyAvailable": false,
        "idClassIdentity": false,
        "embeddedIdentity": false,
        "identifierAssigned": false
      }
    ]
  }
]

```

After creating form and grids for each of these entities client can then search them by using method POST for url:

`nrich/registry/data/list`

requests for searching Author:

```json

{
  "registryId": "net.croz.demoregistry.model.Author",
  "searchParameter": {
    "propertyNameList": [
      "firstName",
      "lastName"
    ],
    "query": "last 1"
  },
  "pageNumber": 0,
  "pageSize": 25,
  "sortPropertyList": [
    {
      "property": "id",
      "direction": "ASC"
    }
  ]
}


```

`registryId` is class name to search, `propertyNameList` is a list of properties to search on that class. `searchParameter` and  `sortPropertyList` are not required but paging parameters are.

Response sent from server returns Springs pageable with list of entity instances: 

```json

{
  "content": [
    {
      "id": 32,
      "firstName": "first 1",
      "lastName": "last 1",
      "class": "net.croz.nrichdemowebboot.demoregistry.model.Author"
    }
  ],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "pageNumber": 0,
    "pageSize": 25,
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 1,
  "totalElements": 1,
  "last": true,
  "first": true,
  "numberOfElements": 1,
  "sort": {
    "sorted": true,
    "unsorted": false,
    "empty": false
  },
  "size": 25,
  "number": 0,
  "empty": false
}

```

For creating entity instance following POST url is used:

`nrich/registry/data/create`

Request for creating Author is: 

```json

{
  "classFullName": "net.croz.nrichdemowebboot.demoregistry.model.Author",
  "jsonEntityData": "{\"firstName\":\"first name\",\"lastName\":\"last name\"}"
}

```

Response is saved entity instance.

For update entity instance following POST url is used:

`nrich/registry/data/update`

Request for updating Author is: 

```json

{
  "id": 1,
  "classFullName": "net.croz.nrichdemowebboot.demoregistry.model.Author",
  "jsonEntityData": "{\"firstName\":\"updated name\",\"lastName\":\"updated last name\"}"
}

```

For deleting entity instance following POST url is used:

`nrich/registry/data/delete`

Request for deleting Author is: 

```json

{
  "id": 1,
  "classFullName": "net.croz.nrichdemowebboot.demoregistry.model.Author"
}

```
