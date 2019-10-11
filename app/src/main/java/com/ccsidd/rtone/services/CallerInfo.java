package com.ccsidd.rtone.services;

import org.pjsip.pjsua2.CallInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Obtains display name and remote uri from a CallInfo object.
 *
 * @author gotev (Aleksandar Gotev)
 */
public class CallerInfo {
    private final Pattern displayNameAndRemoteUriPattern = Pattern.compile("^\"([^\"]+).*?sip:(.*?)>$");
    private final Pattern remoteUriPattern = Pattern.compile("^.*?sip:(.*?)>$");
    private final Pattern remoteContactPattern = Pattern.compile("<?sip(s)?:(.*)@.*>?");

    private static final String UNKNOWN = "Unknown";

    private String phone;
    private String remoteUri;

    public CallerInfo(final CallInfo callInfo) {

        String temp = callInfo.getRemoteUri();

        if (temp == null || temp.isEmpty()) {
            remoteUri = UNKNOWN;
            return;
        }

        Matcher completeInfo = displayNameAndRemoteUriPattern.matcher(temp);
        if (completeInfo.matches()) {
            remoteUri = completeInfo.group(2);

        } else {
            Matcher remoteUriInfo = remoteUriPattern.matcher(temp);
            if (remoteUriInfo.matches()) {
                remoteUri = remoteUriInfo.group(1);
            } else {
                remoteUri = UNKNOWN;
            }
        }

        Matcher matcher = remoteContactPattern.matcher(callInfo.getRemoteUri());
        while (matcher.find()) {
            phone = matcher.group(2);
        }

        if (phone == null) {
            matcher = remoteContactPattern.matcher(callInfo.getRemoteContact());
            Logger.debug("RemoteContact", callInfo.getRemoteContact());
            while (matcher.find()) {
                if (phone == null && matcher.group(2) != null)
                    phone = matcher.group(2);
                else
                    phone = UNKNOWN;
            }
        }
    }

    public String getPhone() {
        return phone;
    }

    public String getRemoteUri() {
        return remoteUri;
    }
}
