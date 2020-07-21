# nrich-validation

## Overview

nrich-validation is a library intended to add additional `jakarta.validation-api` constraints that have proved to be useful on projects.
It contains a list of constraints and corresponding validators. Registration of validators is tied to `hibernate-validator` implementation.
It also contains a list of messages for standard and additional constraints in english and croatian (`validationMessages`).


## Setting up Spring beans
If automatic registration of messages is required then following configuration is required 
(this can also be registered through application.properties by manually including `validationMessages` when using Spring Boot):


```

    @Bean
    public ValidationMessageSourceRegistrar validationMessageSourceRegistrar(final MessageSource messageSource) {
        return new ValidationMessageSourceRegistrar(messageSource);
    }

    @RequiredArgsConstructor
    public static class ValidationMessageSourceRegistrar {

        private final MessageSource messageSource;

        @PostConstruct
        void registerValidationMessages() {
            if (messageSource instanceof AbstractResourceBasedMessageSource) {
                ((AbstractResourceBasedMessageSource) messageSource).addBasenames("validationMessages");
            }
        }
    }

```



## Usage

The library provides following constraints:

#### InList

Validates that a property is in a list of String values i.e:

```

    @InList(value = { "first", "second" })
    private String value;


```

will validate that values is either equal to `first` or equal to `second`.

#### MaxSizeInBytes

Validates that values doesn't exceed specified number of bytes in specified encoding (default UTF-8) i.e:

```

    @MaxSizeInBytes(value = 5)
    private final String value;

```

will validate that value contains less than or equal to 5 bytes in `UTF-8` encoding.

#### NotNullWhen

Validates that a property is not null when a condition is satisfied i.e:

```


@Setter
@Getter
@NotNullWhen(property = "firstName", condition = CreatePersonRequest.Condition.class)
public class CreatePersonRequest {

    private String firstName;

    private String lastName;

    public static class Condition implements Predicate<CreatePersonRequest> {
        @Override
        public boolean test(final CreatePersonRequest request) {
            return request.getLastName() != null;
        }
    }
}


```

will validate that firstName is not null when lastName is not null (effectively requiring both properties to be null or both properties to be empty).

#### NullWhen

Validates that a property is null when a condition is satisfied i.e:

```


@Setter
@Getter
@NullWHen(property = "companyName", condition = CreateBusinessEntityRequest.Condition.class)
public class CreateBusinessEntityRequest {

    private String companyName;

    private String fistName;

    private String lastName;

    public static class Condition implements Predicate<CreateBusinessEntityRequest> {
        @Override
        public boolean test(final CreateBusinessEntityRequest request) {
            return request.getFirstName() != null || request.getLastName() != null;
        }
    }
}


```

will validate that either companyName is filled or firstName or lastName are filled. 

#### ValidFile

Validates that file (either Springs `MultipartFile` or `FilePart`) is in provided content type list, has extension that is in provided extension list
and/or satisfies regex. All constraints properties are optional so defining a constraint without any will always pass validation.


```

    @ValidFile(allowedContentTypeList = "text/plain", allowedExtensionList = "txt", allowedFileNameRegex = "(?U)[\\w-.]+")
    private MultipartFile file;


```

will validate that file has content type of `text/plain`, extension `txt` and matches regex: `(?U)[\\w-.]+`

#### ValidFileResolvable

Does the same thing as `ValidFile` constraint but resolves allowedContentTypeList, allowedExtensionList and allowedFileNameRegex from Springs `Environment` bean.
Allowing those properties to be defined in property files.

```

 @ValidFileResolvable(
            allowedContentTypeListPropertyName = "my.custom.validation.file.allowed-content-type-list",
            allowedExtensionListPropertyName = "my.custom.validation.file.allowed-extension-list",
            allowedFileNameRegexPropertyName = "my.custom.validation.file.allowed-file-name-regex"
    )
    private final MultipartFile file;

```

will resolve  allowedContentTypeList from `my.custom.validation.file.allowed-content-type-list`, allowedExtensionList from `my.custom.validation.file.allowed-extension-list`
and allowedFileNameRegex from `my.custom.validation.file.allowed-file-name-regex` properties and perform validation while skipping resolved empty or null property values.

### ValidOib

Validates that a property is a valida OIB (personal identification number in Croatia).

```

    @ValidOib
    String value; 


```

#### ValidRange

Validates that from property (resolved from validated class through `fromPropertyName`) is less than (or equal to if `inclusive` flag has been set) than
to property (resolved from validated class through `toPropertyName`) i.e:

```

@Setter
@Getter
@ValidRange(fromPropertyName = "dateFrom", toPropertyName = "dateTo")
public class ValidRangeValidatorDifferentTypeTestRequest {

    private Instant dateFrom;

    private Instant dateTo;

}


```   

validates that dateFrom is less than dateTo.

#### ValidSearchProperties

Is useful when it is required that all properties from any one group of properties are not null

```

@Setter
@Getter
@ValidSearchProperties(propertyGroup = { @ValidSearchProperties.PropertyGroup({ "firstName", "lastName" }), @ValidSearchProperties.PropertyGroup("id") })
public class SearchPersonRequest {

    private String firstName;

    private String lastName;

    private Long id;

}


```

Above request will require that either both firstName and lastName are not null or that id is not null.