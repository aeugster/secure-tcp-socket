# secure-tcp-socket
Create secure connections without requirement of certificates and key stores.
Optimized for Java 21.

## Usage
- Client & Server - start SecSocketExample and have a look at the log
- Explore unitTest ExampleClientIT and connect to an existing server

## Issues and Sonar
Issues: https://github.com/aeugster/secure-tcp-socket/issues

Sonar: https://sonarcloud.io/summary/overall?id=aeugster_secure-tcp-socket

[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=aeugster_secure-tcp-socket&metric=coverage)](https://sonarcloud.io/summary/new_code?id=aeugster_secure-tcp-socket)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=aeugster_secure-tcp-socket&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=aeugster_secure-tcp-socket)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=aeugster_secure-tcp-socket&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=aeugster_secure-tcp-socket)

## Exceptions
- SecureSocket<b>TechnicalException</b> is thrown for unexpected errors
- SecureSocket<b>ApplicationException</b> is kind of expected. Currently, IOExceptions are used instead


## Misc

### Trigger sonar manually (login required)
- Use start workflow buttons
- Don't change analysis method https://sonarcloud.io/project/analysis_method?id=aeugster_secure-tcp-socket

### Formatting
- use intellij defaults (e.g. spaces instead of tab)

### Motivation
The use of RestTemplate was not an option (since it relies on trusted certificates):
- For 'lets encrypt' the server must be public
- For private CA the JRE needs cert import
- Long polls would be needed for information-push to the client

Java SSLSocket was not an option (since it relies on trusted certificates):
- Application needs a truststore with imported server certificate

### Notes and tricks
- Versioning follows java-defaults Major, Minor & Patch
- show COMMITS on gui: click on the clock below the green [Code] button or use https://github.com/aeugster/secure-tcp-socket/commits/main

### Used code examples
| What    | URL                                                |
|---------|----------------------------------------------------|
| pom.xml | https://javabydeveloper.com/junit-5-maven-example/ |

