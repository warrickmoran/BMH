/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.edex.bmh.dac;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;

import com.raytheon.uf.common.bmh.dac.DacConfigEvent;
import com.raytheon.uf.common.bmh.datamodel.dac.Dac;
import com.raytheon.uf.common.bmh.datamodel.dac.DacChannel;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * Manages configuration of the DACs directly.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 5, 2015  5113       bkowal      Initial creation
 * Nov 12, 2015 5113       bkowal      Updated to support DAC reboots. Improved retry
 *                                     logic for verifying that a DAC has restarted.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public final class OrionPost {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(OrionPost.class);

    private static final int DEFAULT_TIMEOUT_SECONDS = 10;

    /*
     * The amount of time (in seconds) to wait before attempts to verify that a
     * DAC has come back online after being rebooted.
     */
    private static final int REBOOT_SLEEP_TIME = 30000;

    /*
     * The maximum number of times to attempt to connect to a DAC to verify that
     * it is accessible after a reboot has been requested.
     */
    private static final int REBOOT_RETRY_COUNT = 25;

    /*
     * The amount of time (in milliseconds) to wait before verifying that a DAC
     * has successfully rebooted after a DAC reboot has been requested.
     */
    private static final long REBOOT_ALLOWANCE = 5000;

    private final Dac dac;

    private String configAddress;

    private final List<DacConfigEvent> events = new LinkedList<>();

    private HttpClient httpClient;

    public OrionPost(final Dac dac, final String configAddress)
            throws DacConfigurationException {
        this.dac = dac;
        if (configAddress == null) {
            this.configAddress = dac.getAddress();
        } else {
            this.configAddress = configAddress;
        }
        this.buildHttpClient();
    }

    private void buildHttpClient() throws DacConfigurationException {
        SSLContextBuilder builder = new SSLContextBuilder();
        try {
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        } catch (Exception e) {
            throw new DacConfigurationException(
                    "Failed to initialize the SSLContextBuilder!", e);
        }
        SSLConnectionSocketFactory sslsf = null;
        try {
            sslsf = new SSLConnectionSocketFactory(builder.build(),
                    SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (Exception e) {
            throw new DacConfigurationException(
                    "Failed to initialize the SSLConnectionSocketFactory!", e);
        }
        httpClient = HttpClientBuilder.create().setSSLSocketFactory(sslsf)
                .build();
    }

    public boolean configureDAC(final boolean reboot)
            throws DacConfigurationException {
        boolean success = true;
        /*
         * First, verify that we can connect to the DAC.
         */
        this.events.add(new DacConfigEvent(DacConfigEvent.MSG_VERIFY));
        boolean available = false;
        try {
            available = this.verifyDacAvailability(DEFAULT_TIMEOUT_SECONDS);
        } catch (Exception e) {
            statusHandler.error("Failed to verify the availability of DAC: "
                    + this.configAddress + "!", e);
        }
        if (available == false) {
            /*
             * Not able to connect to the DAC.
             */
            this.events.add(new DacConfigEvent(
                    DacConfigEvent.MSG_VERIFY_FAILURE,
                    DacConfigEvent.DEFAULT_ACTION));
            this.events.add(new DacConfigEvent(DacConfigEvent.MSG_FAIL));
            return false;
        }
        this.events.add(new DacConfigEvent(DacConfigEvent.MSG_VERIFY_SUCCESS));
        try {
            List<NameValuePair> params = null;
            if (this.dac == null) {
                params = this.getAllOrionConfiguration();
            } else {
                params = this.getUnmanagedOrionConfiguration();
                this.addBmhManagedConfiguration(params);
                this.events
                        .add(new DacConfigEvent(DacConfigEvent.MSG_CONFIGURE));
            }
            this.saveConfiguration(reboot, params);
        } catch (Exception e) {
            if (this.dac == null) {
                this.events.add(new DacConfigEvent(
                        DacConfigEvent.MSG_REBOOT_TRIGGER_FAILURE,
                        DacConfigEvent.DEFAULT_ACTION));
            } else {
                this.events.add(new DacConfigEvent(
                        DacConfigEvent.MSG_CONFIGURE_FAILURE,
                        DacConfigEvent.DEFAULT_ACTION));
            }
            this.events.add(new DacConfigEvent(DacConfigEvent.MSG_FAIL));
            throw e;
        }
        /*
         * If the DAC has been rebooted, we will attempt to wait until it
         * becomes accessible again.
         */
        if (this.dac != null) {
            this.events.add(new DacConfigEvent(
                    DacConfigEvent.MSG_CONFIGURE_SUCCESS));
        }
        if (reboot) {
            /*
             * Wait a while to ensure that we do not attempt to connect to the
             * DAC before it even fully processes the configuration change and
             * starts the reboot process.
             */
            try {
                Thread.sleep(REBOOT_ALLOWANCE);
            } catch (InterruptedException e) {
                statusHandler
                        .warn("Interrupted while waiting for the DAC to process configuration changes.");
            }
            /*
             * When the DAC reboots, we want to verify that it is accessible at
             * the address associated with the {@link Dac}.
             */
            if (this.dac != null) {
                /*
                 * DAC configuration + reboot.
                 */
                this.configAddress = this.dac.getAddress();
            }
            this.events.add(new DacConfigEvent(DacConfigEvent.MSG_REBOOT));
            available = false;
            int count = 0;
            while (count < REBOOT_RETRY_COUNT && available == false) {
                ++count;
                try {
                    available = this
                            .verifyDacAvailability(DEFAULT_TIMEOUT_SECONDS);
                } catch (DacHttpCloseException e) {
                    statusHandler
                            .error("Failed to close an HTTP connection to the DAC. Terminating DAC configuration session ...",
                                    e);
                    break;
                } catch (Exception e) {
                    if (count >= REBOOT_RETRY_COUNT) {
                        statusHandler.error("Failed to verify that DAC: "
                                + this.configAddress
                                + " has rebooted successfully!", e);
                    }
                }
                if (available == false) {
                    this.events.add(new DacConfigEvent(
                            DacConfigEvent.MSG_REBOOT_WAIT));
                    try {
                        Thread.sleep(REBOOT_SLEEP_TIME);
                    } catch (InterruptedException e) {
                        statusHandler
                                .warn("Interrupted while waiting to verify that the DAC has rebooted.");
                    }
                }
            }
            success = available;
            if (available) {
                this.events.add(new DacConfigEvent(
                        DacConfigEvent.MSG_REBOOT_SUCCESS));
            } else {
                this.events.add(new DacConfigEvent(
                        DacConfigEvent.MSG_REBOOT_FAILURE,
                        DacConfigEvent.DEFAULT_ACTION));
            }
        }
        if (this.dac != null && success) {
            this.events.add(new DacConfigEvent(DacConfigEvent.MSG_SUCCESS));
        }
        return success;
    }

    public boolean verifyDacAvailability(final int secondsTimeout)
            throws DacConfigurationException, DacHttpCloseException {
        final int millisecondsTimeout = secondsTimeout
                * (int) TimeUtil.MILLIS_PER_SECOND;
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        requestConfigBuilder.setConnectTimeout(millisecondsTimeout);
        requestConfigBuilder.setSocketTimeout(millisecondsTimeout);

        HttpGet httpGet = new HttpGet(this.buildPostURL(null));
        httpGet.setConfig(requestConfigBuilder.build());

        HttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
        } catch (Exception e) {
            throw new DacConfigurationException("Failed to execute Http GET!",
                    e);
        }
        final int statusCode = response.getStatusLine().getStatusCode();
        if (response.getEntity() != null) {
            try {
                EntityUtils.consume(response.getEntity());
            } catch (IOException e) {
                statusHandler
                        .error("Failed to fully consume the HTTP Response returned by verifying DAC availability.",
                                e);
            }
        }

        return (statusCode == HttpStatus.SC_OK);
    }

    /**
     * Returns a {@link List} of {@link NameValuePair}s associated with the
     * orion configuration parameters that are not managed by BMH; primarily the
     * IPv6 information.
     * 
     * @return the retrieved {@link List} of {@link NameValuePair}s
     * @throws DacConfigurationException
     */
    private List<NameValuePair> getUnmanagedOrionConfiguration()
            throws DacConfigurationException {
        return this
                .getOrionConfiguration(OrionPatterns.ORION_UNMANAGED_PARAM_PATTERNS);
    }

    private List<NameValuePair> getAllOrionConfiguration()
            throws DacConfigurationException {
        return this
                .getOrionConfiguration(OrionPatterns.ALL_ORION_PARAM_PATTERNS);
    }

    /**
     * Returns a {@link List} of {@link NameValuePair}s associated with the
     * specified Orion configuration parameter {@link Pattern}s.
     * 
     * @param orionConfigPatterns
     *            the specified Orion configuration parameter {@link Pattern}s.
     * 
     * @return the retrieved {@link List} of {@link NameValuePair}s
     * @throws DacConfigurationException
     */
    private List<NameValuePair> getOrionConfiguration(
            final Pattern[] orionConfigPatterns)
            throws DacConfigurationException {
        final HttpPost post = new HttpPost(
                this.buildPostURL(OrionPostParams.INDEX));
        try {
            post.setEntity(new UrlEncodedFormEntity(OrionPostParams.getLogin()));
        } catch (UnsupportedEncodingException e) {
            throw new DacConfigurationException(
                    "Failed to set Http POST parameters!", e);
        }

        HttpResponse response = null;
        try {
            response = httpClient.execute(post);
        } catch (Exception e) {
            throw new DacConfigurationException(
                    "Failed to execute Http POST to " + OrionPostParams.INDEX
                            + "!", e);
        }

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new DacConfigurationException("Http POST to "
                    + OrionPostParams.INDEX + " failed with status: "
                    + response.getStatusLine().getStatusCode() + "!");
        }

        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent()));
            String line = StringUtils.EMPTY;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            throw new DacConfigurationException(
                    "Failed to read the response from DAC: "
                            + this.dac.getAddress() + "!", e);
        }

        final String formData = sb.toString();

        List<NameValuePair> existingOrionConfigParams = new ArrayList<>();
        for (Pattern pattern : orionConfigPatterns) {
            Matcher matcher = pattern.matcher(formData);
            while (matcher.find()) {
                existingOrionConfigParams.add(new BasicNameValuePair(matcher
                        .group(1), matcher.group(2)));
            }
        }
        existingOrionConfigParams.add(new BasicNameValuePair(
                OrionPostParams.PARAM_IPTYPE, OrionPostParams.IP_TYPE_IPV4));
        return existingOrionConfigParams;
    }

    private void addBmhManagedConfiguration(final List<NameValuePair> params) {
        /*
         * Add the remaining configuration information to the parameters.
         */
        params.add(new BasicNameValuePair(OrionPostParams.PARAM_IP_ADDRESS,
                this.dac.getAddress()));
        params.add(new BasicNameValuePair(OrionPostParams.PARAM_NETMASK,
                this.dac.getNetMask()));
        params.add(new BasicNameValuePair(OrionPostParams.PARAM_GATEWAY,
                this.dac.getGateway()));
        params.add(new BasicNameValuePair(OrionPostParams.PARAM_JITTER, Integer
                .toString(this.dac.getBroadcastBuffer())));
        params.add(new BasicNameValuePair(OrionPostParams.PARAM_TXIPADDR,
                this.dac.getReceiveAddress()));
        params.add(new BasicNameValuePair(OrionPostParams.PARAM_TXPORT, Integer
                .toString(this.dac.getReceivePort())));
        List<DacChannel> channels = this.dac.getChannels();
        for (int i = 0; i < channels.size(); i++) {
            DacChannel channel = channels.get(i);
            final String channelString = Integer.toString(channel.getId()
                    .getChannel() + 1);
            final String portParam = String.format(OrionPostParams.PARAM_PORT,
                    channelString);
            final String levelParam = String.format(
                    OrionPostParams.PARAM_LEVEL, channelString);

            params.add(new BasicNameValuePair(portParam, Integer
                    .toString(channel.getPort())));
            params.add(new BasicNameValuePair(levelParam, Double
                    .toString(channel.getLevel())));
        }
    }

    private void saveConfiguration(final boolean reboot,
            final List<NameValuePair> params) throws DacConfigurationException {
        final HttpPost post = new HttpPost(
                this.buildPostURL(OrionPostParams.TEST));
        if (reboot) {
            params.add(new BasicNameValuePair(OrionPostParams.PARAM_REBOOT,
                    OrionPostParams.REBOOT_YES));
        }
        params.add(new BasicNameValuePair(OrionPostParams.PARAM_SUB,
                OrionPostParams.SUB_SUBMIT));
        try {
            post.setEntity(new UrlEncodedFormEntity(params));
        } catch (UnsupportedEncodingException e) {
            throw new DacConfigurationException(
                    "Failed to set Http POST parameters!", e);
        }

        HttpResponse response = null;
        try {
            response = httpClient.execute(post);
        } catch (Exception e) {
            throw new DacConfigurationException(
                    "Failed to execute Http POST to " + OrionPostParams.INDEX
                            + "!", e);
        }

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new DacConfigurationException("Http POST to "
                    + OrionPostParams.INDEX + " failed with status: "
                    + response.getStatusLine().getStatusCode() + "!");
        }
    }

    private String buildPostURL(final String endpoint) {
        StringBuilder sb = new StringBuilder(OrionPostParams.HTTPS);
        sb.append(this.configAddress).append("/");
        if (endpoint != null) {
            sb.append(OrionPostParams.CGI_BIN).append("/").append(endpoint);
        }

        return sb.toString();
    }

    /**
     * @return the events
     */
    public List<DacConfigEvent> getEvents() {
        return events;
    }
}