/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2005 Klaus Bartz
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.core.os;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.coi.tools.os.win.RegDataContainer;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.util.TargetFactory;

/**
 * This class provides a wrapper around {@link RegistryHandler} that delegates to a platform specific implementation
 * if registry access is supported, or a no-op implementation if it is not.
 *
 * @author Klaus Bartz
 * @author Tim Anderson
 */
public class RegistryDefaultHandler extends RegistryHandler
{

    /**
     * The registry handler to delegate to.
     */
    private RegistryHandler handler;

    /**
     * A no-op handler, for use when the registry is not supported.
     */
    private static final RegistryHandler NOOP_HANDLER = new RegistryHandler();

    /**
     * The factory for creating {@link RegistryHandler} instances for the current platform.
     */
    private TargetFactory factory;

    /**
     * True if an attempt has been made to initialise {@link #handler}.
     */
    private boolean initialized = false;

    /**
     * The logger.
     */
    private static final Logger log = Logger.getLogger(RegistryDefaultHandler.class.getName());

    /**
     * Constructs a {@code RegistryDefaultHandler}.
     *
     * @param factory the factory for creating {@link RegistryHandler} instances for the current platform
     */
    public RegistryDefaultHandler(TargetFactory factory)
    {
        this.factory = factory;
    }

    /**
     * Sets the given contents to the given registry value. If a sub key or the registry value does
     * not exist, it will be created.
     *
     * @param key      the registry key which should be used or created
     * @param value    the registry value into which the contents should be set
     * @param contents the contents for the value
     * @throws NativeLibException for any native library error
     */
    @Override
    public void setValue(String key, String value, String contents) throws NativeLibException
    {
        getHandler().setValue(key, value, contents);
    }

    /**
     * Sets the given contents to the given registry value. If a sub key or the registry value does
     * not exist, it will be created.
     *
     * @param key      the registry key which should be used or created
     * @param value    the registry value into which the contents should be set
     * @param contents the contents for the value
     * @throws NativeLibException for any registry error
     */
    @Override
    public void setValue(String key, String value, String[] contents) throws NativeLibException
    {
        getHandler().setValue(key, value, contents);
    }

    /**
     * Sets the given contents to the given registry value. If a sub key or the registry value does
     * not exist, it will be created.
     *
     * @param key      the registry key which should be used or created
     * @param value    the registry value into which the contents should be set
     * @param contents the contents for the value
     * @throws NativeLibException for any registry error
     */
    @Override
    public void setValue(String key, String value, byte[] contents) throws NativeLibException
    {
        getHandler().setValue(key, value, contents);
    }

    /**
     * Sets the given contents to the given registry value. If a sub key or the registry value does
     * not exist, it will be created.
     *
     * @param key      the registry key which should be used or created
     * @param value    the registry value into which the contents should be set
     * @param contents the contents for the value
     * @throws NativeLibException for any registry error
     */
    @Override
    public void setValue(String key, String value, long contents) throws NativeLibException
    {
        getHandler().setValue(key, value, contents);
    }

    /**
     * Returns the contents of the key/value pair if value exist, else the given default value.
     *
     * @param key        the registry key which should be used
     * @param value      the registry value from which the contents should be requested
     * @param defaultVal value to be used if no value exist in the registry
     * @return requested value if exist, else the default value
     * @throws NativeLibException for any registry error
     */
    @Override
    public RegDataContainer getValue(String key, String value, RegDataContainer defaultVal) throws NativeLibException
    {
        return getHandler().getValue(key, value, defaultVal);
    }

    /**
     * Returns whether a key exist or not.
     *
     * @param key key to be evaluated
     * @return whether a key exist or not
     * @throws NativeLibException for any registry error
     */
    @Override
    public boolean keyExist(String key) throws NativeLibException
    {
        return getHandler().keyExist(key);
    }

    /**
     * Returns whether a the given value under the given key exist or not.
     *
     * @param key   key to be used as path for the value
     * @param value value name to be evaluated
     * @return whether a the given value under the given key exist or not
     * @throws NativeLibException for any registry error
     */
    @Override
    public boolean valueExist(String key, String value) throws NativeLibException
    {
        return getHandler().valueExist(key, value);
    }

    /**
     * Returns all keys which are defined under the given key.
     *
     * @param key key to be used as path for the sub keys
     * @return all keys which are defined under the given key
     * @throws NativeLibException for any registry error
     */
    @Override
    public String[] getSubkeys(String key) throws NativeLibException
    {
        return getHandler().getSubkeys(key);
    }

    /**
     * Returns all value names which are defined under the given key.
     *
     * @param key key to be used as path for the value names
     * @return all value names which are defined under the given key
     * @throws NativeLibException for any registry error
     */
    @Override
    public String[] getValueNames(String key) throws NativeLibException
    {
        return getHandler().getValueNames(key);
    }

    /**
     * Returns the contents of the key/value pair if value exist, else an exception is raised.
     *
     * @param key   the registry key which should be used
     * @param value the registry value from which the contents should be requested
     * @return requested value if exist, else an exception
     * @throws NativeLibException for any registry error
     */
    @Override
    public RegDataContainer getValue(String key, String value) throws NativeLibException
    {
        return getHandler().getValue(key, value);
    }

    /**
     * Creates the given key in the registry.
     *
     * @param key key to be created
     * @throws NativeLibException for any registry error
     */
    @Override
    public void createKey(String key) throws NativeLibException
    {
        getHandler().createKey(key);
    }

