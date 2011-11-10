package org.lantern;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.lastbamboo.common.stun.client.PublicIpAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.maxmind.geoip.Country;

public class CensoredUtils {

    private static final Logger LOG = 
        LoggerFactory.getLogger(CensoredUtils.class);

    /**
     * Censored country codes, in order of population.
     */
    /*
    private static final Collection<String> CENSORED =
        Sets.newHashSet(
            // Asia 
            "CN",
            "VN",
            "MM",
            //Mideast: 
            "IR", 
            "BH", // Bahrain
            "YE", // Yemen
            "SA", // Saudi Arabia
            "SY",
            //Eurasia: 
            "UZ", // Uzbekistan
            "TM", // Turkmenistan
            //Africa: 
            "ET", // Ethiopia
            "ER", // Eritrea
            // LAC: 
            "CU");
            */

    // These country codes have US export restrictions, and therefore cannot
    // access App Engine sites.
    private static final Collection<String> EXPORT_RESTRICTED =
        Sets.newHashSet(
            "SY");

    private static String countryCode;
    
    public static String countryCode() {
        if (StringUtils.isNotBlank(countryCode)) {
            return countryCode;
        }
        
        countryCode = countryCode(new PublicIpAddress().getPublicIpAddress());
        return countryCode;
    }
    
    public static String countryCode(final InetAddress address) {
        final Country country = LanternHub.getGeoIpLookup().getCountry(address);
        LOG.info("Country is: {}", country.getName());
        return country.getCode().trim();
    }
    
    public static boolean isCensored() {
        return isCensored(new PublicIpAddress().getPublicIpAddress());
    }
    
    public static boolean isCensored(final InetAddress address) {
        return isMatch(address, StatsTracker.CENSORED);
    }

    public static boolean isCensored(final String address) throws IOException {
        return isCensored(InetAddress.getByName(address));
    }
    
    public static boolean isExportRestricted() {
        return isExportRestricted(new PublicIpAddress().getPublicIpAddress());
    }
    
    public static boolean isExportRestricted(final InetAddress address) { 
        return isMatch(address, EXPORT_RESTRICTED);
    }

    public static boolean isExportRestricted(final String address) 
        throws IOException {
        return isExportRestricted(InetAddress.getByName(address));
    }
    
    public static boolean isMatch(final InetAddress address, 
        final Collection<String> countries) { 
        final Country country = LanternHub.getGeoIpLookup().getCountry(address);
        LOG.info("Country is: {}", country.getName());
        countryCode = country.getCode().trim();
        return countries.contains(countryCode);
    }
    
    public static boolean isCensored(final Country country) { 
        countryCode = country.getCode().trim();
        return StatsTracker.CENSORED.contains(countryCode);
    }
    

    public static boolean isForceCensored() {
        final boolean force = 
            LanternUtils.getBooleanProperty(LanternConstants.FORCE_CENSORED);
        LOG.info("Forcing proxy: "+force);
        return force;
    }

    public static void forceCensored() {
        LanternUtils.setBooleanProperty(LanternConstants.FORCE_CENSORED, true);
    }

    public static void unforceCensored() {
        LanternUtils.setBooleanProperty(LanternConstants.FORCE_CENSORED, false);
    }
}
