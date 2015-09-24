# hbase-java-api-example
This is a simple example usage of HBase on Trusted Analytics Platform.

This application utilizes HBase service broker (from TAP) and HBase Client API to connect to HBase. 
It performs basic operations, like:

* list tables
* show table description (column families)
* get _n_ last rows from given table
* get _n_ first rows from given table
* create a table

After being deployed to TAP it provides these functionalities through the following endpoints:
  
|URL   	                |method  |operation                          |
|---	                |---     |---	                             |
|/api/tables   	        |GET     |list the tables   	             |
|/api/tables   	        |POST    |create new table   	             |
|/api/tables/{name}     |GET     |describe details of given table    |
|/api/tables/{name}/head|GET     |get first rows of given table   	 |
|/api/tables/{name}/tail|GET     |get last rows of given table   	 |
|/api/tables/{name}/row |POST     |add new value for given row|
|/api/tables/{name}/row/{rowKey}|GET     |get row by given row key |


## Under the hood
This is a simple spring boot application. Key point of interest here are:

* extracting HBase configuration information (required for connection; provided by HBase broker)
* using HBase client to perform some admin operations (in our case: getting information on tables)
* using HBase client to perform some operations on tables (in our case: reading data)


The following sections will present information on the broker and client API role. 

### HBase broker
HBase broker of TAP provisiones a namespace for the user. 
After binding to an app, it also provides some configuration information.

```
{
  "VCAP_SERVICES": {
    "hbase": [
      {
        "credentials": {
          "HADOOP_CONFIG_KEY": { 
            ...
            "hbase.zookeeper.property.clientPort": "2181",
            "hbase.zookeeper.quorum": "cdh-master-0.node.server.com,cdh-master-1.node.server.com,cdh-master-2.node.server.com",
            ...
          },
          "hbase.namespace": "2bd6c4db32236dd4a33d19f8ef76257b4a69ff1b",
          "kerberos": {
            "kdc": "cdh-manager.server.com",
            "krealm": "CLOUDERA"
          }
        },
        "label": "hbase",
        "name": "hbase1",
        "plan": "shared",
        "tags": []
      }
   ]
   ...
```

Essential fragments here are:

* _name_ key - service instance name
* _credential_ section - crutial configuration information, including:
  * zookeeper settings (required to connect to HBase)
  * kerberos settings (used when working with kerberized environment) 
  * hbase.namespace key - the namespace created for the user
  
This information is extracted by HBaseConfig class.

```Java
@Configuration
public class HBaseConfig {
    @Value("${hbase.namespace}")
    private String hbaseNamespace;

    @Value("${zookeeper.quorum}")
    private String zookeeperQuorum;

    @Value("${zookeeper.clientPort}")
    private String zookeeperClientPort;

    @Bean
    protected org.apache.hadoop.conf.Configuration HBaseConfiguration() throws IOException {
        org.apache.hadoop.conf.Configuration conf = HBaseConfiguration.create();
        conf.set(HConstants.ZOOKEEPER_QUORUM, zookeeperQuorum);
        conf.set(HConstants.ZOOKEEPER_CLIENT_PORT, zookeeperClientPort);
        return conf;
    }
}
```

In this sample code passage we simply extract zookeeper qorum and port and use it to create Configuration 
object required to obtain connection to HBase (see: HBase Java API section).

> Please, note the expressions used in `@Value` annotation. They indicate to properties defined in src/main/resources/application.properties.
Adjust properties file accordingly to your needs.

### HBase Java API (1.1.2)
HBase project provides Java client API. 

If you want to use the API in your Maven project, the corresponding dependency is:
 
```
<dependency>
	<groupId>org.apache.hbase</groupId>
	<artifactId>hbase-client</artifactId>
	<version>1.1.2</version>
</dependency>
```

("org.apache.hbase:hbase-client:1.1.2" for Gradle).

You'll find javadocs here: [https://hbase.apache.org/apidocs/index.html/](https://hbase.apache.org/apidocs/org/apache/hadoop/hbase/client/package-summary.html)

The API allows for interaction with HBase for DDL (administrative tasks like tables creation/deletion) and DML (data importing, querying).

This sample application shows some examples of these operations. 

#### Row get
```Java
       Result r = null; 
       try (Connection connection = ConnectionFactory.createConnection(conf)) {
            Table table = connection.getTable(TableName.valueOf(name));
            Get get = new Get(Bytes.toBytes(rowKey));
            r = table.get(get);
        } catch (org.apache.hadoop.hbase.TableNotFoundException e) {
            throw new TableNotFoundException(name);
        } catch (IOException e) {
            LOG.error("Error while talking to HBase.", e);
        }
```

#### Table scan
Get first 10 rows of given table (by _name_):

```Java

        List<RowValue> result = new ArrayList<>();
        try (Connection connection = ConnectionFactory.createConnection(conf)) {
            Table table = connection.getTable(TableName.valueOf(name));

            Scan scan = new Scan();
            scan.setFilter(new PageFilter(10));

            try (ResultScanner rs = table.getScanner(scan)) {
                for (Result r = rs.next(); r != null; r = rs.next()) {
                    //conversionsService.constructRowValue is a helper method (defined in the app)
                    result.add(conversionsService.constructRowValue(r));
                }
            }
        }
```        

#### Admin API usage
Fetch list of tables:

```Java
      List<TableDescription> result = null;
      try (Connection connection = ConnectionFactory.createConnection(conf);
          Admin admin = connection.getAdmin()) {
          HTableDescriptor[] tables = admin.listTables();

          Stream<HTableDescriptor> tableDescriptorsStream = Arrays.stream(tables);

          //ConversionService.constructTableDecription is a helper method (defined in the app)
          result = tableDescriptorsStream.map(conversionsService::constructTableDescription) 
              .collect(Collectors.toList());
      } catch (IOException e) {
          LOG.error("Error while talking to HBase.", e);
      }
```

Of course, obtaining the connection for every operation is costly (connect to ZooKeeper, connect to HBase takes time).
In real life, you'd probably strive to reuse HBase connections.

## Compiling and deploying the example
App deployment is described in details on the Platform Wiki: [Getting started Guilde](https://github.com/trustedanalytics/platform-wiki/wiki/Getting%20Started%20Guide).

The procedure boils down to following steps.
After cloning the repository you will be able to compile the project with:

```
./gradlew clean assemble
```

(or `mvn clean package` for Maven)

Before deploying, which can be done with `cf push`, make sure there is an HBase instance available for you.



If it is not already done, create an instance of HBase service:

```
cf create-service hbase shared hbase1
```

To use this instance either add it to manifest.yml or bind it to the app through CLI.


You can define the binding in _services_ section of app's manifest file:

```
---
applications:
- name: hbase-reader
  memory: 1G
  instances: 1
  host: hbase-reader
  path: build/libs/hbase-rest-0.0.1-SNAPSHOT.jar
  services:
      - hbase1
```

> Sample manifest is provided in this project for your convenience. Please modify it for your needs (application name, service name, etc.)

After this you are ready to push your application to the platform:
```
cf push
```
   

If you plan to *bind an instance of HBase to applications that is already running*, you could do this with following commands: 

```
cf bind-service hbase-reader hbase1

cf restage hbase-reader
```

# TODO

* kerberos support
