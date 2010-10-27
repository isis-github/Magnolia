/**
 * This file Copyright (c) 2003-2010 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.exchangesimple;

import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.exchange.ActivationManagerFactory;
import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.cms.exchange.Subscriber;
import info.magnolia.cms.exchange.Subscription;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import EDU.oswego.cs.dl.util.concurrent.CountDown;
import EDU.oswego.cs.dl.util.concurrent.Sync;


/**
 * Implementation of syndicator that simply sends all activated content over http connection specified in the subscriber.
 * @author Sameer Charles $Id$
 */
public class SimpleSyndicator extends BaseSyndicatorImpl {
    private static final Logger log = LoggerFactory.getLogger(SimpleSyndicator.class);

    public SimpleSyndicator() {
    }

    public void activate(final ActivationContent activationContent, String nodePath) throws ExchangeException {
        String nodeUUID = activationContent.getproperty(NODE_UUID);
        Collection<Subscriber> subscribers = ActivationManagerFactory.getActivationManager().getSubscribers();
        Iterator<Subscriber> subscriberIterator = subscribers.iterator();
        final Sync done = new CountDown(subscribers.size());
        final Map<Subscriber, Exception> errors = new ConcurrentHashMap<Subscriber, Exception>(subscribers.size());
        while (subscriberIterator.hasNext()) {
            final Subscriber subscriber = (Subscriber) subscriberIterator.next();
            if (subscriber.isActive()) {
                // Create runnable task for each subscriber execute
                if (Boolean.parseBoolean(activationContent.getproperty(ItemType.DELETED_NODE_MIXIN))) {
                    executeInPool(getDeactivateTask(done, errors, subscriber, nodeUUID, nodePath));
                } else {
                    executeInPool(getActivateTask(activationContent, done, errors, subscriber, nodePath));
                }
            } else {
                // count down directly
                done.release();
            }
        } //end of subscriber loop

        // wait until all tasks are executed before returning back to user to make sure errors can be propagated back to the user.
        acquireIgnoringInterruption(done);

        // collect all the errors and send them back.
        if (!errors.isEmpty()) {
            Exception e = null;
            StringBuffer msg = new StringBuffer(errors.size() + " error").append(errors.size() > 1 ? "s" : "").append(" detected: ");
            Iterator<Map.Entry<Subscriber, Exception>> iter = errors.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<Subscriber, Exception> entry = iter.next();
                e = (Exception) entry.getValue();
                Subscriber subscriber = (Subscriber) entry.getKey();
                msg.append("\n").append(e.getMessage()).append(" on ").append(subscriber.getName());
                log.error(e.getMessage(), e);
            }

            throw new ExchangeException(msg.toString(), e);
        }

