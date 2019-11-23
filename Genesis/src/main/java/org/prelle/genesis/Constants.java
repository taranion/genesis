/**
 *
 */
package org.prelle.genesis;

import java.util.ResourceBundle;

/**
 * @author prelle
 *
 */
public interface Constants {

	public final static String KEY_APPLICATION_ID      = "application.id";
	public final static String KEY_APPLICATION_VERSION = "application.version";
	public final static String KEY_APPLICATION_DATE    = "application.date";
	public final static String KEY_APPLICATION_BRANCH  = "application.branch";
	public final static String PREFKEY_FULLSCREEN      = "fullscreen";
	public final static String PREFKEY_USE_SECONDARY_SCREEN = "secondary";
	public final static String PREFKEY_LAST_RELEASE_NOTES = "lastRelShown";

	public final static String PREFIX = "org/prelle/genesis";
	public final static ResourceBundle RES = ResourceBundle.getBundle(PREFIX+"/i18n/genesis");


}