    /**
     * Deletes the given key if exist, else throws an exception.
     *
     * @param key key to be deleted
     * @throws NativeLibException for any registry error
     */
    @Override
    public void deleteKey(String key) throws NativeLibException
    {
        getHandler().deleteKey(key);
    }

    /**
     * Deletes a key under the current root if it is empty, else do nothing.
     *
     * @param key key to be deleted
     * @throws NativeLibException for any registry error
     */
    @Override
    public void deleteKeyIfEmpty(String key) throws NativeLibException
    {
        getHandler().deleteKeyIfEmpty(key);
    }

    /**
     * Deletes a value.
     *
     * @param key   key of the value which should be deleted
     * @param value value name to be deleted
     * @throws NativeLibException for any registry error
     */
    @Override
    public void deleteValue(String key, String value) throws NativeLibException
    {
        getHandler().deleteValue(key, value);
    }

    /**
     * Sets the root for the next registry access.
     * <p/>
     * TODO - this doesn't support multi-threaded access
     *
     * @param i an integer which refers to a HKEY
     * @throws NativeLibException for any registry error
     */
    @Override
    public void setRoot(int i) throws NativeLibException
    {
        getHandler().setRoot(i);
    }

    /**
     * Return the root as integer (HKEY_xxx).
     *
     * @return the root as integer
     * @throws NativeLibException for any registry error
     */
    @Override
    public int getRoot() throws NativeLibException
    {
        return getHandler().getRoot();
    }

    /**
     * Sets up whether or not previous contents of registry values will
     * be logged by the 'setValue()' method.  When registry values are
     * overwritten by repeated installations, the desired behavior can
     * be to have the registry value removed rather than rewound to the
     * last-set contents (achieved via 'false').  If this method is not
     * called then the flag wll default to 'true'.
     *
     * @param flagVal true to have the previous contents of registry
     *                values logged by the 'setValue()' method.
     * @throws NativeLibException for any registry error
     */
    @Override
    public void setLogPrevSetValueFlag(boolean flagVal) throws NativeLibException
    {
        getHandler().setLogPrevSetValueFlag(flagVal);
    }

    /**
     * Determines whether or not previous contents of registry values
     * will be logged by the 'setValue()' method.
     *
     * @return true if the previous contents of registry values will be
     *         logged by the 'setValue()' method.
     * @throws NativeLibException for any registry error
     */
    @Override
    public boolean getLogPrevSetValueFlag() throws NativeLibException
    {
        return getHandler().getLogPrevSetValueFlag();
    }

    /**
     * Activates logging of registry changes.
     *
     * @throws NativeLibException for any registry error
     */
    @Override
    public void activateLogging() throws NativeLibException
    {
        getHandler().activateLogging();
    }

    /**
     * Suspends logging of registry changes.
     *
     * @throws NativeLibException for any registry error
     */
    @Override
    public void suspendLogging() throws NativeLibException
    {
        getHandler().suspendLogging();
    }

    /**
     * Resets logging of registry changes.
     *
     * @throws NativeLibException for any registry error
     */
    @Override
    public void resetLogging() throws NativeLibException
    {
        getHandler().resetLogging();
    }

    /**
     * Returns a copy of the collected logging information.
     *
     * @return a copy of the collected logging information
     * @throws NativeLibException for any registry error
     */
    @Override
    public List<Object> getLoggingInfo() throws NativeLibException
    {
        return getHandler().getLoggingInfo();
    }

    /**
     * Registers logging information. This replaces any existing logging information.
     *
     * @param info the logging information
     * @throws NativeLibException for any registry error
     */
    @Override
    public void setLoggingInfo(List info) throws NativeLibException
    {
        getHandler().setLoggingInfo(info);
    }

    /**
     * Adds logging information.
     *
     * @param info the logging information
     * @throws NativeLibException for any registry error
     */
    @Override
    public void addLoggingInfo(List info) throws NativeLibException
    {
        getHandler().addLoggingInfo(info);
    }

    /**
     * Rewinds all logged actions.
     *
     * @throws NativeLibException for any registry error
     */
    @Override
    public void rewind() throws NativeLibException
    {
        getHandler().rewind();
    }

    /**
     * Returns the uninstall name.
     *
     * @return the uninstall name. May be {@code null}
     */
    @Override
    public String getUninstallName()
    {
        return getHandler().getUninstallName();
    }

    /**
     * Sets the uninstall name.
     *
     * @param name the uninstall name. May be {@code null}
     */
    @Override
    public void setUninstallName(String name)
    {
        getHandler().setUninstallName(name);
    }

    /**
     * Returns the underlying handler.
     *
     * @return the underlying handler, or {@code null} if it is not supported for the current platform
     * @deprecated no longer required now that RegistryDefaultHandler extends RegistryHandler
     */
    @Deprecated
    public RegistryHandler getInstance()
    {
        return getHandler().equals(NOOP_HANDLER) ? null : handler;
    }

    /**
     * Determines if registry access is supported on the current platform.
     *
     * @return {@code true} if registry access is supported, otherwise {@code false}
     */
    @Override
    public boolean isSupported()
    {
        return getHandler().isSupported();
    }

    /**
     * Returns the handler, creating it if required.
     *
     * @return the handler, or {@code null} if it can't be created
     */
    private synchronized RegistryHandler getHandler()
    {
        if (!initialized)
        {
            try
            {
                // Load the system dependant handler.
                handler = factory.makeObject(RegistryHandler.class);
            }
            catch (Throwable exception)
            {
                log.log(Level.WARNING, "Failed to create RegistryHandler: " + exception.getMessage(), exception);
                handler = NOOP_HANDLER;
            }
            initialized = true;
        }
        return handler;
    }
}
