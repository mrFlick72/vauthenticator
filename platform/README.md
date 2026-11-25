# VAuthenticator Platform

The starting point of every VAuthenticator application:

- `vauthenticator-platform-parent`: build conventions and managed versions that `vauthenticator-platform` and every server inherit
- `vauthenticator-platform`: the shared mechanisms, starting with Document storage on S3 or the file system

## Using the platform

Inherit `vauthenticator-platform-parent` and add `vauthenticator-platform` (its version is managed by the parent):

```xml
<dependency>
    <groupId>com.vauthenticator</groupId>
    <artifactId>vauthenticator-platform</artifactId>
</dependency>
```

### Document storage

```yaml
document:
  engine: file-system    # or s3
  fs-base-path: dist     # file-system only
  bucket-name: my-bucket # s3 only
```

With `s3`, the server adds `software.amazon.awssdk:s3` and defines an `S3Client` bean. The platform then provides a
`DocumentRepository`; documents are stored under `<type>/<path>`, where the type is a name the server chooses. Any
missing or wrong setting stops the startup with a message naming the key.

Build everything from the repo root with `./mvnw install`. See `AGENTS.md` and the root `docs/adr/0001`.
