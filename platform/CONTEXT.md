# VAuthenticator Platform

The shared ground every VAuthenticator server is built on: the capabilities more than one server needs, each one a server takes only if it uses it.

## Language

### Storage

**Document**:
A file a server keeps in the tenant's storage, such as an Email Template, an SMS Template or a file of a Page. Every document belongs to exactly one server: the server that stores it is the only one that reads it, and servers never share documents with each other.
_Avoid_: asset, blob, resource (for the stored file as a whole)

**Document Type**:
The kind of a **Document**, which the server that owns it names and the platform only uses to keep kinds apart. The platform knows no document type of its own.
_Avoid_: category, folder
