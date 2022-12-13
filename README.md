# secure-tcp-socket
Create secure connections without requirement of certificates and key stores.

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
- SecureSocket<b>ApplicationException</b> is kind of expected


## Misc

### Trigger sonar manually (login required)
- Use start workflow buttons
- Don't change analysis method https://sonarcloud.io/project/analysis_method?id=aeugster_secure-tcp-socket

### Formatting
- use intellij defaults (e.g. spaces instead of tab)

### Notes and tricks
- Versioning does not really follow Major, Minor & Patch:
  - a patch can contain breaking changes, but no features
- show COMMITS on gui: click on the clock below the green [Code] button or use https://github.com/aeugster/secure-tcp-socket/commits/main

### Used code examples
| What    | URL                                                |
|---------|----------------------------------------------------|
| pom.xml | https://javabydeveloper.com/junit-5-maven-example/ |

