package com.seriouslyhypersonic.library.content

import android.content.ContentProvider
import android.net.Uri

/**
 * Exception indicating that no valid [ContentProvider] was found for the specified content `uri`.
 * @param uri The URI to watch for changes. This can be a specific row URI, or a base URI for a
 * whole class of content.
 */
public class ContentProviderNotFound(uri: Uri) : Exception("Failed to find provider for $uri")
