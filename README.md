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

* extracting HBase configuration information (required for connection; provided by hbase-broker and kerberos-broker)
* connect to HBase and authenticate in kerberos
* using HBase client to perform some admin operations (in our case: getting information on tables)
* using HBase client to perform some operations on tables (in our case: reading data)


The following sections will present information on the broker and client API role. 

### HBase broker
HBase broker of TAP provisions a namespace for the user.
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
          ...
        },
        "label": "hbase",
        "name": "hbase1",
        "plan": "bare",
        "tags": []
      }
   ]
   ...
```

Essential fragments here are:

* _name_ key - service instance name
* _credential_ section - crucial configuration information, including:
  * zookeeper settings (required to connect to HBase)
  * hbase.namespace key - the namespace created for the user

### Kerberos broker
In TAP Kerberos credentials can be obtained from kerberos-broker. After creating service instance and binding it to an application, the following information are available:

```
  "kerberos": [
   {
    "credentials": {
     "enabled": true,
     "kcacert": "...",
     "kdc": "...",
     "kpassword": "...",
     "krealm": "...",
     "kuser": "..."
    },
    "label": "kerberos",
    "name": "kerberos-instance",
    "plan": "shared",
    "tags": [
     "kerberos"
    ]
   }
  ]
```

### Connecting to HBase
TAP platform provides [hadoop-utils library](https://github.com/trustedanalytics/hadoop-utils "hadoop-utils"). It contains many usefull utils.
For example, connecting to HBase boils down to:
```Java
    Hbase.newInstance().createConnection().connect();
```

hadoop-utils takes care of the configuration and authentication (reads data from HBase and Kerberos service binding).

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

In our case, we depend on hadoop-utils instead  which bring all required dependencies:

```
<dependency>
	<groupId>org.trustedanalytics</groupId>
	<artifactId>hadoop-utils</artifactId>
	<version>0.6.5</version>
</dependency>
```

("org.trustedanalytics:hadoop-utils:0.6.5" for Gradle)


You'll find javadocs here: [https://hbase.apache.org/apidocs/index.html/](https://hbase.apache.org/apidocs/org/apache/hadoop/hbase/client/package-summary.html)

The API allows for interaction with HBase for DDL (administrative tasks like tables creation/deletion) and DML (data importing, querying).

This sample application shows some examples of these operations. 

#### Row get
```Java
       Result r = null; 
       try (Connection connection = hBaseConnectionFactory.connect()) {
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
       try (Connection connection = hBaseConnectionFactory.connect()) {
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
       try (Connection connection = hBaseConnectionFactory.connect();
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
./gradlew clean check assemble
```

(optional) to update headers use

```
./gradlew licenseFormatMain
```

Before deploying, which can be done with `cf push`, make sure there is an HBase instance available for you.



If it is not already done, create an instance of HBase service:

```
cf create-service hbase bare hbase1
```

To use this instance either add it to manifest.yml or bind it to the app through CLI.


If it is not already done, create an instance of Kerberos service:
```
cf create-service kerberos shared kerberos-instance
```


You can define the bindings in _services_ section of app's manifest file:

```
---
applications:
- name: hbase-reader
  memory: 1G
  instances: 1
  host: hbase-reader
  path: build/libs/hbase-rest-0.0.2.jar
  services:
      - hbase1
      - kerberos-instance
```

> Sample manifest is provided in this project for your convenience. Please modify it for your needs (application name, service name, etc.)
> For example, src/main/resources/application-cloud.properties uses HBase service name for some keys. Adjust properties file accordingly to your needs.


After this you are ready to push your application to the platform:
```
cf push
```
   

If you plan to *bind an instance of HBase to applications that is already running*, you could do this with following commands: 

```
cf bind-service hbase-reader hbase1

cf bind-service hbase-reader kerberos-instance


cf restage hbase-reader
```

# TO DO
* update info about namespace and service name in applciation.properties. How namespace is read/used.

