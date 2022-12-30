
## CLI Command to execute
```
java -DLOG_DIR=<path for the log files> -jar kunum-standalone-generator.jar --file ./kunum.properties
```


## Execute SonarQube analysis

```
gradle sonar -DLOG_DIR=. -DSONAR_TOKEN=<token from sonar cloud>
```