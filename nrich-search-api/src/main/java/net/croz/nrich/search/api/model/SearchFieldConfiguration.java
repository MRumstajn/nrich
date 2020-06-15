package net.croz.nrich.search.api.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.List;

@Setter
@Getter
@Builder
public class SearchFieldConfiguration {

    private List<String> searchIgnoredFieldList;

    private List<Class<?>> rangeQuerySupportedClassList;

    private String defaultSubqueryJoinAttribute;

    private String rangeQueryFromIncludingSuffix;

    private String rangeQueryFromSuffix;

    private String rangeQueryToIncludingSuffix;

    private String rangeQueryToSuffix;

    private String collectionQuerySuffix;

    public static SearchFieldConfiguration defaultSearchFieldConfiguration() {
        return new SearchFieldConfigurationBuilder()
                .rangeQuerySupportedClassList(Arrays.asList(Date.class, java.util.Date.class, Temporal.class, Number.class))
                .defaultSubqueryJoinAttribute("id")
                .rangeQueryFromIncludingSuffix("FromIncluding")
                .rangeQueryFromSuffix("From")
                .rangeQueryToIncludingSuffix("ToIncluding")
                .rangeQueryToSuffix("To")
                .collectionQuerySuffix("SearchList")
                .build();
    }
}