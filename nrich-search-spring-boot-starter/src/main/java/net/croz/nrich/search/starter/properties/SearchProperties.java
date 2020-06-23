package net.croz.nrich.search.starter.properties;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

@Getter
@ConstructorBinding
@ConfigurationProperties("nrich.search")
public class SearchProperties {

    private final List<String> dateFormatList;

    private final List<String> decimalNumberFormatList;

    private final String booleanTrueRegexPattern;

    private final String booleanFalseRegexPattern;

    private final boolean defaultConverterEnabled;

    public SearchProperties(@DefaultValue({ "dd.MM.yyyy", "yyyy-MM-dd'T'HH:mm" }) final List<String> dateFormatList, @DefaultValue({ "#0.00", "#0,00" }) final List<String> decimalNumberFormatList, @DefaultValue("^(?i)\\s*(true|yes|da)\\s*$") final String booleanTrueRegexPattern, @DefaultValue("^(?i)\\s*(false|no|ne)\\s*$") final String booleanFalseRegexPattern, @DefaultValue("true") final boolean defaultConverterEnabled) {
        this.dateFormatList = dateFormatList;
        this.decimalNumberFormatList = decimalNumberFormatList;
        this.booleanTrueRegexPattern = booleanTrueRegexPattern;
        this.booleanFalseRegexPattern = booleanFalseRegexPattern;
        this.defaultConverterEnabled = defaultConverterEnabled;
    }
}
