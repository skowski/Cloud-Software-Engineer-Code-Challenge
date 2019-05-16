# otto-code-challenge

Application written in Kotlin with Ktor to solve this
[Code Challenge](https://github.com/aacml/Recruiting/wiki/Cloud-Software-Engineer-Code-Challenge).

## Requirements
- UNIX system.
- System provides [docker](https://www.docker.com.
- System provides bash.

## Building and running
Build Fat JAR using docker Gradle container:
```bash
./docker_build.sh
```

Build docker container:
```bash
docker build -t kowski/otto-code-challenge:latest .
```

Run docker container (on port 80):
```bash
docker run -d --name otto-code-challenge -p 80:8080 kowski/otto-code-challenge:latest
```

## Usage
The microservice provides the following **http endpoint**:
* **/links**
    * That provides a JSON of the following structure:
        ```json
        [
          {
            "label": "Sortiment - Alter - Baby & Kleinkind - 0-6 Monate",
            "url": "http:\/\/www.mytoys.de\/0-6-months\/"
          },
          {
            "label": "Sortiment - Alter - Baby & Kleinkind - 7-12 Monate",
            "url": "http:\/\/www.mytoys.de\/7-12-months\/"
          },
          {
            "label": "Sortiment - Alter - Baby & Kleinkind - 13-24 Monate",
            "url": "http:\/\/www.mytoys.de\/13-24-months\/"
          },
          {
            "label": "Sortiment - Alter - Kindergarten - 2-3 Jahre",
            "url": "http:\/\/www.mytoys.de\/24-47-months\/"
          },
          {
            "label": "Sortiment - Alter - Kindergarten - 4-5 Jahre",
            "url": "http:\/\/www.mytoys.de\/48-71-months\/"
          }
        ]
        ```
    * That can be invoked via
        ```bash
        curl http://localhost/links
        ```
    * Accepts the optional **GET-Parameter** `parent`.

        Examples:
        * Request:
            ```bash
            curl http://localhost/links?parent=Alter
            ```
        * Response:
            ```json
            [
              {
                "label": "Baby & Kleinkind - 0-6 Monate",
                "url": "http:\/\/www.mytoys.de\/0-6-months\/"
              },
              {
                "label": "Baby & Kleinkind - 7-12 Monate",
                "url": "http:\/\/www.mytoys.de\/7-12-months\/"
              },
              {
                "label": "Baby & Kleinkind - 13-24 Monate",
                "url": "http:\/\/www.mytoys.de\/13-24-months\/"
              },
              {
                "label": "Kindergarten - 2-3 Jahre",
                "url": "http:\/\/www.mytoys.de\/24-47-months\/"
              },
              {
                "label": "Kindergarten - 4-5 Jahre",
                "url": "http:\/\/www.mytoys.de\/48-71-months\/"
              }
            ]
            ```
        * Request:
            ```bash
            curl http://localhost/links?parent=Baby%20%26%20Kleinkind
            ```
        * Response:
            ```json
            [
              {
                "label": "0-6 Monate",
                "url": "http:\/\/www.mytoys.de\/0-6-months\/"
              },
              {
                "label": "7-12 Monate",
                "url": "http:\/\/www.mytoys.de\/7-12-months\/"
              },
              {
                "label": "13-24 Monate",
                "url": "http:\/\/www.mytoys.de\/13-24-months\/"
              }
            ]
            ```
    * Accepts the optional **GET-Parameter** `sort`.
        * Implement it as specified [here](https://specs.openstack.org/openstack/api-wg/guidelines/pagination_filter_sort.html#sorting).
        * In our case the keys are `label` and `url`.
        * Examples:
            * `curl http://localhost/links?sort=url`
            * `curl http://localhost/links?sort=label:asc,url`
            * `curl http://localhost/links?sort=url:desc`


            
# Additional Notes

## Approach and considerations 

First decision was for using Kotlin in favor of Java as many things are way easier to write and I want to get better in it.
Next decision was made for using Ktor in favor of Spring Boot with Kotlin support, as it is more lightweight and request
processing works fully asynchronous (also possible with Spring Boot Webflux).

I started with defining input and output model. Then I thought about how to do the filtering the tree structure and 
transformation the input to a flat list. The choices for filtering were using JSONPath or implement a search/filter 
on the the input model. I decided for the later to keep it simple and not introduce another language like JSONPath.

I tried to keep the service simple. As I am coming from the Spring world I thought about dependency injection and wanted
to give Koin a try. For now I am not yet convinced of it, maybe another DI framework would be a better fit in bigger projects.
I think in a small service like this it is also totally okay to do without dependency injection, as the scope is so limited.

## Other notes / Todo's

- Metrics are exposed via JMX, could be switched to Grafana or similar
- No Authorization implemented
- Simple error handling with throwing exceptions and using StatusPages feature of Ktor; avoid throwing of exceptions in non
exceptional cases, like wrong input parameter, and do error handling with Try-Monad (Arrow) or something similar 
- No retry, rate limit, custom timeout etc implemented yet for requesting the external Navigation API
- Wanted to test the Navigtion API client with Ktor MockEngine for HttpClient, but had some problems with it so skipped that for now
- Maybe add more unit tests for Service etc using mocks and stuff, but already good coverage with application tests and in this case they
do not take too long
- Some problems with metrics to JMX in tests, so diasbled for testing
- Trouble with automatic conversion of URL string, slashes are converted, did not solve that
- Add some more logging


## Resume

I don't think the code is really "production ready", there are still some things to do and I am also not sure about some
idiomatic usage of Kotlin, Ktor, DI/Koin etc. In a work situation I would have consulted one or more colleagues for the
issues I had and invested more time.
As I did not had time to kill, I did not specify the microservice's endpoint and datastructure(s) using OpenApi/Swagger.