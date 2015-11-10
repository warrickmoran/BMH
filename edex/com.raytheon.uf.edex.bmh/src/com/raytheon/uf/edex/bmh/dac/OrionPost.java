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
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;

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

    private static final int REBOOT_RETRY_COUNT = 12;

    private final Dac dac;

    private final List<DacConfigEvent> events = new LinkedList<>();

    private HttpClient httpClient;

    public OrionPost(final Dac dac) throws DacConfigurationException {
        this.dac = dac;
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

    public void configureDAC(final boolean reboot)
            throws DacConfigurationException {
        /*
         * First, verify that we can connect to the DAC.
         */
        this.events.add(new DacConfigEvent(DacConfigEvent.MSG_VERIFY));
        boolean available = false;
        try {
            available = this.verifyDacAvailability(DEFAULT_TIMEOUT_SECONDS);
        } catch (Exception e) {
            statusHandler.error("Failed to verify the availability of DAC: "
                    + this.dac.getAddress() + "!", e);
        }
        if (available == false) {
            /*
             * Not able to connect to the DAC.
             */
            this.events.add(new DacConfigEvent(
                    DacConfigEvent.MSG_VERIFY_FAILURE,
                    DacConfigEvent.DEFAULT_ACTION));
            this.events.add(new DacConfigEvent(DacConfigEvent.MSG_FAIL));
            return;
        }
        this.events.add(new DacConfigEvent(DacConfigEvent.MSG_VERIFY_SUCCESS));
        try {
            this.events.add(new DacConfigEvent(DacConfigEvent.MSG_CONFIGURE));
            List<NameValuePair> params = this.getConfiguration();
            this.saveConfiguration(reboot, params);
        } catch (Exception e) {
            this.events.add(new DacConfigEvent(
                    DacConfigEvent.MSG_CONFIGURE_FAILURE,
                    DacConfigEvent.DEFAULT_ACTION));
            this.events.add(new DacConfigEvent(DacConfigEvent.MSG_FAIL));
            throw e;
        }
        /*
         * If the DAC has been rebooted, we will attempt to wait until it
         * becomes accessible again.
         */
        this.events
                .add(new DacConfigEvent(DacConfigEvent.MSG_CONFIGURE_SUCCESS));
        if (reboot) {
            this.events.add(new DacConfigEvent(DacConfigEvent.MSG_REBOOT));
            available = false;
            int count = 0;
            while (count < REBOOT_RETRY_COUNT && available == false) {
                ++count;
                try {
                    available = this
                            .verifyDacAvailability(DEFAULT_TIMEOUT_SECONDS);
                } catch (Exception e) {
                    if (count >= REBOOT_RETRY_COUNT) {
                        statusHandler.error("Failed to verify that DAC: "
                                + this.dac.getAddress()
                                + " has rebooted successfully!", e);
                    }
                }
                if (available == false) {
                    this.events.add(new DacConfigEvent(
                            DacConfigEvent.MSG_REBOOT_WAIT));
                }
            }
            if (available) {
                this.events.add(new DacConfigEvent(
                        DacConfigEvent.MSG_REBOOT_SUCCESS));
            } else {
                this.events.add(new DacConfigEvent(
                        DacConfigEvent.MSG_REBOOT_FAILURE,
                        DacConfigEvent.DEFAULT_ACTION));
            }
        }
        this.events.add(new DacConfigEvent(DacConfigEvent.MSG_SUCCESS));
    }

    public boolean verifyDacAvailability(final int secondsTimeout)
            throws DacConfigurationException {
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

        return (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
    }

    /**
     * Returns a {@link List} of {@link NameValuePair}s associated with the
     * orion configuration parameters that are not managed by BMH; primarily the
     * IPv6 information.
     * 
     * @return the retrieved {@link List} of {@link NameValuePair}s
     * @throws DacConfigurationException
     */
    private List<NameValuePair> getConfiguration()
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
        for (Pattern pattern : OrionPatterns.ORION_PARAM_PATTERNS) {
            Matcher matcher = pattern.matcher(formData);
            while (matcher.find()) {
                existingOrionConfigParams.add(new BasicNameValuePair(matcher
                        .group(1), matcher.group(2)));
            }
        }
        return existingOrionConfigParams;
    }

    private void saveConfiguration(final boolean reboot,
            final List<NameValuePair> params) throws DacConfigurationException {
        final HttpPost post = new HttpPost(
                this.buildPostURL(OrionPostParams.TEST));
        /*
         * Add the remaining configuration information to the parameters.
         */
        params.add(new BasicNameValuePair(OrionPostParams.PARAM_IPTYPE,
                OrionPostParams.IP_TYPE_IPV4));
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
        sb.append(dac.getAddress()).append("/");
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