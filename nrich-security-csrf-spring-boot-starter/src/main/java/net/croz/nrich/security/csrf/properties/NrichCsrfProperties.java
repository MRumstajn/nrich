package net.croz.nrich.security.csrf.properties;

import lombok.Getter;
import net.croz.nrich.security.csrf.core.constants.CsrfConstants;
import net.croz.nrich.security.csrf.core.model.CsrfExcludeConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.List;

@Getter
@ConstructorBinding
@ConfigurationProperties("nrich.security.csrf")
public class NrichCsrfProperties {

    /**
     * If CSRF is active
     */
    private final boolean active;

    /**
     * Duration of CSRF token
     */
    private final Duration tokenExpirationInterval;

    /**
     * Duration of how long token can be in the future (can happen when server and client time is not in sync)
     */
    private final Duration tokenFutureThreshold;

    /**
     * Name of CSRF token
     */
    private final String tokenKeyName;

    /**
     * Length of crypto key (128, 256...)
     */
    private final Integer cryptoKeyLength;

    /**
     * Initial application url (i.e. url that user is redirected after login). Token will be added to response from this url as <pre>csrfInitialToken</pre> parameter.
     */
    private final String initialTokenUrl;

    /**
     * Url used for CSRF ping request.
     */
    private final String csrfPingUri;

    /**
     * A list of {@link CsrfExcludeConfig} instances that contain urls or regexps excluded from CSRF check
     */
    private final List<CsrfExcludeConfig> csrfExcludeConfigList;

    public NrichCsrfProperties(@DefaultValue("true") boolean active, @DefaultValue("35m") Duration tokenExpirationInterval, @DefaultValue("1m") Duration tokenFutureThreshold, @DefaultValue("X-CSRF-Token") String tokenKeyName, @DefaultValue("128") Integer cryptoKeyLength, String initialTokenUrl, @DefaultValue(CsrfConstants.CSRF_DEFAULT_PING_URI) String csrfPingUri, List<CsrfExcludeConfig> csrfExcludeConfigList) {
        this.active = active;
        this.tokenExpirationInterval = tokenExpirationInterval;
        this.tokenFutureThreshold = tokenFutureThreshold;
        this.tokenKeyName = tokenKeyName;
        this.cryptoKeyLength = cryptoKeyLength;
        this.initialTokenUrl = initialTokenUrl;
        this.csrfPingUri = csrfPingUri;
        this.csrfExcludeConfigList = csrfExcludeConfigList;
    }
}