        executeInPool(new Runnable() {
            public void run() {
                cleanTemporaryStore(activationContent);
            }
        });
    }

    private Runnable getActivateTask(final ActivationContent activationContent, final Sync done, final Map<Subscriber, Exception> errors, final Subscriber subscriber, final String nodePath) {
        Runnable r = new Runnable() {
            public void run() {
                try {
                    activate(subscriber, activationContent, nodePath);
                } catch (ExchangeException e) {
                    log.error("Failed to activate content.", e);
                    errors.put(subscriber,e);
                } finally {
                    done.release();
                }
            }
        };
        return r;
    }

    /**
     * Send activation request if subscribed to the activated URI.
     * @param subscriber
     * @param activationContent
     * @throws ExchangeException
     */
    public String activate(Subscriber subscriber, ActivationContent activationContent, String nodePath) throws ExchangeException {
        log.debug("activate");
        if (null == subscriber) {
            throw new ExchangeException("Null Subscriber");
        }

        String parentPath = null;

        Subscription subscription = subscriber.getMatchedSubscription(nodePath, this.repositoryName);
        if (null != subscription) {
            // its subscribed since we found the matching subscription
            // unfortunately activationContent is not thread safe and is used by multiple threads in case of multiple subscribers so we can't use it as a vessel for transfer of parentPath value
            parentPath = this.getMappedPath(this.parent, subscription);
        } else {
            log.debug("Exchange : subscriber [{}] is not subscribed to {}", subscriber.getName(), nodePath);
            return null;
        }
        log.debug("Exchange : sending activation request to {} with user {}", subscriber.getName(), this.user.getName()); //$NON-NLS-1$

        if(SystemProperty.getBooleanProperty(SystemProperty.MAGNOLIA_UTF8_ENABLED)) {
            try {
                parentPath = URLEncoder.encode(parentPath, "UTF-8");
            }
            catch (UnsupportedEncodingException e) {
                // do nothing
            }
        }

        URLConnection urlConnection = null;
        try {
            urlConnection = prepareConnection(subscriber);
            this.addActivationHeaders(urlConnection, activationContent);
            // set a parent path manually instead of via activationHeaders since it can differ between subscribers.
            urlConnection.setRequestProperty(PARENT_PATH, parentPath);

            Transporter.transport((HttpURLConnection) urlConnection, activationContent);

            String status = urlConnection.getHeaderField(ACTIVATION_ATTRIBUTE_STATUS);

            // check if the activation failed
            if (StringUtils.equals(status, ACTIVATION_FAILED)) {
                String message = urlConnection.getHeaderField(ACTIVATION_ATTRIBUTE_MESSAGE);
                throw new ExchangeException("Message received from subscriber: " + message);
            }
            urlConnection.getContent();
            log.debug("Exchange : activation request sent to {}", subscriber.getName()); //$NON-NLS-1$
        }
        catch (ExchangeException e) {
            throw e;
        }
        catch (IOException e) {
            throw new ExchangeException("Not able to send the activation request [" + (urlConnection == null ? null : urlConnection.getURL()) + "]: " + e.getMessage());
        }
        catch (Exception e) {
            throw new ExchangeException(e);
        }
        return null;
    }

    protected URLConnection prepareConnection(Subscriber subscriber) throws ExchangeException {

        String handle = getActivationURL(subscriber);

        try {
            // authentication headers
            if (subscriber.getAuthenticationMethod() != null && "form".equalsIgnoreCase(subscriber.getAuthenticationMethod())) {
                handle += (handle.indexOf('?') > 0 ? "&" : "?") + AUTH_USER + "=" + this.user.getName();
                handle += "&" + AUTH_CREDENTIALS + "=" + this.user.getPassword();
            }
            URL url = new URL(handle);
            URLConnection urlConnection = url.openConnection();
            // authentication headers
            if (subscriber.getAuthenticationMethod() == null || "basic".equalsIgnoreCase(subscriber.getAuthenticationMethod())) {
                urlConnection.setRequestProperty(AUTHORIZATION, this.basicCredentials);
            } else if (!"form".equalsIgnoreCase(subscriber.getAuthenticationMethod())) {
                log.info("Unknown Authentication method for deactivation: " + subscriber.getAuthenticationMethod());
            }

            return urlConnection;
        } catch (MalformedURLException e) {
            throw new ExchangeException("Incorrect URL for subscriber " + subscriber + "[" + handle + "]");
        } catch (IOException e) {
            throw new ExchangeException("Not able to send the activation request [" + handle + "]: " + e.getMessage());
        } catch (Exception e) {
            throw new ExchangeException(e);
        }
    }

    public void doDeactivate(String nodeUUID, String nodePath) throws ExchangeException {
        Collection<Subscriber> subscribers = ActivationManagerFactory.getActivationManager().getSubscribers();
        Iterator<Subscriber> subscriberIterator = subscribers.iterator();
        final Sync done = new CountDown(subscribers.size());
        final Map<Subscriber, Exception> errors = new ConcurrentHashMap<Subscriber, Exception>();
        while (subscriberIterator.hasNext()) {
            final Subscriber subscriber = (Subscriber) subscriberIterator.next();
            if (subscriber.isActive()) {
                // Create runnable task for each subscriber.
                executeInPool(getDeactivateTask(done, errors, subscriber, nodeUUID, nodePath));
            } else {
                // count down directly
                done.release();
            }
        } //end of subscriber loop

        // wait until all tasks are executed before returning back to user to make sure errors can be propagated back to the user.
        acquireIgnoringInterruption(done);

        // collect all the errors and send them back.
        if (!errors.isEmpty()) {
            Exception e = null;
            StringBuffer msg = new StringBuffer(errors.size() + " error").append(
            errors.size() > 1 ? "s" : "").append(" detected: ");
            Iterator<Entry<Subscriber, Exception>> iter = errors.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<Subscriber, Exception> entry = iter.next();
                e = entry.getValue();
                Subscriber subscriber = entry.getKey();
                msg.append("\n").append(e.getMessage()).append(" on ").append(subscriber.getName());
                log.error(e.getMessage(), e);
            }

            throw new ExchangeException(msg.toString(), e);
        }
    }

    private Runnable getDeactivateTask(final Sync done, final Map<Subscriber, Exception> errors, final Subscriber subscriber, final String nodeUUID, final String nodePath) {
        Runnable r = new Runnable() {
            public void run() {
                try {
                    doDeactivate(subscriber, nodeUUID, nodePath);
                } catch (ExchangeException e) {
                    log.error("Failed to deactivate content.", e);
                    errors.put(subscriber,e);
                } finally {
                    done.release();
                }
            }
        };
        return r;
    }

    /**
     * Deactivate from a specified subscriber.
     * @param subscriber
     * @throws ExchangeException
     */
    public String doDeactivate(Subscriber subscriber, String nodeUUID, String path) throws ExchangeException {
        Subscription subscription = subscriber.getMatchedSubscription(path, this.repositoryName);
        if (null != subscription) {
            String handle = getDeactivationURL(subscriber);
            try {
                URLConnection urlConnection = prepareConnection(subscriber);

                this.addDeactivationHeaders(urlConnection, nodeUUID);
                String status = urlConnection.getHeaderField(ACTIVATION_ATTRIBUTE_STATUS);

                // check if the activation failed
                if (StringUtils.equals(status, ACTIVATION_FAILED)) {
                    String message = urlConnection.getHeaderField(ACTIVATION_ATTRIBUTE_MESSAGE);
                    throw new ExchangeException("Message received from subscriber: " + message);
                }

                urlConnection.getContent();

            }
            catch (MalformedURLException e) {
                throw new ExchangeException("Incorrect URL for subscriber " + subscriber + "[" + handle + "]");
            }
            catch (IOException e) {
                throw new ExchangeException("Not able to send the deactivation request [" + handle + "]: " + e.getMessage());
            }
            catch (Exception e) {
                throw new ExchangeException(e);
            }
        }
        return null;
    }

    /**
     * Gets target path to which the current path is mapped in given subscription. Provided path should be without trailing slash.
     */
    protected String getMappedPath(String path, Subscription subscription) {
        String toURI = subscription.getToURI();
        if (null != toURI) {
            String fromURI = subscription.getFromURI();
            // remove trailing slash if any
            fromURI = StringUtils.removeEnd(fromURI, "/");
            toURI = StringUtils.removeEnd(toURI, "/");
            // apply path transformation if any
            path = path.replaceFirst(fromURI, toURI);
            if (path.equals("")) {
                return "/";
            }
        }
        return path;
    }

}
